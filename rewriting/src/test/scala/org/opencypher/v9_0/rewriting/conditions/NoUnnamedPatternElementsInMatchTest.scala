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
package org.opencypher.v9_0.rewriting.conditions

import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class NoUnnamedPatternElementsInMatchTest extends CypherFunSuite with AstConstructionTestSupport {

  private val condition: Any => Seq[String] = noUnnamedPatternElementsInMatch

  test("unhappy when a node pattern is unnamed") {
    val nodePattern: NodePattern = node(None)
    val ast: ASTNode = SingleQuery(Seq(
      Match(
        optional = false,
        Pattern(Seq(EveryPath(chain(
          chain(node(Some(varFor("n"))), relationship(Some(varFor("p"))), nodePattern),
          relationship(Some(varFor("r"))),
          node(Some(varFor("m")))
        )))) _,
        Seq.empty,
        None
      ) _,
      Return(
        distinct = false,
        ReturnItems(
          includeExisting = false,
          Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos, isAutoAliased = false))
        ) _,
        None,
        None,
        None
      ) _
    )) _

    condition(ast) shouldBe Seq(s"NodePattern at ${nodePattern.position} is unnamed")
  }

  test("unhappy when a relationship pattern is unnamed") {
    val relationshipPattern: RelationshipPattern = relationship(None)
    val ast: ASTNode = SingleQuery(Seq(
      Match(
        optional = false,
        Pattern(Seq(EveryPath(chain(
          chain(node(Some(varFor("n"))), relationship(Some(varFor("p"))), node(Some(varFor("k")))),
          relationshipPattern,
          node(Some(varFor("m")))
        )))) _,
        Seq.empty,
        None
      ) _,
      Return(
        distinct = false,
        ReturnItems(
          includeExisting = false,
          Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos, isAutoAliased = false))
        ) _,
        None,
        None,
        None
      ) _
    )) _

    condition(ast) shouldBe Seq(s"RelationshipPattern at ${relationshipPattern.position} is unnamed")
  }

  test("unhappy when there are unnamed node and relationship patterns") {
    val nodePattern: NodePattern = node(None)
    val relationshipPattern: RelationshipPattern = relationship(None)
    val ast: ASTNode = SingleQuery(Seq(
      Match(
        optional = false,
        Pattern(Seq(EveryPath(chain(
          chain(node(Some(varFor("n"))), relationshipPattern, node(Some(varFor("k")))),
          relationship(Some(varFor("r"))),
          nodePattern
        )))) _,
        Seq.empty,
        None
      ) _,
      Return(
        distinct = false,
        ReturnItems(
          includeExisting = false,
          Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos, isAutoAliased = false))
        ) _,
        None,
        None,
        None
      ) _
    )) _

    condition(ast) shouldBe Seq(
      s"NodePattern at ${nodePattern.position} is unnamed",
      s"RelationshipPattern at ${relationshipPattern.position} is unnamed"
    )
  }

  test("happy when all elements in pattern are named") {
    val ast: ASTNode = SingleQuery(Seq(
      Match(
        optional = false,
        Pattern(Seq(EveryPath(chain(
          chain(node(Some(varFor("n"))), relationship(Some(varFor("p"))), node(Some(varFor("k")))),
          relationship(Some(varFor("r"))),
          node(Some(varFor("m")))
        )))) _,
        Seq.empty,
        None
      ) _,
      Return(
        distinct = false,
        ReturnItems(
          includeExisting = false,
          Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos, isAutoAliased = false))
        ) _,
        None,
        None,
        None
      ) _
    )) _

    condition(ast) shouldBe empty
  }

  test("should leave where clause alone") {
    val where: Where =
      Where(PatternExpression(RelationshipsPattern(chain(node(None), relationship(None), node(None))) _)(
        Set.empty,
        "",
        ""
      )) _
    val ast: ASTNode = SingleQuery(Seq(
      Match(
        optional = false,
        Pattern(Seq(EveryPath(chain(
          chain(node(Some(varFor("n"))), relationship(Some(varFor("p"))), node(Some(varFor("k")))),
          relationship(Some(varFor("r"))),
          node(Some(varFor("m")))
        )))) _,
        Seq.empty,
        Some(where)
      ) _,
      Return(
        distinct = false,
        ReturnItems(
          includeExisting = false,
          Seq(AliasedReturnItem(varFor("n"), varFor("n"))(pos, isAutoAliased = false))
        ) _,
        None,
        None,
        None
      ) _
    )) _

    condition(ast) shouldBe empty
  }

  private def chain(left: PatternElement, rel: RelationshipPattern, right: NodePattern): RelationshipChain = {
    RelationshipChain(left, rel, right) _
  }

  private def relationship(id: Option[Variable]): RelationshipPattern = {
    RelationshipPattern(id, Seq.empty, None, None, None, SemanticDirection.OUTGOING) _
  }

  private def node(id: Option[Variable]): NodePattern = {
    NodePattern(id, None, None, None) _
  }
}
