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

class LabelPrivilegeAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {
  Seq(
    ("GRANT", "TO"),
    ("DENY", "TO"),
    ("REVOKE GRANT", "FROM"),
    ("REVOKE DENY", "FROM"),
    ("REVOKE", "FROM")
  ).foreach {
    case (verb: String, preposition: String) =>

      Seq(
        "SET",
        "REMOVE"
      ).foreach {
        setOrRemove =>

          test(s"$verb $setOrRemove LABEL label ON GRAPH foo $preposition role") {
            assertSameAST(testName)
          }

          // Multiple labels should be allowed

          test(s"$verb $setOrRemove LABEL * ON GRAPH foo $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL label1, label2 ON GRAPH foo $preposition role") {
            assertSameAST(testName)
          }

          // Multiple graphs should be allowed

          test(s"$verb $setOrRemove LABEL label ON GRAPHS * $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL label ON GRAPHS foo,baz $preposition role") {
            assertSameAST(testName)
          }

          // Home graph should be allowed

          test(s"$verb $setOrRemove LABEL label ON HOME GRAPH $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL * ON HOME GRAPH $preposition role") {
            assertSameAST(testName)
          }

          // Default graph should be allowed

          test(s"$verb $setOrRemove LABEL label ON DEFAULT GRAPH $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL * ON DEFAULT GRAPH $preposition role") {
            assertSameAST(testName)
          }

          // Multiple roles should be allowed

          test(s"$verb $setOrRemove LABEL label ON GRAPHS foo $preposition role1, role2") {
            assertSameAST(testName)
          }

          // Parameter values

          test(s"$verb $setOrRemove LABEL label ON GRAPH $$foo $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL label ON GRAPH foo $preposition $$role") {
            assertSameAST(testName)
          }

          // TODO: should this one be supported?
          test(s"$verb $setOrRemove LABEL $$label ON GRAPH foo $preposition role") {
            assertSameAST(testName)
          }

          // LABELS instead of LABEL

          test(s"$verb $setOrRemove LABELS label ON GRAPH * $preposition role") {
            assertJavaCCExceptionStart(testName, s"""Invalid input 'LABELS': expected""")
          }

          // Database instead of graph keyword

          test(s"$verb $setOrRemove LABEL label ON DATABASES * $preposition role") {
            val offset = verb.length + setOrRemove.length + 17
            assertJavaCCExceptionStart(testName, s"""Invalid input 'DATABASES': expected""")
          }

          test(s"$verb $setOrRemove LABEL label ON DATABASE foo $preposition role") {
            val offset = verb.length + setOrRemove.length + 17
            assertJavaCCExceptionStart(testName, s"""Invalid input 'DATABASE': expected""")
          }

          test(s"$verb $setOrRemove LABEL label ON HOME DATABASE $preposition role") {
            assertSameAST(testName)
          }

          test(s"$verb $setOrRemove LABEL label ON DEFAULT DATABASE $preposition role") {
            assertSameAST(testName)
          }
      }
  }
}
