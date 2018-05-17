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
package org.opencypher.v9_1.frontend.phases.rewriting

import org.opencypher.v9_1.ast.Query
import org.opencypher.v9_1.rewriting.rewriters.mergeInPredicates
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite
import org.opencypher.v9_1.frontend.phases.CNFNormalizer
import org.opencypher.v9_1.rewriting.AstRewritingTestSupport

class mergeInPredicatesTest extends CypherFunSuite with AstRewritingTestSupport {

  test("MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [2,3,4] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [2,3,4] RETURN *",
      "MATCH (a) WHERE a.prop IN [2,3] RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [4,5,6] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [4,5,6] RETURN *",
      "MATCH (a) WHERE false RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [2,3,4] AND a.prop IN [3,4,5] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] AND a.prop IN [2,3,4] AND a.prop IN [3,4,5] RETURN *",
      "MATCH (a) WHERE a.prop IN [3] RETURN *")
  }

  test("MATCH (a) WHERE (a.prop IN [1,2,3] AND a.prop IN [2,3,4]) OR (a.prop IN [2,3,4] AND a.prop IN [3,4,5]) RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE (a.prop IN [1,2,3] AND a.prop IN [2,3,4]) OR (a.prop IN [2,3,4] AND a.prop IN [3,4,5]) RETURN *",
      "MATCH (a) WHERE a.prop IN [2,3,4] RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] AND a.foo IN ['foo', 'bar'] AND a.prop IN [2,3,4] AND a.foo IN ['bar'] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] AND a.foo IN ['foo','bar'] AND a.prop IN [2,3,4] AND a.foo IN ['bar'] RETURN *",
      "MATCH (a) WHERE a.prop IN [2,3] AND a.foo IN ['bar'] RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] OR a.prop IN [2,3,4] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] OR a.prop IN [2,3,4] RETURN *",
      "MATCH (a) WHERE a.prop IN [1,2,3,4] RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] OR a.prop IN [2,3,4] OR a.prop IN [3,4,5] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] OR a.prop IN [2,3,4] OR a.prop IN [3,4,5] RETURN *",
      "MATCH (a) WHERE a.prop IN [1,2,3,4,5] RETURN *")
  }

  test("MATCH (a) WHERE (a.prop IN [1,2,3] OR a.prop IN [2,3,4]) AND (a.prop IN [2,3,4] OR a.prop IN [3,4,5]) RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE (a.prop IN [1,2,3] OR a.prop IN [2,3,4]) AND (a.prop IN [2,3,4] OR a.prop IN [3,4,5]) RETURN *",
      "MATCH (a) WHERE a.prop IN [2,3,4] RETURN *")
  }

  test("MATCH (a) WHERE a.prop IN [1,2,3] OR a.foo IN ['foo', 'bar'] OR a.prop IN [2,3,4] OR a.foo IN ['bar'] RETURN *") {
    shouldRewrite(
      "MATCH (a) WHERE a.prop IN [1,2,3] OR a.foo IN ['foo','bar'] OR a.prop IN [2,3,4] OR a.foo IN ['bar'] RETURN *",
      "MATCH (a) WHERE a.prop IN [1,2,3,4] OR a.foo IN ['foo','bar'] RETURN *")
  }

  test("MATCH (n) RETURN n.prop IN [1,2,3] AND n.prop IN [3,4,5]") {
    shouldRewrite("MATCH (n) RETURN n.prop IN [1,2,3] AND n.prop IN [3,4,5] AS FOO",
                  "MATCH (n) RETURN n.prop IN [3] AS FOO")
  }

  private def shouldRewrite(from: String, to: String) {
    val original = parser.parse(from).asInstanceOf[Query]
    val expected = parser.parse(to).asInstanceOf[Query]
    val common = CNFNormalizer.instance(TestContext())
    val result = mergeInPredicates(original)

    common(result) should equal(common(expected))
  }
}
