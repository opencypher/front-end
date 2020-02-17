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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.False
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.Ors
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.True
import org.opencypher.v9_0.expressions.Xor
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.CypherTestSupport

trait PredicateTestSupport extends CypherTestSupport {
  self: CypherFunSuite =>

  private val pos = DummyPosition(0)

  def rewriter: Rewriter

  val P = anExp("P")
  val Q = anExp("Q")
  val R = anExp("R")
  val S = anExp("S")
  val V = anExp("V")

  implicit class IFF(x: Expression) {
    def <=>(other: Expression) = {
      val output = rewriter(x)

      output should equal(other)
    }
  }

  def anExp(s: String) = StringLiteral(s)(pos)
  def and(p1: Expression, p2: Expression) = And(p1, p2)(pos)
  def ands(predicates: Expression*) = Ands(predicates.toSet)(pos)
  def or(p1: Expression, p2: Expression) = Or(p1, p2)(pos)
  def ors(predicates: Expression*) = Ors(predicates.toSet)(pos)
  def xor(p1: Expression, p2: Expression) = Xor(p1, p2)(pos)
  def not(e: Expression) = Not(e)(pos)
  def TRUE: Expression = True()(pos)
  def FALSE: Expression = False()(pos)
}
