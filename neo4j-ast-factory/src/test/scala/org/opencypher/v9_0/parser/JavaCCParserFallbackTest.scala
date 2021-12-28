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
      // Not (yet) supported commands
      "GRANT EXECUTE FUNCTION * ON DBMS TO role",
      "DENY EXECUTE BOOSTED PROCEDURE apoc.match ON DBMS TO role",

      // Supported commands containing fallback keywords
      "MATCH (n) RETURN n // EXECUTE",
    ).foreach(t => {
      withClue(t) { JavaCCParser.shouldFallback(t) shouldBe true }
    })
  }

  test("should not fall back") {
    Seq(
      "MATCH (n) RETURN n",
      "CREATE (n:Label)",
      "CREATE INDEX people FOR (n:Person) ON n.name",
      "DROP CONSTRAINT constr IF EXISTS",
      "SHOW DATABASE foo",
      "CREATE USER username SET PASSWORD 'secret'",
      "SHOW ROLES",
      "GRANT ACCESS ON DATABASE foo TO role",
      "SHOW PRIVILEGES AS COMMANDS"
    ).foreach(t => {
      withClue(t) { JavaCCParser.shouldFallback(t) shouldBe false }
    })
  }

}
