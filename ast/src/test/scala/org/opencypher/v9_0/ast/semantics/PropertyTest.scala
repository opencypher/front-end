/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_0.ast.semantics

import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTDate
import org.opencypher.v9_0.util.symbols.CTDateTime
import org.opencypher.v9_0.util.symbols.CTDuration
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTLocalDateTime
import org.opencypher.v9_0.util.symbols.CTLocalTime
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CTTime
import org.opencypher.v9_0.util.symbols.StorableType

class PropertyTest extends SemanticFunSuite {

  Seq(CTMap, CTPoint, CTDate, CTTime, CTLocalTime, CTLocalDateTime, CTDateTime, CTDuration).foreach { cypherType =>
    test(s"accepts property access on a $cypherType") {
      val mapExpr: Variable = variable("map")
      val propertyKey: PropertyKeyName = propertyKeyName("prop")

      val beforeState = SemanticState.clean.newChildScope.declareVariable(mapExpr, cypherType).right.get

      val propExpr = property(mapExpr, propertyKey)
      val result = SemanticExpressionCheck.simple(propExpr)(beforeState)

      result.errors shouldBe empty
      types(propExpr)(result.state) should equal(CTAny.covariant)
    }
  }

  test("accepts property access on a node") {
    val mapExpr: Variable = variable("map")
    val propertyKey: PropertyKeyName = propertyKeyName("prop")

    val beforeState = SemanticState.clean.newChildScope.declareVariable(mapExpr, CTNode).right.get

    val propExpr = property(mapExpr, propertyKey)
    val result = SemanticExpressionCheck.simple(property(mapExpr, propertyKey))(beforeState)

    result.errors shouldBe empty
    types(propExpr)(result.state) should equal(StorableType.storableType)
  }

  test("accepts property access on a relationship") {
    val mapExpr: Variable = variable("map")
    val propertyKey: PropertyKeyName = propertyKeyName("prop")

    val beforeState = SemanticState.clean.newChildScope.declareVariable(mapExpr, CTRelationship).right.get

    val propExpr = property(mapExpr, propertyKey)
    val result = SemanticExpressionCheck.simple(propExpr)(beforeState)

    result.errors shouldBe empty
    types(propExpr)(result.state) should equal(StorableType.storableType)
  }

  test("refuses property access on an Integer") {
    val mapExpr: Variable = variable("map")
    val propertyKey: PropertyKeyName = propertyKeyName("prop")

    val beforeState = SemanticState.clean.newChildScope.declareVariable(mapExpr, CTInteger).right.get

    val result = SemanticExpressionCheck.simple(property(mapExpr, propertyKey))(beforeState)

    result.errors should equal(Seq(SemanticError("Type mismatch: expected Map, Node, Relationship, Point, Duration, Date, Time, LocalTime, LocalDateTime or DateTime but was Integer", pos)))
  }
}
