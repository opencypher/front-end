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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PathExpression
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.rewriting.rewriters.inlineNamedPathsInPatternComprehensions
import org.opencypher.v9_0.rewriting.rewriters.projectNamedPaths
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class inlineNamedPathsInPatternComprehensionsTest extends CypherFunSuite with AstConstructionTestSupport {

  // [ ()-->() | 'foo' ]
  test("does not touch comprehensions without named path") {
    val input: ASTNode = PatternComprehension(
      None,
      RelationshipsPattern(RelationshipChain(
        NodePattern(None, None, None, None) _,
        RelationshipPattern(None, None, None, None, None, SemanticDirection.OUTGOING) _,
        NodePattern(None, None, None, None) _
      ) _) _,
      None,
      literalString("foo")
    )(pos, Set.empty, "", "")

    inlineNamedPathsInPatternComprehensions.instance(input) should equal(input)
  }

  // [ p = (a)-[r]->(b) | 'foo' ]
  test("removes named path if not used") {
    val input: PatternComprehension = PatternComprehension(
      Some(varFor("p")),
      RelationshipsPattern(RelationshipChain(
        NodePattern(Some(varFor("a")), None, None, None) _,
        RelationshipPattern(Some(varFor("r")), None, None, None, None, SemanticDirection.OUTGOING) _,
        NodePattern(Some(varFor("b")), None, None, None) _
      ) _) _,
      None,
      literalString("foo")
    )(pos, Set.empty, "", "")

    inlineNamedPathsInPatternComprehensions.instance(input) should equal(input.copy(namedPath = None)(
      pos,
      input.outerScope,
      "",
      ""
    ))
  }

  // [ p = (a)-[r]->(b) | p ]
  test("replaces named path in projection") {
    val element: RelationshipChain = RelationshipChain(
      NodePattern(Some(varFor("a")), None, None, None) _,
      RelationshipPattern(Some(varFor("r")), None, None, None, None, SemanticDirection.OUTGOING) _,
      NodePattern(Some(varFor("b")), None, None, None) _
    ) _
    val input: PatternComprehension = PatternComprehension(
      Some(varFor("p")),
      RelationshipsPattern(element) _,
      None,
      varFor("p")
    )(pos, Set.empty, "", "")
    val output = input.copy(
      namedPath = None,
      projection = PathExpression(projectNamedPaths.patternPartPathExpression(element))(pos)
    )(pos, input.outerScope, "", "")

    inlineNamedPathsInPatternComprehensions.instance(input) should equal(output)
  }

  // [ p = (a)-[r]->(b) WHERE p | 'foo' ]
  test("replaces named path in predicate") {
    val element: RelationshipChain = RelationshipChain(
      NodePattern(Some(varFor("a")), None, None, None) _,
      RelationshipPattern(Some(varFor("r")), None, None, None, None, SemanticDirection.OUTGOING) _,
      NodePattern(Some(varFor("b")), None, None, None) _
    ) _
    val input: PatternComprehension = PatternComprehension(
      Some(varFor("p")),
      RelationshipsPattern(element) _,
      Some(varFor("p")),
      literalString("foo")
    )(pos, Set.empty, "", "")
    val output = input.copy(
      namedPath = None,
      predicate = Some(PathExpression(projectNamedPaths.patternPartPathExpression(element)) _)
    )(pos, input.outerScope, "", "")

    inlineNamedPathsInPatternComprehensions.instance(input) should equal(output)
  }

  // [ p = (a)-[r]->(b) WHERE p | p ]
  test("replaces named path in predicate and projection") {
    val element: RelationshipChain = RelationshipChain(
      NodePattern(Some(varFor("a")), None, None, None) _,
      RelationshipPattern(Some(varFor("r")), None, None, None, None, SemanticDirection.OUTGOING) _,
      NodePattern(Some(varFor("b")), None, None, None) _
    ) _
    val input: PatternComprehension = PatternComprehension(
      Some(varFor("p")),
      RelationshipsPattern(element) _,
      Some(varFor("p")),
      varFor("p")
    )(pos, Set.empty, "", "")
    val output = input.copy(
      namedPath = None,
      predicate = Some(PathExpression(projectNamedPaths.patternPartPathExpression(element)) _),
      projection = PathExpression(projectNamedPaths.patternPartPathExpression(element))(pos)
    )(pos, input.outerScope, "", "")

    inlineNamedPathsInPatternComprehensions.instance(input) should equal(output)
  }
}
