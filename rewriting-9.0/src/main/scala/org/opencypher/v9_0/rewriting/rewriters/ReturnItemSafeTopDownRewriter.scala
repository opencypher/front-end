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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.util.Foldable.TreeAny
import org.opencypher.v9_0.util.Rewritable._
import org.opencypher.v9_0.util.{InternalException, Rewritable, Rewriter}

import scala.annotation.tailrec
import scala.collection.mutable

/*
This rewriter is an alternative to the topDown rewriter that does the same thing,
but does not rewrite ReturnItem alias, only the projected expression
*/
case class ReturnItemSafeTopDownRewriter(inner: Rewriter) extends Rewriter {

  override def apply(that: AnyRef): AnyRef = {
    val initialStack = mutable.ArrayStack((List(that), new mutable.MutableList[AnyRef]()))
    val result = tailrecApply(initialStack)
    assert(result.size == 1)
    result.head
  }

  @tailrec
  private def tailrecApply(stack: mutable.ArrayStack[(List[AnyRef], mutable.MutableList[AnyRef])]): mutable.MutableList[AnyRef] = {
    val (currentJobs, _) = stack.top
    if (currentJobs.isEmpty) {
      val (_, newChildren) = stack.pop()
      if (stack.isEmpty) {
        newChildren
      } else {
        stack.pop() match {
          case (Nil, _) => throw new InternalException("only to stop warnings. should never happen")
          case ((returnItem@AliasedReturnItem(expression, variable)) :: jobs, doneJobs) =>
            val newExpression = newChildren.head.asInstanceOf[Expression]
            val newReturnItem = returnItem.copy(expression = newExpression)(returnItem.position)
            stack.push((jobs, doneJobs += newReturnItem))
          case (job :: jobs, doneJobs) =>
            val doneJob = Rewritable.dupAny(job, newChildren)
            stack.push((jobs, doneJobs += doneJob))
        }

        tailrecApply(stack)
      }
    } else {
      val (newJob :: jobs, doneJobs) = stack.pop()
      val rewrittenJob = newJob.rewrite(inner)
      stack.push((rewrittenJob :: jobs, doneJobs))
      stack.push((rewrittenJob.children.toList, new mutable.MutableList()))
      tailrecApply(stack)
    }
  }

}
