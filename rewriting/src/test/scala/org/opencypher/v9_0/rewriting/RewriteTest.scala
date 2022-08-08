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

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.factory.neo4j.JavaCCParser
import org.opencypher.v9_0.ast.prettifier.ExpressionStringifier
import org.opencypher.v9_0.ast.prettifier.Prettifier
import org.opencypher.v9_0.ast.semantics.SemanticChecker
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

trait RewriteTest {
  self: CypherFunSuite =>

  def rewriterUnderTest: Rewriter

  val prettifier = Prettifier(ExpressionStringifier(_.asCanonicalStringVal))

  protected def assertRewrite(originalQuery: String, expectedQuery: String): Unit = {
    val (expected, result) = getRewrite(originalQuery, expectedQuery)
    assert(
      result === expected,
      s"\n$originalQuery\nshould be rewritten to:\n$expectedQuery\nbut was rewritten to:\n${prettifier.asString(result.asInstanceOf[Statement])}"
    )
  }

  protected def getRewrite(originalQuery: String, expectedQuery: String): (Statement, AnyRef) = {
    val original = parseForRewriting(originalQuery)
    val expected = parseForRewriting(expectedQuery)
    SemanticChecker.check(original)
    val result = rewrite(original)
    (expected, result)
  }

  protected def parseForRewriting(queryText: String): Statement = {
    val preparedQuery = queryText.replace("\r\n", "\n")
    JavaCCParser.parse(preparedQuery, OpenCypherExceptionFactory(None))
  }

  protected def rewrite(original: Statement): AnyRef =
    original.rewrite(rewriterUnderTest)

  protected def endoRewrite(original: Statement): Statement =
    original.endoRewrite(rewriterUnderTest)

  protected def assertIsNotRewritten(query: String): Unit = {
    val original = JavaCCParser.parse(query, OpenCypherExceptionFactory(None))
    val result = original.rewrite(rewriterUnderTest)
    assert(
      result === original,
      s"\n$query\nshould not have been rewritten but was to:\n${prettifier.asString(result.asInstanceOf[Statement])}"
    )
  }
}
