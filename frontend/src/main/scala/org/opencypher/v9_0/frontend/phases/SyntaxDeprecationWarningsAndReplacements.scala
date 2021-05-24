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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.DEPRECATION_WARNINGS
import org.opencypher.v9_0.rewriting.Deprecation
import org.opencypher.v9_0.rewriting.Deprecations
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.InternalNotification
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Condition
import org.opencypher.v9_0.util.bottomUp

case object DeprecatedSyntaxReplaced extends Condition

/**
 * Find deprecated Cypher constructs, generate warnings for them, and replace deprecated syntax with currently accepted syntax.
 */
case class SyntaxDeprecationWarningsAndReplacements(deprecations: Deprecations) extends Phase[BaseContext, BaseState, BaseState] {

  override def process(state: BaseState, context: BaseContext): BaseState = {
    // collect notifications and replacements
    case class Acc(notifications: Set[InternalNotification], replacements: Map[ASTNode, ASTNode]) {
      def +(deprecation: Deprecation): Acc = Acc(notifications ++ deprecation.notification, replacements ++ deprecation.replacement)
      def +(acc: Acc) = Acc(notifications ++ acc.notifications, replacements ++ acc.replacements)
    }

    val foundWithoutContext = state.statement().fold(Acc(Set.empty, Map.empty)) {
      deprecations.find.andThen(deprecation => acc => acc + deprecation)
    }
    val foundWithContext = deprecations.findWithContext(state.statement(), state.maybeSemanticTable).foldLeft(Acc(Set.empty, Map.empty)) {
      case (acc, deprecation) => acc + deprecation
    }

    val Acc(notifications, replacements) = foundWithoutContext + foundWithContext

    // issue notifications
    notifications.foreach(context.notificationLogger.log)

    // apply replacements
    val rewriter: Rewriter = bottomUp(Rewriter.lift {
      case astNode: ASTNode => replacements.getOrElse(astNode, astNode)
    })
    val newStatement = state.statement().endoRewrite(rewriter)
    state.withStatement(newStatement)
  }

  override def postConditions: Set[StepSequencer.Condition] = Set(DeprecatedSyntaxReplaced)

  override def phase = DEPRECATION_WARNINGS
}
