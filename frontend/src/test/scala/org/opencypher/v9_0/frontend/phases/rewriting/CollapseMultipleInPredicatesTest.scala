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
package org.opencypher.v9_0.frontend.phases.rewriting

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.frontend.helpers.TestState
import org.opencypher.v9_0.frontend.phases.Monitors
import org.opencypher.v9_0.frontend.phases.collapseMultipleInPredicates
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.TestContext
import org.opencypher.v9_0.frontend.phases.rewriting.cnf.flattenBooleanOperators
import org.opencypher.v9_0.rewriting.RewriteTest
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class CollapseMultipleInPredicatesTest extends CypherFunSuite with RewriteTest {

  override def rewriterUnderTest: Rewriter =
    collapseMultipleInPredicates.instance(TestState(None), new TestContext(mock[Monitors]))

  test("should rewrite simple case") {
    assertRewrite(
      "MATCH (n) WHERE n.prop IN [1,2,3] OR n.prop IN [4,5,6] RETURN n.prop",
      "MATCH (n) WHERE n.prop IN [1,2,3,4,5,6] RETURN n.prop"
    )
  }

  test("should rewrite overlapping case") {
    assertRewrite(
      "MATCH (n) WHERE n.prop IN [1,2,3] OR n.prop IN [1,3,5] OR n.prop IN [4,5,6] RETURN n.prop",
      "MATCH (n) WHERE n.prop IN [1,2,3,5,4,6] RETURN n.prop"
    )
  }

  test("should rewrite interleaved case") {
    assertRewrite(
      "MATCH (n) WHERE n.prop IN [1,2,3] OR n.prop2 IN [1,3,5] OR n.prop IN [4,5,6] RETURN n.prop",
      "MATCH (n) WHERE n.prop2 IN [1, 3, 5] OR n.prop IN [1,2,3,4,5,6] RETURN n.prop"
    )
  }

  override protected def parseForRewriting(queryText: String): Statement =
    super.parseForRewriting(queryText).endoRewrite(inSequence(flattenBooleanOperators))
}
