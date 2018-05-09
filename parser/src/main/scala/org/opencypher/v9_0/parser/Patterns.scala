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
package org.opencypher.v9_0.parser

/*
 *|                                            NamedPatternPart                                            |
 *|Variable |                                      AnonymousPatternPart                                     |
 *                           |                                 PatternElement                              |
 *                           |               PatternElement               |        RelationshipChain       |
 *                           |NodePattern |        RelationshipChain      ||RelationshipPattern|NodePattern|
 *                                        |RelationshipPattern|NodePattern|
 *    p =      shortestPath(    (a)             -[r1]->           (b)            -[r2]->           (c)       )
 */

import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.{expressions => ast}
import org.parboiled.scala._

trait Patterns extends Parser
  with Literals
  with Base {

  def Pattern: Rule1[org.opencypher.v9_0.expressions.Pattern] = rule("a pattern") {
    oneOrMore(PatternPart, separator = CommaSep) ~~>> (ast.Pattern(_))
  }

  def PatternPart: Rule1[org.opencypher.v9_0.expressions.PatternPart] = rule("a pattern") (
      group(Variable ~~ operator("=") ~~ AnonymousPatternPart) ~~>> (ast.NamedPatternPart(_, _))
    | AnonymousPatternPart
  )

  private def AnonymousPatternPart: Rule1[org.opencypher.v9_0.expressions.AnonymousPatternPart] = rule (
      ShortestPathPattern
    | PatternElement ~~> ast.EveryPath
  )

  def ShortestPathPattern: Rule1[org.opencypher.v9_0.expressions.ShortestPaths] = rule (
      (group(keyword("shortestPath") ~~ "(" ~~ PatternElement ~~ ")") memoMismatches) ~~>> (ast.ShortestPaths(_, single = true))
    | (group(keyword("allShortestPaths") ~~ "(" ~~ PatternElement ~~ ")") memoMismatches) ~~>> (ast.ShortestPaths(_, single = false))
  ).memoMismatches

  def RelationshipsPattern: Rule1[org.opencypher.v9_0.expressions.RelationshipsPattern] = rule {
    group(NodePattern ~ oneOrMore(WS ~ PatternElementChain)) ~~>> (ast.RelationshipsPattern(_))
  }.memoMismatches

  private def PatternElement: Rule1[org.opencypher.v9_0.expressions.PatternElement] = rule (
      NodePattern ~ zeroOrMore(WS ~ PatternElementChain)
    | "(" ~~ PatternElement ~~ ")"
  )

  private def PatternElementChain: ReductionRule1[org.opencypher.v9_0.expressions.PatternElement, org.opencypher.v9_0.expressions.RelationshipChain] = rule("a relationship pattern") {
    group(RelationshipPattern ~~ NodePattern) ~~>> (ast.RelationshipChain(_, _, _))
  }

  private def RelationshipPattern: Rule1[org.opencypher.v9_0.expressions.RelationshipPattern] = rule {
    (
        LeftArrowHead ~~ Dash ~~ RelationshipDetail ~~ Dash ~~ RightArrowHead ~ push(SemanticDirection.BOTH)
      | LeftArrowHead ~~ Dash ~~ RelationshipDetail ~~ Dash ~ push(SemanticDirection.INCOMING)
      | Dash ~~ RelationshipDetail ~~ Dash ~~ RightArrowHead ~ push(SemanticDirection.OUTGOING)
      | Dash ~~ RelationshipDetail ~~ Dash ~ push(SemanticDirection.BOTH)
    ) ~~>> ((variable, relTypes, range, props, dir) => ast.RelationshipPattern(variable, relTypes.types, range,
      props, dir, relTypes.legacySeparator))
  }

  private def RelationshipDetail: Rule4[
      Option[org.opencypher.v9_0.expressions.Variable],
      MaybeLegacyRelTypes,
      Option[Option[org.opencypher.v9_0.expressions.Range]],
      Option[org.opencypher.v9_0.expressions.Expression]] = rule("[") {
    (
        "[" ~~
          MaybeVariable ~~
          RelationshipTypes ~~ MaybeVariableLength ~
          MaybeProperties ~~
        "]"
      | EMPTY ~ push(None) ~ push(MaybeLegacyRelTypes()) ~ push(None) ~ push(None)
    )
  }

  private def RelationshipTypes: Rule1[MaybeLegacyRelTypes] = rule("relationship types") (
    (":" ~~ RelTypeName ~~ zeroOrMore(WS ~ "|" ~~ LegacyCompatibleRelTypeName)) ~~>> (
      (first: org.opencypher.v9_0.expressions.RelTypeName, more: List[(Boolean, org.opencypher.v9_0.expressions.RelTypeName)]) => (pos: InputPosition) => {
        MaybeLegacyRelTypes(first +: more.map(_._2), more.exists(_._1))
      })
    | EMPTY ~ push(MaybeLegacyRelTypes())
  )

  private def LegacyCompatibleRelTypeName: Rule1[(Boolean, org.opencypher.v9_0.expressions.RelTypeName)] =
    ((":" ~ push(true)) | EMPTY ~ push(false)) ~~ RelTypeName ~~>> (
      (legacy: Boolean, name: org.opencypher.v9_0.expressions.RelTypeName) => (pos: InputPosition) => (legacy,name))

  private def MaybeVariableLength: Rule1[Option[Option[org.opencypher.v9_0.expressions.Range]]] = rule("a length specification") (
      "*" ~~ (
          RangeLiteral ~~> (r => Some(Some(r)))
        | EMPTY ~ push(Some(None))
      )
    | EMPTY ~ push(None)
  )

  private def NodePattern: Rule1[org.opencypher.v9_0.expressions.NodePattern] = rule("a node pattern") (
      group("(" ~~ MaybeVariable ~ MaybeNodeLabels ~ MaybeProperties ~~ ")") ~~>> (ast.NodePattern(_, _, _))
    | group(Variable ~ MaybeNodeLabels ~ MaybeProperties)  ~~>> (ast.InvalidNodePattern(_, _, _)) // Here to give nice error messages
  )

  private def MaybeVariable: Rule1[Option[org.opencypher.v9_0.expressions.Variable]] = rule("a variable") {
    optional(Variable)
  }

  private def MaybeNodeLabels: Rule1[Seq[org.opencypher.v9_0.expressions.LabelName]] = rule("node labels") (
    WS ~ NodeLabels | EMPTY ~ push(Seq())
  )

  private def MaybeProperties: Rule1[Option[org.opencypher.v9_0.expressions.Expression]] = rule("a property map") (
    optional(WS ~ (MapLiteral | Parameter))
  )
}

case class MaybeLegacyRelTypes(types: Seq[org.opencypher.v9_0.expressions.RelTypeName] = Seq.empty, legacySeparator: Boolean = false)
