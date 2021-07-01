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

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.topDown

case class recordScopes(semanticState: SemanticState) extends Rewriter {

  def apply(that: AnyRef): AnyRef = instance.apply(that)

  private val instance: Rewriter = topDown(Rewriter.lift {
    case x: PatternExpression =>
      x.withOuterScope(semanticState.recordedScopes(x).availableSymbolDefinitions.map(_.asVariable.asInstanceOf[Variable])) // FIXME fix casts
    case x: PatternComprehension =>
      x.withOuterScope(semanticState.recordedScopes(x).availableSymbolDefinitions.map(_.asVariable.asInstanceOf[Variable]))
    case x: ExistsSubClause =>
      x.withOuterScope(semanticState.recordedScopes(x).availableSymbolDefinitions.map(_.asVariable.asInstanceOf[Variable]))
  })
}
