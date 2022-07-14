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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Range
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.rewriting.rewriters.factories.PreparatoryRewritingRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.topDown

case object NoShortestPathWithFixedLength extends StepSequencer.Condition

/**
 * Rewrites shortestPath and allShortestPaths for fixed length relationships to limit 1 or just the pattern.
 */
object rewriteShortestPathWithFixedLengthRel extends Rewriter {

  private val instance = topDown(Rewriter.lift {
    case s @ ShortestPaths(
        r @ RelationshipChain(_: NodePattern, relPat @ RelationshipPattern(_, _, None, _, _, _), _),
        _
      ) =>
      val one = Some(UnsignedDecimalIntegerLiteral("1")(InputPosition.NONE))
      val range = Some(Some(Range(one, one)(InputPosition.NONE)))

      s.copy(element = r.copy(relationship = relPat.copy(length = range)(relPat.position))(r.position))(s.position)
  })

  override def apply(v: AnyRef): AnyRef = instance(v)

}

case object rewriteShortestPathWithFixedLengthRelationship extends Step with PreparatoryRewritingRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set()

  override def postConditions: Set[StepSequencer.Condition] = Set(NoShortestPathWithFixedLength)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set()

  override def getRewriter(
    cypherExceptionFactory: CypherExceptionFactory,
    notificationLogger: InternalNotificationLogger
  ): Rewriter = rewriteShortestPathWithFixedLengthRel
}
