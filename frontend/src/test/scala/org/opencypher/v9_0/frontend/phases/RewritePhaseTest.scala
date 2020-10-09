/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
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

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.StatementHelper.RichStatement
import org.opencypher.v9_0.ast.prettifier.ExpressionStringifier
import org.opencypher.v9_0.ast.prettifier.Prettifier
import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.frontend.PlannerName
import org.opencypher.v9_0.frontend.helpers.TestContext
import org.opencypher.v9_0.parser.ParserFixture.parser
import org.opencypher.v9_0.rewriting.rewriters.SameNameNamer
import org.opencypher.v9_0.rewriting.rewriters.normalizeWithAndReturnClauses
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.devNullLogger
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

trait RewritePhaseTest {
  self: CypherFunSuite with AstConstructionTestSupport =>

  def rewriterPhaseUnderTest: Transformer[BaseContext, BaseState, BaseState]

  def rewriterPhaseForExpected: Transformer[BaseContext, BaseState, BaseState] =
    new Transformer[BaseContext, BaseState, BaseState] {
      override def transform(from: BaseState,
                             context: BaseContext): BaseState = from

      override def name: String = "do nothing"
    }

  val prettifier = Prettifier(ExpressionStringifier(_.asCanonicalStringVal))

  private val plannerName = new PlannerName {
    override def name: String = "fake"
    override def toTextOutput: String = "fake"
    override def version: String = "fake"
  }

  val astRewriter = new ASTRewriter(innerVariableNamer = SameNameNamer)

  def assertNotRewritten(from: String): Unit = assertRewritten(from, from)

  def assertRewritten(from: String, to: String): Unit = assertRewritten(from, to, List.empty)

  def assertRewritten(from: String, to: String, semanticTableExpressions: List[Expression], features: SemanticFeature*): Unit = {
    val fromOutState: BaseState = prepareFrom(from, features: _*)

    val toOutState = prepareFrom(to, features: _*)

    fromOutState.statement() should equal(toOutState.statement())
    semanticTableExpressions.foreach { e =>
      fromOutState.semanticTable().types.keys should contain(e)
    }
  }

  def assertRewritten(from: String, to: Statement, semanticTableExpressions: List[Expression], features: SemanticFeature*): Unit = {
    val fromOutState: BaseState = prepareFrom(from, features: _*)

    fromOutState.statement() should equal(to)
    semanticTableExpressions.foreach { e =>
      fromOutState.semanticTable().types.keys should contain(e)
    }
  }

  private def parseAndRewrite(queryText: String, features: SemanticFeature*): Statement = {
    val exceptionFactory = OpenCypherExceptionFactory(None)
    val parsedAst = parser.parse(queryText, exceptionFactory)
    val cleanedAst = parsedAst.endoRewrite(inSequence(normalizeWithAndReturnClauses(exceptionFactory, devNullLogger)))
    astRewriter.rewrite(cleanedAst, cleanedAst.semanticState(features: _*), Map.empty, exceptionFactory)
  }

 def prepareFrom(from: String, features: SemanticFeature*): BaseState = {
    val fromAst = parseAndRewrite(from, features: _*)
    val fromInState = SemanticAnalysis(warn = false, features: _*).process(InitialState(from, None, plannerName, maybeStatement = Some(fromAst)), TestContext())
    val fromOutState = rewriterPhaseUnderTest.transform(fromInState, ContextHelper.create())
    fromOutState
  }
}
