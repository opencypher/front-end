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

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.mergeDuplicateBooleanOperators
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.normalizeSargablePredicates
import org.opencypher.v9_0.rewriting.ListStepAccumulator
import org.opencypher.v9_0.rewriting.RewriterStep
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.AddUniquenessPredicates
import org.opencypher.v9_0.rewriting.rewriters.ProjectionClausesHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.desugarMapProjection
import org.opencypher.v9_0.rewriting.rewriters.expandStar
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.rewriting.rewriters.factories.combineSetProperty
import org.opencypher.v9_0.rewriting.rewriters.foldConstants
import org.opencypher.v9_0.rewriting.rewriters.inlineNamedPathsInPatternComprehensions
import org.opencypher.v9_0.rewriting.rewriters.moveWithPastMatch
import org.opencypher.v9_0.rewriting.rewriters.nameAllPatternElements
import org.opencypher.v9_0.rewriting.rewriters.normalizeArgumentOrder
import org.opencypher.v9_0.rewriting.rewriters.normalizeComparisons
import org.opencypher.v9_0.rewriting.rewriters.normalizeExistsPatternExpressions
import org.opencypher.v9_0.rewriting.rewriters.normalizeHasLabelsAndHasType
import org.opencypher.v9_0.rewriting.rewriters.normalizeMatchPredicates
import org.opencypher.v9_0.rewriting.rewriters.normalizeNotEquals
import org.opencypher.v9_0.rewriting.rewriters.normalizePatternComprehensionPredicates
import org.opencypher.v9_0.rewriting.rewriters.parameterValueTypeReplacement
import org.opencypher.v9_0.rewriting.rewriters.replaceLiteralDynamicPropertyLookups
import org.opencypher.v9_0.rewriting.rewriters.rewriteOrderById
import org.opencypher.v9_0.rewriting.rewriters.simplifyIterablePredicates
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.AccumulatedSteps
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.symbols.CypherType

object ASTRewriter {

  private val AccumulatedSteps(orderedSteps, _) = StepSequencer(ListStepAccumulator[StepSequencer.Step with ASTRewriterFactory]()).orderSteps(Set(
    combineSetProperty,
    expandStar,
    normalizeHasLabelsAndHasType,
    desugarMapProjection,
    moveWithPastMatch,
    normalizeComparisons,
    foldConstants,
    mergeDuplicateBooleanOperators(),
    normalizeExistsPatternExpressions,
    nameAllPatternElements,
    normalizeMatchPredicates,
    normalizePatternComprehensionPredicates,
    normalizeNotEquals,
    normalizeArgumentOrder,
    normalizeSargablePredicates,
    AddUniquenessPredicates,
    simplifyIterablePredicates,
    replaceLiteralDynamicPropertyLookups,
    inlineNamedPathsInPatternComprehensions,
    parameterValueTypeReplacement,
    rewriteOrderById,
  ), initialConditions = Set(ProjectionClausesHaveSemanticInfo, PatternExpressionsHaveSemanticInfo))

  def rewrite(statement: Statement,
              semanticState: SemanticState,
              parameterTypeMapping: Map[String, CypherType],
              cypherExceptionFactory: CypherExceptionFactory,
              anonymousVariableNameGenerator: AnonymousVariableNameGenerator
             ): Statement = {
    val rewriters = orderedSteps.map { step =>
      val rewriter = step.getRewriter(semanticState, parameterTypeMapping, cypherExceptionFactory, anonymousVariableNameGenerator)
      RewriterStep.validatingRewriter(rewriter, step)
    }

    val combined = inSequence(rewriters.toSeq: _*)

    statement.endoRewrite(combined)
  }
}
