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
package org.opencypher.v9_0.ast.semantics.functions

import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTDate
import org.opencypher.v9_0.util.symbols.CTDateTime
import org.opencypher.v9_0.util.symbols.CTDuration
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTLocalDateTime
import org.opencypher.v9_0.util.symbols.CTLocalTime
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.symbols.CTTime

class ToStringListTest extends FunctionTestBase("toStringList")  {

  test("shouldAcceptCorrectTypes") {
    testValidTypes(CTList(CTAny))(CTList(CTString))
    testValidTypes(CTList(CTString))(CTList(CTString))
    testValidTypes(CTList(CTFloat))(CTList(CTString))
    testValidTypes(CTList(CTInteger))(CTList(CTString))
    testValidTypes(CTList(CTBoolean))(CTList(CTString))
    testValidTypes(CTList(CTNumber))(CTList(CTString))
    testValidTypes(CTList(CTDuration))(CTList(CTString))
    testValidTypes(CTList(CTDate))(CTList(CTString))
    testValidTypes(CTList(CTTime))(CTList(CTString))
    testValidTypes(CTList(CTLocalTime))(CTList(CTString))
    testValidTypes(CTList(CTLocalDateTime))(CTList(CTString))
    testValidTypes(CTList(CTDateTime))(CTList(CTString))
    testValidTypes(CTList(CTPoint))(CTList(CTString))
    testValidTypes(CTList(CTMap))(CTList(CTString))
    testValidTypes(CTList(CTNode))(CTList(CTString))
    testValidTypes(CTList(CTRelationship))(CTList(CTString))
  }

  test("shouldFailTypeCheckForIncompatibleArguments") {
    testInvalidApplication(CTNode)(
      "Type mismatch: expected List<T> but was Node"
    )

    testInvalidApplication(CTDate)(
      "Type mismatch: expected List<T> but was Date"
    )

    testInvalidApplication(CTString)(
      "Type mismatch: expected List<T> but was String"
    )
  }

  test("shouldFailIfWrongNumberOfArguments") {
    testInvalidApplication()(
      "Insufficient parameters for function 'toStringList'"
    )
    testInvalidApplication(CTString, CTString)(
      "Too many parameters for function 'toStringList'"
    )
  }
}
