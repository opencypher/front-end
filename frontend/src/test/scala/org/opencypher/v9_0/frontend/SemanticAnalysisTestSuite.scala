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

import org.opencypher.v9_0.ast.semantics.SemanticErrorDef
import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.frontend.helpers.ErrorCollectingContext
import org.opencypher.v9_0.frontend.helpers.NoPlannerName
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.frontend.phases.InitialState
import org.opencypher.v9_0.frontend.phases.OpenCypherJavaCCParsing
import org.opencypher.v9_0.frontend.phases.PreparatoryRewriting
import org.opencypher.v9_0.frontend.phases.SemanticAnalysis
import org.opencypher.v9_0.frontend.phases.Transformer
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.test_helpers.TestName

case class SemanticAnalysisResult(context: ErrorCollectingContext, state: BaseState) {
  def errors: Seq[SemanticErrorDef] = context.errors

  def errorMessages: Seq[String] = errors.map(_.msg)
}

trait SemanticAnalysisTestSuite {

  type Pipeline = Transformer[BaseContext, BaseState, BaseState]

  def runSemanticAnalysisWithPipelineAndState(pipeline: Pipeline, initialState: BaseState): SemanticAnalysisResult = {
    val context = new ErrorCollectingContext()
    val state = pipeline.transform(initialState, context)
    SemanticAnalysisResult(context, state)
  }

  def initialStateWithQuery(query: String): InitialState =
    InitialState(query, None, NoPlannerName, new AnonymousVariableNameGenerator)

  def runSemanticAnalysisWithPipeline(pipeline: Pipeline, query: String): SemanticAnalysisResult =
    runSemanticAnalysisWithPipelineAndState(pipeline, initialStateWithQuery(query))

  // This test invokes SemanticAnalysis twice because that's what the production pipeline does
  def pipelineWithSemanticFeatures(semanticFeatures: SemanticFeature*): Pipeline =
    OpenCypherJavaCCParsing andThen
      PreparatoryRewriting andThen
      SemanticAnalysis(warn = true, semanticFeatures: _*) andThen
      SemanticAnalysis(warn = false, semanticFeatures: _*)

  def runSemanticAnalysisWithSemanticFeatures(
    semanticFeatures: Seq[SemanticFeature],
    query: String
  ): SemanticAnalysisResult =
    runSemanticAnalysisWithPipeline(pipelineWithSemanticFeatures(semanticFeatures: _*), query)

  def runSemanticAnalysis(query: String): SemanticAnalysisResult =
    runSemanticAnalysisWithSemanticFeatures(Seq.empty, query)

}

trait SemanticAnalysisTestSuiteWithDefaultQuery extends SemanticAnalysisTestSuite {

  def defaultQuery: String

  def runSemanticAnalysis(): SemanticAnalysisResult = runSemanticAnalysis(defaultQuery)

}

trait NameBasedSemanticAnalysisTestSuite extends SemanticAnalysisTestSuiteWithDefaultQuery with TestName {

  override def defaultQuery: String = testName

}
