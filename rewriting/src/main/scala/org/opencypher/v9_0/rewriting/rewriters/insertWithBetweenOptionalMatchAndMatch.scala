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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.With
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.rewriting.rewriters.factories.PreparatoryRewritingRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Condition
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.topDown

case object WithBetweenOptionalMatchAndMatchInserted extends Condition

// Rewrites OPTIONAL MATCH (<n>) MATCH (<n>) RETURN <n> ==> OPTIONAL MATCH (<n>) WITH * MATCH (<n>) RETURN <n>
case object insertWithBetweenOptionalMatchAndMatch extends Rewriter with Step with PreparatoryRewritingRewriterFactory {

  override def apply(that: AnyRef): AnyRef = instance(that)

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(WithBetweenOptionalMatchAndMatchInserted)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty

  private val instance: Rewriter = topDown(Rewriter.lift {
    case sq@SingleQuery(clauses) if clauses.nonEmpty =>
      val newClauses = clauses.sliding(2).collect {
        case Seq(match1: Match, match2: Match) if match1.optional && !match2.optional =>
          val withStar = With(distinct = false, ReturnItems(includeExisting = true, Seq.empty)(match1.position), None, None, None, None)(match1.position)
          Seq(match1, withStar)
        case Seq(firstClause, _) => Seq(firstClause)
      }.flatten.toSeq :+ clauses.last
      SingleQuery(newClauses)(sq.position)
  })

  override def getRewriter(deprecations: Deprecations,
                           cypherExceptionFactory: CypherExceptionFactory,
                           notificationLogger: InternalNotificationLogger): Rewriter = instance
}
