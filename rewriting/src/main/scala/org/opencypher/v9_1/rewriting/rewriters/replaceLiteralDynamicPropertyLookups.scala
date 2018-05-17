/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_1.rewriting.rewriters

import org.opencypher.v9_1.expressions.{ContainerIndex, Property, StringLiteral}
import org.opencypher.v9_0.util.{Rewriter, bottomUp}
import org.opencypher.v9_1.expressions.PropertyKeyName

case object replaceLiteralDynamicPropertyLookups extends Rewriter {

  private val instance = bottomUp(Rewriter.lift {
    case index @ ContainerIndex(expr, lit: StringLiteral) =>
      Property(expr, PropertyKeyName(lit.value)(lit.position))(index.position)
  })

  override def apply(v: AnyRef): AnyRef = instance(v)
}
