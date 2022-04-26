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

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.symbols.CTAny

/* Tests for listing transactions */
class ShowTransactionsCommandParserTest extends AdministrationAndSchemaCommandParserTestBase {

  Seq("TRANSACTION", "TRANSACTIONS").foreach { transactionKeyword =>
    test(s"SHOW $transactionKeyword") {
      assertAst(query(ast.ShowTransactionsClause(Left(List.empty), None, hasYield = false)(defaultPos)))
    }

    test(s"SHOW $transactionKeyword 'db1-transaction-123'") {
      assertAst(
        query(ast.ShowTransactionsClause(Left(List("db1-transaction-123")), None, hasYield = false)(defaultPos))
      )
    }

    test(s"""SHOW $transactionKeyword "db1-transaction-123"""") {
      assertAst(
        query(ast.ShowTransactionsClause(Left(List("db1-transaction-123")), None, hasYield = false)(defaultPos))
      )
    }

    test(s"SHOW $transactionKeyword 'my.db-transaction-123'") {
      assertAst(
        query(ast.ShowTransactionsClause(Left(List("my.db-transaction-123")), None, hasYield = false)(defaultPos))
      )
    }

    test(s"SHOW $transactionKeyword $$param") {
      assertAst(query(ast.ShowTransactionsClause(
        Right(Parameter("param", CTAny)(1, 7 + transactionKeyword.length, 6 + transactionKeyword.length)),
        None,
        hasYield = false
      )(defaultPos)))
    }

    test(s"""SHOW $transactionKeyword 'db1 - transaction - 123', "db2-transaction-45a6"""") {
      assertAst(query(ast.ShowTransactionsClause(
        Left(List("db1 - transaction - 123", "db2-transaction-45a6")),
        None,
        hasYield = false
      )(defaultPos)))
    }

    test(s"SHOW $transactionKeyword 'yield-transaction-123'") {
      assertAst(
        query(ast.ShowTransactionsClause(Left(List("yield-transaction-123")), None, hasYield = false)(defaultPos))
      )
    }

    test(s"SHOW $transactionKeyword 'where-transaction-123'") {
      assertAst(
        query(ast.ShowTransactionsClause(Left(List("where-transaction-123")), None, hasYield = false)(defaultPos))
      )
    }

    test(s"USE db SHOW $transactionKeyword") {
      assertAst(
        query(use(varFor("db")), ast.ShowTransactionsClause(Left(List.empty), None, hasYield = false)(pos)),
        comparePosition = false
      )
    }

  }

  // Filtering tests

  test("SHOW TRANSACTION WHERE transactionId = 'db1-transaction-123'") {
    assertAst(query(
      ast.ShowTransactionsClause(
        Left(List.empty),
        Some(ast.Where(
          Equals(Variable("transactionId")(1, 24, 23), StringLiteral("db1-transaction-123")(1, 40, 39))(1, 38, 37)
        )(1, 18, 17)),
        hasYield = false
      )(defaultPos)
    ))
  }

