/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
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
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.GreaterThanOrEqual
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.HasTypes
import org.opencypher.v9_0.expressions.InvalidNotEquals
import org.opencypher.v9_0.expressions.LessThan
import org.opencypher.v9_0.expressions.LessThanOrEqual
import org.opencypher.v9_0.expressions.NotEquals
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.containsNamedPathOnlyForShortestPath
import org.opencypher.v9_0.rewriting.conditions.noReferenceEqualityAmongVariables
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object OnlySingleHasLabels extends StepSequencer.Condition

case object normalizeComparisons extends Rewriter with StepSequencer.Step with ASTRewriterFactory {

  override def getRewriter(innerVariableNamer: InnerVariableNamer,
                           semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory): Rewriter = instance

  override def apply(that: AnyRef): AnyRef = instance(that)

  override def preConditions: Set[StepSequencer.Condition] = Set(
    HasLabelsOrTypesReplacedIfPossible, // These have to have been rewritten to HasLabels / HasTypes at this point
    !containsNamedPathOnlyForShortestPath // this rewriter will not achieve 'noReferenceEqualityAmongVariables' if projectNamedPaths run first
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(OnlySingleHasLabels, noReferenceEqualityAmongVariables)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  private val instance: Rewriter = topDown(Rewriter.lift {
    case c@NotEquals(lhs, rhs) =>
      NotEquals(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@Equals(lhs, rhs) =>
      Equals(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@LessThan(lhs, rhs) =>
      LessThan(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@LessThanOrEqual(lhs, rhs) =>
      LessThanOrEqual(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@GreaterThan(lhs, rhs) =>
      GreaterThan(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@GreaterThanOrEqual(lhs, rhs) =>
      GreaterThanOrEqual(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@InvalidNotEquals(lhs, rhs) =>
      InvalidNotEquals(lhs.endoRewrite(copyVariables), rhs.endoRewrite(copyVariables))(c.position)
    case c@HasLabels(expr, labels) if labels.size > 1 =>
      val hasLabels = labels.map(l => HasLabels(expr.endoRewrite(copyVariables), Seq(l))(c.position))
      Ands(hasLabels)(c.position)
    case c@HasTypes(expr, types) if types.size > 1 =>
      val hasTypes = types.map(t => HasTypes(expr.endoRewrite(copyVariables), Seq(t))(c.position))
      Ands(hasTypes)(c.position)
  })
}
