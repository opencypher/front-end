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
import org.opencypher.v9_0.ast.AscSortItem
import org.opencypher.v9_0.ast.OrderBy
import org.opencypher.v9_0.ast.Query
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.ShowProceduresClause
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.Variable

/* Tests for listing procedures */
class ShowProceduresCommandParserTest extends AdministrationAndSchemaCommandParserTestBase {

  Seq("PROCEDURE", "PROCEDURES").foreach { procKeyword =>

    test(s"SHOW $procKeyword") {
      assertAst(query(ast.ShowProceduresClause(None, None, hasYield = false)(defaultPos)))
    }

    test(s"SHOW $procKeyword EXECUTABLE") {
      assertAst(query(ast.ShowProceduresClause(Some(ast.CurrentUser), None, hasYield = false)(defaultPos)))
    }

    test(s"SHOW $procKeyword EXECUTABLE BY CURRENT USER") {
      assertAst(query(ast.ShowProceduresClause(Some(ast.CurrentUser), None, hasYield = false)(defaultPos)))
    }

    test(s"SHOW $procKeyword EXECUTABLE BY user") {
      assertAst(query(ast.ShowProceduresClause(Some(ast.User("user")), None, hasYield = false)(defaultPos)))
    }

    test(s"SHOW $procKeyword EXECUTABLE BY CURRENT") {
      assertAst(query(ast.ShowProceduresClause(Some(ast.User("CURRENT")), None, hasYield = false)(defaultPos)))
    }

    test(s"USE db SHOW $procKeyword") {
      assertAst(Query(None, SingleQuery(
        List(use(Variable("db")(1, 5, 4)), ShowProceduresClause(None, None, hasYield = false)(1, 8, 7)))(1, 8, 7))(1, 8, 7))
    }

  }

  // Filtering tests

  test("SHOW PROCEDURE WHERE name = 'my.proc'") {
    assertAst(query(ShowProceduresClause(None,
      Some(Where(
        Equals(
          Variable("name")(1, 22, 21),
          StringLiteral("my.proc")(1, 29, 28)
        )(1, 27, 26))
        (1, 16, 15)
      ), hasYield = false)(defaultPos)))
  }

  test("SHOW PROCEDURES YIELD description") {
    assertAst(query(ShowProceduresClause(None, None, hasYield = true)(defaultPos),
      yieldClause(
        ReturnItems(includeExisting = false, Seq(variableReturnItem("description", (1, 23, 22))))(1, 23, 22)
      )))
  }

  test("SHOW PROCEDURES EXECUTABLE BY user YIELD *") {
    assertAst(query(ast.ShowProceduresClause(Some(ast.User("user")), None, hasYield = true)(defaultPos), yieldClause(returnAllItems)))
  }

  test("SHOW PROCEDURES YIELD * ORDER BY name SKIP 2 LIMIT 5") {
    assertAst(query(ShowProceduresClause(None, None, hasYield = true)(defaultPos),
      yieldClause(returnAllItems((1, 25, 24)),
        Some(OrderBy(Seq(
            AscSortItem(Variable("name")(1, 34, 33))((1, 34, 33)))
        )(1, 25, 24)),
        Some(skip(2, (1, 39, 38))), Some(limit(5, (1, 46, 45))))
    ))
  }

  test("USE db SHOW PROCEDURES YIELD name, description AS pp WHERE pp < 50.0 RETURN name") {
    assertAst(query(
      use(varFor("db")),
      ast.ShowProceduresClause(None, None, hasYield = true)(pos),
      yieldClause(returnItems(variableReturnItem("name"), aliasedReturnItem("description", "pp")),
        where = Some(where(lessThan(varFor("pp"), literalFloat(50.0))))),
      return_(variableReturnItem("name"))
    ), comparePosition = false)
  }

  test("USE db SHOW PROCEDURES EXECUTABLE YIELD name, description AS pp ORDER BY pp SKIP 2 LIMIT 5 WHERE pp < 50.0 RETURN name") {
    assertAst(query(
      use(varFor("db")),
      ast.ShowProceduresClause(Some(ast.CurrentUser), None, hasYield = true)(pos),
      yieldClause(returnItems(variableReturnItem("name"), aliasedReturnItem("description", "pp")),
        Some(orderBy(sortItem(varFor("pp")))),
        Some(skip(2)),
        Some(limit(5)),
        Some(where(lessThan(varFor("pp"), literalFloat(50.0))))),
      return_(variableReturnItem("name"))
    ), comparePosition = false)
  }

  test("SHOW PROCEDURES YIELD name AS PROCEDURE, mode AS OUTPUT") {
    assertAst(query(ast.ShowProceduresClause(None, None, hasYield = true)(pos),
      yieldClause(returnItems(aliasedReturnItem("name", "PROCEDURE"), aliasedReturnItem("mode", "OUTPUT")))),
      comparePosition = false)
  }

  // Negative tests

  test("SHOW PROCEDURES YIELD (123 + xyz)") {
    failsToParse
  }

  test("SHOW PROCEDURES YIELD (123 + xyz) AS foo") {
    failsToParse
  }

  test("SHOW PROCEDURES YIELD") {
    failsToParse
  }

  test("SHOW PROCEDURES YIELD * YIELD *") {
    failsToParse
  }

  test("SHOW PROCEDURES WHERE name = 'my.proc' YIELD *") {
    failsToParse
  }

  test("SHOW PROCEDURES WHERE name = 'my.proc' RETURN *") {
    failsToParse
  }

  test("SHOW PROCEDURES YIELD a b RETURN *") {
    failsToParse
  }

