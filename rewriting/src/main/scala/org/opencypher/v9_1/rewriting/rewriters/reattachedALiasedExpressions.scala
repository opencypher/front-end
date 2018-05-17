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

import org.opencypher.v9_1.ast.{Return, ReturnItem, With}
import org.opencypher.v9_1.expressions._
import org.opencypher.v9_0.util.{Rewriter, bottomUp}

case object reattachAliasedExpressions extends Rewriter {
  override def apply(in: AnyRef): AnyRef = findingRewriter.apply(in)

  private val findingRewriter: Rewriter = bottomUp(Rewriter.lift {
    case clause: Return =>
      val innerRewriter = expressionRewriter(clause.returnItems.items)
      clause.copy(
        orderBy = clause.orderBy.endoRewrite(innerRewriter)
      )(clause.position)

    case clause: With =>
      val innerRewriter = expressionRewriter(clause.returnItems.items)
      clause.copy(
        orderBy = clause.orderBy.endoRewrite(innerRewriter)
      )(clause.position)
  })

  private def expressionRewriter(items: Seq[ReturnItem]): Rewriter = {
    val aliasedExpressions: Map[String, Expression] = items.map { returnItem =>
      (returnItem.name, returnItem.expression)
    }.toMap

    bottomUp(Rewriter.lift {
      case id@Variable(name) if aliasedExpressions.contains(name) => aliasedExpressions(name)
    })
  }
}
