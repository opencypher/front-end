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

import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInMatch
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object NoPredicatesInNamedPartsOfMatchPattern extends StepSequencer.Condition

case object normalizeMatchPredicates extends StepSequencer.Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set(
    noUnnamedPatternElementsInMatch // unnamed pattern cannot be rewritten, so they need to handled first
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(NoPredicatesInNamedPartsOfMatchPattern)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    HasLabelsOrTypesReplacedIfPossible,
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter =
    normalizeMatchPredicates(MatchPredicateNormalizer.defaultNormalizer(anonymousVariableNameGenerator))
}

case class normalizeMatchPredicates(normalizer: MatchPredicateNormalizer) extends Rewriter {
  override def apply(that: AnyRef): AnyRef = instance(that)

  private val rewriter = Rewriter.lift {
    case m @ Match(_, pattern, _, where) =>
      val predicates = normalizer.extractAllFrom(pattern)
      val rewrittenPredicates = predicates ++ where.map(_.expression)
      val predOpt: Option[Expression] = rewrittenPredicates.reduceOption(And(_, _)(m.position))

      val newWhere: Option[Where] = predOpt.map {
        exp =>
          val pos: InputPosition = where.fold(m.position)(_.position)
          Where(exp)(pos)
      }

      m.copy(
        pattern = pattern.endoRewrite(topDown(Rewriter.lift(normalizer.replace))),
        where = newWhere
      )(m.position)
  }

  private val instance = topDown(rewriter, _.isInstanceOf[Expression])
}
