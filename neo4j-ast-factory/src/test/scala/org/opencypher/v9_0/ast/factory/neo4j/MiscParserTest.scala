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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.Remove
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.SetClause
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Range
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.util.ASTNode

class MiscParserTest extends JavaccParserAstTestBase[ASTNode] {

  test("RETURN 1 AS x //l33t comment") {
    implicit val parser: JavaccRule[Statement] = JavaccRule.Statement
    gives {
      query(returnLit(1 -> "x"))
    }
  }

  test("keywords are allowed names") {
    implicit val parser: JavaccRule[Statement] = JavaccRule.Statement
    val keywords =
      Seq("TRUE", "FALSE", "NULL", "RETURN", "CREATE", "DELETE", "SET", "REMOVE", "DETACH", "MATCH", "WITH",
        "UNWIND", "USE", "GRAPH", "CALL", "YIELD", "LOAD", "CSV", "PERIODIC", "COMMIT",
        "HEADERS", "FROM", "FIELDTERMINATOR", "FOREACH", "WHERE", "DISTINCT", "MERGE",
        "OPTIONAL", "USING", "ORDER", "BY", "ASC", "ASCENDING", "DESC", "DESCENDING",
        "SKIP", "LIMIT", "UNION", "DROP", "INDEX", "SEEK", "SCAN", "JOIN", "CONSTRAINT",
        "ASSERT", "IS", "NODE", "KEY", "UNIQUE", "ON", "AS", "OR", "XOR", "AND", "NOT",
        "STARTS", "ENDS", "CONTAINS", "IN", "count", "FILTER", "EXTRACT", "REDUCE", "ROW", "ROWS",
        "EXISTS", "ALL", "ANY", "NONE", "SINGLE", "CASE", "ELSE", "WHEN", "THEN", "END",
        "shortestPath", "allShortestPaths")

    for (keyword <- keywords) {
      parsing(s"WITH $$$keyword AS x RETURN x AS $keyword")
    }
  }

  test("should allow chained map access in SET/REMOVE") {
    implicit val parser: JavaccRule[Clause] = JavaccRule.Clause

    val chainedProperties = prop(prop(varFor("map"), "node"),"property")

    parsing("SET map.node.property = 123") shouldGive
      SetClause(Seq(
        SetPropertyItem(chainedProperties, literal(123))(pos)
      ))_

    parsing("REMOVE map.node.property") shouldGive
      Remove(Seq(
        RemovePropertyItem(chainedProperties)
      ))_
  }

  test("should allow True and False as label name") {
    implicit val parser: JavaccRule[NodePattern] = JavaccRule.NodePattern

    parsing("(:True)") shouldGive NodePattern(None, Some(labelAtom("True")), None, None)_
    parsing("(:False)") shouldGive NodePattern(None, Some(labelAtom("False")), None, None)_

    parsing("(t:True)") shouldGive nodePat(name = Some("t"), labelExpression = Some(labelAtom("True")))
    parsing("(f:False)") shouldGive nodePat(name = Some("f"), labelExpression = Some(labelAtom("False")))
  }

  test("-[:Person*1..2]-") {
    implicit val parser: JavaccRule[RelationshipPattern] = JavaccRule.RelationshipPattern
    yields {
      RelationshipPattern(None, List(relTypeName("Person")),
        Some(Some(
          Range(
            Some(literalUnsignedInt(1)),
            Some(literalUnsignedInt(2)))(pos)
        )),
        None,
        None,
        SemanticDirection.BOTH
      )
    }
  }

  test("should not parse list literal as pattern comprehension") {
    implicit val parser: JavaccRule[Expression] = JavaccRule.Expression

    val listLiterals = Seq(
      "[x = '1']",
      "[x = ()--()]",
      "[x = ()--()--()--()--()--()--()--()--()--()--()]",
    )
    for (l <- listLiterals) withClue(l) {
      parsing(l) shouldVerify (_ shouldBe a[ListLiteral])
    }
  }

  test("should not parse pattern comprehensions with single nodes") {
    implicit val parser: JavaccRule[Expression] = JavaccRule.PatternComprehension
    assertFails("[p = (x) | p]")
  }

  test("should handle escaping in string literals") {
    implicit val parser: JavaccRule[Expression] = JavaccRule.StringLiteral
    parsing("""'\\\''""") shouldGive literalString("""\'""")
  }
}
