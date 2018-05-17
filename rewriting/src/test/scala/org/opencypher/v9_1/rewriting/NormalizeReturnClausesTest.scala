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
import org.opencypher.v9_1.rewriting.rewriters.normalizeReturnClauses
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.{Rewriter, SyntaxException}
import org.opencypher.v9_1.ast.semantics.SyntaxExceptionCreator

class NormalizeReturnClausesTest extends CypherFunSuite with RewriteTest with AstConstructionTestSupport {
  val mkException = new SyntaxExceptionCreator("<Query>", Some(pos))
  val rewriterUnderTest: Rewriter = normalizeReturnClauses(mkException)

  test("alias RETURN clause items") {
    assertRewrite(
      """MATCH (n)
        |RETURN n, n.foo AS foo, n.bar
      """.stripMargin,
      """MATCH (n)
        |RETURN n AS `n`, n.foo AS foo, n.bar AS `n.bar`
      """.stripMargin)
  }

  test("introduce WITH clause for ORDER BY") {
    assertRewrite(
      """MATCH (n)
        |RETURN n.foo AS foo, n.bar ORDER BY foo SKIP 2 LIMIT 5""".stripMargin,
      """MATCH (n)
        |WITH n.foo AS `  FRESHID19`, n.bar AS `  FRESHID33` ORDER BY `  FRESHID19` SKIP 2 LIMIT 5
        |RETURN `  FRESHID19` AS foo, `  FRESHID33` AS `n.bar`""".stripMargin)
  }

  test("introduce WITH clause for ORDER BY where returning all IDs") {
    assertRewrite(
      """MATCH (n)
        |RETURN * ORDER BY n.foo SKIP 2 LIMIT 5""".stripMargin,
      """MATCH (n)
        |WITH * ORDER BY n.foo SKIP 2 LIMIT 5
        |RETURN *""".stripMargin)
  }

  test("match (n) return n, count(*) as c order by c") {
    assertRewrite(
      "match (n) return n, count(*) as c order by c",
      """match (n)
        |with n as `  FRESHID17`, count(*) as `  FRESHID20` order by `  FRESHID20`
        |return `  FRESHID17` as n, `  FRESHID20` as c""".stripMargin)
  }

  test("match (n),(m) return n as m, m as m2 order by m") {
    assertRewrite(
      "match (n),(m) return n as m, m as m2 order by m",
      """match (n), (m)
        |with n as `  FRESHID21`, m as `  FRESHID29` order by `  FRESHID21`
        |return `  FRESHID21` as m, `  FRESHID29` as m2""".stripMargin)
  }

  test("match (n),(m) return m as m2, n as m order by m") {
    assertRewrite(
      "match (n),(m) return m as m2, n as m order by m",
      """match (n), (m)
        |with m as `  FRESHID21`, n as `  FRESHID30` order by `  FRESHID30`
        |return `  FRESHID21` as m2, `  FRESHID30` as m""".stripMargin)
  }

  test("rejects use of aggregation in ORDER BY if aggregation is not used in associated RETURN") {
    // Note: aggregations in ORDER BY that don't also appear in WITH are invalid
    try {
      rewrite(parseForRewriting(
        """MATCH (n)
          |RETURN n.prop AS prop ORDER BY max(n.foo)
        """.stripMargin))
      fail("We shouldn't get here")
    } catch {
      case (e: SyntaxException) =>
        e.getMessage should equal("Cannot use aggregation in ORDER BY if there are no aggregate expressions in the preceding RETURN (line 2, column 1 (offset: 10))")
    }
  }

  test("accepts use of aggregation in ORDER BY if aggregation is used in associated RETURN") {
    assertRewrite(
      """MATCH (n)
        |RETURN n.prop AS prop, max(n.foo) AS m ORDER BY max(n.foo)
      """.stripMargin,
      """MATCH (n)
        |WITH n.prop AS `  FRESHID19`, max(n.foo) AS `  FRESHID33` ORDER BY `  FRESHID33`
        |RETURN  `  FRESHID19` AS prop,  `  FRESHID33` AS m
      """.stripMargin
    )
  }

  test("should replace the aggregation function in the order by") {
    assertRewrite(
      """MATCH (n)
        |RETURN n as n, count(n) as count ORDER BY count(n)""".stripMargin,
      """MATCH (n)
        |WITH n AS `  FRESHID17`, count(n) AS `  FRESHID25` ORDER BY `  FRESHID25`
        |RETURN `  FRESHID17` AS n, `  FRESHID25` as count""".stripMargin)
  }

  protected override def assertRewrite(originalQuery: String, expectedQuery: String) {
    val original = parseForRewriting(originalQuery)
    val expected = parseForRewriting(expectedQuery)
    val result = endoRewrite(original)
    assert(result === expected, "\n" + originalQuery)
  }

  protected def rewriting(queryText: String): Unit = {
    endoRewrite(parseForRewriting(queryText))
  }
}
