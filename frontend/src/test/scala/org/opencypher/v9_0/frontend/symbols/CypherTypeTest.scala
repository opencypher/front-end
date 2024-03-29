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
package org.opencypher.v9_0.frontend.symbols

import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTGraphRef
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class CypherTypeTest extends CypherFunSuite {

  test("parents should be full path up type tree branch") {
    CTInteger.parents should equal(Seq(CTNumber, CTAny))
    CTNumber.parents should equal(Seq(CTAny))
    CTAny.parents should equal(Seq())
    CTList(CTString).parents should equal(Seq(CTList(CTAny), CTAny))
  }

  test("foo") {
    val covariant = CTGraphRef.covariant
    covariant should not be empty
  }

  test("should be assignable from sub-type") {
    CTNumber.isAssignableFrom(CTInteger) should equal(true)
    CTAny.isAssignableFrom(CTString) should equal(true)
    CTList(CTString).isAssignableFrom(CTList(CTString)) should equal(true)
    CTList(CTNumber).isAssignableFrom(CTList(CTInteger)) should equal(true)
    CTInteger.isAssignableFrom(CTNumber) should equal(false)
    CTList(CTInteger).isAssignableFrom(CTList(CTString)) should equal(false)
  }

  test("should find leastUpperBound") {
    assertLeastUpperBound(CTNumber, CTNumber, CTNumber)
    assertLeastUpperBound(CTNumber, CTAny, CTAny)
    assertLeastUpperBound(CTNumber, CTString, CTAny)
    assertLeastUpperBound(CTNumber, CTList(CTAny), CTAny)
    assertLeastUpperBound(CTInteger, CTFloat, CTNumber)
    assertLeastUpperBound(CTMap, CTFloat, CTAny)
  }

  private def assertLeastUpperBound(a: CypherType, b: CypherType, result: CypherType): Unit = {
    val simpleMergedType: CypherType = a leastUpperBound b
    simpleMergedType should equal(result)
    val listMergedType: CypherType = CTList(a) leastUpperBound CTList(b)
    listMergedType should equal(CTList(result))
  }

  test("should find greatestLowerBound") {
    assertGreatestLowerBound(CTNumber, CTNumber, Some(CTNumber))
    assertGreatestLowerBound(CTNumber, CTAny, Some(CTNumber))
    assertGreatestLowerBound(CTList(CTNumber), CTList(CTInteger), Some(CTList(CTInteger)))
    assertGreatestLowerBound(CTNumber, CTString, None)
    assertGreatestLowerBound(CTNumber, CTList(CTAny), None)
    assertGreatestLowerBound(CTInteger, CTFloat, None)
    assertGreatestLowerBound(CTMap, CTFloat, None)
    assertGreatestLowerBound(CTBoolean, CTList(CTAny), None)
  }

  private def assertGreatestLowerBound(a: CypherType, b: CypherType, result: Option[CypherType]): Unit = {
    val simpleMergedType: Option[CypherType] = a greatestLowerBound b
    simpleMergedType should equal(result)
    val listMergedType: Option[CypherType] = CTList(a) greatestLowerBound CTList(b)
    listMergedType should equal(for (t <- result) yield CTList(t))
  }
}
