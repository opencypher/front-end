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

import org.opencypher.v9_0.ast._
import org.opencypher.v9_0.ast.semantics.{SemanticFeature, SemanticState, SyntaxExceptionCreator}
import org.opencypher.v9_1.rewriting.rewriters.{expandStar, normalizeGraphReturnItems, normalizeReturnClauses, normalizeWithClauses}
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class ExpandStarTest extends CypherFunSuite with AstConstructionTestSupport {

  import org.opencypher.v9_1.parser.ParserFixture.parser

  test("rewrites * in return") {
    assertRewrite(
      "match (n) return *",
      "match (n) return n")

    assertRewrite(
      "match (n),(c) return *",
      "match (n),(c) return c,n")

    assertRewrite(
      "match (n)-->(c) return *",
      "match (n)-->(c) return c,n")

    assertRewrite(
      "match (n)-[r]->(c) return *",
      "match (n)-[r]->(c) return c,n,r")

    assertRewrite(
      "create (n) return *",
      "create (n) return n")

    assertRewrite(
      "match p = shortestPath((a)-[r*]->(x)) return *",
      "match p = shortestPath((a)-[r*]->(x)) return a,p,r,x")

    assertRewrite(
      "match p=(a:Start)-->(b) return *",
      "match p=(a:Start)-->(b) return a, b, p")
  }

  test("rewrites * in RETURN GRAPHS") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo, GRAPH AT 'url2' AS bar RETURN * GRAPHS *",
      "WITH 1 AS a GRAPH AT 'url' AS foo, GRAPH AT 'url2' AS bar RETURN a GRAPHS bar, foo"
    )

    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo, GRAPH AT 'url2' AS bar RETURN GRAPHS *",
      "WITH 1 AS a GRAPH AT 'url' AS foo, GRAPH AT 'url2' AS bar RETURN GRAPHS bar, foo"
    )
  }

  test("rewrites * in WITH GRAPHS") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH foo, GRAPH bar WITH * GRAPHS * RETURN 1",
      "WITH 1 AS a GRAPH foo, GRAPH bar WITH a GRAPHS bar, foo RETURN 1"
    )

    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH foo, GRAPH bar WITH GRAPHS * RETURN 1",
      "WITH 1 AS a GRAPH foo, GRAPH bar WITH GRAPHS bar, foo RETURN 1"
    )
  }

  test("Rewrites unaliased SOURCE GRAPH") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN * GRAPHS *, SOURCE GRAPH",
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN a AS a GRAPH bar AS bar, GRAPH foo AS foo"
    )
  }

  test("Does not rewrite aliased SOURCE GRAPH") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN * GRAPHS *, SOURCE GRAPH AS fizz",
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN a AS a GRAPH bar AS bar, GRAPH foo AS foo, SOURCE GRAPH AS fizz"
    )
  }

  test("Rewrites unaliased TARGET GRAPH") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN * GRAPHS *, TARGET GRAPH",
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN a AS a GRAPH bar AS bar, GRAPH foo AS foo"
    )
  }

  test("Does not rewrite aliased TARGET GRAPH") {
    assertMultipleGraphsRewrite(
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN * GRAPHS *, TARGET GRAPH AS fizz",
      "WITH 1 AS a GRAPH AT 'url' AS foo >> GRAPH AT 'url2' AS bar RETURN a AS a GRAPH bar AS bar, GRAPH foo AS foo, TARGET GRAPH AS fizz"
    )
  }

  test("expands correctly when no graphs are in scope") {
    // This invariant does not have a syntactical version, e.g. GRAPHS -
    // So we need to check the AST

    val prep = prepRewrite("WITH 1 AS a GRAPHS * RETURN 1 AS a")
    prep should equal(
      Query(None, SingleQuery(Seq(
        With(ReturnItems(includeExisting = false, Seq(AliasedReturnItem(literalInt(1), varFor("a"))(pos)))(pos), GraphReturnItems(includeExisting = true, Seq.empty)(pos))(pos),
        Return(ReturnItems(includeExisting = false, Seq(AliasedReturnItem(literalInt(1), varFor("a"))(pos)))(pos), None)(pos)
      ))(pos))(pos)
    )
    prep.rewrite(expandStar(prep.semanticCheck(SemanticState.clean.withFeatures(SemanticFeature.MultipleGraphs)).state)) should equal(
      Query(None, SingleQuery(Seq(
        With(ReturnItems(includeExisting = false, Seq(AliasedReturnItem(literalInt(1), varFor("a"))(pos)))(pos), GraphReturnItems(includeExisting = false, Seq.empty)(pos))(pos),
        Return(ReturnItems(includeExisting = false, Seq(AliasedReturnItem(literalInt(1), varFor("a"))(pos)))(pos), None)(pos)
      ))(pos))(pos)
    )
  }

  test("rewrites * in with") {
    assertRewrite(
      "match (n) with * return n",
      "match (n) with n return n")

    assertRewrite(
      "match (n),(c) with * return n",
      "match (n),(c) with c,n return n")

    assertRewrite(
      "match (n)-->(c) with * return n",
      "match (n)-->(c) with c,n return n")

    assertRewrite(
      "match (n)-[r]->(c) with * return n",
      "match (n)-[r]->(c) with c,n,r return n")

    assertRewrite(
      "match (n)-[r]->(c) with *, r.pi as x return n",
      "match (n)-[r]->(c) with c, n, r, r.pi as x return n")

    assertRewrite(
      "create (n) with * return n",
      "create (n) with n return n")

    assertRewrite(
      "match p = shortestPath((a)-[r*]->(x)) with * return p",
      "match p = shortestPath((a)-[r*]->(x)) with a,p,r,x return p")
  }

  test("symbol shadowing should be taken into account") {
    assertRewrite(
      "match a,x,y with a match (b) return *",
      "match a,x,y with a match (b) return a, b")
  }

  test("expands _PRAGMA WITHOUT") {
    assertRewrite(
      "MATCH a,x,y _PRAGMA WITHOUT a MATCH b RETURN *",
      "MATCH a,x,y WITH x, y MATCH b RETURN b, x, y")
  }

  test("keeps listed items during expand") {
    assertRewrite(
      "MATCH (n) WITH *, 1 AS b RETURN *",
      "MATCH (n) WITH n, 1 AS b RETURN b, n"
    )
  }

  private def assertRewrite(originalQuery: String, expectedQuery: String) {
    val original = prepRewrite(originalQuery)
    val expected = prepRewrite(expectedQuery)

    val checkResult = original.semanticCheck(SemanticState.clean)
    val rewriter = expandStar(checkResult.state)

    val result = original.rewrite(rewriter)
    assert(result === expected)
  }


  private def assertMultipleGraphsRewrite(originalQuery: String, expectedQuery: String) {
    val original = prepRewrite(originalQuery, multipleGraphs = true)
    val expected = prepRewrite(expectedQuery, multipleGraphs = true)

    val checkResult = original.semanticCheck(SemanticState.clean.withFeatures(SemanticFeature.MultipleGraphs))
    val rewriter = expandStar(checkResult.state)

    val result = original.rewrite(rewriter)
    assert(result === expected)
  }

  private def prepRewrite(q: String, multipleGraphs: Boolean = false) = {
    val mkException = new SyntaxExceptionCreator(q, Some(pos))
    val rewriter = if (multipleGraphs)
      inSequence(normalizeGraphReturnItems, normalizeReturnClauses(mkException), normalizeWithClauses(mkException))
    else
      inSequence(normalizeReturnClauses(mkException), normalizeWithClauses(mkException))
    parser.parse(q).endoRewrite(rewriter)
  }
}
