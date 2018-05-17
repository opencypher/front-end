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
package org.opencypher.v9_1.rewriting.conditions

import org.opencypher.v9_0.ast.{OrderBy, SortItem}
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_1.rewriting.Condition

case object orderByOnlyOnVariables extends Condition {
  def apply(that: Any): Seq[String] = {
    val orderBys = collectNodesOfType[OrderBy].apply(that)
    orderBys.flatMap { orderBy =>
      orderBy.sortItems.collect {
        case item: SortItem if !item.expression.isInstanceOf[Variable] =>
          s"OrderBy at ${orderBy.position} is ordering on an expression (${item.expression}) instead of a variable"
      }
    }
  }

  override def name: String = productPrefix
}
