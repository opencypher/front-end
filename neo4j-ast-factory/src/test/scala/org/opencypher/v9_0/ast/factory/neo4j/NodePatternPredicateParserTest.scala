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

import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.TestName

class NodePatternPredicateParserTest extends CypherFunSuite with TestName with JavaccParserAstTestBase[NodePattern] {

  implicit val parser: JavaccRule[NodePattern] = JavaccRule.NodePattern

  test("MATCH (n WHERE n.prop > 123)") {
    val expected = Seq(
      NodePattern(
        Some(varFor("n")),
        None,
        None,
        Some(greaterThan(prop("n", "prop"), literalInt(123)))
      )(pos)
    )
    parseNodePatterns(testName) shouldBe expected
    parseNodePatterns(testName.replaceAllLiterally("WHERE", "wHeRe")) shouldBe expected
  }

  test("(n:A:B:C {prop: 42} WHERE n.otherProp < 123)") {
    givesIncludingPositions {
      NodePattern(
        variable = Some(varFor("n", (1, 2, 1))),
        labelExpression = Some(
          labelColonConjunction(
            labelColonConjunction(
              labelLeaf("A", (1, 4, 3)),
              labelLeaf("B", (1, 6, 5))
            ),
            labelLeaf("C", (1, 8, 7))
          )
        ),
        properties = Some(mapOf("prop" -> literalInt(42))),
        predicate = Some(lessThan(prop("n", "otherProp"), literalInt(123)))
      )((1, 1, 0))
    }
  }

  test("MATCH (WHERE WHERE WHERE.prop > 123)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("WHERE")),
        None,
        None,
        Some(greaterThan(prop("WHERE", "prop"), literalInt(123)))
      )(pos)
    )
  }

  test("RETURN [(n:A WHERE n.prop >= 123)-->(end WHERE end.prop < 42) | n]") {
    parseNodePatterns(testName).toSet shouldBe Set(
      NodePattern(
        Some(varFor("n")),
        Some(labelLeaf("A")),
        None,
        Some(greaterThanOrEqual(prop("n", "prop"), literalInt(123)))
      )(pos),
      NodePattern(
        Some(varFor("end")),
        None,
        None,
        Some(lessThan(prop("end", "prop"), literalInt(42)))
      )(pos)
    )
  }

  test("RETURN exists((n {prop: 'test'} WHERE n.otherProp = 123)-->(end WHERE end.prop = 42)) AS result") {
    parseNodePatterns(testName).toSet shouldBe Set(
      NodePattern(
        Some(varFor("n")),
        None,
        Some(mapOf("prop" -> literalString("test"))),
        Some(equals(prop("n", "otherProp"), literalInt(123)))
      )(pos),
      NodePattern(
        Some(varFor("end")),
        None,
        None,
        Some(equals(prop("end", "prop"), literalInt(42)))
      )(pos)
    )
  }

  test("MATCH (WHERE {prop: 123})") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("WHERE")),
        None,
        Some(mapOf("prop" -> literal(123))),
        None
      )(pos)
    )
  }

  test("MATCH (:Label {prop: 123} WHERE 2 > 1)") {
    val e = the[Exception] thrownBy parseNodePatterns(testName)
    e.getMessage should include("Invalid input 'WHERE'")
  }

  private val exceptionFactory = OpenCypherExceptionFactory(None)

  private def parseNodePatterns(query: String): Seq[NodePattern] = {
    val ast = JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    ast.folder.findAllByClass[NodePattern]
  }
}
