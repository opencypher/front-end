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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.rewriting.RewriteTest
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class timestampRewriterTest extends CypherFunSuite with RewriteTest {

  override val rewriterUnderTest: Rewriter = timestampRewriter

  test("Rewrites timestamp to datetime.epochMillis") {
    assertRewrite("RETURN timestamp() as t", "RETURN datetime().epochMillis as t")
    assertRewrite("WITH timestamp() as t RETURN t", "WITH datetime().epochMillis as t RETURN t")
    assertRewrite(
      "RETURN timestamp() as t, timestamp() as d",
      "RETURN datetime().epochMillis as t, datetime().epochMillis as d"
    )
    assertRewrite("RETURN TiMeStAmP() AS t", "RETURN datetime().epochMillis AS t")
    assertIsNotRewritten("RETURN test.timestamp() AS t")
  }
}
