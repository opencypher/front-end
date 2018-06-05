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
package org.opencypher.v9_0.parser

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.{AstConstructionTestSupport, QualifiedGraphName, ReturnGraph}
import org.parboiled.scala.Rule1

class CatalogDDLParserTest
  extends ParserAstTest[ast.Statement] with Statement with AstConstructionTestSupport {

  implicit val parser: Rule1[ast.Statement] = Statement

  val singleQuery = ast.SingleQuery(Seq(ast.ConstructGraph()(pos)))(pos)
  private val returnGraph: ReturnGraph = ast.ReturnGraph(None)(pos)

  test("CREATE GRAPH foo.bar { RETURN GRAPH }") {
    val query = ast.SingleQuery(Seq(returnGraph))(pos)
    val graphName = ast.QualifiedGraphName("foo", List("bar"))

    yields(ast.CreateGraph(graphName, query))
  }

  test("CREATE GRAPH foo.bar { FROM GRAPH foo RETURN GRAPH UNION ALL FROM GRAPH bar RETURN GRAPH }") {
    val useGraph1 = ast.FromGraph(ast.QualifiedGraphName("foo"))(pos)
    val useGraph2 = ast.FromGraph(ast.QualifiedGraphName("bar"))(pos)
    val lhs = ast.SingleQuery(Seq(useGraph1, returnGraph))(pos)
    val rhs = ast.SingleQuery(Seq(useGraph2, returnGraph))(pos)
    val union = ast.UnionAll(lhs, rhs)(pos)
    val graphName = ast.QualifiedGraphName("foo", List("bar"))

    yields(ast.CreateGraph(graphName, union))
  }

  test("CREATE GRAPH foo.bar { FROM GRAPH foo RETURN GRAPH UNION FROM GRAPH bar RETURN GRAPH }") {
    val useGraph1 = ast.FromGraph(ast.QualifiedGraphName("foo"))(pos)
    val useGraph2 = ast.FromGraph(ast.QualifiedGraphName("bar"))(pos)
    val lhs = ast.SingleQuery(Seq(useGraph1, returnGraph))(pos)
    val rhs = ast.SingleQuery(Seq(useGraph2, returnGraph))(pos)
    val union = ast.UnionDistinct(lhs, rhs)(pos)
    val graphName = ast.QualifiedGraphName("foo", List("bar"))

    yields(ast.CreateGraph(graphName, union))
  }

  test("CREATE GRAPH foo.bar { CONSTRUCT }") {
    val graphName = ast.QualifiedGraphName("foo", List("bar"))

    yields(ast.CreateGraph(graphName, singleQuery))
  }

  // missing graph name
  test("CREATE GRAPH { RETURN GRAPH }") {
    failsToParse
  }

  test("CREATE GRAPH `foo.bar.baz.baz` { CONSTRUCT }"){
    yields(ast.CreateGraph(
      new QualifiedGraphName(List("foo.bar.baz.baz")),
      singleQuery
    ))
  }

  test("CREATE GRAPH `foo.bar`.baz { CONSTRUCT }"){
    yields(ast.CreateGraph(
      new QualifiedGraphName(List("foo.bar", "baz")),
      singleQuery
    ))
  }


  test("CREATE GRAPH foo.`bar.baz` { CONSTRUCT }"){
    yields(ast.CreateGraph(
      new QualifiedGraphName(List("foo", "bar.baz")),
      singleQuery
    ))
  }

  test("CREATE GRAPH `foo.bar`.`baz.baz` { CONSTRUCT }"){
    yields(ast.CreateGraph(
      new QualifiedGraphName(List("foo.bar", "baz.baz")),
      singleQuery
    ))
  }

  // missing graph name
  test("DELETE GRAPH union") {
    val graphName = ast.QualifiedGraphName("union")

    yields(ast.DeleteGraph(graphName))
  }

  // missing graph name; doesn't fail because it's a valid query if GRAPH is a variable
  ignore("DELETE GRAPH") {
    failsToParse
  }
}
