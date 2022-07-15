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

import org.opencypher.v9_0.ast.ProjectionClause
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.semantics.SemanticErrorDef
import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.SEMANTIC_CHECK
import org.opencypher.v9_0.util.Foldable.FoldableAny
import org.opencypher.v9_0.util.StepSequencer

/**
 * Verify aggregation expressions and make sure there are no ambiguous grouping keys.
 */
case class AmbiguousAggregationAnalysis(features: SemanticFeature*)
    extends Phase[BaseContext, BaseState, BaseState] {

  override def process(from: BaseState, context: BaseContext): BaseState = {
    val errors = from.folder.fold(Seq.empty[SemanticErrorDef]) {
      // If we project '*', we don't need to check ambiguity since we group on all available variables.
      case projectionClause: ProjectionClause if !projectionClause.returnItems.includeExisting =>
        acc =>
          acc ++
            projectionClause.orderBy.toSeq.flatMap(_.checkIllegalOrdering(projectionClause.returnItems)) ++
            ReturnItems.checkAmbiguousGrouping(projectionClause.returnItems)
    }

    context.errorHandler(errors)
    from
  }

  override def phase: CompilationPhaseTracer.CompilationPhase = SEMANTIC_CHECK

  override def postConditions: Set[StepSequencer.Condition] = Set.empty
}
