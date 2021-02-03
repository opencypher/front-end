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

import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.frontend.phases.factories.PlanPipelineTransformerFactory
import org.opencypher.v9_0.rewriting.AstRewritingMonitor
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.rewriters.deMorganRewriter
import org.opencypher.v9_0.rewriting.rewriters.distributeLawsRewriter
import org.opencypher.v9_0.rewriting.rewriters.flattenBooleanOperators
import org.opencypher.v9_0.rewriting.rewriters.normalizeInequalities
import org.opencypher.v9_0.rewriting.rewriters.normalizeSargablePredicates
import org.opencypher.v9_0.rewriting.rewriters.simplifyPredicates
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.inSequence

case object BooleanPredicatesInCNF extends StepSequencer.Condition

case object CNFNormalizer extends StatementRewriter with StepSequencer.Step with PlanPipelineTransformerFactory {

  override def description: String = "normalize boolean predicates into conjunctive normal form"

  override def instance(context: BaseContext): Rewriter = {
    implicit val monitor = context.monitors.newMonitor[AstRewritingMonitor]()
    inSequence(
      deMorganRewriter(),
      distributeLawsRewriter(),
      normalizeInequalities,
      flattenBooleanOperators,
      simplifyPredicates,
      // Redone here since CNF normalization might introduce negated inequalities (which this removes)
      normalizeSargablePredicates
    )
  }

  override def preConditions: Set[StepSequencer.Condition] =
    flattenBooleanOperators.preConditions ++ normalizeSargablePredicates.preConditions

  override def postConditions: Set[StepSequencer.Condition] = Set(BooleanPredicatesInCNF) ++ flattenBooleanOperators.postConditions ++ normalizeSargablePredicates.postConditions

  override def invalidatedConditions: Set[StepSequencer.Condition] =
    normalizeSargablePredicates.invalidatedConditions ++
      flattenBooleanOperators.invalidatedConditions ++
      SemanticInfoAvailable // Introduces new AST nodes

  override def getTransformer(pushdownPropertyReads: Boolean,
                              semanticFeatures: Seq[SemanticFeature]): Transformer[BaseContext, BaseState, BaseState] = this
}
