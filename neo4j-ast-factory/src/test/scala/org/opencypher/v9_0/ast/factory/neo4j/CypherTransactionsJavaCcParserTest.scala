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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class CypherTransactionsJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName with AstConstructionTestSupport {

  test("CALL { CREATE (n) } IN TRANSACTIONS") {
    val expected = query(subqueryCallInTransactions(create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF 1 ROW") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(literalInt(1))), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF 1 ROWS") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(literalInt(1))), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF 42 ROW") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(literalInt(42))), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF 42 ROWS") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(literalInt(42))), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF $param ROWS") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(parameter("param", CTAny))), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }

  test("CALL { CREATE (n) } IN TRANSACTIONS OF NULL ROWS") {
    val expected = query(subqueryCallInTransactions(inTransactionsParameters(Some(nullLiteral)), create(nodePat("n"))))
    assertJavaCCAST(testName, expected)
  }
}
