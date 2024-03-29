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
package org.opencypher.v9_0.expressions

import org.opencypher.v9_0.expressions.functions.AggregatingFunction
import org.opencypher.v9_0.expressions.functions.UserDefinedFunctionInvocation

object IsAggregate {

  def unapply(v: Any) = v match {
    case expr: CountStar =>
      Some(expr)

    case fi: FunctionInvocation if fi.distinct =>
      Some(fi)

    case fi: FunctionInvocation =>
      fi.function match {
        case _: AggregatingFunction => Some(fi)
        case _                      => None
      }

    case fi: UserDefinedFunctionInvocation if fi.isAggregate =>
      Some(fi)

    case _ =>
      None
  }

  def apply(e: Expression): Boolean = unapply(e).nonEmpty
}
