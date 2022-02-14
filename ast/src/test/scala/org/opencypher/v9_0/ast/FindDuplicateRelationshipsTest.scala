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
package org.opencypher.v9_0.ast

import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

//noinspection ZeroIndexToHead
class FindDuplicateRelationshipsTest extends CypherFunSuite {

  private val pos = DummyPosition(0)
  private val pos2 = DummyPosition(1)
  private val node = NodePattern(None, None, None, None)(pos)
  private val relR = Variable("r")(pos)
  private val relRCopy = Variable("r")(pos2)
  private val relS = Variable("s")(pos)

  test("find duplicate relationships across pattern parts") {
    val relPath0 = EveryPath(RelationshipChain(node, relPattern(relR), node)(pos))
    val relPath1 = EveryPath(RelationshipChain(node, relPattern(relRCopy), node)(pos))
    val pattern = Pattern(Seq(relPath0, relPath1))(pos)

    RelationshipChain.findDuplicateRelationships(pattern) should equal(Seq(relR))
    RelationshipChain.findDuplicateRelationships(pattern)(0).position should equal(relR.position)
  }

  test("find duplicate relationships in a long rel chain") {
    val relPath = expressions.EveryPath(relChain(relR, relS, relRCopy))
    val pattern = Pattern(Seq(relPath))(pos)

    RelationshipChain.findDuplicateRelationships(pattern) should equal(Seq(relR))
    RelationshipChain.findDuplicateRelationships(pattern)(0).position should equal(relR.position)
  }

  test("does not find duplicate relationships across pattern parts if there is none") {
    val relPath = EveryPath(expressions.RelationshipChain(node, relPattern(relR), node)(pos))
    val otherRelPath = EveryPath(expressions.RelationshipChain(node, relPattern(relS), node)(pos))
    val pattern = Pattern(Seq(relPath, otherRelPath))(pos)

    RelationshipChain.findDuplicateRelationships(pattern) should equal(Seq.empty)
  }

  test("does not find duplicate relationships in a long rel chain if there is none") {
    val relPath = expressions.EveryPath(relChain(relS, relR))
    val pattern = Pattern(Seq(relPath))(pos)

    RelationshipChain.findDuplicateRelationships(pattern) should equal(Seq.empty)
  }

  private def relChain(ids: Variable*) =
    ids.foldLeft(node.asInstanceOf[PatternElement]) {
      (n, id) => expressions.RelationshipChain(n, relPattern(id), node)(pos)
    }

  private def relPattern(id: Variable) =
    RelationshipPattern(Some(id), Seq(), None, None, None, SemanticDirection.OUTGOING)(pos)
}
