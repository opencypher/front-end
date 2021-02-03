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
import org.opencypher.v9_0.util.symbols.CTLocalDateTime
import org.opencypher.v9_0.util.symbols.CTLocalTime
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.symbols.CTTime

class ToStringTest extends FunctionTestBase("toString")  {

  test("should accept correct types or types that could be correct at runtime") {
    testValidTypes(CTString)(CTString)
    testValidTypes(CTFloat)(CTString)
    testValidTypes(CTInteger)(CTString)
    testValidTypes(CTBoolean)(CTString)
    testValidTypes(CTAny.covariant)(CTString)
    testValidTypes(CTNumber.covariant)(CTString)
    testValidTypes(CTDuration)(CTString)
    testValidTypes(CTDate)(CTString)
    testValidTypes(CTTime)(CTString)
    testValidTypes(CTLocalTime)(CTString)
    testValidTypes(CTLocalDateTime)(CTString)
    testValidTypes(CTDateTime)(CTString)
    testValidTypes(CTPoint)(CTString)
  }

  test("should fail type check for incompatible arguments") {
    testInvalidApplication(CTRelationship)(
      "Type mismatch: expected Boolean, Float, Integer, Point, String, Duration, Date, Time, LocalTime, LocalDateTime or DateTime but was Relationship"
    )

    testInvalidApplication(CTNode)(
      "Type mismatch: expected Boolean, Float, Integer, Point, String, Duration, Date, Time, LocalTime, LocalDateTime or DateTime but was Node"
    )
  }

  test("should fail if wrong number of arguments") {
    testInvalidApplication()(
      "Insufficient parameters for function 'toString'"
    )
    testInvalidApplication(CTString, CTString)(
      "Too many parameters for function 'toString'"
    )
  }
}
