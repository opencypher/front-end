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

import org.opencypher.v9_0.ast.AlterDatabaseAlias
import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.CreateDatabaseAlias
import org.opencypher.v9_0.ast.DropDatabaseAlias
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.IfExistsInvalidSyntax
import org.opencypher.v9_0.ast.IfExistsReplace
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class AliasAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName with AstConstructionTestSupport {

  // CREATE ALIAS
  test("CREATE ALIAS alias FOR DATABASE target") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS alias IF NOT EXISTS FOR DATABASE target") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsDoNothing)(pos))
  }

  test("CREATE OR REPLACE ALIAS alias FOR DATABASE target") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsReplace)(pos))
  }

  test("CREATE OR REPLACE ALIAS alias IF NOT EXISTS FOR DATABASE target") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsInvalidSyntax)(pos))
  }

  test("CREATE ALIAS alias.name FOR DATABASE db.name") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias.name"), Left("db.name"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS alias . name FOR DATABASE db.name") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("alias.name"), Left("db.name"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS IF FOR DATABASE db.name") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("IF"), Left("db.name"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS $alias FOR DATABASE $target") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Right(parameter("alias", CTString)), Right(parameter("target", CTString)), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS IF") {
    assertJavaCCException(testName, """Invalid input '': expected ".", "FOR" or "IF" (line 1, column 16 (offset: 15))""")
  }

  test("CREATE ALIAS") {
    assertJavaCCException(testName, """Invalid input '': expected a parameter or an identifier (line 1, column 13 (offset: 12))""")
  }

  test("CREATE ALIAS #Malmö FOR DATABASE db1") {
    assertJavaCCException(testName,
      s"""Invalid input '#': expected a parameter or an identifier (line 1, column 14 (offset: 13))""".stripMargin)
  }

  test("CREATE ALIAS Mal#mö FOR DATABASE db1") {
    assertJavaCCException(testName, s"""Invalid input '#': expected ".", "FOR" or "IF" (line 1, column 17 (offset: 16))""".stripMargin)
  }

  test("CREATE ALIAS `Mal#mö` FOR DATABASE db1") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("Mal#mö"), Left("db1"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS `#Malmö` FOR DATABASE db1") {
    assertJavaCCAST(testName, CreateDatabaseAlias(Left("#Malmö"), Left("db1"), IfExistsThrowError)(pos))
  }

  test("CREATE ALIAS name FOR DATABASE") {
    assertJavaCCException(testName,
      s"""Invalid input '': expected a parameter or an identifier (line 1, column 31 (offset: 30))""")
  }

  // DROP ALIAS
  test("DROP ALIAS name FOR DATABASE") {
    assertJavaCCAST(testName, DropDatabaseAlias(Left("name"), false)(pos))
  }

  test("DROP ALIAS $name FOR DATABASE") {
    assertJavaCCAST(testName, DropDatabaseAlias(Right(parameter("name", CTString)), false)(pos))
  }

  test("DROP ALIAS name IF EXISTS FOR DATABASE") {
    assertJavaCCAST(testName, DropDatabaseAlias(Left("name"), true)(pos))
  }

  test("DROP ALIAS wait FOR DATABASE") {
    assertJavaCCAST(testName, DropDatabaseAlias(Left("wait"), false)(pos))
  }

  test("DROP ALIAS nowait FOR DATABASE") {
    assertJavaCCAST(testName, DropDatabaseAlias(Left("nowait"), false)(pos))
  }

  // ALTER ALIAS
  test("ALTER ALIAS name SET DATABASE TARGET db") {
    assertJavaCCAST(testName, AlterDatabaseAlias(Left("name"), Left("db"), false)(pos))
  }

  test("ALTER ALIAS name IF EXISTS SET DATABASE TARGET db") {
    assertJavaCCAST(testName, AlterDatabaseAlias(Left("name"), Left("db"), true)(pos))
  }

  test("ALTER ALIAS $name SET DATABASE TARGET $db") {
    assertJavaCCAST(testName, AlterDatabaseAlias(Right(parameter("name", CTString)), Right(parameter("db", CTString)), false)(pos))
  }

  test("ALTER ALIAS $name if exists SET DATABASE TARGET $db") {
    assertJavaCCAST(testName, AlterDatabaseAlias(Right(parameter("name", CTString)), Right(parameter("db", CTString)), true)(pos))
  }

  test("ALTER ALIAS name if exists SET db TARGET") {
    assertJavaCCException(testName, """Invalid input 'db': expected "DATABASE" (line 1, column 32 (offset: 31))""")
  }

  test("ALTER DATABASE ALIAS name SET TARGET db if exists") {
    assertJavaCCException(testName, """Invalid input 'name': expected ".", "IF" or "SET" (line 1, column 22 (offset: 21))""")
  }

  test("ALTER FUNCTION name SET TARGET db if exists") {
    assertJavaCCException(testName, """Invalid input 'FUNCTION': expected "ALIAS", "CURRENT", "DATABASE" or "USER" (line 1, column 7 (offset: 6))""")
  }

  test("ALTER FUNCTION name SET TARGET db if not exists") {
    assertJavaCCException(testName, """Invalid input 'FUNCTION': expected "ALIAS", "CURRENT", "DATABASE" or "USER" (line 1, column 7 (offset: 6))""")
  }
}
