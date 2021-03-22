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

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.simplifyPredicates.coerceInnerExpressionToBooleanIfNecessary
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.rewriters.InnerVariableNamer
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.helpers.fixedPoint
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object mergeDuplicateBooleanOperators extends ASTRewriterFactory with CnfPhase {
  override def preConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable ++ Set(!AndRewrittenToAnds)

  override def postConditions: Set[StepSequencer.Condition] = Set(NoDuplicateNeighbouringBooleanOperands)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty

  override def getRewriter(innerVariableNamer: InnerVariableNamer,
                           semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory): Rewriter = mergeDuplicateBooleanOperators(semanticState)

  override def getRewriter(from: BaseState,
                           context: BaseContext): Rewriter = this (from.semantics())
}

case class mergeDuplicateBooleanOperators(semanticState: SemanticState) extends Rewriter {

  private def instance(semanticState: SemanticState) = fixedPoint(topDown(Rewriter.lift {
    case p@And(lhs, rhs) if (lhs == rhs) => coerceInnerExpressionToBooleanIfNecessary(semanticState, p, lhs)
    case p@Or(lhs, rhs) if (lhs == rhs) => coerceInnerExpressionToBooleanIfNecessary(semanticState, p, lhs)
  }))

  def apply(that: AnyRef): AnyRef = instance(semanticState).apply(that)

}