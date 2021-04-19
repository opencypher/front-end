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
import org.opencypher.v9_0.ast.CreateDatabase
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.IndefiniteWait
import org.opencypher.v9_0.ast.NoWait
import org.opencypher.v9_0.ast.OptionsMap
import org.opencypher.v9_0.ast.OptionsParam
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class MultiDatabaseAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName with AstConstructionTestSupport {
  // SHOW DATABASE

  Seq(
    "DATABASES",
    "DEFAULT DATABASE",
    "HOME DATABASE",
    "DATABASE $db",
    "DATABASE neo4j"
  ).foreach { dbType =>

    test(s"SHOW $dbType") {
      assertSameAST(testName)
    }

    test(s"USE SYSTEM SHOW $dbType") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType WHERE access = 'GRANTED'") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType WHERE access = 'GRANTED' AND action = 'match'") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD access ORDER BY access") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD access ORDER BY access WHERE access ='none'") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD access ORDER BY access SKIP 1 LIMIT 10 WHERE access ='none'") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD access SKIP -1") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD access ORDER BY access RETURN access") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType WHERE access = 'GRANTED' RETURN action") {
      assertSameAST(testName)
    }

    test(s"SHOW $dbType YIELD * RETURN *") {
      assertSameAST(testName)
    }
  }

  test("SHOW DATABASE `foo.bar`") {
    assertSameAST(testName)
  }

  test("SHOW DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("SHOW DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 14 (offset: 13))")
  }

  test("SHOW DATABASE blah YIELD *,database, databases, default, dbms RETURN user") {
    assertSameAST(testName)
  }

  // CREATE DATABASE

  test("CREATE DATABASE foo") {
    assertSameAST(testName)
  }

  test("USE system CREATE DATABASE foo") {
    // can parse USE clause, but is not included in AST
    assertSameAST(testName)
  }

  test("CREATE DATABASE $wait") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE `nowait.sec`") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE second WAIT") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE seconds WAIT 12") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE dump WAIT 12 SEC") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE destroy WAIT 12 SECOND") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE data WAIT 12 SECONDS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE `foo.bar`") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE `graph.db`.`db.db`") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE `foo-bar42`") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE `_foo-bar42`") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE ``") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo IF NOT EXISTS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo IF NOT EXISTS WAIT 10 SECONDS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo IF NOT EXISTS WAIT") {
    assertSameAST(testName)
  }

  test("CREATE  DATABASE foo IF NOT EXISTS NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE `_foo-bar42` IF NOT EXISTS") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE foo") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE foo WAIT 10 SECONDS") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE foo WAIT") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE foo NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE OR REPLACE DATABASE `_foo-bar42`") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE foo IF NOT EXISTS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE \"foo.bar\"") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE foo-bar42") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE _foo-bar42") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE 42foo-bar") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 24 (offset: 23))")
  }

  test("CATALOG CREATE DATABASE _foo-bar42 IF NOT EXISTS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE  IF NOT EXISTS") {
    val exceptionMessage =
      s"""Invalid input 'NOT': expected
         |  "."
         |  "IF"
         |  "NOWAIT"
         |  "OPTIONS"
         |  "WAIT"
         |  <EOF> (line 1, column 21 (offset: 20))""".stripMargin

    assertJavaCCException(testName, exceptionMessage)
  }

  test("CREATE DATABASE foo IF EXISTS") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo WAIT -12") {
    assertSameAST(testName)
  }

  test("CREATE DATABASE foo WAIT 3.14") {
    assertJavaCCException(testName, "Invalid input '3.14': expected <EOF> or <UNSIGNED_DECIMAL_INTEGER> (line 1, column 26 (offset: 25))")
  }

  test("CREATE DATABASE foo WAIT bar") {
    assertSameAST(testName)
  }

  test("CATALOG CREATE OR REPLACE DATABASE _foo-bar42") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 27 (offset: 26))")
  }

  test("CREATE DATABASE foo OPTIONS {existingData: 'use', existingDataSeedInstance: '84c3ee6f-260e-47db-a4b6-589c807f2c2e'}") {
    assertJavaCCAST(testName,
      CreateDatabase(Left("foo"), IfExistsThrowError, OptionsMap(Map("existingData" -> literalString("use"),
        "existingDataSeedInstance" -> literalString("84c3ee6f-260e-47db-a4b6-589c807f2c2e"))), NoWait)(pos))
  }

  test("CREATE DATABASE foo OPTIONS {existingData: 'use', existingDataSeedInstance: '84c3ee6f-260e-47db-a4b6-589c807f2c2e'} WAIT") {
    assertJavaCCAST(testName,
      CreateDatabase(Left("foo"), IfExistsThrowError, OptionsMap(Map("existingData" -> literalString("use"),
        "existingDataSeedInstance" -> literalString("84c3ee6f-260e-47db-a4b6-589c807f2c2e"))), IndefiniteWait)(pos))
  }

  test("CREATE DATABASE foo OPTIONS $param") {
    assertJavaCCAST(testName,
      CreateDatabase(Left("foo"), IfExistsThrowError, OptionsParam(parameter("param", CTMap)), NoWait)(pos))
  }

  // DROP DATABASE

  test("DROP DATABASE foo") {
    assertSameAST(testName)
  }

  test("DROP DATABASE $foo") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo WAIT") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo WAIT 10") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo WAIT 10 SEC") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo WAIT 10 SECOND") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo WAIT 10 SECONDS") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG DROP DATABASE `foo.bar`") {
    assertSameAST(testName)
  }

  test("CATALOG DROP DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS WAIT") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS NOWAIT") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo DUMP DATA") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo DESTROY DATA") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS DUMP DATA") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS DESTROY DATA") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF EXISTS DESTROY DATA WAIT") {
    assertSameAST(testName)
  }

  test("DROP DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 14 (offset: 13))")
  }

  test("DROP DATABASE  IF EXISTS") {
    assertSameAST(testName)
  }

  test("DROP DATABASE foo IF NOT EXISTS") {
    assertSameAST(testName)
  }

  test("DROP DATABASE KEEP DATA") {
    val exceptionMessage =
      s"""Invalid input 'DATA': expected
         |  "."
         |  "DESTROY"
         |  "DUMP"
         |  "IF"
         |  "NOWAIT"
         |  "WAIT"
         |  <EOF> (line 1, column 20 (offset: 19))""".stripMargin

    assertJavaCCException(testName, exceptionMessage)
  }

  // START DATABASE

  test("START DATABASE start") {
    assertSameAST(testName)
  }

  test("START DATABASE $foo") {
    assertSameAST(testName)
  }

  test("START DATABASE foo WAIT") {
    assertSameAST(testName)
  }

  test("START DATABASE foo WAIT 5") {
    assertSameAST(testName)
  }

  test("START DATABASE foo WAIT 5 SEC") {
    assertSameAST(testName)
  }

  test("START DATABASE foo WAIT 5 SECOND") {
    assertSameAST(testName)
  }

  test("START DATABASE foo WAIT 5 SECONDS") {
    assertSameAST(testName)
  }

  test("START DATABASE foo NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG START DATABASE `foo.bar`") {
    assertSameAST(testName)
  }

  test("CATALOG START DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("START DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 15 (offset: 14))")
  }

  // STOP DATABASE

  test("STOP DATABASE stop") {
    assertSameAST(testName)
  }

  test("STOP DATABASE $foo") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo WAIT") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo WAIT 99") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo WAIT 99 SEC") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo WAIT 99 SECOND") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo WAIT 99 SECONDS") {
    assertSameAST(testName)
  }

  test("STOP DATABASE foo NOWAIT") {
    assertSameAST(testName)
  }

  test("CATALOG STOP DATABASE `foo.bar`") {
    assertSameAST(testName)
  }

  test("CATALOG STOP DATABASE foo.bar") {
    assertSameAST(testName)
  }

  test("STOP DATABASE") {
    assertJavaCCException(testName, "Invalid input '': expected a parameter or an identifier (line 1, column 14 (offset: 13))")
  }
}
