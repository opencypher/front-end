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

import org.opencypher.v9_0.ast.AscSortItem
import org.opencypher.v9_0.ast.DescSortItem
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.functions.Id
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CypherType

case object OrderByIdRewritten extends StepSequencer.Condition

/**
 * Rewrites `ORDER BY id(n)` TO `ORDER BY n`.
 * This allows using the order of a label scan or rel-type scan to solve the query.
 */
case class rewriteOrderById(semanticState: SemanticState) extends Rewriter {

  def isEntity(expr: Expression): Boolean =
    semanticState.expressionType(expr).actual == CTNode.invariant ||
      semanticState.expressionType(expr).actual == CTRelationship.invariant

  private val instance = bottomUp(Rewriter.lift {
    case si@AscSortItem(Id(v)) if isEntity(v) => AscSortItem(v)(si.position)
    case si@DescSortItem(Id(v)) if isEntity(v) => DescSortItem(v)(si.position)
  })

  override def apply(v: AnyRef): AnyRef = instance(v)

}

object rewriteOrderById extends Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set()

  override def postConditions: Set[StepSequencer.Condition] = Set(OrderByIdRewritten)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set()

  override def getRewriter(semanticState: SemanticState,
                          parameterTypeMapping: Map[String, CypherType],
                          cypherExceptionFactory: CypherExceptionFactory,
                          anonymousVariableNameGenerator: AnonymousVariableNameGenerator): Rewriter = rewriteOrderById(semanticState)
}
