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

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SemanticDirection.OUTGOING
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.TestName

class RelationshipPatternPredicateParserTest extends CypherFunSuite with TestName with AstConstructionTestSupport {

  test("MATCH (n)-[r WHERE r.prop > 123]->()") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("r")),
        None,
        None,
        None,
        Some(greaterThan(prop("r", "prop"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  test("MATCH (n)-[r:Foo|Bar*1..5 {prop: 'test'} WHERE r.otherProp > 123]->()") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("r")),
        Some(labelDisjunction(labelRelTypeLeaf("Foo"), labelRelTypeLeaf("Bar"))),
        Some(Some(range(Some(1), Some(5)))),
        Some(mapOf("prop" -> literalString("test"))),
        Some(greaterThan(prop("r", "otherProp"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  test("MATCH ()-[r:R|S|T {prop: 42} WHERE r.otherProp > 123]->()") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("r")),
        Some(labelDisjunctions(Seq(labelRelTypeLeaf("R"), labelRelTypeLeaf("S"), labelRelTypeLeaf("T")))),
        None,
        Some(mapOf("prop" -> literal(42))),
        Some(greaterThan(prop("r", "otherProp"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  test("MATCH ()-[WHERE WHERE WHERE.prop > 123]->()") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("WHERE")),
        None,
        None,
        None,
        Some(greaterThan(prop("WHERE", "prop"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  test("RETURN [()-[r:R WHERE r.prop > 123]->() | r]") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("r")),
        Some(labelRelTypeLeaf("R")),
        None,
        None,
        Some(greaterThan(prop("r", "prop"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  test("RETURN exists(()-[r {prop: 'test'} WHERE r.otherProp = 123]->()) AS result") {
    parseRelationshipPatterns(testName) shouldBe Seq(
      RelationshipPattern(
        Some(varFor("r")),
        None,
        None,
        Some(mapOf("prop" -> literal("test"))),
        Some(equals(prop("r", "otherProp"), literalInt(123))),
        OUTGOING
      )(pos)
    )
  }

  private val exceptionFactory = OpenCypherExceptionFactory(None)

  private def parseRelationshipPatterns(query: String): Seq[RelationshipPattern] = {
    val ast = JavaCCParser.parse(query, exceptionFactory)
    ast.folder.findAllByClass[RelationshipPattern]
  }
}
