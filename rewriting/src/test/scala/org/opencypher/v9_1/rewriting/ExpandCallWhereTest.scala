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
package org.opencypher.v9_1.rewriting

import org.opencypher.v9_1.ast.AstConstructionTestSupport
import org.opencypher.v9_1.rewriting.rewriters.expandCallWhere
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite


class ExpandCallWhereTest extends CypherFunSuite with RewriteTest with AstConstructionTestSupport {

  override val rewriterUnderTest = expandCallWhere

  test("rewrite call yield where") {
    assertRewrite("CALL foo() YIELD a, b WHERE a > b RETURN *", "CALL foo() YIELD a, b WITH * WHERE a > b RETURN *")
  }

  test("does not rewrite") {
    assertIsNotRewritten("CALL foo() YIELD a, b WITH * WHERE a > b RETURN *")
    assertIsNotRewritten("CALL foo() YIELD a, b RETURN *")
  }
}
