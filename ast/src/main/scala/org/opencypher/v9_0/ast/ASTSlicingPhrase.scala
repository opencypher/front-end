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
package org.opencypher.v9_0.ast

import org.opencypher.v9_0.ast.semantics.SemanticAnalysisTooling
import org.opencypher.v9_0.ast.semantics.SemanticCheck
import org.opencypher.v9_0.ast.semantics.SemanticCheckResult
import org.opencypher.v9_0.ast.semantics.SemanticCheckable
import org.opencypher.v9_0.ast.semantics.SemanticError
import org.opencypher.v9_0.ast.semantics.SemanticExpressionCheck
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Literal
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.PathExpression
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.symbols.CTInteger

// Skip/Limit
trait ASTSlicingPhrase extends SemanticCheckable with SemanticAnalysisTooling {
  self: ASTNode =>
  def name: String
  def dependencies: Set[LogicalVariable] = expression.dependencies
  def expression: Expression

  def semanticCheck: SemanticCheck =
    containsNoVariables chain
      doesNotTouchTheGraph chain
      literalShouldBeUnsignedInteger chain
      SemanticExpressionCheck.simple(expression) chain
      expectType(CTInteger.covariant, expression)

  private def containsNoVariables: SemanticCheck = {
    val deps = dependencies
    if (deps.nonEmpty) {
      val id = deps.toSeq.minBy(_.position)
      error(s"It is not allowed to refer to variables in $name", id.position)
    }
    else SemanticCheckResult.success
  }

  private def doesNotTouchTheGraph: SemanticCheck = {
    val badExpressionFound = expression.treeExists {
      case _: PatternComprehension |
           _: PatternExpression |
           _: PathExpression =>
        true
    }
    when(badExpressionFound) {
      error(s"It is not allowed to refer to variables in $name", expression.position)
    }
  }

  private def literalShouldBeUnsignedInteger: SemanticCheck = {
    try {
      expression match {
        case _: UnsignedDecimalIntegerLiteral => SemanticCheckResult.success
        case i: SignedDecimalIntegerLiteral if i.value >= 0 => SemanticCheckResult.success
        case lit: Literal => error(s"Invalid input. '${lit.asCanonicalStringVal}' is not a valid value. Must be a non-negative integer.", lit.position)
        case _ => SemanticCheckResult.success
      }
    } catch {
      case nfe: NumberFormatException => SemanticError("Invalid input for " + name +
        ". Either the string does not have the appropriate format or the provided number is bigger then 2^63-1", expression.position)
    }
  }
}
