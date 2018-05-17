/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_1.ast.semantics

import org.opencypher.v9_0.expressions.{DummyExpression, ListComprehension}
import org.opencypher.v9_0.util.symbols._

class FilteringExpressionTest extends SemanticFunSuite {

  test("shouldSemanticCheckPredicateInStateContainingTypedVariable") {
    val expression = DummyExpression(CTList(CTNode) | CTBoolean | CTList(CTString), pos)

    val error = SemanticError("dummy error", pos)
    val predicate = CustomExpression(
      (ctx, self) => s => {
        s.symbolTypes("x") should equal(CTNode | CTString)
        SemanticCheckResult.error(s, error)
      }
    )

    val filter = ListComprehension(variable("x"), expression, Some(predicate), None)(pos)
    val result = SemanticExpressionCheck.simple(filter)(SemanticState.clean)
    result.errors should equal(Seq(error))
    result.state.symbol("x") should equal(None)
  }
}
