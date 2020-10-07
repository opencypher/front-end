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

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.HasTypes
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.topDown

case class normalizeHasLabelsAndHasType(semanticState: SemanticState) extends Rewriter {

  override def apply(that: AnyRef): AnyRef = instance(that)

  private val instance: Rewriter = topDown(Rewriter.lift {
    case p@HasLabelsOrTypes(e, labels) =>
      if (semanticState.expressionType(e).actual == CTRelationship.invariant) HasTypes(e, labels.map(l => RelTypeName(l.name)(l.position)))(p.position)
      //we don't need to check if it is a node here, if not it will fail in semantic checking
      else HasLabels(e, labels)(p.position)
  })
}
