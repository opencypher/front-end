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
package org.opencypher.v9_0.logical.plans

import org.opencypher.v9_0.ast.semantics.SemanticCheck
import org.opencypher.v9_0.ast.semantics.SemanticCheckableExpression
import org.opencypher.v9_0.expressions.BooleanExpression
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Expression.SemanticContext
import org.opencypher.v9_0.util.InputPosition

case class CoerceToPredicate(inner: Expression) extends BooleanExpression with SemanticCheckableExpression {

  override def semanticCheck(ctx: SemanticContext): SemanticCheck = SemanticCheck.success

  override def asCanonicalStringVal: String = {
    s"CoerceToPredicate(${inner.asCanonicalStringVal})"
  }

  override def position: InputPosition = InputPosition.NONE
}
