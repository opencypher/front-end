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

import org.opencypher.v9_0.expressions.NotEquals
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.AST_REWRITE
import org.opencypher.v9_0.rewriting.conditions.containsNoNodesOfType
import org.opencypher.v9_0.rewriting.conditions.containsNoReturnAll
import org.opencypher.v9_0.rewriting.conditions.noDuplicatesInReturnItems
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInMatch
import org.opencypher.v9_0.rewriting.conditions.noUnnamedPatternElementsInPatternComprehension
import org.opencypher.v9_0.rewriting.conditions.normalizedEqualsArguments
import org.opencypher.v9_0.rewriting.rewriters.InnerVariableNamer
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType

/**
 * Normalize the AST into a form easier for the planner to work with.
 */
case class AstRewriting(innerVariableNamer: InnerVariableNamer,
                        parameterTypeMapping : Map[String, CypherType] = Map.empty
) extends Phase[BaseContext, BaseState, BaseState] {

  private val astRewriter = new ASTRewriter(innerVariableNamer)

  override def process(in: BaseState, context: BaseContext): BaseState = {
    val rewrittenStatement = astRewriter.rewrite(in.statement(), in.semantics(), parameterTypeMapping, context.cypherExceptionFactory)
    in.withStatement(rewrittenStatement)
  }

  override def phase = AST_REWRITE

  override def postConditions: Set[StepSequencer.Condition] = {
    val rewriterConditions = Set(
      noDuplicatesInReturnItems,
      containsNoReturnAll,
      noUnnamedPatternElementsInMatch,
      containsNoNodesOfType[NotEquals],
      normalizedEqualsArguments,
      noUnnamedPatternElementsInPatternComprehension
    )

    rewriterConditions.map(StatementCondition.apply)
  }
}
