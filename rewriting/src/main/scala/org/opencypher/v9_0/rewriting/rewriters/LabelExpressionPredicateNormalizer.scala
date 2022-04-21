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
import org.opencypher.v9_0.expressions.LabelExpressionPredicate
import org.opencypher.v9_0.rewriting.ValidatingCondition
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.containsNoMatchingNodes
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object containsNoLabelExpressionPredicates extends ValidatingCondition {

  private val matcher = containsNoMatchingNodes({
    case pattern: LabelExpressionPredicate => pattern.asCanonicalStringVal
  })

  def apply(that: Any): Seq[String] = matcher(that)

  override def name: String = productPrefix
}

object LabelExpressionPredicateNormalizer extends Rewriter with StepSequencer.Step with ASTRewriterFactory {

  private val instance: Rewriter = topDown(Rewriter.lift {
    case pred: LabelExpressionPredicate => LabelExpressionNormalizer(pred.entity, None)(pred.labelExpression)
  })

  override def apply(that: AnyRef): AnyRef = instance(that)

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = this

  /**
   * @return the conditions that needs to be met before this step can be allowed to run.
   */
  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  /**
   * @return the conditions that are guaranteed to be met after this step has run.
   *         Must not be empty, and must not contain any elements that are postConditions of other steps.
   */
  override def postConditions: Set[StepSequencer.Condition] = Set(
    containsNoLabelExpressionPredicates
  )

  /**
   * @return the conditions that this step invalidates as a side-effect of its work.
   */
  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    HasLabelsOrTypesReplacedIfPossible,
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo // It can invalidate this condition by rewriting things inside PatternExpressions.
  )
}
