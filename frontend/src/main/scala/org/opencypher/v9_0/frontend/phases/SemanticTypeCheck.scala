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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.semantics.SemanticError
import org.opencypher.v9_0.ast.semantics.SemanticTable
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.functions.Exists
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.SEMANTIC_TYPE_CHECK
import org.opencypher.v9_0.frontend.phases.PatternExpressionInNonExistenceCheck.patternExpressionInNonExistenceCheck
import org.opencypher.v9_0.frontend.phases.SemanticTypeCheck.SemanticErrorCheck
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CTBoolean

/**
 * Checks for semantic errors when semantic table has been initialized.
 *
 * Does not change the State, just checks for semantic errors.
 */
case object SemanticTypeCheck extends Phase[BaseContext, BaseState, BaseState] {

  type SemanticErrorCheck = BaseState => Seq[SemanticError]

  val checks: Seq[SemanticErrorCheck] = Seq(
    patternExpressionInNonExistenceCheck
  )

  override def process(from: BaseState, context: BaseContext): BaseState = {
    context.errorHandler(checks.flatMap(_.apply(from)))

    from
  }

  override val phase = SEMANTIC_TYPE_CHECK

  override def postConditions: Set[StepSequencer.Condition] = Set.empty

}

object PatternExpressionInNonExistenceCheck {

  private def isExpectedTypeBoolean(semanticTable: SemanticTable, e: Expression) =
    semanticTable.types.get(e)
      .flatMap(_.expected)
      .exists(CTBoolean.covariant.containsAll)

  def patternExpressionInNonExistenceCheck: SemanticErrorCheck = baseState => {

    baseState.statement().folder.treeFold(Seq.empty[SemanticError]) {
      case Exists(_) =>
        // Don't look inside exists()
        errors => SkipChildren(errors)

      case p: PatternExpression if !isExpectedTypeBoolean(baseState.semanticTable(), p) =>
        errors => SkipChildren(errors :+ SemanticError(errorMessage, p.position))
    }
  }

  val errorMessage: String = "A pattern expression should only be used in order to test the existence of a pattern. " +
    "It should therefore only be used in contexts that evaluate to a boolean, e.g. inside the function exists() or in a WHERE-clause. " +
    "No other uses are allowed, instead they should be replaced by a pattern comprehension."
}
