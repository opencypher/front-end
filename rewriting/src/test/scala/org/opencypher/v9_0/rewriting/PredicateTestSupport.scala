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
import org.opencypher.v9_0.logical.plans.CoerceToPredicate
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.CypherTestSupport
import org.scalatest.Assertion

trait PredicateTestSupport extends CypherTestSupport {
  self: CypherFunSuite =>

  private val pos = DummyPosition(0)

  def rewriter: Rewriter

  val P: Expression = anExp("P")
  val Q: Expression = anExp("Q")
  val R: Expression = anExp("R")
  val S: Expression = anExp("S")
  val V: Expression = anExp("V")

  implicit class IFF(x: Expression) {

    def <=>(other: Expression): Assertion = {
      val output = rewriter(x)

      output should equal(other)
    }
  }

  def anExp(s: String): Expression = StringLiteral(s)(pos)
  def and(p1: Expression, p2: Expression): Expression = And(p1, p2)(pos)
  def ands(predicates: Expression*): Expression = Ands(predicates)(pos)
  def or(p1: Expression, p2: Expression): Expression = Or(p1, p2)(pos)
  def ors(predicates: Expression*): Expression = Ors(predicates)(pos)
  def xor(p1: Expression, p2: Expression): Expression = Xor(p1, p2)(pos)
  def not(e: Expression): Expression = Not(e)(pos)
  def bool(e: Expression): Expression = CoerceToPredicate(e)
  def TRUE: Expression = True()(pos)
  def FALSE: Expression = False()(pos)
}
