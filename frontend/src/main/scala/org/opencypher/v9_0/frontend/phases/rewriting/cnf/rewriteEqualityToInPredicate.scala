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

import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.expressions.DeterministicFunctionInvocation
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.In
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.frontend.phases.EqualityRewrittenToIn
import org.opencypher.v9_0.frontend.phases.StatementRewriter
import org.opencypher.v9_0.frontend.phases.Transformer
import org.opencypher.v9_0.frontend.phases.factories.PlanPipelineTransformerFactory
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp

/**
 * Normalize equality predicates into IN comparisons.
 */
case object rewriteEqualityToInPredicate extends StatementRewriter with StepSequencer.Step
    with PlanPipelineTransformerFactory {

  val instance: Rewriter = bottomUp(Rewriter.lift {
    // if f is deterministic: f(a) = value => f(a) IN [value]
    case predicate @ Equals(DeterministicFunctionInvocation(invocation), value) =>
      In(invocation, ListLiteral(Seq(value))(value.position))(predicate.position)

    // Equality between two property lookups should not be rewritten
    case predicate @ Equals(_: Property, _: Property) =>
      predicate

    // a.prop = value => a.prop IN [value]
    case predicate @ Equals(prop @ Property(id: Variable, propKeyName), idValueExpr) =>
      In(prop, ListLiteral(Seq(idValueExpr))(idValueExpr.position))(predicate.position)
  })

  override def instance(from: BaseState, ignored: BaseContext): Rewriter = instance

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(EqualityRewrittenToIn)

  override def invalidatedConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable // Introduces new AST nodes

  override def getTransformer(
    pushdownPropertyReads: Boolean,
    semanticFeatures: Seq[SemanticFeature]
  ): Transformer[BaseContext, BaseState, BaseState] = this
}
