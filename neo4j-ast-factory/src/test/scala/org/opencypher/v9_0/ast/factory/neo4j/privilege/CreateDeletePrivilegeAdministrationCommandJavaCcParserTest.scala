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

import org.opencypher.v9_0.ast.factory.neo4j.ParserComparisonTestBase
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class CreateDeletePrivilegeAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {

  Seq(
    ("GRANT", "TO"),
    ("DENY", "TO"),
    ("REVOKE GRANT", "FROM"),
    ("REVOKE DENY", "FROM"),
    ("REVOKE", "FROM")
  ).foreach {
    case (verb: String, preposition: String) =>

      Seq(
        "CREATE",
        "DELETE"
      ).foreach {
        createOrDelete =>

          test(s"$verb $createOrDelete ON GRAPH foo $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON GRAPH foo ELEMENTS A $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON GRAPH foo NODE A $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON GRAPH foo RELATIONSHIPS * $preposition role") {
            assertSameAST(testName)
          }

          // Home graph

          test(s"$verb $createOrDelete ON HOME GRAPH $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON HOME GRAPH $preposition role1, role2") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON HOME GRAPH $preposition $$role1, role2") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON HOME GRAPH RELATIONSHIPS * $preposition role") {
            assertSameAST(testName)
          }

          // Both Home and * should not parse
          test(s"$verb $createOrDelete ON HOME GRAPH * $preposition role") {
            assertSameAST(testName)
          }

          // Default graph

          test(s"$verb $createOrDelete ON DEFAULT GRAPH $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON DEFAULT GRAPH $preposition role1, role2") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON DEFAULT GRAPH $preposition $$role1, role2") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON DEFAULT GRAPH RELATIONSHIPS * $preposition role") {
            assertSameAST(testName)
          }

          // Both Default and * should not parse
          test(s"$verb $createOrDelete ON DEFAULT GRAPH * $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $createOrDelete ON DATABASE blah $preposition role") {
            val offset = verb.length + createOrDelete.length + 5
            assertJavaCCException(testName, s"""Invalid input 'DATABASE': expected "DEFAULT", "GRAPH", "GRAPHS" or "HOME" (line 1, column ${offset + 1} (offset: $offset))""")
          }
      }
  }
}
