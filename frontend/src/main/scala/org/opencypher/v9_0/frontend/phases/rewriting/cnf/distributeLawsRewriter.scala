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

import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.BaseState
import org.opencypher.v9_0.rewriting.AstRewritingMonitor
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.rewriters.copyVariables
import org.opencypher.v9_0.rewriting.rewriters.repeatWithSizeLimit
import org.opencypher.v9_0.util.Foldable.FoldableAny
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp

case class distributeLawsRewriter()(implicit monitor: AstRewritingMonitor) extends Rewriter {
  def apply(that: AnyRef): AnyRef = {
    if (dnfCounts(that) < distributeLawsRewriter.DNF_CONVERSION_LIMIT) {
      instance(that)
    } else {
      monitor.abortedRewritingDueToLargeDNF(that)
      that
    }
  }

  private def dnfCounts(value: Any) = value.folder.treeFold(1) {
    case Or(lhs, a: And) => acc => TraverseChildren(acc + 1)
    case Or(a: And, rhs) => acc => TraverseChildren(acc + 1)
  }

  private val step = Rewriter.lift {
    case p@Or(exp1, And(exp2, exp3)) => And(Or(exp1, exp2)(p.position), Or(exp1.endoRewrite(copyVariables), exp3)(p.position))(p.position)
    case p@Or(And(exp1, exp2), exp3) => And(Or(exp1, exp3)(p.position), Or(exp2, exp3.endoRewrite(copyVariables))(p.position))(p.position)
  }

  private val instance: Rewriter = repeatWithSizeLimit(bottomUp(step))(monitor)
}

case object distributeLawsRewriter extends CnfPhase {
  // converting from DNF to CNF is exponentially expensive, so we only do it for a small amount of clauses
  // see https://en.wikipedia.org/wiki/Conjunctive_normal_form#Conversion_into_CNF
  val DNF_CONVERSION_LIMIT = 8

  override def getRewriter(from: BaseState,
                           context: BaseContext): Rewriter = {
    implicit val monitor: AstRewritingMonitor = context.monitors.newMonitor[AstRewritingMonitor]()
    distributeLawsRewriter()
  }

  override def preConditions: Set[StepSequencer.Condition] = Set(!AndRewrittenToAnds)

  override def postConditions: Set[StepSequencer.Condition] = Set(AndsAboveOrs)

  override def invalidatedConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable
}