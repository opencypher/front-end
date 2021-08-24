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

import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.expressions.StartsWith
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.rewriting.rewriters.expandShowWhere
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class ExpandShowWhereTest extends CypherFunSuite with RewriteTest {
  val rewriterUnderTest: Rewriter = expandShowWhere

  test("SHOW DATABASES") {
    val originalQuery = "SHOW DATABASES WHERE name STARTS WITH 's'"
    val original = parseForRewriting(originalQuery)
    val result = rewrite(original)

    result match {
      // Rewrite to approximately `SHOW DATABASES YIELD * WHERE name STARTS WITH 's'` but because we didn't have a YIELD * in the original
      // query the columns are brief and not verbose so it's not exactly the same
      case ShowDatabase(_, Some(Left((Yield(ReturnItems(returnStar, _, _ ), None, None, None, Some(Where(StartsWith(Variable("name"),StringLiteral("s"))))), None))), _)  if returnStar => ()
      case _ => fail(s"\n$originalQuery\nshould be rewritten to:\nSHOW DATABASES YIELD * WHERE name STARTS WITH 's'\nbut was rewritten to:\n${prettifier.asString(result.asInstanceOf[Statement])}")
    }
  }

  test("SHOW ROLES") {
    assertRewrite(
      "SHOW ROLES WHERE name STARTS WITH 's'",
      "SHOW ROLES YIELD * WHERE name STARTS WITH 's'"
    )
  }

  test("SHOW PRIVILEGES") {
    assertRewrite(
      "SHOW PRIVILEGES WHERE scope STARTS WITH 's'",
      "SHOW PRIVILEGES YIELD * WHERE scope STARTS WITH 's'"
    )
  }

  test("SHOW PRIVILEGES AS COMMANDS") {
    assertRewrite(
      "SHOW PRIVILEGES AS COMMANDS WHERE command CONTAINS 'MATCH'",
      "SHOW PRIVILEGES AS COMMANDS YIELD * WHERE command CONTAINS 'MATCH'"
    )
  }

  test("SHOW USERS") {
    assertRewrite(
      "SHOW USERS WHERE name STARTS WITH 'g'",
      "SHOW USERS YIELD * WHERE name STARTS WITH 'g'"
    )
  }

  test("SHOW CURRENT USER") {
    assertRewrite(
      "SHOW CURRENT USER WHERE name STARTS WITH 'g'",
      "SHOW CURRENT USER YIELD * WHERE name STARTS WITH 'g'"
    )
  }
}
