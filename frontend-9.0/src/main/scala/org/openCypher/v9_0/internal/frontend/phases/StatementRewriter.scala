/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.openCypher.v9_0.internal.frontend.phases

import org.openCypher.v9_0.internal.frontend.CompilationPhaseTracer.CompilationPhase
import org.openCypher.v9_0.internal.util.Rewriter
import org.openCypher.v9_0.internal.frontend.CompilationPhaseTracer.CompilationPhase.AST_REWRITE

trait StatementRewriter extends Phase[BaseContext, BaseState, BaseState] {
  override def phase: CompilationPhase = AST_REWRITE

  def instance(context: BaseContext): Rewriter

  override def process(from: BaseState, context: BaseContext): BaseState = {
    val rewritten = from.statement().endoRewrite(instance(context))
    from.withStatement(rewritten)
  }
}
