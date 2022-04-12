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
import org.opencypher.v9_0.rewriting.SemanticDeprecations
import org.opencypher.v9_0.rewriting.SyntacticDeprecations
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.Ref
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
    val allDeprecations = deprecations match {
      case syntacticDeprecations: SyntacticDeprecations =>
        val foundWithoutContext = state.statement().folder.fold(Set.empty[Deprecation]) {
          syntacticDeprecations.find.andThen(deprecation => acc => acc + deprecation)
        }
        val foundWithContext = syntacticDeprecations.findWithContext(state.statement())
        foundWithoutContext ++ foundWithContext
      case semanticDeprecations: SemanticDeprecations =>
        val semanticTable = state.maybeSemanticTable.getOrElse(
          throw new IllegalStateException(s"Got semantic deprecations ${semanticDeprecations.getClass.getSimpleName} but no SemanticTable")
        )

        state.statement().folder.fold(Set.empty[Deprecation]) {
          semanticDeprecations.find(semanticTable).andThen(deprecation => acc => acc + deprecation)
        }
    }

    val notifications = allDeprecations.flatMap(_.notification)
    val replacements = allDeprecations.flatMap(_.replacement).toMap

    // issue notifications
    notifications.foreach(context.notificationLogger.log)

    // apply replacements
    val rewriter: Rewriter = bottomUp(Rewriter.lift {
      case astNode: ASTNode => replacements.getOrElse(Ref(astNode), astNode)
    })
    val newStatement = state.statement().endoRewrite(rewriter)
    state.withStatement(newStatement)
  }

  override def postConditions: Set[StepSequencer.Condition] = Set(DeprecatedSyntaxReplaced)

  override def phase = DEPRECATION_WARNINGS
}
