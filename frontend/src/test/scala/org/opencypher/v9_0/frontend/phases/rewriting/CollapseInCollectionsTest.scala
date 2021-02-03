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

import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.frontend.phases.CNFNormalizer
import org.opencypher.v9_0.frontend.phases.RewritePhaseTest
import org.opencypher.v9_0.frontend.phases.Transformer
import org.opencypher.v9_0.frontend.phases.collapseMultipleInPredicates
import org.opencypher.v9_0.rewriting.AstRewritingTestSupport
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class CollapseInCollectionsTest extends CypherFunSuite with AstRewritingTestSupport with RewritePhaseTest {

  override def rewriterPhaseUnderTest: Transformer[BaseContext, BaseState, BaseState] = CNFNormalizer andThen collapseMultipleInPredicates

  test("should collapse collection containing ConstValues for id function") {
    assertRewritten(
      "MATCH (a) WHERE id(a) IN [42] OR id(a) IN [13] RETURN a",
      "MATCH (a) WHERE id(a) IN [42, 13] RETURN a"
    )
  }

  test("should collapse collections containing ConstValues and nonConstValues for id function") {
    assertRewritten(
      "MATCH (a) WHERE id(a) IN [42] OR id(a) IN [rand()] RETURN a",
      "MATCH (a) WHERE id(a) IN [42, rand()] RETURN a"
    )
  }

  test("should collapse collection containing ConstValues for property") {
    assertRewritten(
      "MATCH (a) WHERE a.prop IN [42] OR a.prop IN [13] RETURN a",
      "MATCH (a) WHERE a.prop IN [42, 13] RETURN a"
    )
  }

  test("should collapse collections containing ConstValues and nonConstValues for property") {
    assertRewritten(
      "MATCH (a) WHERE a.prop IN [42] OR a.prop IN [rand()] RETURN a",
      "MATCH (a) WHERE a.prop IN [42, rand()] RETURN a"
    )
  }
}
