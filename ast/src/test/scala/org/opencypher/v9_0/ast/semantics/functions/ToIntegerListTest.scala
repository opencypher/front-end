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
import org.opencypher.v9_0.util.symbols.CTDate
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTString

class ToIntegerListTest extends FunctionTestBase("toIntegerList") {

  test("shouldAcceptCorrectTypes") {
    testValidTypes(CTList(CTAny))(CTList(CTInteger))
    testValidTypes(CTList(CTString))(CTList(CTInteger))
    testValidTypes(CTList(CTFloat))(CTList(CTInteger))
    testValidTypes(CTList(CTInteger))(CTList(CTInteger))
    testValidTypes(CTList(CTPoint))(CTList(CTInteger))
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
      "Insufficient parameters for function 'toIntegerList'"
    )
    testInvalidApplication(CTString, CTString)(
      "Too many parameters for function 'toIntegerList'"
    )
  }
}
