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
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.OpenCypherExceptionFactory.SyntaxException
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.TestName

class NodeLabelExpressionsJavaCcParserTest extends CypherFunSuite with TestName with AstConstructionTestSupport {

  test("MATCH (n)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        None,
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq(labelName("A")),
        None,
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A $param)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq(labelName("A")),
        None,
        Some(parameter("param", CTAny)),
        None
      )(pos)
    )
  }

  test("MATCH (n:A&B)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelConjunction(
            labelAtom("A"),
            labelAtom("B")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A&B|C)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelDisjunction(
            labelConjunction(
              labelAtom("A"),
              labelAtom("B")
            ),
            labelAtom("C")
          ),
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A|B&C)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelDisjunction(
            labelAtom("A"),
            labelConjunction(
              labelAtom("B"),
              labelAtom("C"),
            ),
          ),
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:!(A))") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelNegation(
            labelAtom("A"),
          ),
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (:A&B)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        None,
        Seq.empty,
        Some(
          labelConjunction(
            labelAtom("A"),
            labelAtom("B")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A|B)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelDisjunction(
            labelAtom("A"),
            labelAtom("B")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:!A)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelNegation(
            labelAtom("A")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A&B&C)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelConjunction(
            labelConjunction(
              labelAtom("A"),
              labelAtom("B"),
            ),
            labelAtom("C")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:!A&B)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelConjunction(
            labelNegation(labelAtom("A")),
            labelAtom("B"),
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A&(B&C))") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelConjunction(
            labelAtom("A"),
            labelConjunction(
              labelAtom("B"),
              labelAtom("C"),
            )
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:!(A&B))") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelNegation(
            labelConjunction(
              labelAtom("A"),
              labelAtom("B"),
            )
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:(A&B)|C)") {
    parseNodePatterns(testName) shouldBe Seq(
      NodePattern(
        Some(varFor("n")),
        Seq.empty,
        Some(
          labelDisjunction(
            labelConjunction(
              labelAtom("A"),
              labelAtom("B"),
            ),
            labelAtom("C")
          )
        ),
        None,
        None
      )(pos)
    )
  }

  test("MATCH (n:A&:B)") {
    val errorMessage = intercept[SyntaxException] (
      parseNodePatterns(testName)
    ).getMessage

    errorMessage should include("Invalid input ':'")
    errorMessage should include("column 12")
  }

  test("MATCH (n:A|:B)") {
    val errorMessage = intercept[SyntaxException] (
      parseNodePatterns(testName)
    ).getMessage

    errorMessage should include("Invalid input ':'")
    errorMessage should include("column 12")
  }

  test("MATCH (n:A|B&(:C)") {
    val errorMessage = intercept[SyntaxException] (
      parseNodePatterns(testName)
    ).getMessage

    errorMessage should include("Invalid input ':'")
    errorMessage should include("column 15")
  }

  test("MATCH (n:A:B&C)") {
    val errorMessage = intercept[SyntaxException] (
      parseNodePatterns(testName)
    ).getMessage

    errorMessage should include("Invalid input '&'")
    errorMessage should include("column 13")
  }

  private val exceptionFactory = OpenCypherExceptionFactory(None)

  private def parseNodePatterns(query: String): Seq[NodePattern] = {
    val ast = JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    ast.findAllByClass[NodePattern]
  }
}
