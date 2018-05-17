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

import org.opencypher.v9_1.parser.ParserFixture.parser
import org.opencypher.v9_1.util.Rewriter
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite
import org.opencypher.v9_1.ast.Statement
import org.opencypher.v9_1.ast.semantics.SemanticChecker

trait RewriteTest {
  self: CypherFunSuite =>

  def rewriterUnderTest: Rewriter

  protected def assertRewrite(originalQuery: String, expectedQuery: String) {
    val original = parseForRewriting(originalQuery)
    val expected = parseForRewriting(expectedQuery)
    SemanticChecker.check(original)
    val result = rewrite(original)
    assert(result === expected, "\n" + originalQuery)
  }

  protected def parseForRewriting(queryText: String): Statement = parser.parse(queryText.replace("\r\n", "\n"))

  protected def rewrite(original: Statement): AnyRef =
    original.rewrite(rewriterUnderTest)

  protected def endoRewrite(original: Statement): Statement =
    original.endoRewrite(rewriterUnderTest)

  protected def assertIsNotRewritten(query: String) {
    val original = parser.parse(query)
    val result = original.rewrite(rewriterUnderTest)
    assert(result === original, "\n" + query)
  }
}
