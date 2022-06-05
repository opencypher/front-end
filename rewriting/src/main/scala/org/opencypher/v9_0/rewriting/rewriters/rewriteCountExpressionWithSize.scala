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
import org.opencypher.v9_0.expressions.CountExpression
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.expressions.functions.Size
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object NoCountExpression extends StepSequencer.Condition

/**
 * Rewrites `COUNT { Pattern [WHERE] }`` TO `size([pt=Pattern [WHERE] | pt)`
 *      and `COUNT { (n) } TO size((n))`
 */
case class rewriteCountExpression(anonymousVariableNameGenerator: AnonymousVariableNameGenerator) extends Rewriter {

  private val instance = topDown(Rewriter.lift {
    case e @ CountExpression(pattern: RelationshipChain, maybeWhere) =>
      val pos = e.position
      val variableToCollect = anonymousVariableNameGenerator.nextName
      val collectionName = anonymousVariableNameGenerator.nextName
      val projection = Variable(anonymousVariableNameGenerator.nextName)(pos)

      Size(
        PatternComprehension(
          namedPath = Some(projection),
          pattern = RelationshipsPattern(pattern)(pos),
          predicate = maybeWhere,
          projection = projection
        )(pos, e.outerScope, variableToCollect, collectionName)
      )(pos)
    case e @ CountExpression(NodePattern(Some(variable), None, None, None), None) =>
      val pos = e.position
      Size(ListLiteral(List(variable))(pos))(pos)
  })

  override def apply(v: AnyRef): AnyRef = instance(v)

}

object rewriteCountExpression extends Step with ASTRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set()

  override def postConditions: Set[StepSequencer.Condition] = Set(NoCountExpression)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set()

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = rewriteCountExpression(anonymousVariableNameGenerator)
}
