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

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.prettifier.ExpressionStringifier
import org.opencypher.v9_0.ast.prettifier.Prettifier
import org.opencypher.v9_0.parser.JavaCCParser
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.WindowsStringSafe

class JavaCCPrettifierIT extends CypherFunSuite {
  private implicit val windowsSafe: WindowsStringSafe.type = WindowsStringSafe

  val prettifier: Prettifier = Prettifier(ExpressionStringifier())

  val parboiledPrettifier = new ParboiledPrettifierIT()
  val tests: Seq[(String, String)] = parboiledPrettifier.tests
  val javaCcOnlyTests: Seq[(String, String)] = Seq[(String, String)]()

  (tests ++ javaCcOnlyTests) foreach {
    case (inputString, expected) =>
      test(inputString) {
        try {
          val parsingResults: Statement = JavaCCParser.parse(inputString, OpenCypherExceptionFactory(None))
          val str = prettifier.asString(parsingResults)
          str should equal(expected)
        } catch {
          case _ if JavaCCParser.shouldFallBack(inputString) =>
          // Should not succeed in new parser so this is correct
        }
      }
  }

  test("Ensure tests don't include fallback strings") {
    // Sanity check
    (parboiledPrettifier.queryTests() ++ javaCcOnlyTests) foreach {
      case (inputString, _) if JavaCCParser.shouldFallBack(inputString) =>
        fail(s"should not use fallback strings in tests: $inputString")
      case _                                                            =>
    }
  }
}
