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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.frontend.phases.rewriting.cnf.CNFNormalizer
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.rewriteEqualityToInPredicate
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.rewriting.rewriters.IfNoParameter
import org.opencypher.v9_0.rewriting.rewriters.LiteralExtractionStrategy
import org.opencypher.v9_0.rewriting.rewriters.SameNameNamer

object CompilationPhases {

  def parsing(literalExtractionStrategy: LiteralExtractionStrategy = IfNoParameter,
              deprecations: Deprecations = Deprecations.deprecatedFeaturesIn4_X
             ): Transformer[BaseContext, BaseState, BaseState] =
    Parsing andThen
      SyntaxDeprecationWarningsAndReplacements(deprecations) andThen
      PreparatoryRewriting andThen
      SemanticAnalysis(warn = true) andThen
      AstRewriting(innerVariableNamer = SameNameNamer) andThen
      LiteralExtraction(literalExtractionStrategy)

  def lateAstRewriting: Transformer[BaseContext, BaseState, BaseState] =
    isolateAggregation andThen
      SemanticAnalysis(warn = false) andThen
      Namespacer andThen
      transitiveClosure andThen
      rewriteEqualityToInPredicate andThen
      CNFNormalizer andThen
      collapseMultipleInPredicates andThen
      SemanticAnalysis(warn = false)
}
