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

import org.opencypher.v9_1.ast._
import org.opencypher.v9_1.expressions.{SignedDecimalIntegerLiteral, StringLiteral}
import org.opencypher.v9_1.rewriting.rewriters.normalizeGraphReturnItems
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite

class normalizeGraphReturnItemsTest extends CypherFunSuite with AstConstructionTestSupport {

  import org.opencypher.v9_1.parser.ParserFixture._

  test("do not rename source graph") {
    val original = parser.parse("FROM GRAPH foo AT 'url' WITH * SOURCE GRAPH RETURN 1")

    val result = original.rewrite(normalizeGraphReturnItems)
    assert(result === original)
  }

  test("do not rename TARGET graph") {
    val original = parser.parse("INTO GRAPH foo AT 'url' WITH * TARGET GRAPH RETURN 1")

    val result = original.rewrite(normalizeGraphReturnItems)
    assert(result === original)
  }

  test("name named graphs") {
    val original = parser.parse("FROM GRAPH foo RETURN 1")
    val expected = parser.parse("FROM GRAPH foo AS foo RETURN 1")

    val result = original.rewrite(normalizeGraphReturnItems)
    assert(result === expected)
  }

  test("name load graph") {
    // need to spell out ast here as there is no syntax for specifying generated symbols
    val original = parser.parse("FROM GRAPH AT 'url' RETURN 1")
    val expected = Query(None,
      SingleQuery(List(
        With(distinct = false,
          ReturnItems(includeExisting = true, List())(pos),
          GraphReturnItems(true,List(
            NewContextGraphs(
              GraphAtAs(
                GraphUrl(Right(StringLiteral("url")(pos)))(pos),
                Some(varFor("  FRESHID21")),
                generated = true)(pos),None)(pos)))(pos),None,None,None,None)(pos),
        Return(distinct = false,
          ReturnItems(includeExisting = false,List(
            UnaliasedReturnItem(SignedDecimalIntegerLiteral("1")(pos), "1")(pos)))(pos),None,None,None,None,Set())(pos)))(pos))(pos)

    val result = original.rewrite(normalizeGraphReturnItems)
    assert(result === expected)
  }
}
