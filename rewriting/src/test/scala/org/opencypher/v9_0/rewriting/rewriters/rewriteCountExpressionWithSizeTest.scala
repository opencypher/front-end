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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.rewriting.RewriteTest
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class rewriteCountExpressionWithSizeTest extends CypherFunSuite with RewriteTest {

  val rewriterUnderTest: Rewriter = rewriteCountExpression(new AnonymousVariableNameGenerator)

  test("rewrites pattern relationship COUNT without predicates") {
    assertRewrite(
      """MATCH (a) WHERE COUNT{(a)-[]->()} > 0 RETURN a""".stripMargin,
      """MATCH (a) WHERE size([`  UNNAMED2`=(a)-[]->() | `  UNNAMED2`]) > 0 RETURN a""".stripMargin
    )
  }

  test("rewrites pattern relationship COUNT with predicates") {
    assertRewrite(
      """MATCH (a) WHERE COUNT{(a)-[]->() WHERE a.key = "value"} > 0 RETURN a""".stripMargin,
      """MATCH (a) WHERE size([`  UNNAMED5`=(a)-[]->() WHERE a.key = "value"| `  UNNAMED5`]) > 0 RETURN a""".stripMargin
    )
  }

  test("rewrites node pattern COUNT") {
    assertRewrite(
      """MATCH (a) WHERE COUNT{(a)} > 0 RETURN a""".stripMargin,
      """MATCH (a) WHERE size([(a)]) > 0 RETURN a""".stripMargin
    )
  }
}
