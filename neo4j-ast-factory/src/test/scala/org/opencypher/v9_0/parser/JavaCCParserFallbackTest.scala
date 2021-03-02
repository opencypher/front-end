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
package org.opencypher.v9_0.parser

import org.opencypher.v9_0.ast.factory.neo4j.JavaCCParser
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class JavaCCParserFallbackTest extends CypherFunSuite {

  test("should fall back") {
    Seq(
      "REVOKE SHOW INDEXES ON DATABASE foo FROM bar",
      "MATCH (n) RETURN n // SHOW",
      "MATCH (n) RETURN n.Id as INDEX",
      "MATCH (n) RETURN n.Id as GRANT",
      "MATCH (n) WITH n as SHOW RETURN SHOW as INDEX",
      "DROP ROLE cheeseRoll",
      "CREATE DATABASE store WAIT",
      "SHOW INDEX WHERE name = 'GRANT'",
      "MATCH (n:Label) WHERE n.cypher = 'SHOW INDEXES' and n.access = 'DENY' RETURN n"
    ).foreach(t => {
      withClue(t) { JavaCCParser.shouldFallBack(t) shouldBe true }
    })
  }

  test("should not fall back") {
    Seq(
      "MATCH (n) RETURN n",
      "CREATE (n:Label)",
    ).foreach(t => {
      withClue(t) { JavaCCParser.shouldFallBack(t) shouldBe false }
    })
  }

}
