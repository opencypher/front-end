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

class FunctionInvocationParserTest extends JavaccParserAstTestBase[Expression] {

  implicit private val parser: JavaccRule[Expression] = JavaccRule.FunctionInvocation

  test("foo()") {
    gives(function("foo"))
  }

  test("foo('test', 1 + 2)") {
    gives(function("foo", literalString("test"), add(literalInt(1), literalInt(2))))
  }
  test("my.namespace.foo()") {
    gives(function(List("my", "namespace"), "foo"))
  }

  test("my.namespace.foo('test', 1 + 2)") {
    gives(function(List("my", "namespace"), "foo", literalString("test"), add(literalInt(1), literalInt(2))))
  }
}
