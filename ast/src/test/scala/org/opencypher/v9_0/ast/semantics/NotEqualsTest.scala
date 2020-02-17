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

import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTDate
import org.opencypher.v9_0.util.symbols.CTDateTime
import org.opencypher.v9_0.util.symbols.CTDuration
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTGeometry
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTLocalDateTime
import org.opencypher.v9_0.util.symbols.CTLocalTime
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.symbols.CTPath
import org.opencypher.v9_0.util.symbols.CTPoint
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.symbols.CTTime

class NotEqualsTest extends InfixExpressionTestBase(expressions.NotEquals(_, _)(DummyPosition(0))) {
  private val types = List(CTList(CTAny), CTInteger, CTFloat, CTNumber, CTNode, CTPath, CTRelationship, CTMap, CTPoint,
                   CTDate, CTDuration, CTBoolean, CTString, CTDateTime, CTGeometry, CTLocalDateTime, CTLocalTime, CTTime)

  test("should support equality checks among all types with Cypher 9 comparison semantics") {
    types.foreach { t1 =>
      types.foreach { t2 =>
        testValidTypes(t1, t2, useCypher9ComparisonSemantics = true)(CTBoolean)
      }
    }
  }
}
