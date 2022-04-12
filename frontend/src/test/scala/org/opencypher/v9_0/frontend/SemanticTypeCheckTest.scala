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
package org.opencypher.v9_0.frontend

import org.opencypher.v9_0.frontend.helpers.ErrorCollectingContext
import org.opencypher.v9_0.frontend.helpers.NoPlannerName
import org.opencypher.v9_0.frontend.phases.InitialState
import org.opencypher.v9_0.frontend.phases.OpenCypherJavaCCParsing
import org.opencypher.v9_0.frontend.phases.PatternExpressionInNonExistenceCheck
import org.opencypher.v9_0.frontend.phases.PreparatoryRewriting
import org.opencypher.v9_0.frontend.phases.SemanticAnalysis
import org.opencypher.v9_0.frontend.phases.SemanticTypeCheck
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class SemanticTypeCheckTest extends CypherFunSuite {

  private val pipeline = OpenCypherJavaCCParsing andThen
    PreparatoryRewriting andThen
    SemanticAnalysis(warn = false) andThen
    SemanticTypeCheck

  private val expectedErrorMessage = PatternExpressionInNonExistenceCheck.errorMessage

  test("should fail if pattern expression is used wherever we don't expect a boolean value") {
    val queries = Seq(
      "MATCH (a) RETURN (a)--()",
      "MATCH (a) WHERE ANY (x IN (a)--() WHERE 1=1) RETURN a",
    )

    queries.foreach { query =>
      withClue(s"Failing query: $query") {
        runPipeline(query).errors.map(_.msg) should contain(expectedErrorMessage)
      }
    }
  }

  test("should not fail if pattern expression is used where we expect a boolean value") {
    val queries = Seq(
      "MATCH (a)--() RETURN a",
      "RETURN NOT ()--()",
      "MATCH (a) WHERE (a)--() RETURN a",
      "RETURN ()--() OR ()--()--()",
      "MATCH (a) RETURN [p=(a)--() | p]",
      "RETURN NOT exists(()--())",
      "MATCH (a) WHERE exists((a)--()) RETURN a",
      """
        |MATCH (actor:Actor)
        |RETURN actor,
        |  CASE
        |    WHEN (actor)-[:WON]->(:Oscar) THEN 'Oscar winner'
        |    WHEN (actor)-[:WON]->(:GoldenGlobe) THEN 'Golden Globe winner'
        |    ELSE 'None'
        |  END AS accolade
        |""".stripMargin
      ,
      """
        |MATCH (movie:Movie)<-[:ACTED_IN]-(actor:Actor)
        |WITH movie, collect(actor) AS cast
        |WHERE ANY(actor IN cast WHERE (actor)-[:WON]->(:Award))
        |RETURN movie
        |""".stripMargin
    )

    queries.foreach { query =>
      withClue(s"Failing query: $query") {
        runPipeline(query).errors.size shouldBe 0
      }
    }
  }

  private def runPipeline(query: String): ErrorCollectingContext = {
    val startState = InitialState(query, None, NoPlannerName, new AnonymousVariableNameGenerator)
    val context = new ErrorCollectingContext()
    pipeline.transform(startState, context)

    context
  }
}
