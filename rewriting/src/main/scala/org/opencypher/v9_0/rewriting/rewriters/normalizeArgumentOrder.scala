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

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.InequalityExpression
import org.opencypher.v9_0.expressions.NotEquals
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.functions
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.containsNoNodesOfType
import org.opencypher.v9_0.rewriting.conditions.normalizedEqualsArguments
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AllNameGenerators
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object ArgumentOrderInComparisonsNormalized extends StepSequencer.Condition

case object normalizeArgumentOrder extends Rewriter with StepSequencer.Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set(
    containsNoNodesOfType[NotEquals] // NotEquals must have been rewritten to Equals
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(
    ArgumentOrderInComparisonsNormalized,
    normalizedEqualsArguments
  )

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def apply(that: AnyRef): AnyRef = instance(that)

  private val instance: Rewriter = topDown(Rewriter.lift {

    // move id(n) on equals to the left
    case predicate @ Equals(func@FunctionInvocation(_, _, _, _), _) if func.function == functions.Id =>
      predicate

    case predicate @ Equals(lhs, rhs @ FunctionInvocation(_, _, _, _)) if rhs.function == functions.Id =>
      predicate.copy(lhs = rhs, rhs = lhs)(predicate.position)

    // move n.prop on equals to the left
    case predicate @ Equals(Property(_, _), _) =>
      predicate

    case predicate @ Equals(lhs, rhs @ Property(_, _)) =>
      predicate.copy(lhs = rhs, rhs = lhs)(predicate.position)

    case inequality: InequalityExpression =>
      val lhsIsProperty = inequality.lhs.isInstanceOf[Property]
      val rhsIsProperty = inequality.rhs.isInstanceOf[Property]
      if (!lhsIsProperty && rhsIsProperty) {
        inequality.swapped
      } else {
        inequality
      }
  })

  override def getRewriter(innerVariableNamer: InnerVariableNamer,
                           semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory,
                           allNameGenerators: AllNameGenerators): Rewriter = instance
}
