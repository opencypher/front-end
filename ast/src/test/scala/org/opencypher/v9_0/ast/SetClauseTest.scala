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
package org.opencypher.v9_0.ast

import org.opencypher.v9_0.ast.semantics.SemanticFunSuite
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.DummyExpression
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTMap

class SetClauseTest extends SemanticFunSuite {

  test("shouldHaveMergedTypesOfAllAlternativesInSimpleCase") {

    val mapLiteral = DummyExpression(CTMap)
    val property = Property(mapLiteral, PropertyKeyName("key")(DummyPosition(3)))(DummyPosition(5))
    val setItem = SetPropertyItem(property, DummyExpression(CTAny))(DummyPosition(42))
    val setClause = SetClause(Seq(setItem))(DummyPosition(6))

    val result = setClause.semanticCheck(SemanticState.clean)

    result.errors should have size 1
    result.errors.head.msg should startWith("Type mismatch: expected Node or Relationship but was Map")
  }
}
