/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_1.rewriting

import org.opencypher.v9_1.rewriting.rewriters.nameGraphOfPatternElements
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite

class NameGraphOfPatternElementTest extends CypherFunSuite {

  import org.opencypher.v9_1.parser.ParserFixture._

  test("name all node patterns in GRAPH OF") {
    val original = parser.parse("RETURN GRAPH OF (n)-[r:Foo]->() RETURN n")
    val expected = parser.parse("RETURN GRAPH OF (n)-[r:Foo]->(`  UNNAMED30`) RETURN n")

    val result = original.rewrite(nameGraphOfPatternElements)
    assert(result === expected)
  }

  test("name all relationship patterns in GRAPH OF") {
    val original = parser.parse("WITH 1 AS a GRAPH OF (n)-[:Foo]->(m) WHERE (n)-[:Bar]->(m) RETURN n")
    val expected = parser.parse("WITH 1 AS a GRAPH OF (n)-[`  UNNAMED25`:Foo]->(m) WHERE (n)-[:Bar]->(m) RETURN n")

    val result = original.rewrite(nameGraphOfPatternElements)
    assert(result === expected)
  }
}
