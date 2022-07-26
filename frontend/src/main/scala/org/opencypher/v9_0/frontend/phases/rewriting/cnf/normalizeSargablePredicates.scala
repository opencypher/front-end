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
package org.opencypher.v9_0.frontend.phases.rewriting.cnf

import org.opencypher.v9_0.ast.semantics.SemanticTable
import org.opencypher.v9_0.expressions.DoubleLiteral
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.InequalityExpression
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTNumber
import org.opencypher.v9_0.util.topDown

case class normalizeSargablePredicatesRewriter(semanticTable: SemanticTable) extends Rewriter {

  private def instance() = topDown(Rewriter.lift {

    // remove not from inequality expressions by negating them
    case Not(inequality: InequalityExpression)
      // If one of the operands is of a different type than number, we can safely do this rewrite,
      // because NOT("foo" > NaN) and "foo" <= NaN both evaluate to null.
      if !couldContainNumber(inequality.lhs) ||
        !couldContainNumber(inequality.rhs) ||
        // If both operands cannot contain NaN, we can also safely do this rewrite.
        (!couldContainNaN(inequality.lhs) && !couldContainNaN(inequality.rhs)) =>
      inequality.negated
  })

  private def couldContainNumber(expression: Expression): Boolean = {
    semanticTable.getActualTypeFor(expression).containsAny(CTNumber)
  }

  /**
   * Used to check if we can safely flip an inequality.
   * E.g 0.0 > NaN = False, but: NOT(0.0 <= NaN) = True
   * Therefore, the rewrite doesn't hold in this case and we consider the change unsafe.
   */
  private def couldContainNaN(expression: Expression): Boolean = {
    expression match {
      case e: DoubleLiteral =>
        e.value.isNaN
      case e =>
        semanticTable.getActualTypeFor(e).contains(CTFloat)
    }
  }

  def apply(that: AnyRef): AnyRef = instance().apply(that)
}

case object normalizeSargablePredicates extends CnfPhase {

  override def preConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable

  override def postConditions: Set[StepSequencer.Condition] = Set(NoInequalityInsideNot)

  // Can invalidate semantic info as it may introduce a new AST
  override def invalidatedConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable

  override def getRewriter(from: BaseState, context: BaseContext): Rewriter =
    normalizeSargablePredicatesRewriter(from.semanticTable())

  override def toString = "normalizeSargablePredicates"
}
