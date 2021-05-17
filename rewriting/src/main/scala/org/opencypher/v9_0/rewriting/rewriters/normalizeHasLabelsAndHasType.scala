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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.HasTypes
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AllNameGenerators
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object HasLabelsOrTypesReplacedIfPossible extends StepSequencer.Condition


trait HasLabelsAndHasTypeNormalizer extends Rewriter {

  override def apply(that: AnyRef): AnyRef = instance(that)

  def rewrite(expression: Expression): Expression = expression match {
    case p@HasLabelsOrTypes(e, labels) if isNode(e)         =>
      HasLabels(e, labels.map(l => LabelName(l.name)(l.position)))(p.position)
    case p@HasLabelsOrTypes(e, labels) if isRelationship(e) =>
      HasTypes(e, labels.map(l => RelTypeName(l.name)(l.position)))(p.position)
    case e =>
      e
  }

  protected val instance: Rewriter = topDown(Rewriter.lift{
    case e:Expression => rewrite(e)
  })

  def isNode(expr: Expression): Boolean
  def isRelationship(expr: Expression): Boolean
}

case class normalizeHasLabelsAndHasType(semanticState: SemanticState) extends HasLabelsAndHasTypeNormalizer  {
  def isNode(expr: Expression): Boolean =
    semanticState.expressionType(expr).actual == CTNode.invariant

  def isRelationship(expr: Expression): Boolean =
    semanticState.expressionType(expr).actual == CTRelationship.invariant
}

object normalizeHasLabelsAndHasType extends StepSequencer.Step with ASTRewriterFactory {
  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(HasLabelsOrTypesReplacedIfPossible)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo, // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def getRewriter(semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory,
                           allNameGenerators: AllNameGenerators): Rewriter = normalizeHasLabelsAndHasType(semanticState)
}
