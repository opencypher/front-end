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
package org.openCypher.v9_0.rewriting

import org.openCypher.v9_0.ast._
import org.openCypher.v9_0.expressions.{Equals, Variable}
import org.openCypher.v9_0.rewriting.rewriters.ReturnItemSafeTopDownRewriter
import org.openCypher.v9_0.util.Rewriter
import org.openCypher.v9_0.util.test_helpers.CypherFunSuite

class ReturnItemSafeTopDownRewriterTest extends CypherFunSuite with AstConstructionTestSupport {

  val rewriter = ReturnItemSafeTopDownRewriter(Rewriter.lift { case v@Variable("foo") => Variable("bar")(v.position) })

  test("works with where") {
    val original = Where(Equals(varFor("foo"), literalInt(42))(pos))(pos)
    val result = original.endoRewrite(rewriter)

    result should equal(Where(Equals(varFor("bar"), literalInt(42))(pos))(pos))
  }

  test("does not rewrite return item alias") {

    def createWith(item: ReturnItem) = {
      val returnItems = ReturnItems(includeExisting = false, Seq(item))(pos)
      With(distinct = false, returnItems, PassAllGraphReturnItems(pos), None, None, None, None)(pos)
    }

    val originalReturnItem = AliasedReturnItem(Equals(varFor("foo"), literalInt(42))(pos), varFor("foo"))(pos)
    val expectedReturnItem = AliasedReturnItem(Equals(varFor("bar"), literalInt(42))(pos), varFor("foo"))(pos)

    createWith(originalReturnItem).endoRewrite(rewriter) should equal(createWith(expectedReturnItem))
  }
}
