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

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class noUnnamedPatternElementsInPatternComprehensionTest extends CypherFunSuite with AstConstructionTestSupport {

  private val condition: Any => Seq[String] = noUnnamedPatternElementsInPatternComprehension

  test("should detect an unnamed pattern element in comprehension") {
    val input: ASTNode = PatternComprehension(
      None,
      RelationshipsPattern(
        RelationshipChain(
          NodePattern(None, None, None, None) _,
          RelationshipPattern(None, None, None, None, None, SemanticDirection.OUTGOING) _,
          NodePattern(None, None, None, None) _
        ) _
      ) _,
      None,
      literalString("foo")
    )(pos, Set.empty)

    condition(input) should equal(Seq(s"Expression $input contains pattern elements which are not named"))
  }

  test("should not react to fully named pattern comprehension") {
    val input: PatternComprehension = PatternComprehension(
      Some(varFor("p")),
      RelationshipsPattern(
        RelationshipChain(
          NodePattern(Some(varFor("a")), None, None, None) _,
          RelationshipPattern(Some(varFor("r")), None, None, None, None, SemanticDirection.OUTGOING) _,
          NodePattern(Some(varFor("b")), None, None, None) _
        ) _
      ) _,
      None,
      literalString("foo")
    )(pos, Set.empty)

    condition(input) shouldBe empty
  }
}
