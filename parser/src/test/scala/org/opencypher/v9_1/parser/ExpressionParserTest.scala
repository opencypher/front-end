package org.opencypher.v9_1.parser


import org.opencypher.v9_1.ast.AstConstructionTestSupport
import org.opencypher.v9_1.{expressions => exp}
import org.parboiled.scala.Rule1

import scala.language.implicitConversions

class ExpressionParserTest
  extends ParserAstTest[exp.Expression]
    with Expressions
    with AstConstructionTestSupport {

  implicit val parser: Rule1[exp.Expression] = Expression

  test("a ~ b") {
    yields(exp.Equivalent(varFor("a"), varFor("b")))
  }

  test("[] ~ []") {
    yields(exp.Equivalent(exp.ListLiteral(Seq.empty)(pos), exp.ListLiteral(Seq.empty)(pos)))
  }

}
