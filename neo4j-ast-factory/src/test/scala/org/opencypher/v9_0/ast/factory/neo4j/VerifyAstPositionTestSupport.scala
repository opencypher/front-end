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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.ast.LoadCSV
import org.opencypher.v9_0.ast.ReadAdministrationCommand
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.ListSlice
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.InputPosition
import org.scalatest.Assertions
import org.scalatest.Matchers

trait VerifyAstPositionTestSupport extends Assertions with Matchers {

  def verifyPositions(javaCCAstNode: ASTNode, expectedAstNode: ASTNode): Unit = {

    def astWithPosition(astNode: ASTNode) = {
      {
        lazy val containsReadAdministratorCommand = astNode.folder.treeExists {
          case _: ReadAdministrationCommand => true
        }

        astNode.folder.treeFold(Seq.empty[(ASTNode, InputPosition)]) {
          case _: Property |
            _: SetPropertyItem |
            _: RemovePropertyItem |
            _: LoadCSV |
            _: UseGraph |
            _: EveryPath |
            _: RelationshipChain |
            _: Yield |
            _: ContainerIndex |
            _: ListSlice |
            _: HasLabelsOrTypes |
            _: SingleQuery |
            _: ReadAdministrationCommand |
            _: SetIncludingPropertiesFromMapItem |
            _: SetExactPropertiesFromMapItem => acc => TraverseChildren(acc)
          case returnItems: ReturnItems if returnItems.items.isEmpty => acc => SkipChildren(acc)
          case _: Variable if containsReadAdministratorCommand       => acc => TraverseChildren(acc)
          case astNode: ASTNode => acc => TraverseChildren(acc :+ (astNode -> astNode.position))
          case _                => acc => TraverseChildren(acc)
        }
      }
    }

    astWithPosition(javaCCAstNode).zip(astWithPosition(expectedAstNode))
      .foreach {
        case ((_, _), (_, InputPosition(a, 1, b))) if a == b => // Ignore DummyPositions
        case ((astChildNode1, pos1), (_, pos2)) =>
          withClue(s"AST node $astChildNode1 was parsed with different positions:") {
            pos1 shouldBe pos2
          }
        case _ => // Do nothing
      }
  }

  implicit protected def lift(pos: (Int, Int, Int)): InputPosition = InputPosition(pos._3, pos._1, pos._2)
}
