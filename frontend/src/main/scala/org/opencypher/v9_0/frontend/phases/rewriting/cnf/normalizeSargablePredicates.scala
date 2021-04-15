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
package org.opencypher.v9_0.frontend.phases.rewriting.cnf

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.InequalityExpression
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.InnerVariableNamer
import org.opencypher.v9_0.rewriting.rewriters.ProjectionClausesHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AllNameGenerators
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object normalizeSargablePredicates extends Rewriter with CnfPhase with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(NoInequalityInsideNot)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def apply(that: AnyRef): AnyRef = instance(that)

  private val instance: Rewriter = topDown(Rewriter.lift {

    // remove not from inequality expressions by negating them
    case Not(inequality: InequalityExpression) =>
      inequality.negated
  })

  override def getRewriter(innerVariableNamer: InnerVariableNamer,
                           semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory,
                           allNameGenerators: AllNameGenerators): Rewriter = instance

  override def getRewriter(from: BaseState,
                           context: BaseContext): Rewriter = this

  override def toString = "normalizeSargablePredicates"
}
