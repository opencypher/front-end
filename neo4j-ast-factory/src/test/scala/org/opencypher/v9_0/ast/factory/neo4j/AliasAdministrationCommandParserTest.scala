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
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.util.symbols.CTString
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class AliasAdministrationCommandParserTest extends AdministrationAndSchemaCommandParserTestBase {

  // CREATE ALIAS
  test("CREATE ALIAS alias FOR DATABASE target") {
    assertAst(CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS alias IF NOT EXISTS FOR DATABASE target") {
    assertAst(CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsDoNothing)(defaultPos))
  }

  test("CREATE OR REPLACE ALIAS alias FOR DATABASE target") {
    assertAst(CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsReplace)(defaultPos))
  }

  test("CREATE OR REPLACE ALIAS alias IF NOT EXISTS FOR DATABASE target") {
    assertAst(CreateDatabaseAlias(Left("alias"), Left("target"), IfExistsInvalidSyntax)(defaultPos))
  }

  test("CREATE ALIAS alias.name FOR DATABASE db.name") {
    assertAst(CreateDatabaseAlias(Left("alias.name"), Left("db.name"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS alias . name FOR DATABASE db.name") {
    assertAst(CreateDatabaseAlias(Left("alias.name"), Left("db.name"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS IF FOR DATABASE db.name") {
    assertAst(CreateDatabaseAlias(Left("IF"), Left("db.name"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS $alias FOR DATABASE $target") {
    assertAst(CreateDatabaseAlias(
      Right(Parameter("alias", CTString)(1, 14, 13)),
      Right(Parameter("target", CTString)(1, 34, 33)),
      IfExistsThrowError
    )(defaultPos))
  }

  test("CREATE ALIAS IF") {
    assertFailsWithMessage(
      testName,
      """Invalid input '': expected ".", "FOR" or "IF" (line 1, column 16 (offset: 15))"""
    )
  }

  test("CREATE ALIAS") {
    assertFailsWithMessage(
      testName,
      """Invalid input '': expected a parameter or an identifier (line 1, column 13 (offset: 12))"""
    )
  }

  test("CREATE ALIAS #Malmö FOR DATABASE db1") {
    assertFailsWithMessage(
      testName,
      s"""Invalid input '#': expected a parameter or an identifier (line 1, column 14 (offset: 13))""".stripMargin
    )
  }

  test("CREATE ALIAS Mal#mö FOR DATABASE db1") {
    assertFailsWithMessage(
      testName,
      s"""Invalid input '#': expected ".", "FOR" or "IF" (line 1, column 17 (offset: 16))""".stripMargin
    )
  }

  test("CREATE ALIAS `Mal#mö` FOR DATABASE db1") {
    assertAst(CreateDatabaseAlias(Left("Mal#mö"), Left("db1"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS `#Malmö` FOR DATABASE db1") {
    assertAst(CreateDatabaseAlias(Left("#Malmö"), Left("db1"), IfExistsThrowError)(defaultPos))
  }

  test("CREATE ALIAS name FOR DATABASE") {
    assertFailsWithMessage(
      testName,
      s"""Invalid input '': expected a parameter or an identifier (line 1, column 31 (offset: 30))"""
    )
  }

  // DROP ALIAS
  test("DROP ALIAS name FOR DATABASE") {
    assertAst(DropDatabaseAlias(Left("name"), ifExists = false)(defaultPos))
  }

  test("DROP ALIAS $name FOR DATABASE") {
    assertAst(DropDatabaseAlias(Right(Parameter("name", CTString)(1, 12, 11)), ifExists = false)(defaultPos))
  }

  test("DROP ALIAS name IF EXISTS FOR DATABASE") {
    assertAst(DropDatabaseAlias(Left("name"), ifExists = true)(defaultPos))
  }

  test("DROP ALIAS wait FOR DATABASE") {
    assertAst(DropDatabaseAlias(Left("wait"), ifExists = false)(defaultPos))
  }

  test("DROP ALIAS nowait FOR DATABASE") {
    assertAst(DropDatabaseAlias(Left("nowait"), ifExists = false)(defaultPos))
  }

  // ALTER ALIAS
  test("ALTER ALIAS name SET DATABASE TARGET db") {
    assertAst(AlterDatabaseAlias(Left("name"), Left("db"), ifExists = false)(defaultPos))
  }

  test("ALTER ALIAS name IF EXISTS SET DATABASE TARGET db") {
    assertAst(AlterDatabaseAlias(Left("name"), Left("db"), ifExists = true)(defaultPos))
  }

  test("ALTER ALIAS $name SET DATABASE TARGET $db") {
    assertAst(AlterDatabaseAlias(
      Right(Parameter("name", CTString)(1, 13, 12)),
      Right(Parameter("db", CTString)(1, 39, 38)),
      ifExists = false
    )(defaultPos))
  }

  test("ALTER ALIAS $name if exists SET DATABASE TARGET $db") {
    assertAst(AlterDatabaseAlias(
      Right(Parameter("name", CTString)(1, 13, 12)),
      Right(Parameter("db", CTString)(1, 49, 48)),
      ifExists = true
    )(defaultPos))
  }

  test("ALTER ALIAS name if exists SET db TARGET") {
    assertFailsWithMessage(testName, """Invalid input 'db': expected "DATABASE" (line 1, column 32 (offset: 31))""")
  }

  test("ALTER DATABASE ALIAS name SET TARGET db if exists") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'name': expected ".", "IF" or "SET" (line 1, column 22 (offset: 21))"""
    )
  }

  test("ALTER FUNCTION name SET TARGET db if exists") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'FUNCTION': expected "ALIAS", "CURRENT", "DATABASE" or "USER" (line 1, column 7 (offset: 6))"""
    )
  }

  test("ALTER FUNCTION name SET TARGET db if not exists") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'FUNCTION': expected "ALIAS", "CURRENT", "DATABASE" or "USER" (line 1, column 7 (offset: 6))"""
    )
  }
}
