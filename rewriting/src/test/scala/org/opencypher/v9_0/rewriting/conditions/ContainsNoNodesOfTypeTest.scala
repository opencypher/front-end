/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_0.rewriting.conditions

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.UnaliasedReturnItem
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class ContainsNoNodesOfTypeTest extends CypherFunSuite with AstConstructionTestSupport {

  val condition: Any => Seq[String] = containsNoNodesOfType[UnaliasedReturnItem]()

  test("Happy when not finding UnaliasedReturnItem") {
    val ast: ASTNode = Match(optional = false, Pattern(Seq(EveryPath(NodePattern(None, Seq(), None)_)))_, Seq(), None)_

    condition(ast) should equal(Seq())
  }

  test("Fails when finding UnaliasedReturnItem") {
    val ast: ASTNode = Return(false, ReturnItems(includeExisting = false, Seq(UnaliasedReturnItem(varFor("foo"), "foo")_))_, None, None, None)_

    condition(ast) should equal(Seq("Expected none but found UnaliasedReturnItem at position line 1, column 0 (offset: 0)"))
  }
}
