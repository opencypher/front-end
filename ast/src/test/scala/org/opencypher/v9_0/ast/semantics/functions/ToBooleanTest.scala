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
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTString

class ToBooleanTest extends FunctionTestBase("toBoolean")  {

  test("shouldAcceptCorrectTypes") {
    testValidTypes(CTInteger)(CTBoolean)
    testValidTypes(CTString)(CTBoolean)
    testValidTypes(CTNumber.covariant)(CTBoolean)
    testValidTypes(CTAny.covariant)(CTBoolean)
    testValidTypes(CTBoolean)(CTBoolean)
  }

  test("shouldNotAcceptIncorrectTypes") {
    testInvalidApplication(CTFloat)(
      "Type mismatch: expected Boolean, Integer or String but was Float"
    )
    testInvalidApplication(CTMap)(
      "Type mismatch: expected Boolean, Integer or String but was Map"
    )
    testInvalidApplication(CTDate)(
      "Type mismatch: expected Boolean, Integer or String but was Date"
    )
    testInvalidApplication(CTPoint)(
      "Type mismatch: expected Boolean, Integer or String but was Point"
    )
  }

  test("shouldFailIfWrongNumberOfArguments") {
    testInvalidApplication()(
      "Insufficient parameters for function 'toBoolean'"
    )
    testInvalidApplication(CTString, CTMap)(
      "Too many parameters for function 'toBoolean'"
    )
  }
}
