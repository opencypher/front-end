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
package org.opencypher.v9_0.ast

import org.opencypher.v9_0.ast.semantics.SemanticCheck
import org.opencypher.v9_0.ast.semantics.SemanticCheckable
import org.opencypher.v9_0.ast.semantics.SemanticExpressionCheck
import org.opencypher.v9_0.ast.semantics.SemanticPatternCheck
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.symbols.CTBoolean

case class Where(expression: Expression)(val position: InputPosition)
    extends ASTNode with SemanticCheckable {

  def dependencies: Set[LogicalVariable] = expression.dependencies

  def semanticCheck: SemanticCheck = Where.checkExpression(expression)
}

object Where {

  def checkExpression(expression: Expression): SemanticCheck =
    SemanticExpressionCheck.simple(expression) chain
      SemanticPatternCheck.checkValidPropertyKeyNames(
        expression.folder.findAllByClass[Property].map(prop => prop.propertyKey)
      ) chain
      SemanticExpressionCheck.expectType(CTBoolean.covariant, expression)
}
