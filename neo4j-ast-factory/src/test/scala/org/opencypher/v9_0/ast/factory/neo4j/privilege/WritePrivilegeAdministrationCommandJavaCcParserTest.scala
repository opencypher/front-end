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

class WritePrivilegeAdministrationCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName {

  Seq(
    ("GRANT", "TO"),
    ("DENY", "TO"),
    ("REVOKE GRANT", "FROM"),
    ("REVOKE DENY", "FROM"),
    ("REVOKE", "FROM")
  ).foreach {
    case (verb: String, preposition: String) =>

      test(s"$verb WRITE ON GRAPH foo $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPHS foo $preposition role") {
        assertSameAST(testName)
      }

      // Multiple graphs should be allowed (with and without plural GRAPHS)

      test(s"$verb WRITE ON GRAPH * $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPHS * $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo, baz $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPHS foo, baz $preposition role") {
        assertSameAST(testName)
      }

      // Default and home graph should parse

      test(s"$verb WRITE ON HOME GRAPH $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON DEFAULT GRAPH $preposition role") {
        assertSameAST(testName)
      }

      // Multiple roles should be allowed

      test(s"$verb WRITE ON GRAPH foo $preposition role1, role2") {
        assertSameAST(testName)
      }

      // Parameters and escaped strings should be allowed

      test(s"$verb WRITE ON GRAPH $$foo $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH `f:oo` $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo $preposition $$role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo $preposition `r:ole`") {
        assertSameAST(testName)
      }

      // Resource or qualifier should not be supported

      test(s"$verb WRITE {*} ON GRAPH foo $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE {prop} ON GRAPH foo $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo NODE A $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo NODES * $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo RELATIONSHIP R $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo RELATIONSHIPS * $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo ELEMENT A $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo ELEMENTS * $preposition role") {
        assertSameAST(testName)
      }

      // Invalid/missing part of the command

      test(s"$verb WRITE ON GRAPH f:oo $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo $preposition ro:le") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH foo $preposition") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE GRAPH foo $preposition role") {
        assertSameAST(testName)
      }

      // DEFAULT and HOME together with plural GRAPHS

      test(s"$verb WRITE ON HOME GRAPHS $preposition role") {
        val offset = verb.length + 15
        assertJavaCCException(testName, s"""Invalid input 'GRAPHS': expected "GRAPH" (line 1, column ${offset + 1} (offset: $offset))""")
      }

      test(s"$verb WRITE ON DEFAULT GRAPHS $preposition role") {
        val offset = verb.length + 18
        assertJavaCCException(testName, s"""Invalid input 'GRAPHS': expected "GRAPH" (line 1, column ${offset + 1} (offset: $offset))""")
      }

      // Default and home graph with named graph

      test(s"$verb WRITE ON HOME GRAPH baz $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON DEFAULT GRAPH baz $preposition role") {
        assertSameAST(testName)
      }

      // Mix of specific graph and *

      test(s"$verb WRITE ON GRAPH foo, * $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON GRAPH *, foo $preposition role") {
        assertSameAST(testName)
      }

      // Database instead of graph keyword

      test(s"$verb WRITE ON DATABASES * $preposition role") {
        val offset = verb.length + 10
        assertJavaCCException(testName, s"""Invalid input 'DATABASES': expected "DEFAULT", "GRAPH", "GRAPHS" or "HOME" (line 1, column ${offset + 1} (offset: $offset))""")
      }

      test(s"$verb WRITE ON DATABASE foo $preposition role") {
        val offset = verb.length + 10
        assertJavaCCException(testName, s"""Invalid input 'DATABASE': expected "DEFAULT", "GRAPH", "GRAPHS" or "HOME" (line 1, column ${offset + 1} (offset: $offset))""")
      }

      test(s"$verb WRITE ON HOME DATABASE $preposition role") {
        assertSameAST(testName)
      }

      test(s"$verb WRITE ON DEFAULT DATABASE $preposition role") {
        assertSameAST(testName)
      }
  }
}
