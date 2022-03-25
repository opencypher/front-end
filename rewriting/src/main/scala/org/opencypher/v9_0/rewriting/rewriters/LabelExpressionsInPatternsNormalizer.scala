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

import org.opencypher.v9_0.expressions.EntityType
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.NODE_TYPE
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.RELATIONSHIP_TYPE
import org.opencypher.v9_0.expressions.RelationshipPattern

object LabelExpressionsInPatternsNormalizer extends MatchPredicateNormalizer {

  override val extract: PartialFunction[AnyRef, IndexedSeq[Expression]] = {
    case NodePattern(Some(id), Some(expression), _, _) =>
      Vector(extractLabelExpressionPredicates(id, expression, NODE_TYPE))
    case RelationshipPattern(Some(id), Some(expression), None, _, _, _)
      if expression.containsGpmSpecificRelTypeExpression =>
      Vector(extractLabelExpressionPredicates(id, expression, RELATIONSHIP_TYPE))
  }

  override val replace: PartialFunction[AnyRef, AnyRef] = {
    case p @ NodePattern(Some(_), Some(_), _, _) => p.copy(labelExpression = None)(p.position)
    case p @ RelationshipPattern(Some(_), Some(expression), None, _, _, _)
      if expression.containsGpmSpecificRelTypeExpression =>
      p.copy(labelExpression = None)(p.position)
  }

  private def extractLabelExpressionPredicates(
    variable: LogicalVariable,
    e: LabelExpression,
    entityType: EntityType
  ): Expression = {
    LabelExpressionNormalizer(variable, Some(entityType))(e).asInstanceOf[Expression]
  }
}
