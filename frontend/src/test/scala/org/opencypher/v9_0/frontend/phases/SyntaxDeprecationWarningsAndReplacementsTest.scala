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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.factory.neo4j.JavaCCParser
import org.opencypher.v9_0.frontend.PlannerName
import org.opencypher.v9_0.frontend.helpers.TestContext
import org.opencypher.v9_0.rewriting.Deprecations.semanticallyDeprecatedFeaturesIn4_X
import org.opencypher.v9_0.rewriting.Deprecations.syntacticallyDeprecatedFeaturesIn4_X
import org.opencypher.v9_0.rewriting.conditions.noReferenceEqualityAmongVariables
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.RecordingNotificationLogger
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class SyntaxDeprecationWarningsAndReplacementsTest extends CypherFunSuite {

  // Add specific tests for syntax deprecation warning and replacements here.

//  test("placeholder query") {
//    check("MATCH (n) RETURN *") should equal(Set.empty)
//  }

  private val plannerName = new PlannerName {
    override def name: String = "fake"
    override def toTextOutput: String = "fake"
    override def version: String = "fake"
  }

  private def check(query: String) = {
    val logger = new RecordingNotificationLogger()
    val statement = parse(query)
    val initialState =
      InitialState(query, None, plannerName, new AnonymousVariableNameGenerator, maybeStatement = Some(statement))

    val pipeline =
      SyntaxDeprecationWarningsAndReplacements(syntacticallyDeprecatedFeaturesIn4_X) andThen
        PreparatoryRewriting andThen
        SemanticAnalysis(warn = true) andThen
        SyntaxDeprecationWarningsAndReplacements(semanticallyDeprecatedFeaturesIn4_X)

    val transformedState = pipeline.transform(initialState, TestContext(logger))

    // Check that we didn't introduce any duplicate AST nodes
    noReferenceEqualityAmongVariables(transformedState.statement()) shouldBe empty

    logger.notifications
  }

  private def parse(queryText: String): Statement = JavaCCParser.parse(
    queryText.replace("\r\n", "\n"),
    OpenCypherExceptionFactory(None),
    new AnonymousVariableNameGenerator()
  )

}
