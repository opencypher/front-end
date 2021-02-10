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

import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.AST_REWRITE
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.rewriting.ListStepAccumulator
import org.opencypher.v9_0.rewriting.RewriterStep
import org.opencypher.v9_0.rewriting.rewriters.LiteralsAreAvailable
import org.opencypher.v9_0.rewriting.rewriters.expandCallWhere
import org.opencypher.v9_0.rewriting.rewriters.expandShowWhere
import org.opencypher.v9_0.rewriting.rewriters.factories.PreparatoryRewritingRewriterFactory
import org.opencypher.v9_0.rewriting.rewriters.insertWithBetweenOptionalMatchAndMatch
import org.opencypher.v9_0.rewriting.rewriters.mergeInPredicates
import org.opencypher.v9_0.rewriting.rewriters.normalizeWithAndReturnClauses
import org.opencypher.v9_0.rewriting.rewriters.replaceDeprecatedCypherSyntax
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.AccumulatedSteps
import org.opencypher.v9_0.util.inSequence

/**
 * Rewrite the AST into a shape that semantic analysis can be performed on.
 */
case class PreparatoryRewriting(deprecations: Deprecations) extends Phase[BaseContext, BaseState, BaseState] {

  val AccumulatedSteps(orderedSteps, _) = new StepSequencer(ListStepAccumulator[StepSequencer.Step with PreparatoryRewritingRewriterFactory]()).orderSteps(Set(
    normalizeWithAndReturnClauses,
    insertWithBetweenOptionalMatchAndMatch,
    expandCallWhere,
    expandShowWhere,
    replaceDeprecatedCypherSyntax,
    mergeInPredicates), initialConditions = Set(LiteralsAreAvailable))

  override def process(from: BaseState, context: BaseContext): BaseState = {

    val rewriters = orderedSteps.map { step =>
      val rewriter = step.getRewriter(deprecations, context.cypherExceptionFactory, context.notificationLogger)
      RewriterStep.validatingRewriter(rewriter, step)
    }

    val rewrittenStatement = from.statement().endoRewrite(inSequence(rewriters: _*))

    from.withStatement(rewrittenStatement)
  }

  override val phase = AST_REWRITE

  override def postConditions: Set[StepSequencer.Condition] = Set.empty
}

