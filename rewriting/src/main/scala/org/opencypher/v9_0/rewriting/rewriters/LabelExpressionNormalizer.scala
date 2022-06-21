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

import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.EntityType
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.HasALabel
import org.opencypher.v9_0.expressions.HasALabelOrType
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.HasTypes
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelExpression.Leaf
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.LabelOrRelTypeName
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.NODE_TYPE
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.RELATIONSHIP_TYPE
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.True
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.topDown

/**
 * Rewrites relationship type/label expressions to HasLabel/HasType predicates that the rest of the query engine can understand.
 * @param entityExpression expression to return the entity to check the label expression on
 * @param entityType if used in a pattern, the type of the pattern, None otherwise (in a predicate)
 */
case class LabelExpressionNormalizer(entityExpression: Expression, entityType: Option[EntityType]) extends Rewriter {

  val instance: Rewriter = Rewriter.lift {
    case labelExpression: LabelExpression => rewriteLabelExpression(labelExpression)
  }

  def rewriteLabelExpression(labelExpression: LabelExpression): Expression = labelExpression match {
    case colonConjunction: LabelExpression.ColonConjunction =>
      And(rewriteLabelExpression(colonConjunction.lhs), rewriteLabelExpression(colonConjunction.rhs))(
        colonConjunction.position
      )

    case conjunction: LabelExpression.Conjunction =>
      And(rewriteLabelExpression(conjunction.lhs), rewriteLabelExpression(conjunction.rhs))(conjunction.position)

    case colonDisjunction: LabelExpression.ColonDisjunction =>
      Or(rewriteLabelExpression(colonDisjunction.lhs), rewriteLabelExpression(colonDisjunction.rhs))(
        colonDisjunction.position
      )

    case disjunction: LabelExpression.Disjunctions =>
      disjunction.children
        .map(rewriteLabelExpression)
        .reduceRight((a, b) => Or(a, b)(a.position))

    // in a node pattern
    case Leaf(name: LabelName) =>
      HasLabels(copy(entityExpression), Seq(name))(name.position)

    // in a label expression predicate
    case Leaf(name: LabelOrRelTypeName) =>
      HasLabelsOrTypes(copy(entityExpression), Seq(name))(name.position)

    // in a relationship pattern
    case Leaf(name: RelTypeName) =>
      HasTypes(copy(entityExpression), Seq(name))(name.position)

    case leaf @ Leaf(_) =>
      throw new IllegalArgumentException(
        s"Unexpected non-implemented label expression leaf $leaf when rewriting label expressions"
      )

    case negation: LabelExpression.Negation =>
      Not(rewriteLabelExpression(negation.e))(negation.position)

    case wildcard: LabelExpression.Wildcard =>
      entityType match {
        case None =>
          HasALabelOrType(copy(entityExpression))(wildcard.position)
        case Some(NODE_TYPE) =>
          HasALabel(copy(entityExpression))(wildcard.position)
        case Some(RELATIONSHIP_TYPE) =>
          // all relationships have a type
          True()(wildcard.position)
      }
  }

  /**
   * Needed to fulfil `noReferenceEqualityAmongVariables`
   */
  def copy(expr: Expression): Expression = expr match {
    case variable: LogicalVariable => variable.copyId
    case _                         => expr
  }

  override def apply(v1: AnyRef): AnyRef = topDown(instance)(v1)
}
