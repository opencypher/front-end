/*
 * Copyright (c) Neo4j Sweden AB (http://neo4j.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.CommandClause
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.rewriting.conditions.containsNoReturnAll
import org.opencypher.v9_0.rewriting.rewriters.factories.PreparatoryRewritingRewriterFactory
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Condition
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.bottomUp

import scala.annotation.tailrec

case object ShowRewrittenToShowYieldWhereReturn extends Condition

// We would want this to be part of a later phase, e.g. ASTRewriter.
// That is problematic because it needs to run expandStar, but the YIELD and RETURN clauses need scoping information.
// A solution would be to introduce another pass of SemanticAnalysis between this rewriter and expandStar.
// However, we really don't need more passes of SemanticAnalysis.
// It is not possible to modify or create scope at this point, to do so we would need to be a phase.
// Because of the restricted nature of SHOW commands being only `SHOW ... YIELD ... RETURN ...` this works for now,
// but we should revisit if we become more complicated/flexible.
case object rewriteShowQuery extends Rewriter with Step with PreparatoryRewritingRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(ShowRewrittenToShowYieldWhereReturn)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    // Introduces YIELD * and RETURN *
    containsNoReturnAll,
    // It can invalidate this condition by introducing YIELD and RETURN clauses
    ProjectionClausesHaveSemanticInfo
  )

  override def apply(v: AnyRef): AnyRef = instance(v)

  private val instance = bottomUp(Rewriter.lift {
    case s@SingleQuery(clauses) => s.copy(clauses = rewriteClauses(clauses.toList, List()))(s.position)
  })

  @tailrec
  private def rewriteClauses(clauses: List[Clause], rewrittenClause: List[Clause]): List[Clause] = clauses match {
    // Just a single command clause (with or without WHERE)
    case (commandClause: CommandClause) :: Nil => rewrittenClause ++ rewriteWithYieldAndReturn(commandClause, commandClause.where)
    // Command clause with only a YIELD
    case (c: CommandClause) :: (yieldClause: Yield) :: Nil => {
      rewrittenClause :+ c :+ yieldClause :+ returnClause(lastPosition(yieldClause))
    }
    case c :: cs => rewriteClauses(cs, rewrittenClause :+ c)
    case Nil => rewrittenClause
  }

  private def rewriteWithYieldAndReturn(commandClause: CommandClause, where: Option[Where]): List[Clause] = {
    List(
      commandClause.moveWhereToYield,
      Yield(ReturnItems(includeExisting = true, Seq())(commandClause.position), None, None, None, where)(commandClause.position.newUniquePos()),
      returnClause(commandClause.position)
    )
  }

  private def returnClause(position: InputPosition): Return = Return(ReturnItems(includeExisting = true, Seq())(position))(position.newUniquePos())

  private def lastPosition(y: Yield): InputPosition = {
    y.treeFold(InputPosition.NONE) {
      case node: ASTNode => acc => TraverseChildren(Seq(acc, node.position).max)
    }
  }

  override def getRewriter(deprecations: Deprecations,
                           cypherExceptionFactory: CypherExceptionFactory,
                           notificationLogger: InternalNotificationLogger): Rewriter = instance
}
