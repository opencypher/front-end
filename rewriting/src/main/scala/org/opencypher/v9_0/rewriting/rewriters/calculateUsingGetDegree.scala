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

import org.opencypher.v9_0.expressions.Add
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.GetDegree
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.SemanticDirection

/*
 * Calculates how to transform a pattern (a)-[:R1:R2...]->() to getDegree call
 * of that very pattern.
 */
object calculateUsingGetDegree {

  def apply(expr: Expression, node: LogicalVariable, types: Seq[RelTypeName], dir: SemanticDirection): Expression = {
      types
        .map(typ => GetDegree(node.copyId, Some(typ), dir)(typ.position))
        .reduceOption[Expression](Add(_, _)(expr.position))
        .getOrElse(GetDegree(node, None, dir)(expr.position))
    }
}
