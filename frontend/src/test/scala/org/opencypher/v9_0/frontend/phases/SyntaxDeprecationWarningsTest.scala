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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.{AstConstructionTestSupport, Statement}
import org.opencypher.v9_0.frontend.helpers.{TestContext, TestState}
import org.opencypher.v9_0.parser.ParserFixture.parser
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.{DeprecatedFunctionNotification, InputPosition}

class SyntaxDeprecationWarningsTest extends CypherFunSuite with  AstConstructionTestSupport {

  test("should warn about deprecated functions") {
    check("RETURN toInt(2.71828)") should equal(Set(DeprecatedFunctionNotification(InputPosition(7, 1, 8), "toInt", "toInteger")))
    check("RETURN upper('hello')") should equal(Set(DeprecatedFunctionNotification(InputPosition(7, 1, 8), "upper", "toUpper")))
    check("RETURN rels($r)") should equal(Set(DeprecatedFunctionNotification(InputPosition(7, 1, 8), "rels", "relationships")))
    check("RETURN timestamp()") shouldBe empty
  }

  private def check(query: String) = {
    val logger = new RecordingNotificationLogger()
    SyntaxDeprecationWarnings(Deprecations.V1).visit(TestState(Some(parse(query))), TestContext(logger))
    logger.notifications
  }

  private def parse(queryText: String): Statement = parser.parse(queryText.replace("\r\n", "\n"))

}
