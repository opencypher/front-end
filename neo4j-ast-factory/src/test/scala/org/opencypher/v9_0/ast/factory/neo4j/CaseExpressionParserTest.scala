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

import org.opencypher.v9_0.expressions.CaseExpression
import org.opencypher.v9_0.expressions.Expression

class CaseExpressionParserTest extends JavaccParserAstTestBase[Expression] {
  implicit private val parser: JavaccRule[Expression] = JavaccRule.CaseExpression

  test("CASE WHEN (e) THEN e ELSE null END") {
    yields {
      CaseExpression(
        None,
        List(varFor("e") -> varFor("e")),
        Some(nullLiteral)
      )
    }
  }

  test("CASE when(e) WHEN (e) THEN e ELSE null END") {
    yields {
      CaseExpression(
        Some(function("when", varFor("e"))),
        List(varFor("e") -> varFor("e")),
        Some(nullLiteral)
      )
    }
  }

  test("CASE when(v1) + 1 WHEN THEN v2 ELSE null END") {
    failsToParse
  }
}
