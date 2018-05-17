/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_1.rewriting

import org.opencypher.v9_1.ast.AstConstructionTestSupport
import org.opencypher.v9_1.expressions.{FunctionInvocation, FunctionName}
import org.opencypher.v9_1.rewriting.rewriters.replaceAliasedFunctionInvocations
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite

class ReplaceAliasedFunctionInvocationsTest extends CypherFunSuite with AstConstructionTestSupport {

  val rewriter = replaceAliasedFunctionInvocations

  test("should rewrite toInt()") {
    val before = FunctionInvocation(FunctionName("toInt")(pos), literalInt(1))(pos)

    rewriter(before) should equal(before.copy(functionName = FunctionName("toInteger")(pos))(pos))
  }

  test("doesn't touch toInteger()") {
    val before = FunctionInvocation(FunctionName("toInteger")(pos), literalInt(1))(pos)

    rewriter(before) should equal(before)
  }

}
