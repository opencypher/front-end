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

import org.opencypher.v9_0.expressions._
import org.opencypher.v9_0.util.{Rewriter, bottomUp}

import scala.collection.immutable.TreeMap

case object replaceAliasedFunctionInvocations extends Rewriter {

  override def apply(that: AnyRef): AnyRef = instance(that)

  /*
   * These are historical names for functions. They are all subject to removal in an upcoming major release.
   */
  val deprecatedFunctionReplacements: Map[String, String] = TreeMap("toInt" -> "toInteger",
                                                                    "upper" -> "toUpper",
                                                                    "lower" -> "toLower",
                                                                    "rels" -> "relationships")(CaseInsensitiveOrdered)

  private def propertyOf(propertyKey:String): Expression => Expression =
    (incoming:Expression) => Property(incoming, PropertyKeyName(propertyKey)(incoming.position))(incoming.position)

  private val renameFunction: FunctionInvocation => Expression =
    (incoming:FunctionInvocation) => renameFunctionTo(deprecatedFunctionReplacements(incoming.name))(incoming)

  private def renameFunctionTo(newName: String) = (incoming: FunctionInvocation) =>
    incoming.copy(functionName = FunctionName(newName)(incoming.functionName.position))(incoming.position)

  private val aliases: Map[String, FunctionInvocation => Expression] = TreeMap("toInt" -> renameFunction,
                                                                               "upper" -> renameFunction,
                                                                               "lower" -> renameFunction,
                                                                               "rels" -> renameFunction,
                                                                               "timestamp" ->
                                                                                 renameFunctionTo("datetime")
                                                                                   .andThen(propertyOf("epochMillis")))(CaseInsensitiveOrdered)

  val instance: Rewriter = bottomUp(Rewriter.lift {
    case func@FunctionInvocation(_, FunctionName(name), _, _) if aliases.contains(name) => aliases(name)(func)
  })
}

object CaseInsensitiveOrdered extends Ordering[String] {
  def compare(x: String, y: String): Int =
    x.compareToIgnoreCase(y)
}
