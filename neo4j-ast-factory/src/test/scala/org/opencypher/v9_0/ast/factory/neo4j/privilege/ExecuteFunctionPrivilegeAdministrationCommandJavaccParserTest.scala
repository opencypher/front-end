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
import org.opencypher.v9_0.ast.ExecuteBoostedFunctionAction
import org.opencypher.v9_0.ast.ExecuteFunctionAction
import org.opencypher.v9_0.ast.FunctionPrivilegeQualifier
import org.opencypher.v9_0.ast.FunctionQualifier
import org.opencypher.v9_0.ast.RevokeBothType
import org.opencypher.v9_0.ast.RevokeDenyType
import org.opencypher.v9_0.ast.RevokeGrantType
import org.opencypher.v9_0.ast.factory.neo4j.ParserComparisonTestBase
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class ExecuteFunctionPrivilegeAdministrationCommandJavaccParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {

  Seq(
    ("GRANT", "TO", grantExecuteFunctionPrivilege: executeFunctionPrivilegeFunc),
    ("DENY", "TO", denyExecuteFunctionPrivilege: executeFunctionPrivilegeFunc),
    ("REVOKE GRANT", "FROM", revokeGrantExecuteFunctionPrivilege: executeFunctionPrivilegeFunc),
    ("REVOKE DENY", "FROM", revokeDenyExecuteFunctionPrivilege: executeFunctionPrivilegeFunc),
    ("REVOKE", "FROM", revokeExecuteFunctionPrivilege: executeFunctionPrivilegeFunc)
  ).foreach {
    case (verb: String, preposition: String, func: executeFunctionPrivilegeFunc) =>

      Seq(
        ("EXECUTE FUNCTION", ExecuteFunctionAction),
        ("EXECUTE USER FUNCTION", ExecuteFunctionAction),
        ("EXECUTE USER DEFINED FUNCTION", ExecuteFunctionAction),
        ("EXECUTE BOOSTED FUNCTION", ExecuteBoostedFunctionAction),
        ("EXECUTE BOOSTED USER FUNCTION", ExecuteBoostedFunctionAction),
        ("EXECUTE BOOSTED USER DEFINED FUNCTION", ExecuteBoostedFunctionAction)
      ).foreach {
        case (execute, action) =>

          test(s"$verb $execute * ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          // The following two tests check that the plural form EXECUTE ... FUNCTIONS is valid

          test(s"$verb ${execute}S * ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb ${execute}S `*` ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.function ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb ${execute}S apoc.function ON DBMS $preposition role") {
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
            assertJavaCCAST(testName, func(action, List(FunctionQualifier("ab")(defaultPos)), Seq(Left("role")))(defaultPos))
          }

          test(s"$verb $execute math.sin, math.cos ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.math.sin, math.* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute apoc.math.sin, math.*, apoc* ON DBMS $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $execute * $preposition role") {
            val offset = testName.length
            assertJavaCCException(testName,
              s"""Invalid input '': expected
                 |  "*"
                 |  "."
                 |  "?"
                 |  "ON"
                 |  an identifier (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }

          test(s"$verb $execute * ON DATABASE * $preposition role") {
            val offset = testName.length
            assertJavaCCException(testName,
              s"""Invalid input '': expected
                 |  "*"
                 |  "."
                 |  "?"
                 |  "ON"
                 |  an identifier (line 1, column ${offset + 1} (offset: $offset))""".stripMargin)
          }
      }

      test(s"$verb EXECUTE DEFINED FUNCTION * ON DATABASE * $preposition role") {
        val offset = s"$verb EXECUTE ".length
        assertJavaCCException(testName,
          s"""Invalid input 'DEFINED': expected "FUNCTION", "FUNCTIONS" or "USER" (line 1, column ${offset + 1} (offset: $offset))""")
      }
  }

  private val defaultPos: InputPosition = InputPosition(0, 1, 1)

  private type executeFunctionPrivilegeFunc = (DbmsAction, List[FunctionPrivilegeQualifier], Seq[Either[String, Parameter]]) => InputPosition => ast.Statement

  private def grantExecuteFunctionPrivilege(a: DbmsAction, q: List[FunctionPrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.GrantPrivilege.dbmsAction(a, r, q)

  private def denyExecuteFunctionPrivilege(a: DbmsAction, q: List[FunctionPrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.DenyPrivilege.dbmsAction(a, r, q)

  private def revokeGrantExecuteFunctionPrivilege(a: DbmsAction, q: List[FunctionPrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeGrantType()(DummyPosition(0)), q)

  private def revokeDenyExecuteFunctionPrivilege(a: DbmsAction, q: List[FunctionPrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeDenyType()(DummyPosition(0)), q)

  private def revokeExecuteFunctionPrivilege(a: DbmsAction, q: List[FunctionPrivilegeQualifier], r: Seq[Either[String, Parameter]]): InputPosition => ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeBothType()(DummyPosition(0)), q)
}
