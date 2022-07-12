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
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object NoNodePatternPredicatesInPatternComprehension extends StepSequencer.Condition

case object normalizePatternComprehensionPredicates extends StepSequencer.Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(NoNodePatternPredicatesInPatternComprehension)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = instance

  private val normalizer = MatchPredicateNormalizerChain(
    NodePatternPredicateNormalizer,
    RelationshipPatternPredicateNormalizer
  )

  private val rewriter = Rewriter.lift {
    case p: PatternComprehension =>
      val predicates = normalizer.extractAllFrom(p.pattern)
      val rewrittenPredicates = predicates ++ p.predicate
      val newPredicate: Option[Expression] = rewrittenPredicates.reduceOption(And(_, _)(p.position))

      p.copy(
        pattern = p.pattern.endoRewrite(topDown(Rewriter.lift(normalizer.replace))),
        predicate = newPredicate
      )(p.position, p.outerScope, p.variableToCollectName, p.collectionName)
  }

  val instance: Rewriter = topDown(rewriter)
}
