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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.rewriting.rewriters.normalizeExistsPatternExpressions
import org.opencypher.v9_0.rewriting.rewriters.simplifyPredicates
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class normalizeExistsPatternExpressionsTest extends CypherFunSuite with AstConstructionTestSupport {

  import org.opencypher.v9_0.parser.ParserFixture.parser

  testRewrite(
    "MATCH (n) WHERE (n)--(m) RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE (n)--(m) AND (n)--(p) RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) AND EXISTS((n)--(p)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT (n)--(m) AND (n)--(p) RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) AND EXISTS((n)--(p)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE size((n)--(m))>0 RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) RETURN n")
  testRewrite(
    "MATCH (n) WHERE 0<size((n)--(m)) RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT size((n)--(m))=0 RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT 0=size((n)--(m)) RETURN n",
    "MATCH (n) WHERE EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT (n)--(m) RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE size((n)--(m))=0 RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE 0=size((n)--(m)) RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT size((n)--(m))>0 RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) RETURN n")

  testRewrite(
    "MATCH (n) WHERE NOT 0<size((n)--(m)) RETURN n",
    "MATCH (n) WHERE NOT EXISTS((n)--(m)) RETURN n")

  testNoRewrite("RETURN (n)--(m)")

  testNoRewrite("RETURN [(n)--(m) | n.prop]")

  testNoRewrite("MATCH (n) WHERE 2 IN [(n)--(m) | n.prop] RETURN n")

  testNoRewrite("MATCH (n) WHERE size((n)--(m))>1 RETURN n")

  testNoRewrite("MATCH (n) WHERE NOT size((n)--(m))>1 RETURN n")

  private def testNoRewrite(query: String): Unit = {
    test(query + " is not rewritten") {
      assertRewrite(query, query)
    }
  }

  private def testRewrite(originalQuery: String, expectedQuery: String): Unit = {
    test(originalQuery + " is rewritten to " + expectedQuery) {
      assertRewrite(originalQuery, expectedQuery)
    }
  }

  private def assertRewrite(originalQuery: String, expectedQuery: String): Unit = {
    val original = parser.parse(originalQuery, OpenCypherExceptionFactory(None))
    val expected = parser.parse(expectedQuery, OpenCypherExceptionFactory(None))

    val checkResult = original.semanticCheck(SemanticState.clean)
    val rewriter = inSequence(normalizeExistsPatternExpressions(checkResult.state), simplifyPredicates)

    val result = original.rewrite(rewriter)
    assert(result === expected)
  }
}
