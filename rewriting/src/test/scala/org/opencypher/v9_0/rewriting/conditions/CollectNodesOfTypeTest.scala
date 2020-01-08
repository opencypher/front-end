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
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class CollectNodesOfTypeTest extends CypherFunSuite with AstConstructionTestSupport {

    private val collector: Any => Seq[Variable] = collectNodesOfType[Variable]()

    test("collect all variables") {
      val idA = varFor("a")
      val idB = varFor("b")
      val ast: ASTNode = Match(optional = false, Pattern(Seq(EveryPath(NodePattern(Some(idA), Seq(), Some(idB))_)))_, Seq(), None)_

      collector(ast) should equal(Seq(idA, idB))
    }

    test("collect no variable") {
      val ast: ASTNode = Match(optional = false, Pattern(Seq(EveryPath(NodePattern(None, Seq(), None)_)))_, Seq(), None)_

      collector(ast) shouldBe empty
    }
}
