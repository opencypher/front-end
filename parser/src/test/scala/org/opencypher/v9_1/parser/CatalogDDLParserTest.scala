package org.opencypher.v9_1.parser

import org.opencypher.v9_1.ast
import org.opencypher.v9_1.ast.{AstConstructionTestSupport, ReturnGraph}
import org.parboiled.scala.Rule1

class CatalogDDLParserTest
  extends ParserAstTest[ast.Statement] with Statement with AstConstructionTestSupport {

  implicit val parser: Rule1[ast.Statement] = Statement

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

    yields(ast.CreateGraph(graphName, ast.SingleQuery(Seq(ast.ConstructGraph()(pos)))(pos)))
  }

  // missing graph name
  test("CREATE GRAPH { RETURN GRAPH }") {
    failsToParse
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
