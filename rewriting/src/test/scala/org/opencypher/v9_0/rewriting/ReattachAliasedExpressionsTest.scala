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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.rewriting.rewriters.reattachAliasedExpressions
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class ReattachAliasedExpressionsTest extends CypherFunSuite with RewriteTest {

  override def rewriterUnderTest: Rewriter = reattachAliasedExpressions

  test("MATCH (a) RETURN a.x AS newAlias ORDER BY newAlias") {
    assertRewrite(
      "MATCH (a) RETURN a.x AS newAlias ORDER BY newAlias",
      "MATCH (a) RETURN a.x AS newAlias ORDER BY a.x"
    )
  }

  test("MATCH (a) RETURN count(*) AS foo ORDER BY foo") {
    assertRewrite(
      "MATCH (a) RETURN count(*) AS foo ORDER BY foo",
      "MATCH (a) RETURN count(*) AS foo ORDER BY count(*)"
    )
  }

  test("MATCH (a) RETURN collect(a) AS foo ORDER BY size(foo)") {
    assertRewrite(
      "MATCH (a) RETURN collect(a) AS foo ORDER BY size(foo)",
      "MATCH (a) RETURN collect(a) AS foo ORDER BY size(collect(a))"
    )
  }

  test("MATCH (x) WITH x AS x RETURN count(x) AS foo ORDER BY foo") {
    assertRewrite(
      "MATCH (x) WITH x AS x RETURN count(x) AS foo ORDER BY foo",
      "MATCH (x) WITH x AS x RETURN count(x) AS foo ORDER BY count(x)"
    )
  }

  test("MATCH (a) WITH a.x AS newAlias ORDER BY newAlias RETURN *") {
    assertRewrite(
      "MATCH (a) WITH a.x AS newAlias ORDER BY newAlias RETURN *",
      "MATCH (a) WITH a.x AS newAlias ORDER BY a.x RETURN *"
    )
  }

  test("MATCH (a) WITH count(*) AS foo ORDER BY foo RETURN *") {
    assertRewrite(
      "MATCH (a) WITH count(*) AS foo ORDER BY foo RETURN *",
      "MATCH (a) WITH count(*) AS foo ORDER BY count(*) RETURN *"
    )
  }

  test("MATCH (x) WITH x AS x WITH count(x) AS foo ORDER BY foo RETURN *") {
    assertRewrite(
      "MATCH (x) WITH x AS x WITH count(x) AS foo ORDER BY foo RETURN *",
      "MATCH (x) WITH x AS x WITH count(x) AS foo ORDER BY count(x) RETURN *"
    )
  }

  test("MATCH (x) WITH x.prop as prop WHERE prop = 42 RETURN prop *") {
    assertIsNotRewritten( // The legacy planner does not want this to be done for WHERE clauses... *sigh*
      "MATCH (x) WITH x.prop as prop WHERE prop = 42 RETURN prop")
  }
}
