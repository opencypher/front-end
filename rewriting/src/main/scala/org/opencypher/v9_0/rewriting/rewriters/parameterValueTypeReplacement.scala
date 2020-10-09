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

import org.opencypher.v9_0.expressions.ExplicitParameter
import org.opencypher.v9_0.rewriting.RewritingStep
import org.opencypher.v9_0.rewriting.conditions.containsNoReturnAll
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CypherType

case object ExplicitParametersKnowTheirTypes extends StepSequencer.Condition

case class parameterValueTypeReplacement(parameterTypeMapping: Map[String, CypherType]) extends RewritingStep {

  private val rewriter: Rewriter = bottomUp(Rewriter.lift {
    case p@ExplicitParameter(name, CTAny) =>
      val cypherType = parameterTypeMapping.getOrElse(name, CTAny)
      ExplicitParameter(name, cypherType)(p.position)
  })


  override def rewrite(that: AnyRef): AnyRef = rewriter(that)

  // TODO depends on SyntaxDeprecationWarnings(Deprecations.V2) being run which replaces ParameterWithOldSyntax with ExplicitParameter
  // TODO this should be captured differently. This has an invalidated condition `ProjectionClausesHaveSemanticInfo`,
  // which is a pre-condition of expandStar. It can invalidate this condition by rewriting things inside WITH/RETURN.
  // But to do that we need a step that introduces that condition which would be SemanticAnalysis.
  override def preConditions: Set[StepSequencer.Condition] = Set(containsNoReturnAll)

  override def postConditions: Set[StepSequencer.Condition] = Set(ExplicitParametersKnowTheirTypes)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty
}
