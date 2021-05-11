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

import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Literal
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.AST_REWRITE
import org.opencypher.v9_0.rewriting.rewriters.LiteralsAreAvailable
import org.opencypher.v9_0.util.Foldable
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.IdentityMap
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.bottomUp

/**
 * Extracts all literals of the query and replaces them with `SensitiveLiteral`
 */
case object extractSensitiveLiterals extends Phase[BaseContext, BaseState, BaseState] with Step {
  type LiteralReplacements = IdentityMap[Expression, Expression]
  private val literalMatcher: PartialFunction[Any, LiteralReplacements => Foldable.FoldingBehavior[LiteralReplacements]] = {
    case l: Literal => acc => SkipChildren(acc + (l -> l.asSensitiveLiteral))
    case _ => acc => TraverseChildren(acc)
  }

  private def rewriter(replacements: LiteralReplacements): Rewriter = bottomUp(Rewriter.lift {
    case e: Expression if replacements.contains(e) =>
      replacements(e)
  })

  override def process(from: BaseState,
                       context: BaseContext): BaseState = {
    val original = from.statement()
    val replaceableLiterals = original.treeFold(IdentityMap.empty: LiteralReplacements)(literalMatcher)
    from.withStatement(original.endoRewrite(rewriter(replaceableLiterals)))
  }

  override def phase = AST_REWRITE
  override def preConditions: Set[StepSequencer.Condition] = Set(LiteralsAreAvailable)
  override def postConditions: Set[StepSequencer.Condition] = Set.empty
  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty
}
