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

import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.PathExpression
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.rewriting.RewritingStep
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInPatternComprehension
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp

case object NoNamedPathsInPatternComprehensions extends StepSequencer.Condition

case object inlineNamedPathsInPatternComprehensions extends RewritingStep {

  override def preConditions: Set[StepSequencer.Condition] = Set(noUnnamedPatternElementsInPatternComprehension)

  override def postConditions: Set[StepSequencer.Condition] = Set(NoNamedPathsInPatternComprehensions)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  private val instance = bottomUp(Rewriter.lift {
    case expr @ PatternComprehension(Some(path), pattern, predicate, projection) =>
      val patternElement = pattern.element
      expr.copy(
        namedPath = None,
        predicate = predicate.map(_.inline(path, patternElement)),
        projection = projection.inline(path, patternElement)
      )(expr.position, expr.outerScope)
  })

  private implicit final class InliningExpression(val expr: Expression) extends AnyVal {
    def inline(path: LogicalVariable, patternElement: PatternElement) =
      expr.copyAndReplace(path) by {
        PathExpression(projectNamedPaths.patternPartPathExpression(patternElement))(expr.position)
      }
  }

  override def rewrite(v: AnyRef): AnyRef = instance(v)
}