  test("SHOW PROCEDURES RETURN *") {
    failsToParse
  }

  test("SHOW EXECUTABLE PROCEDURE") {
    assertFailsWithMessage(testName,
      """Invalid input 'EXECUTABLE': expected
        |  "ALL"
        |  "BTREE"
        |  "BUILT"
        |  "CONSTRAINT"
        |  "CONSTRAINTS"
        |  "CURRENT"
        |  "DATABASE"
        |  "DATABASES"
        |  "DEFAULT"
        |  "EXIST"
        |  "EXISTENCE"
        |  "EXISTS"
        |  "FULLTEXT"
        |  "FUNCTION"
        |  "FUNCTIONS"
        |  "HOME"
        |  "INDEX"
        |  "INDEXES"
        |  "LOOKUP"
        |  "NODE"
        |  "POINT"
        |  "POPULATED"
        |  "PRIVILEGE"
        |  "PRIVILEGES"
        |  "PROCEDURE"
        |  "PROCEDURES"
        |  "PROPERTY"
        |  "RANGE"
        |  "REL"
        |  "RELATIONSHIP"
        |  "ROLE"
        |  "ROLES"
        |  "TEXT"
        |  "TRANSACTION"
        |  "TRANSACTIONS"
        |  "UNIQUE"
        |  "USER"
        |  "USERS" (line 1, column 6 (offset: 5))""".stripMargin)
  }

  test("SHOW PROCEDURE EXECUTABLE user") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE CURRENT USER") {
    failsToParse
  }

  test("SHOW PROCEDURE EXEC") {
    assertFailsWithMessage(testName, """Invalid input 'EXEC': expected "EXECUTABLE", "WHERE", "YIELD" or <EOF> (line 1, column 16 (offset: 15))""")
  }

  test("SHOW PROCEDURE EXECUTABLE BY") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE BY user1, user2") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE BY CURRENT USER user") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE BY CURRENT USER, user") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE BY user CURRENT USER") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE BY user, CURRENT USER") {
    failsToParse
  }

  test("SHOW PROCEDURE CURRENT USER") {
    failsToParse
  }

  test("SHOW PROCEDURE user") {
    failsToParse
  }

  test("SHOW CURRENT USER PROCEDURE") {
    failsToParse
  }

  test("SHOW user PROCEDURE") {
    assertFailsWithMessage(testName, """Invalid input '': expected ",", "PRIVILEGE" or "PRIVILEGES" (line 1, column 20 (offset: 19))""")
  }

  test("SHOW USER user PROCEDURE") {
    assertFailsWithMessage(testName, """Invalid input 'PROCEDURE': expected ",", "PRIVILEGE" or "PRIVILEGES" (line 1, column 16 (offset: 15))""")
  }

  test("SHOW PROCEDURE EXECUTABLE BY USER user") {
    failsToParse
  }

  test("SHOW PROCEDURE EXECUTABLE USER user") {
    failsToParse
  }

  test("SHOW PROCEDURE USER user") {
    failsToParse
  }

  // Invalid clause order

  for (prefix <- Seq("USE neo4j", "")) {
    test(s"$prefix SHOW PROCEDURES YIELD * WITH * MATCH (n) RETURN n") {
      // Can't parse WITH after SHOW
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix UNWIND range(1,10) as b SHOW PROCEDURES YIELD * RETURN *") {
      // Can't parse SHOW  after UNWIND
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW PROCEDURES WITH name, type RETURN *") {
      // Can't parse WITH after SHOW
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix WITH 'n' as n SHOW PROCEDURES YIELD name RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW PROCEDURES RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'RETURN': expected")
    }

    test(s"$prefix SHOW PROCEDURES WITH 1 as c RETURN name as numIndexes") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW PROCEDURES WITH 1 as c") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW PROCEDURES YIELD a WITH a RETURN a") {
      assertFailsWithMessageStart(testName, "Invalid input 'WITH': expected")
    }

    test(s"$prefix SHOW PROCEDURES YIELD as UNWIND as as a RETURN a") {
      assertFailsWithMessageStart(testName, "Invalid input 'UNWIND': expected")
    }

    test(s"$prefix SHOW PROCEDURES YIELD name SHOW PROCEDURES YIELD name2 RETURN name2") {
      assertFailsWithMessageStart(testName, "Invalid input 'SHOW': expected")
    }

    test(s"$prefix SHOW PROCEDURES RETURN name2 YIELD name2") {
      assertFailsWithMessageStart(testName, "Invalid input 'RETURN': expected")
    }
  }

  // Brief/verbose not allowed

  test("SHOW PROCEDURE BRIEF") {
    failsToParse
  }

  test("SHOW PROCEDURE BRIEF OUTPUT") {
    failsToParse
  }

  test("SHOW PROCEDURES BRIEF YIELD *") {
    failsToParse
  }

  test("SHOW PROCEDURES BRIEF RETURN *") {
    failsToParse
  }

  test("SHOW PROCEDURES BRIEF WHERE name = 'my.proc'") {
    failsToParse
  }

  test("SHOW PROCEDURE VERBOSE") {
    failsToParse
  }

  test("SHOW PROCEDURE VERBOSE OUTPUT") {
    failsToParse
  }

  test("SHOW PROCEDURES VERBOSE YIELD *") {
    failsToParse
  }

  test("SHOW PROCEDURES VERBOSE RETURN *") {
    failsToParse
  }

  test("SHOW PROCEDURES VERBOSE WHERE name = 'my.proc'") {
    failsToParse
  }

  test("SHOW PROCEDURE OUTPUT") {
    failsToParse
  }

}
