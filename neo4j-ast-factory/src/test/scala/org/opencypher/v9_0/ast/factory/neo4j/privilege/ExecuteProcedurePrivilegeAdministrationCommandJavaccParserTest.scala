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
package org.opencypher.v9_0.ast.factory.neo4j.privilege

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.DbmsAction
import org.opencypher.v9_0.ast.ExecuteBoostedProcedureAction
import org.opencypher.v9_0.ast.ExecuteProcedureAction
import org.opencypher.v9_0.ast.ProcedurePrivilegeQualifier
import org.opencypher.v9_0.ast.ProcedureQualifier
import org.opencypher.v9_0.ast.RevokeBothType
import org.opencypher.v9_0.ast.RevokeDenyType
import org.opencypher.v9_0.ast.RevokeGrantType
import org.opencypher.v9_0.ast.factory.neo4j.ParserComparisonTestBase
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class ExecuteProcedurePrivilegeAdministrationCommandJavaccParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {

  Seq(
    ("GRANT", "TO", grantExecuteProcedurePrivilege: executeProcedurePrivilegeFunc),
    ("DENY", "TO", denyExecuteProcedurePrivilege: executeProcedurePrivilegeFunc),
    ("REVOKE GRANT", "FROM", revokeGrantExecuteProcedurePrivilege: executeProcedurePrivilegeFunc),
    ("REVOKE DENY", "FROM", revokeDenyExecuteProcedurePrivilege: executeProcedurePrivilegeFunc),
    ("REVOKE", "FROM", revokeExecuteProcedurePrivilege: executeProcedurePrivilegeFunc)
  ).foreach {
    case (verb: String, preposition: String, func: executeProcedurePrivilegeFunc) =>

      Seq(
        ("EXECUTE PROCEDURE", ExecuteProcedureAction),
        ("EXECUTE BOOSTED PROCEDURE", ExecuteBoostedProcedureAction)
      ).foreach {
        case (execute, action) =>

          test(s"$verb $execute * ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          // The following two tests check that the plural form EXECUTE [BOOSTED] PROCEDURES is valid

          test(s"$verb ${execute}S * ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb ${execute}S `*` ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.procedure ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb ${execute}S apoc.procedure ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.math.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute *apoc ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute *apoc, *.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute *.sin, apoc* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute *.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.*.math.* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute math.*n ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute math.si? ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute mat*.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute mat?.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute ?ath.sin ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute mat?.`a.\n`.*n ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute `a b` ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute a b ON DBMS $preposition role") {
            assertJavaCCAST(testName, func(action, List(ProcedureQualifier("ab")(defaultPos)), Seq(Left("role")))(defaultPos))
          }

          test(s"$verb $execute apoc.math.* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute math.sin, math.cos ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.math.sin, math.* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute * $preposition role") {
            val offset = testName.length
            assertJavaCCException(testName, s"""Invalid input '': expected
                                               |  "*"
                                               |  "."
                                               |  "?"
                                               |  "ON"
                                               |  an identifier (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }

          test(s"$verb $execute * ON DATABASE * $preposition role") {
            val offset = testName.length
            assertJavaCCException(testName, s"""Invalid input '': expected
                                               |  "*"
                                               |  "."
                                               |  "?"
                                               |  "ON"
                                               |  an identifier (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }
      }
  }

  Seq(
    ("GRANT", "TO"),
    ("DENY", "TO"),
    ("REVOKE GRANT", "FROM"),
    ("REVOKE DENY", "FROM"),
    ("REVOKE", "FROM")
  ).foreach {
    case (verb: String, preposition: String) =>
      Seq(
        "EXECUTE ADMIN PROCEDURES",
        "EXECUTE ADMINISTRATOR PROCEDURES"
      ).foreach {
        command =>

          test(s"$verb $command ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $command * ON DBMS $preposition role") {
            val offset = s"$verb $command ".length
            assertJavaCCException(testName, s"""Invalid input '*': expected "ON" (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }

          test(s"$verb $command ON DATABASE * $preposition role") {
            val offset = s"$verb $command ON ".length
            assertJavaCCException(testName, s"""Invalid input 'DATABASE': expected "DBMS" (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }
      }

      test(s"$verb EXECUTE ADMIN PROCEDURE ON DBMS $preposition role") {
        val offset = s"$verb EXECUTE ADMIN ".length
        assertJavaCCException(testName, s"""Invalid input 'PROCEDURE': expected "PROCEDURES" (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
      }
  }

  private val defaultPos: InputPosition = InputPosition(0, 1, 1)

  private type executeProcedurePrivilegeFunc = (DbmsAction, List[ProcedurePrivilegeQualifier], Seq[Either[String, Parameter]]) => InputPosition => ast.Statement

  private def grantExecuteProcedurePrivilege(a: DbmsAction, q: List[ProcedurePrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege.dbmsAction(a, r, q)

  private def denyExecuteProcedurePrivilege(a: DbmsAction, q: List[ProcedurePrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege.dbmsAction(a, r, q)

  private def revokeGrantExecuteProcedurePrivilege(a: DbmsAction, q: List[ProcedurePrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeGrantType()(DummyPosition(0)), q)

  private def revokeDenyExecuteProcedurePrivilege(a: DbmsAction, q: List[ProcedurePrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeDenyType()(DummyPosition(0)), q)

  private def revokeExecuteProcedurePrivilege(a: DbmsAction, q: List[ProcedurePrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeBothType()(DummyPosition(0)), q)
}
