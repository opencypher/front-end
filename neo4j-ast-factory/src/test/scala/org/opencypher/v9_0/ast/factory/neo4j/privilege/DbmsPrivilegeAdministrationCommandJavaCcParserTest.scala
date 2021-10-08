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

import org.opencypher.v9_0.ast.AlterDatabaseAction

import org.opencypher.v9_0.ast.DbmsAction
import org.opencypher.v9_0.ast.RevokeBothType
import org.opencypher.v9_0.ast.RevokeDenyType
import org.opencypher.v9_0.ast.RevokeGrantType
import org.opencypher.v9_0.ast.SetDatabaseAccessAction
import org.opencypher.v9_0.ast.factory.neo4j.ParserComparisonTestBase
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class DbmsPrivilegeAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {

  protected val pos: InputPosition = DummyPosition(0)

  type dbmsPrivilegeFunc = (DbmsAction, Seq[Either[String, Parameter]]) =>  ast.Statement

  def grantDbmsPrivilege(a: DbmsAction, r: Seq[Either[String, Parameter]]): ast.Statement =
    ast.GrantPrivilege.dbmsAction(a, r)(pos)

  def denyDbmsPrivilege(a: DbmsAction, r: Seq[Either[String, Parameter]]): ast.Statement =
    ast.DenyPrivilege.dbmsAction(a, r)(pos)

  def revokeGrantDbmsPrivilege(a: DbmsAction, r: Seq[Either[String, Parameter]]): ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeGrantType()(pos))(pos)

  def revokeDenyDbmsPrivilege(a: DbmsAction, r: Seq[Either[String, Parameter]]): ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeDenyType()(pos))(pos)

  def revokeDbmsPrivilege(a: DbmsAction, r: Seq[Either[String, Parameter]]): ast.Statement =
    ast.RevokePrivilege.dbmsAction(a, r, RevokeBothType()(pos))(pos)

  def privilegeTests(command: String, preposition: String, privilegeFunc: dbmsPrivilegeFunc): Unit = {
    val offset = command.length + 1

    val privilegesSupportedInParboiled = Seq(
      "CREATE ROLE",
      "RENAME ROLE",
      "DROP ROLE",
      "SHOW ROLE",
      "ASSIGN ROLE",
      "REMOVE ROLE",
      "ROLE MANAGEMENT",
      "CREATE USER",
      "RENAME USER",
      "DROP USER",
      "SHOW USER",
      "SET PASSWORD",
      "SET PASSWORDS",
      "SET USER STATUS",
      "SET USER HOME DATABASE",
      "ALTER USER",
      "USER MANAGEMENT",
      "CREATE DATABASE",
      "DROP DATABASE",
      "DATABASE MANAGEMENT",
      "SHOW PRIVILEGE",
      "ASSIGN PRIVILEGE",
      "REMOVE PRIVILEGE",
      "PRIVILEGE MANAGEMENT"
    )

    val privilegesOnlySupportedInJavaCc = Seq(
      "ALTER DATABASE",
      "SET DATABASE ACCESS"
    )

    privilegesSupportedInParboiled.foreach {
      privilege: String =>
        test(s"$command $privilege ON DBMS $preposition role") {
          assertSameAST(testName)
        }

        test(s"$command $privilege ON DBMS $preposition role1, $$role2") {
          assertSameAST(testName)
        }

        test(s"$command $privilege ON DBMS $preposition `r:ole`") {
          assertSameAST(testName)
        }
    }

    test(s"$command ALTER DATABASE ON DBMS $preposition role") {
      assertJavaCCAST(testName, privilegeFunc(AlterDatabaseAction, Seq(Left("role"))))
    }

    test(s"$command SET DATABASE ACCESS ON DBMS $preposition role") {
      assertJavaCCAST(testName, privilegeFunc(SetDatabaseAccessAction, Seq(Left("role"))))
    }

    (privilegesSupportedInParboiled ++ privilegesOnlySupportedInJavaCc).foreach {
      privilege: String =>
        test(s"$command $privilege ON DATABASE $preposition role") {
          val offset = command.length + 5 + privilege.length
          assertJavaCCException(testName, s"""Invalid input 'DATABASE': expected "DBMS" (line 1, column ${offset + 1} (offset: $offset))""")
        }

        test(s"$command $privilege ON HOME DATABASE $preposition role") {
          val offset = command.length + 5 + privilege.length
          assertJavaCCException(testName, s"""Invalid input 'HOME': expected "DBMS" (line 1, column ${offset + 1} (offset: $offset))""")
        }

        test(s"$command $privilege DBMS $preposition role") {
          val offset = command.length + 2 + privilege.length
          val expected = (command, privilege) match {
            // this case looks like granting/revoking a role named MANAGEMENT to/from a user
            case ("GRANT", "ROLE MANAGEMENT") => s"""Invalid input 'DBMS': expected "," or "TO" (line 1, column ${offset + 1} (offset: $offset))"""
            case ("REVOKE", "ROLE MANAGEMENT") => s"""Invalid input 'DBMS': expected "," or "FROM" (line 1, column ${offset + 1} (offset: $offset))"""
            case _ => s"""Invalid input 'DBMS': expected "ON" (line 1, column ${offset + 1} (offset: $offset))"""
          }
          assertJavaCCException(testName, expected)
        }

        test(s"$command $privilege ON $preposition role") {
          val offset = command.length + 5 + privilege.length
          assertJavaCCException(testName, s"""Invalid input '$preposition': expected "DBMS" (line 1, column ${offset + 1} (offset: $offset))""")
        }

        test(s"$command $privilege ON DBMS $preposition r:ole") {
          val offset = command.length + 12 + privilege.length + preposition.length
          assertJavaCCException(testName, s"""Invalid input ':': expected "," or <EOF> (line 1, column ${offset + 1} (offset: $offset))""")
        }

        test(s"$command $privilege ON DBMS $preposition") {
          val offset = command.length + 10 + privilege.length + preposition.length
          assertJavaCCException(testName, s"""Invalid input '': expected a parameter or an identifier (line 1, column ${offset + 1} (offset: $offset))""")
        }

        test(s"$command $privilege ON DBMS") {
          val offset = command.length + 9 + privilege.length
          assertJavaCCException(testName, s"""Invalid input '': expected "$preposition" (line 1, column ${offset + 1} (offset: $offset))""")
        }
    }

    // The tests below needs to be outside the loop since ALL [PRIVILEGES] ON DATABASE is a valid (but different) command

    test(s"$command ALL ON DBMS $preposition $$role") {
      assertSameAST(testName)
    }

    test(s"$command ALL ON DBMS $preposition role1, role2") {
      assertSameAST(testName)
    }

    test(s"$command ALL PRIVILEGES ON DBMS $preposition role") {
      assertSameAST(testName)
    }

    test(s"$command ALL PRIVILEGES ON DBMS $preposition $$role1, role2") {
      assertSameAST(testName)
    }

    test(s"$command ALL DBMS PRIVILEGES ON DBMS $preposition role") {
      assertSameAST(testName)
    }

    test(s"$command ALL DBMS PRIVILEGES ON DBMS $preposition `r:ole`, $$role2") {
      assertSameAST(testName)
    }

    test(s"$command ALL DBMS PRIVILEGES ON DATABASE $preposition role") {
      assertJavaCCException(testName,
        s"""Invalid input 'DATABASE': expected "DBMS" (line 1, column ${offset+24} (offset: ${offset+23}))""")
    }

    test(s"$command ALL DBMS PRIVILEGES ON HOME DATABASE $preposition role") {
      assertJavaCCException(testName,
        s"""Invalid input 'HOME': expected "DBMS" (line 1, column ${offset+24} (offset: ${offset+23}))""")
    }

    test(s"$command ALL DBMS PRIVILEGES DBMS $preposition role") {
      assertJavaCCException(testName,
        s"""Invalid input 'DBMS': expected "ON" (line 1, column ${offset+21} (offset: ${offset+20}))""")
    }

    test(s"$command ALL DBMS PRIVILEGES $preposition") {
      assertJavaCCException(testName,
        s"""Invalid input '$preposition': expected "ON" (line 1, column ${offset+21} (offset: ${offset+20}))""")
    }

    test(s"$command ALL DBMS PRIVILEGES ON $preposition") {
      assertJavaCCException(testName,
        s"""Invalid input '$preposition': expected
           |  "DATABASE"
           |  "DATABASES"
           |  "DBMS"
           |  "DEFAULT"
           |  "GRAPH"
           |  "GRAPHS"
           |  "HOME" (line 1, column ${offset+24} (offset: ${offset+23}))""".stripMargin)
    }

    test(s"$command ALL DBMS PRIVILEGES ON DBMS $preposition r:ole") {
      val finalOffset = offset+30+preposition.length
      assertJavaCCException(testName,
        s"""Invalid input ':': expected "," or <EOF> (line 1, column ${finalOffset+1} (offset: $finalOffset))""")
    }

    test(s"$command ALL DBMS PRIVILEGES ON DBMS $preposition") {
      val finalOffset = offset+28+preposition.length
      assertJavaCCException(testName,
        s"""Invalid input '': expected a parameter or an identifier (line 1, column ${finalOffset+1} (offset: $finalOffset))""")
    }

    test(s"$command ALL DBMS PRIVILEGES ON DBMS") {
      assertJavaCCException(testName,
        s"""Invalid input '': expected "$preposition" (line 1, column ${offset+28} (offset: ${offset+27}))""")
    }
  }
}
