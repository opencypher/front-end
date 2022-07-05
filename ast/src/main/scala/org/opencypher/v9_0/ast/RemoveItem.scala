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

import org.opencypher.v9_0.ast.semantics.SemanticCheckable
import org.opencypher.v9_0.ast.semantics.SemanticExpressionCheck
import org.opencypher.v9_0.ast.semantics.SemanticPatternCheck
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.LogicalProperty
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.symbols.CTNode

sealed trait RemoveItem extends ASTNode with SemanticCheckable

case class RemoveLabelItem(variable: LogicalVariable, labels: Seq[LabelName])(val position: InputPosition)
    extends RemoveItem {

  def semanticCheck =
    SemanticExpressionCheck.simple(variable) chain
      SemanticPatternCheck.checkValidLabels(labels, position) chain
      SemanticExpressionCheck.expectType(CTNode.covariant, variable)
}

case class RemovePropertyItem(property: LogicalProperty) extends RemoveItem {
  def position = property.position

  def semanticCheck = SemanticExpressionCheck.simple(property) chain
    SemanticPatternCheck.checkValidPropertyKeyNames(Seq(property.propertyKey))
}
