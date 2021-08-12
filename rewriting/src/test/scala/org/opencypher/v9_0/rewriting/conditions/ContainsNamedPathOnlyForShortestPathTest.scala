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
package org.opencypher.v9_0.rewriting.conditions

import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Query
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NamedPatternPart
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class ContainsNamedPathOnlyForShortestPathTest extends CypherFunSuite with AstConstructionTestSupport {
  private val condition: Any => Seq[String] = containsNamedPathOnlyForShortestPath

  test("happy when we have no named paths") {
    val ast = Query(None, SingleQuery(Seq(
      Match(optional = false, Pattern(Seq(EveryPath(NodePattern(Some(varFor("n")), Seq.empty, None, None)(pos))))(pos), Seq.empty, None)(pos),
      Return(distinct = false, ReturnItems(includeExisting = false, Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos)))(pos), None, None, None)(pos)
    ))(pos))(pos)

    condition(ast) shouldBe empty
  }

  test("unhappy when we have a named path") {
    val namedPattern: NamedPatternPart = NamedPatternPart(varFor("p"), EveryPath(NodePattern(Some(varFor("n")), Seq.empty, None, None)(pos)))(pos)
    val ast = Query(None, SingleQuery(Seq(
      Match(optional = false, Pattern(Seq(namedPattern))(pos), Seq.empty, None)(pos),
      Return(distinct = false, ReturnItems(includeExisting = false, Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos)))(pos), None, None, None)(pos)
    ))(pos))(pos)

    condition(ast) should equal(Seq(s"Expected none but found $namedPattern at position $pos"))
  }

  test("should allow named path for shortest path") {
    val ast = Query(None, SingleQuery(Seq(
      Match(optional = false, Pattern(Seq(NamedPatternPart(varFor("p"), ShortestPaths(NodePattern(Some(varFor("n")), Seq.empty, None, None)(pos), single = true)(pos))(pos)))(pos), Seq.empty, None)(pos),
      Return(distinct = false, ReturnItems(includeExisting = false, Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos)))(pos), None, None, None)(pos)
    ))(pos))(pos)

    condition(ast) shouldBe empty
  }
}
