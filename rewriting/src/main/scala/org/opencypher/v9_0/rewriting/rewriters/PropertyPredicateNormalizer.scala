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

import org.opencypher.v9_0.expressions.AllIterablePredicate
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.MapExpression
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator

case class PropertyPredicateNormalizer(anonymousVariableNameGenerator: AnonymousVariableNameGenerator)
    extends MatchPredicateNormalizer {

  override val extract: PartialFunction[AnyRef, IndexedSeq[Expression]] = {
    case NodePattern(Some(id), _, Some(props), _) if !isParameter(props) =>
      propertyPredicates(id, props)

    case RelationshipPattern(Some(id), _, None, Some(props), _, _) if !isParameter(props) =>
      propertyPredicates(id, props)

    case RelationshipPattern(Some(id), _, Some(_), Some(props), _, _) if !isParameter(props) =>
      Vector(varLengthPropertyPredicates(id, props))
  }

  override val replace: PartialFunction[AnyRef, AnyRef] = {
    case p @ NodePattern(Some(_), _, Some(props), _) if !isParameter(props) => p.copy(properties = None)(p.position)
    case p @ RelationshipPattern(Some(_), _, _, Some(props), _, _) if !isParameter(props) =>
      p.copy(properties = None)(p.position)
  }

  private def isParameter(expr: Expression) = expr match {
    case _: Parameter => true
    case _            => false
  }

  private def propertyPredicates(id: LogicalVariable, props: Expression): IndexedSeq[Expression] = props match {
    case mapProps: MapExpression =>
      mapProps.items.map {
        // MATCH (a {a: 1, b: 2}) => MATCH (a) WHERE a.a = 1 AND a.b = 2
        case (propId, expression) =>
          Equals(Property(id.copyId, propId)(mapProps.position), expression)(mapProps.position)
      }.toIndexedSeq
    case expr: Expression =>
      Vector(Equals(id.copyId, expr)(expr.position))
    case _ =>
      Vector.empty
  }

  private def varLengthPropertyPredicates(id: LogicalVariable, props: Expression): Expression = {
    val idName = anonymousVariableNameGenerator.nextName
    val newId = Variable(idName)(id.position)
    val expressions = propertyPredicates(newId, props)
    val conjunction = conjunct(expressions.toList)
    AllIterablePredicate(newId, id.copyId, Some(conjunction))(props.position)
  }

  private def conjunct(exprs: List[Expression]): Expression = exprs match {
    case Nil          => throw new IllegalArgumentException("There should be at least one predicate to be rewritten")
    case expr :: Nil  => expr
    case expr :: tail => And(expr, conjunct(tail))(expr.position)
  }
}
