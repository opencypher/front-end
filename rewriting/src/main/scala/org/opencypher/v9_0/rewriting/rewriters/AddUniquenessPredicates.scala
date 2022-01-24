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

import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Merge
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.AnyIterablePredicate
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.NoneIterablePredicate
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternPart
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.ScopeExpression
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInMatch
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInPatternComprehension
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CypherType

case object RelationshipUniquenessPredicatesInMatchAndMerge extends StepSequencer.Condition

case class AddUniquenessPredicates(anonymousVariableNameGenerator: AnonymousVariableNameGenerator) extends Rewriter {

  override def apply(that: AnyRef): AnyRef = instance(that)

  private val rewriter = Rewriter.lift {
    case m@Match(_, pattern: Pattern, _, where: Option[Where]) =>
      val uniqueRels: Seq[UniqueRel] = collectUniqueRels(pattern)
      if (uniqueRels.size < 2) {
        m
      } else {
        val newWhere = addPredicate(m, uniqueRels, where)
        m.copy(where = newWhere)(m.position)
      }
    case m@Merge(pattern: PatternPart, _, where: Option[Where]) =>
      val uniqueRels: Seq[UniqueRel] = collectUniqueRels(pattern)
      if (uniqueRels.size < 2) {
        m
      } else {
        val newWhere = addPredicate(m, uniqueRels, where)
        m.copy(where = newWhere)(m.position)
      }
  }

  private def addPredicate(clause: Clause, uniqueRels:  Seq[UniqueRel], where: Option[Where]): Option[Where] = {
    val maybePredicate: Option[Expression] = createPredicateFor(uniqueRels, clause.position)
    val newWhere: Option[Where] = (where, maybePredicate) match {
      case (Some(oldWhere), Some(newPredicate)) =>
        Some(oldWhere.copy(expression = And(oldWhere.expression, newPredicate)(clause.position))(clause.position))

      case (None, Some(newPredicate)) =>
        Some(Where(expression = newPredicate)(clause.position))

      case (oldWhere, None) => oldWhere
    }
    newWhere
  }

  private val instance = bottomUp(rewriter, _.isInstanceOf[Expression])

  def collectUniqueRels(pattern: ASTNode): Seq[UniqueRel] =
    pattern.treeFold(Seq.empty[UniqueRel]) {
      case _:ScopeExpression =>
        acc => SkipChildren(acc)

      case _: ShortestPaths =>
        acc => SkipChildren(acc)

      case RelationshipChain(_, patRel@RelationshipPattern(optIdent, types, _, _, _, _, _), _) =>
        acc => {
          val ident = optIdent.getOrElse(throw new IllegalStateException("This rewriter cannot work with unnamed patterns"))
          TraverseChildren(acc :+ UniqueRel(ident, types.toSet, patRel.isSingleLength))
        }
    }

  private def createPredicateFor(uniqueRels: Seq[UniqueRel], pos: InputPosition): Option[Expression] = {
    createPredicatesFor(uniqueRels, pos).reduceOption(expressions.And(_, _)(pos))
  }

  def createPredicatesFor(uniqueRels: Seq[UniqueRel], pos: InputPosition): Seq[Expression] =
    for {
      x <- uniqueRels
      y <- uniqueRels if x.name < y.name && !x.isAlwaysDifferentFrom(y)
    } yield {
      (x.singleLength, y.singleLength) match {
        case (true, true) =>
          Not(Equals(x.variable.copyId, y.variable.copyId)(pos))(pos)

        case (true, false) =>
          val innerY = Variable(anonymousVariableNameGenerator.nextName)(y.variable.position)
          NoneIterablePredicate(innerY, y.variable.copyId, Some(Equals(x.variable.copyId, innerY.copyId)(pos)))(pos)

        case (false, true) =>
          val innerX = Variable(anonymousVariableNameGenerator.nextName)(x.variable.position)
          NoneIterablePredicate(innerX, x.variable.copyId, Some(Equals(innerX.copyId, y.variable.copyId)(pos)))(pos)

        case (false, false) =>
          val innerX = Variable(anonymousVariableNameGenerator.nextName)(x.variable.position)
          val innerY = Variable(anonymousVariableNameGenerator.nextName)(y.variable.position)
          NoneIterablePredicate(innerX, x.variable.copyId, Some(AnyIterablePredicate(innerY, y.variable.copyId, Some(Equals(innerX.copyId, innerY.copyId)(pos)))(pos)))(pos)
      }
    }

  case class UniqueRel(variable: LogicalVariable, types: Set[RelTypeName], singleLength: Boolean) {
    def name: String = variable.name

    def isAlwaysDifferentFrom(other: UniqueRel): Boolean =
      types.nonEmpty && other.types.nonEmpty && (types intersect other.types).isEmpty
  }
}

object AddUniquenessPredicates extends Step with ASTRewriterFactory {
  override def preConditions: Set[StepSequencer.Condition] = Set(
    noUnnamedPatternElementsInMatch,
    noUnnamedPatternElementsInPatternComprehension
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(RelationshipUniquenessPredicatesInMatchAndMerge)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def getRewriter(semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory,
                           anonymousVariableNameGenerator: AnonymousVariableNameGenerator): Rewriter = AddUniquenessPredicates(anonymousVariableNameGenerator)
}
