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
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelExpression.ColonConjunction
import org.opencypher.v9_0.expressions.LabelExpression.ColonDisjunction
import org.opencypher.v9_0.expressions.LabelExpression.Conjunction
import org.opencypher.v9_0.expressions.LabelExpression.Disjunctions
import org.opencypher.v9_0.expressions.LabelExpression.Leaf
import org.opencypher.v9_0.expressions.LabelExpression.Negation
import org.opencypher.v9_0.expressions.LabelExpression.Wildcard
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
import org.opencypher.v9_0.expressions.SymbolicName
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInMatch
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInPatternComprehension
import org.opencypher.v9_0.rewriting.rewriters.AddUniquenessPredicates.getRelTypesToConsider
import org.opencypher.v9_0.rewriting.rewriters.AddUniquenessPredicates.overlaps
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

import scala.util.control.TailCalls
import scala.util.control.TailCalls.TailRec

case object RelationshipUniquenessPredicatesInMatchAndMerge extends StepSequencer.Condition

case class AddUniquenessPredicates(anonymousVariableNameGenerator: AnonymousVariableNameGenerator) extends Rewriter {

  override def apply(that: AnyRef): AnyRef = instance(that)

  private val rewriter = Rewriter.lift {
    case m @ Match(_, pattern: Pattern, _, where: Option[Where]) =>
      val uniqueRels: Seq[UniqueRel] = collectUniqueRels(pattern)
      if (uniqueRels.size < 2) {
        m
      } else {
        val newWhere = addPredicate(m, uniqueRels, where)
        m.copy(where = newWhere)(m.position)
      }
    case m @ Merge(pattern: PatternPart, _, where: Option[Where]) =>
      val uniqueRels: Seq[UniqueRel] = collectUniqueRels(pattern)
      if (uniqueRels.size < 2) {
        m
      } else {
        val newWhere = addPredicate(m, uniqueRels, where)
        m.copy(where = newWhere)(m.position)
      }
  }

  private def addPredicate(clause: Clause, uniqueRels: Seq[UniqueRel], where: Option[Where]): Option[Where] = {
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
    pattern.folder.treeFold(Seq.empty[UniqueRel]) {
      case _: ScopeExpression =>
        acc => SkipChildren(acc)

      case _: ShortestPaths =>
        acc => SkipChildren(acc)

      case RelationshipChain(_, patRel @ RelationshipPattern(optIdent, labelExpression, _, _, _, _), _) =>
        acc => {
          val ident =
            optIdent.getOrElse(throw new IllegalStateException("This rewriter cannot work with unnamed patterns"))
          TraverseChildren(acc :+ UniqueRel(ident, labelExpression, patRel.isSingleLength))
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
          NoneIterablePredicate(
            innerX,
            x.variable.copyId,
            Some(AnyIterablePredicate(innerY, y.variable.copyId, Some(Equals(innerX.copyId, innerY.copyId)(pos)))(pos))
          )(pos)
      }
    }

  case class UniqueRel(variable: LogicalVariable, labelExpression: Option[LabelExpression], singleLength: Boolean) {
    def name: String = variable.name

    def isAlwaysDifferentFrom(other: UniqueRel): Boolean = {
      val relTypesToConsider =
        getRelTypesToConsider(labelExpression).concat(getRelTypesToConsider(other.labelExpression)).distinct
      val labelExpressionOverlaps = overlaps(relTypesToConsider, labelExpression)
      labelExpressionOverlaps.isEmpty || (labelExpressionOverlaps intersect overlaps(
        relTypesToConsider,
        other.labelExpression
      )).isEmpty
    }
  }
}

case object AddUniquenessPredicates extends Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set(
    noUnnamedPatternElementsInMatch,
    noUnnamedPatternElementsInPatternComprehension
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(RelationshipUniquenessPredicatesInMatchAndMerge)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = AddUniquenessPredicates(anonymousVariableNameGenerator)

  def evaluate(expression: LabelExpression, relType: SymbolicName): TailRec[Boolean] =
    expression match {
      case Conjunction(lhs, rhs)                => and(lhs, rhs, relType)
      case ColonConjunction(lhs, rhs)           => and(lhs, rhs, relType)
      case Disjunctions(children)               => ors(children, relType)
      case ColonDisjunction(lhs, rhs)           => ors(Seq(lhs, rhs), relType)
      case Negation(e)                          => TailCalls.tailcall(evaluate(e, relType)).map(value => !value)
      case Wildcard()                           => TailCalls.done(true)
      case Leaf(expressionRelType: RelTypeName) => TailCalls.done(expressionRelType == relType)
      case x =>
        throw new IllegalArgumentException(s"Unexpected label expression $x when evaluating relationship overlap")
    }

  private def ors(exprs: Seq[LabelExpression], relType: SymbolicName): TailRec[Boolean] = {
    if (exprs.isEmpty) TailCalls.done(false)
    else {
      for {
        head <- TailCalls.tailcall(evaluate(exprs.head, relType))
        tail <- if (head) TailCalls.done(true) else ors(exprs.tail, relType)
      } yield head || tail
    }
  }

  private def and(lhs: LabelExpression, rhs: LabelExpression, relType: SymbolicName): TailRec[Boolean] = {
    TailCalls.tailcall(evaluate(lhs, relType)).flatMap {
      case true  => TailCalls.tailcall(evaluate(rhs, relType))
      case false => TailCalls.done(false)
    }
  }

  def overlaps(relTypesToConsider: Seq[SymbolicName], labelExpression: Option[LabelExpression]): Seq[SymbolicName] = {
    relTypesToConsider.filter(relType => labelExpression.forall(le => evaluate(le, relType).result))
  }

  def getRelTypesToConsider(labelExpression: Option[LabelExpression]): Seq[SymbolicName] = {
    // also add the arbitrary rel type "" to check for rel types which are not explicitly named (such as in -[r]-> or -[r:%]->)
    labelExpression.map(_.flatten).getOrElse(Seq.empty) appended RelTypeName("")(InputPosition.NONE)
  }
}