  test("SHOW TRANSACTIONS YIELD database") {
    assertAst(
      query(
        ast.ShowTransactionsClause(Left(List.empty), None, hasYield = true)(pos),
        yieldClause(returnItems(variableReturnItem("database")))
      ),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTIONS 'db1-transaction-123', 'db2-transaction-456' YIELD *") {
    assertAst(query(
      ast.ShowTransactionsClause(Left(List("db1-transaction-123", "db2-transaction-456")), None, hasYield = true)(
        defaultPos
      ),
      yieldClause(returnAllItems)
    ))
  }

  test("SHOW TRANSACTIONS 'db1-transaction-123', 'db2-transaction-456', 'yield' YIELD *") {
    assertAst(
      query(
        ast.ShowTransactionsClause(
          Left(List("db1-transaction-123", "db2-transaction-456", "yield")),
          None,
          hasYield = true
        )(pos),
        yieldClause(returnAllItems)
      ),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTIONS YIELD * ORDER BY transactionId SKIP 2 LIMIT 5") {
    assertAst(
      query(
        ast.ShowTransactionsClause(Left(List.empty), None, hasYield = true)(pos),
        yieldClause(returnAllItems, Some(orderBy(sortItem(varFor("transactionId")))), Some(skip(2)), Some(limit(5)))
      ),
      comparePosition = false
    )
  }

  test("USE db SHOW TRANSACTIONS YIELD transactionId, activeLockCount AS pp WHERE pp < 50 RETURN transactionId") {
    assertAst(
      query(
        use(varFor("db")),
        ast.ShowTransactionsClause(Left(List.empty), None, hasYield = true)(pos),
        yieldClause(
          returnItems(variableReturnItem("transactionId"), aliasedReturnItem("activeLockCount", "pp")),
          where = Some(where(lessThan(varFor("pp"), literalInt(50L))))
        ),
        return_(variableReturnItem("transactionId"))
      ),
      comparePosition = false
    )
  }

  test(
    "USE db SHOW TRANSACTIONS YIELD transactionId, activeLockCount AS pp ORDER BY pp SKIP 2 LIMIT 5 WHERE pp < 50 RETURN transactionId"
  ) {
    assertAst(
      query(
        use(varFor("db")),
        ast.ShowTransactionsClause(Left(List.empty), None, hasYield = true)(pos),
        yieldClause(
          returnItems(variableReturnItem("transactionId"), aliasedReturnItem("activeLockCount", "pp")),
          Some(orderBy(sortItem(varFor("pp")))),
          Some(skip(2)),
          Some(limit(5)),
          Some(where(lessThan(varFor("pp"), literalInt(50L))))
        ),
        return_(variableReturnItem("transactionId"))
      ),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTIONS 'db1-transaction-123' YIELD transactionId AS TRANSACTION, database AS OUTPUT") {
    assertAst(
      query(
        ast.ShowTransactionsClause(Left(List("db1-transaction-123")), None, hasYield = true)(pos),
        yieldClause(returnItems(
          aliasedReturnItem("transactionId", "TRANSACTION"),
          aliasedReturnItem("database", "OUTPUT")
        ))
      ),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTIONS 'where' YIELD transactionId AS TRANSACTION, database AS OUTPUT") {
    assertAst(
      query(
        ast.ShowTransactionsClause(Left(List("where")), None, hasYield = true)(pos),
        yieldClause(returnItems(
          aliasedReturnItem("transactionId", "TRANSACTION"),
          aliasedReturnItem("database", "OUTPUT")
        ))
      ),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTION 'db1-transaction-123' WHERE transactionId = 'db1-transaction-124'") {
    assertAst(
      query(ast.ShowTransactionsClause(
        Left(List("db1-transaction-123")),
        Some(where(equals(varFor("transactionId"), literalString("db1-transaction-124")))),
        hasYield = false
      )(pos)),
      comparePosition = false
    )
  }

  test("SHOW TRANSACTION 'yield' WHERE transactionId = 'where'") {
    assertAst(
      query(ast.ShowTransactionsClause(
        Left(List("yield")),
        Some(where(equals(varFor("transactionId"), literalString("where")))),
        hasYield = false
      )(pos)),
      comparePosition = false
    )
  }

  test(
    "SHOW TRANSACTION 'db1-transaction-123', 'db1-transaction-124' WHERE transactionId IN ['db1-transaction-124', 'db1-transaction-125']"
  ) {
    assertAst(
      query(ast.ShowTransactionsClause(
        Left(List("db1-transaction-123", "db1-transaction-124")),
        Some(where(in(varFor("transactionId"), listOfString("db1-transaction-124", "db1-transaction-125")))),
        hasYield = false
      )(pos)),
      comparePosition = false
    )
  }

  // Negative tests

  test("SHOW TRANSACTION db-transaction-123") {
    failsToParse
  }

  test("SHOW TRANSACTIONS 'db-transaction-123', $param") {
    failsToParse
  }

  test("SHOW TRANSACTIONS $param, 'db-transaction-123'") {
    failsToParse
  }

  test("SHOW TRANSACTIONS $param, $param2") {
    failsToParse
  }

  test("SHOW TRANSACTIONS ['db1-transaction-123', 'db2-transaction-456']") {
    failsToParse
  }

  test("SHOW TRANSACTION foo") {
    failsToParse
  }

  test("SHOW TRANSACTION x+2") {
    failsToParse
  }

  test("SHOW TRANSACTIONS YIELD") {
    failsToParse
  }

  test("SHOW TRANSACTIONS YIELD * YIELD *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS YIELD (123 + xyz)") {
    failsToParse
  }

  test("SHOW TRANSACTIONS YIELD (123 + xyz) AS foo") {
    failsToParse
  }

  test("SHOW TRANSACTIONS WHERE transactionId = 'db1-transaction-123' YIELD *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS WHERE transactionId = 'db1-transaction-123' RETURN *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS YIELD a b RETURN *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS RETURN *") {
    failsToParse
  }

  test("SHOW CURRENT USER TRANSACTION") {
    failsToParse
  }

  test("SHOW USER user TRANSACTION") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'TRANSACTION': expected ",", "PRIVILEGE" or "PRIVILEGES" (line 1, column 16 (offset: 15))""".stripMargin
    )
  }

  test("SHOW TRANSACTION EXECUTED BY USER user") {
    failsToParse
  }

  test("SHOW ALL TRANSACTIONS") {
    failsToParse
  }

  test("SHOW TRANSACTIONS ALL") {
    failsToParse
  }

  // Invalid clause order

  for (prefix <- Seq("USE neo4j", "")) {
    test(s"$prefix SHOW TRANSACTIONS YIELD * WITH * MATCH (n) RETURN n") {
      // Can't parse WITH after SHOW
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix UNWIND range(1,10) as b SHOW TRANSACTIONS YIELD * RETURN *") {
      // Can't parse SHOW  after UNWIND
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS WITH name, type RETURN *") {
      // Can't parse WITH after SHOW
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix WITH 'n' as n SHOW TRANSACTIONS YIELD name RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'RETURN': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS WITH 1 as c RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS WITH 1 as c") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS YIELD a WITH a RETURN a") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS YIELD as UNWIND as as a RETURN a") {
      assertFailsWithMessageStart(testName, "Invalid input 'UNWIND': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS YIELD id SHOW TRANSACTIONS YIELD id2 RETURN id2") {
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW TRANSACTIONS RETURN id2 YIELD id2") {
      assertFailsWithMessageStart(testName, "Invalid input 'RETURN': expected")
    }
  }

  // Brief/verbose not allowed

  test("SHOW TRANSACTION BRIEF") {
    failsToParse
  }

  test("SHOW TRANSACTIONS BRIEF YIELD *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS BRIEF WHERE transactionId = 'db1-transaction-123'") {
    failsToParse
  }

  test("SHOW TRANSACTION VERBOSE") {
    failsToParse
  }

  test("SHOW TRANSACTIONS VERBOSE YIELD *") {
    failsToParse
  }

  test("SHOW TRANSACTIONS VERBOSE WHERE transactionId = 'db1-transaction-123'") {
    failsToParse
  }

  test("SHOW TRANSACTION OUTPUT") {
    failsToParse
  }

  test("SHOW TRANSACTION BRIEF OUTPUT") {
    failsToParse
  }

  test("SHOW TRANSACTIONS BRIEF RETURN *") {
    failsToParse
  }

  test("SHOW TRANSACTION VERBOSE OUTPUT") {
    failsToParse
  }

  test("SHOW TRANSACTIONS VERBOSE RETURN *") {
    failsToParse
  }

}
