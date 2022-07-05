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
import org.opencypher.v9_0.expressions.CountExpression
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PathConcatenation
import org.opencypher.v9_0.expressions.PathFactor
import org.opencypher.v9_0.expressions.QuantifiedPath
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInMatch
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInPatternComprehension
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object QppsHavePaddedNodes extends StepSequencer.Condition

/**
 * A Quantified Path Pattern should always have a node or relationship chain surrounding it,
 * a user may omit these in their query whilst the planner expects them, this rewriter will therefore add
 * a filler Node in
 */
case object QuantifiedPathPatternNodeInsertRewriter extends StepSequencer.Step with ASTRewriterFactory {

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = instance

  private val filler = NodePattern(None, None, None, None)(InputPosition.NONE)

  override def preConditions: Set[StepSequencer.Condition] = Set()

  override def postConditions: Set[StepSequencer.Condition] = Set(QppsHavePaddedNodes)

  override def invalidatedConditions: Set[StepSequencer.Condition] =
    SemanticInfoAvailable ++ Set(
      // we potentially introduce unnamed pattern nodes
      noUnnamedPatternElementsInMatch,
      noUnnamedPatternElementsInPatternComprehension
    )

  val instance: Rewriter = topDown(Rewriter.lift {
    // A `PatternElement` occurs only in `ShortestPaths`, `EveryPath` and `CountExpressions`
    // However, `ShortestPaths` may only contain `RelationshipChain`s. That's why we
    // only need to check the other 2.
    case ce @ CountExpression(p @ PathConcatenation(factors), _) =>
      val newFactors = padQuantifiedPathPatterns(factors)
      ce.copy(PathConcatenation(newFactors)(p.position))(ce.position, ce.outerScope)

    case ce @ CountExpression(q: QuantifiedPath, _) =>
      ce.copy(PathConcatenation(Seq(filler, q, filler))(q.position))(ce.position, ce.outerScope)

    case EveryPath(p @ PathConcatenation(factors)) =>
      val newFactors = padQuantifiedPathPatterns(factors)
      EveryPath(PathConcatenation(newFactors)(p.position))

    case EveryPath(q: QuantifiedPath) =>
      EveryPath(PathConcatenation(Seq(filler, q, filler))(q.position))
  })

  private def padQuantifiedPathPatterns(factors: Seq[PathFactor]): Seq[PathFactor] = {
    val newFactors = (None +: factors.map(Some(_)) :+ None)
      .sliding(2).flatMap {
        case Seq(None, Some(q: QuantifiedPath))                    => Seq(filler, q)
        case Seq(Some(_: QuantifiedPath), None)                    => Seq(filler)
        case Seq(_, None)                                          => Seq()
        case Seq(Some(_: QuantifiedPath), Some(q: QuantifiedPath)) => Seq(filler, q)
        case Seq(_, Some(second))                                  => Seq(second)
        // Sliding should always return sequence of at most 2 elements, we know our list is
        // at least 3 elements so we cannot hit the 1 element case
        case _ => throw new IllegalStateException()
      }
    newFactors.toSeq
  }
}
