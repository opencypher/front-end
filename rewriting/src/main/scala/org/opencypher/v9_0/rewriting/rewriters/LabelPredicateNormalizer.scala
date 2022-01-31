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
import org.opencypher.v9_0.expressions.BooleanExpression
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral

object LabelPredicateNormalizer extends MatchPredicateNormalizer {
  override val extract: PartialFunction[AnyRef, IndexedSeq[Expression]] = {
    case p@NodePattern(Some(id), labels, _, _, _) if labels.nonEmpty => Vector(HasLabels(id.copyId, labels)(p.position))
    case NodePattern(Some(id), _, Some(expression), _, _)            => Vector(extractLabelExpressionPredicates(id, expression))
  }

  override val replace: PartialFunction[AnyRef, AnyRef] = {
    case p@NodePattern(Some(_), labels, _, _, _) if labels.nonEmpty => p.copy(labels = Seq.empty)(p.position)
    case p@NodePattern(Some(_), _, Some(_), _, _)                   => p.copy(labelExpression = None)(p.position)
  }

  private def extractLabelExpressionPredicates(variable: LogicalVariable, e: LabelExpression): BooleanExpression = {
    e match {
      case c: LabelExpression.Conjunction => And(
        extractLabelExpressionPredicates(variable.copyId, c.lhs),
        extractLabelExpressionPredicates(variable.copyId, c.rhs)
      )(c.position)

      case d: LabelExpression.Disjunction => Or(
        extractLabelExpressionPredicates(variable.copyId, d.lhs),
        extractLabelExpressionPredicates(variable.copyId, d.rhs)
      )(d.position)

      case n: LabelExpression.Negation => Not(
        extractLabelExpressionPredicates(variable.copyId, n.e)
      )(n.position)

      case n: LabelExpression.Wildcard =>
        val size: Expression => FunctionInvocation = FunctionInvocation(FunctionName("size")(n.position), _)(n.position)
        val labels: Expression => FunctionInvocation = FunctionInvocation(FunctionName("labels")(n.position), _)(n.position)
        val zero = SignedDecimalIntegerLiteral("0")(n.position)

        GreaterThan(size(labels(variable.copyId)), zero)(n.position)

      case n: LabelExpression.Label => HasLabels(variable.copyId, Seq(LabelName(n.label.name)(n.position)))(n.position)
    }
  }
}
