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

import org.opencypher.v9_0.parser.javacc.Cypher
import org.opencypher.v9_0.parser.javacc.CypherCharStream
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.OpenCypherExceptionFactory.SyntaxException
import org.scalatest.Assertion
import org.scalatest.Assertions
import org.scalatest.Matchers

import scala.util.Failure
import scala.util.Success
import scala.util.Try

abstract class ParserComparisonTestBase() extends Assertions with Matchers {

  protected def assertSyntaxException(query: String): Unit = {
    val exceptionFactory = new OpenCypherExceptionFactory(None)
    val parser = new Cypher(new Neo4jASTFactory(query), new Neo4jASTExceptionFactory(exceptionFactory), new CypherCharStream(query))

    an[OpenCypherExceptionFactory.SyntaxException] should be thrownBy {
      parser.Statements()
    }
  }

  /**
   * Tests that the parboiled and JavaCC parsers produce the same AST and error positions.
   */
  protected def assertSameAST(query: String): Assertion = {
    withClue(query+System.lineSeparator()) {
      val parboiledParser = new org.opencypher.v9_0.parser.CypherParser()
      val exceptionFactory = new OpenCypherExceptionFactory(None)
      val parboiledAST = Try(parboiledParser.parse(query, exceptionFactory, None))

      val parser = new Cypher(new Neo4jASTFactory(query), new Neo4jASTExceptionFactory(exceptionFactory), new CypherCharStream(query))
      val javaccAST = Try(parser.Statements().get(0))

      (javaccAST, parboiledAST) match {
        case (Failure(javaccEx: SyntaxException), Failure(parboiledEx: SyntaxException)) =>
          withClue(Seq(javaccEx, parboiledEx).mkString("", "\n\n", "\n\n")) {
            javaccEx.pos shouldBe parboiledEx.pos
          }
        case (Failure(javaccEx), Success(_)) =>
          throw javaccEx
        case _ =>
          javaccAST shouldBe parboiledAST
      }
    }
  }
}
