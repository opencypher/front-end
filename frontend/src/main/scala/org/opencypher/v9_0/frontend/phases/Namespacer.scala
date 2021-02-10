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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.ProcedureResultItem
import org.opencypher.v9_0.ast.ProjectingUnionAll
import org.opencypher.v9_0.ast.ProjectingUnionDistinct
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.UnionAll
import org.opencypher.v9_0.ast.UnionDistinct
import org.opencypher.v9_0.ast.semantics.Scope
import org.opencypher.v9_0.ast.semantics.SemanticFeature
import org.opencypher.v9_0.ast.semantics.SymbolUse
import org.opencypher.v9_0.expressions.ExpressionWithOuterScope
import org.opencypher.v9_0.expressions.ProcedureOutput
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.CompilationPhase.AST_REWRITE
import org.opencypher.v9_0.frontend.phases.factories.PlanPipelineTransformerFactory
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.conditions.containsNoNodesOfType
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.Ref
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.inSequence

case object AmbiguousNamesDisambiguated extends StepSequencer.Condition

/**
 * Rename variables so they are all unique.
 */
case object Namespacer extends Phase[BaseContext, BaseState, BaseState] with StepSequencer.Step with PlanPipelineTransformerFactory {
  type VariableRenamings = Map[Ref[Variable], Variable]

  override def phase: CompilationPhase = AST_REWRITE

  override def process(from: BaseState, ignored: BaseContext): BaseState = {
    val withProjectedUnions = from.statement().endoRewrite(projectUnions)
    val table = from.semanticTable()

    val ambiguousNames = shadowedNames(from.semantics().scopeTree)
    val variableDefinitions: Map[SymbolUse, SymbolUse] = from.semantics().scopeTree.allVariableDefinitions
    val renamings = variableRenamings(withProjectedUnions, variableDefinitions, ambiguousNames)

    if (renamings.isEmpty) {
      from.withStatement(withProjectedUnions).withSemanticTable(table)
    } else {
      val rewriter = renamingRewriter(renamings)
      val newStatement = withProjectedUnions.endoRewrite(rewriter)
      val newSemanticTable = table.replaceExpressions(rewriter)
      from.withStatement(newStatement).withSemanticTable(newSemanticTable)
    }
  }

  private def shadowedNames(scopeTree: Scope): Set[String] = {
    val definitions = scopeTree.allSymbolDefinitions

    definitions.collect {
      case (name, symbolDefinitions) if symbolDefinitions.size > 1 => name
    }.toSet
  }

  private def variableRenamings(statement: Statement, variableDefinitions: Map[SymbolUse, SymbolUse],
                                ambiguousNames: Set[String]): VariableRenamings =
    statement.treeFold(Map.empty[Ref[Variable], Variable]) {
      case i: Variable if ambiguousNames(i.name) =>
        val renaming = createVariableRenaming(variableDefinitions, i)
        acc => TraverseChildren(acc + renaming)
      case e: ExpressionWithOuterScope =>
        val renamings = e.outerScope
          .filter(v => ambiguousNames(v.name))
          .foldLeft(Set[(Ref[Variable], Variable)]()) { (innerAcc, v) =>
            innerAcc + createVariableRenaming(variableDefinitions, v)
          }
        acc => TraverseChildren(acc ++ renamings)
    }

  private def createVariableRenaming(variableDefinitions: Map[SymbolUse, SymbolUse], v: Variable): (Ref[Variable], Variable) = {
    val symbolDefinition = variableDefinitions(SymbolUse(v))
    val newVariable = v.renameId(s"  ${symbolDefinition.nameWithPosition}")
    val renaming = Ref(v) -> newVariable
    renaming
  }

  private def projectUnions: Rewriter =
    bottomUp(Rewriter.lift {
      case u: UnionAll => ProjectingUnionAll(u.part, u.query, u.unionMappings)(u.position)
      case u: UnionDistinct => ProjectingUnionDistinct(u.part, u.query, u.unionMappings)(u.position)
    })

  private def renamingRewriter(renamings: VariableRenamings): Rewriter = inSequence(
    bottomUp(Rewriter.lift {
      case item@ProcedureResultItem(None, v: Variable) if renamings.contains(Ref(v)) =>
        item.copy(output = Some(ProcedureOutput(v.name)(v.position)))(item.position)
    }),
    bottomUp(Rewriter.lift {
      case v: Variable =>
        renamings.get(Ref(v)) match {
          case Some(newVariable) => newVariable
          case None              => v
        }
      case e: ExpressionWithOuterScope =>
        val newOuterScope = e.outerScope.map(v => {
          renamings.get(Ref(v)) match {
            case Some(newVariable) => newVariable
            case None              => v
          }
        })
        e.withOuterScope(newOuterScope)
    }))

  override def preConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable

  override def postConditions: Set[StepSequencer.Condition] = Set(
    StatementCondition(containsNoNodesOfType[UnionAll]),
    StatementCondition(containsNoNodesOfType[UnionDistinct]),
    AmbiguousNamesDisambiguated
  )

  override def invalidatedConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable // Introduces new AST nodes

  override def getTransformer(pushdownPropertyReads: Boolean,
                              semanticFeatures: Seq[SemanticFeature]): Transformer[BaseContext, BaseState, BaseState] = this
}
