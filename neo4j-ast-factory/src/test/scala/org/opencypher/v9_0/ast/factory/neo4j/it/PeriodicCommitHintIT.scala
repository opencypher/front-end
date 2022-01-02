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
package org.opencypher.v9_0.ast.factory.neo4j.it

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.factory.neo4j.JavaccParserTestBase
import org.opencypher.v9_0.ast.factory.neo4j.JavaccRule
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.util.DummyPosition

class PeriodicCommitHintIT extends JavaccParserTestBase[ast.Query, ast.PeriodicCommitHint] {

  implicit val parserToTest: JavaccRule[ast.Query] = JavaccRule.fromQueryAndParser(
    transformQuery = q => s"$q LOAD CSV FROM '' AS line RETURN 1",
    runParser = _.PeriodicCommitQuery()
  )

  private val t = DummyPosition(0)

  test("tests") {
    parsing("USING PERIODIC COMMIT") shouldGive ast.PeriodicCommitHint(None)(t)
    parsing("USING PERIODIC COMMIT 300") shouldGive ast.PeriodicCommitHint(Some(UnsignedDecimalIntegerLiteral("300")(t)))(t)
  }

  override def convert(astNode: ast.Query): ast.PeriodicCommitHint = astNode.periodicCommitHint.get
}
