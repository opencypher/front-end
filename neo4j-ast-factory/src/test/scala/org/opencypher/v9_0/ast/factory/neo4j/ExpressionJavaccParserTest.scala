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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.expressions.Expression

class ExpressionJavaccParserTest extends JavaccParserAstTestBase[Expression] {

  implicit private val parser: JavaccRule[Expression] = JavaccRule.Expression

  test("thing CONTAINS 'a' + 'b'") {
    gives(contains(varFor("thing"), add(literalString("a"), literalString("b"))))
  }

  test("thing STARTS WITH 'a' + 'b'") {
    gives(startsWith(varFor("thing"), add(literalString("a"), literalString("b"))))
  }

  test("thing ENDS WITH 'a' + 'b'") {
    gives(endsWith(varFor("thing"), add(literalString("a"), literalString("b"))))
  }

  test("2*(2.0-1.5)") {
    gives {
      multiply(literal(2), subtract(literal(2.0), literal(1.5)))
    }
  }

  test("+1.5") {
    gives {
      unaryAdd(literal(1.5))
    }
  }

  test("+1") {
    gives {
      unaryAdd(literal(1))
    }
  }

  test("2*(2.0 - +1.5)") {
    gives {
      multiply(literal(2), subtract(literal(2.0), unaryAdd(literal(1.5))))
    }
  }

  test("0-1") {
    gives {
      subtract(literal(0), literal(1))
    }
  }

  test("0-0.1") {
    gives {
      subtract(literal(0), literal(0.1))
    }
  }
}
