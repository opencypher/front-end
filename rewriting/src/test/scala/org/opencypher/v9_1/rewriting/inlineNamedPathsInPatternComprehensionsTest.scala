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
package org.opencypher.v9_1.rewriting

import org.opencypher.v9_1.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions._
import org.opencypher.v9_1.rewriting.rewriters.{inlineNamedPathsInPatternComprehensions, projectNamedPaths}
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class inlineNamedPathsInPatternComprehensionsTest extends CypherFunSuite with AstConstructionTestSupport {

  // [ ()-->() | 'foo' ]
  test("does not touch comprehensions without named path") {
    val input: ASTNode = PatternComprehension(None, RelationshipsPattern(RelationshipChain(NodePattern(None, Seq.empty, None) _, RelationshipPattern(None, Seq.empty, None, None, SemanticDirection.OUTGOING) _, NodePattern(None, Seq.empty, None) _) _)_, None, StringLiteral("foo")_)_

    inlineNamedPathsInPatternComprehensions(input) should equal(input)
  }

  // [ p = (a)-[r]->(b) | 'foo' ]
  test("removes named path if not used") {
    val input: PatternComprehension = PatternComprehension(Some(varFor("p")), RelationshipsPattern(RelationshipChain(NodePattern(Some(varFor("a")), Seq.empty, None) _, RelationshipPattern(Some(varFor("r")), Seq.empty, None, None, SemanticDirection.OUTGOING) _, NodePattern(Some(varFor("b")), Seq.empty, None) _) _)_, None, StringLiteral("foo")_)_

    inlineNamedPathsInPatternComprehensions(input) should equal(input.copy(namedPath = None)(pos))
  }

  // [ p = (a)-[r]->(b) | p ]
  test("replaces named path in projection") {
    val element: RelationshipChain = RelationshipChain(NodePattern(Some(varFor("a")), Seq.empty, None) _,
                                                       RelationshipPattern(Some(varFor("r")), Seq.empty, None,
                                                                           None, SemanticDirection.OUTGOING) _,
                                                       NodePattern(Some(varFor("b")), Seq.empty, None) _) _
    val input: PatternComprehension = PatternComprehension(Some(varFor("p")), RelationshipsPattern(element)_, None, Variable("p")_)_
    val output = input.copy(namedPath = None, projection = PathExpression(projectNamedPaths.patternPartPathExpression(element))_)(pos)

    inlineNamedPathsInPatternComprehensions(input) should equal(output)
  }

  // [ p = (a)-[r]->(b) WHERE p | 'foo' ]
  test("replaces named path in predicate") {
    val element: RelationshipChain = RelationshipChain(NodePattern(Some(varFor("a")), Seq.empty, None) _,
                                                       RelationshipPattern(Some(varFor("r")), Seq.empty, None,
                                                                           None, SemanticDirection.OUTGOING) _,
                                                       NodePattern(Some(varFor("b")), Seq.empty, None) _) _
    val input: PatternComprehension = PatternComprehension(Some(varFor("p")), RelationshipsPattern(element)_, Some(varFor("p")), StringLiteral("foo")_)_
    val output = input.copy(
      namedPath = None,
      predicate = Some(PathExpression(projectNamedPaths.patternPartPathExpression(element))_)
    )(pos)

    inlineNamedPathsInPatternComprehensions(input) should equal(output)
  }


  // [ p = (a)-[r]->(b) WHERE p | p ]
  test("replaces named path in predicate and projection") {
    val element: RelationshipChain = RelationshipChain(NodePattern(Some(varFor("a")), Seq.empty, None) _,
                                                       RelationshipPattern(Some(varFor("r")), Seq.empty, None,
                                                                           None, SemanticDirection.OUTGOING) _,
                                                       NodePattern(Some(varFor("b")), Seq.empty, None) _) _
    val input: PatternComprehension = PatternComprehension(Some(varFor("p")), RelationshipsPattern(element)_, Some(varFor("p")), Variable("p")_)_
    val output = input.copy(
      namedPath = None,
      predicate = Some(PathExpression(projectNamedPaths.patternPartPathExpression(element))_),
      projection = PathExpression(projectNamedPaths.patternPartPathExpression(element))_
    )(pos)

    inlineNamedPathsInPatternComprehensions(input) should equal(output)
  }
}
