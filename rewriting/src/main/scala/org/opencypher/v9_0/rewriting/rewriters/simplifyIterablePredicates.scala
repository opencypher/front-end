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
import org.opencypher.v9_0.expressions.AnyIterablePredicate
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.FilterScope
import org.opencypher.v9_0.expressions.In
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.NoneIterablePredicate
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.rewriting.conditions.SemanticInfoAvailable
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CypherType

case object IterablePredicatesRewrittenToIn extends StepSequencer.Condition

/**
 * Rewrites [[org.opencypher.v9_0.expressions.IterablePredicateExpression]]s to IN expressions when possible.
 *
 * For example:
 * any(x IN list WHERE x = 1) ==> 1 IN x
 * none(x IN list WHERE x = 2) ==> not(2 IN x)
 */
case object simplifyIterablePredicates extends Rewriter with StepSequencer.Step with ASTRewriterFactory {

  private val instance: Rewriter = bottomUp(Rewriter.lift {
    case any @ AnyIterablePredicate(SimpleEqualsFilterScope(inLhs), list) => In(inLhs, list)(any.position)
    case none @ NoneIterablePredicate(SimpleEqualsFilterScope(inLhs), list) => Not(In(inLhs, list)(none.position))(none.position)
  })

  override def apply(that: AnyRef): AnyRef = instance(that)

  override def preConditions: Set[StepSequencer.Condition] = Set(
    RelationshipUniquenessPredicatesInMatchAndMerge // Introduces AnyIterablePredicate and NoneIterablePredicate
  )

  override def postConditions: Set[StepSequencer.Condition] = Set(IterablePredicatesRewrittenToIn)

  override def invalidatedConditions: Set[StepSequencer.Condition] = SemanticInfoAvailable // Introduces new AST nodes

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = instance
}

object SimpleEqualsFilterScope {
  def unapply(scope: FilterScope): Option[Expression] = scope match {
    case FilterScope(scope, Some(EqualEquivalent(lhs, rhs)))
      if scope == lhs && !rhs.dependencies.contains(scope) => Some(rhs)
    case FilterScope(scope, Some(EqualEquivalent(lhs, rhs)))
      if scope == rhs && !lhs.dependencies.contains(scope) => Some(lhs)
    case _ => None
  }
}

object EqualEquivalent {
  def unapply(expression: Expression): Option[(Expression, Expression)] = expression match {
    case Equals(lhs, rhs) => Some((lhs, rhs))
    case In(lhs, ListLiteral(Seq(singleItem))) => Some((lhs, singleItem))
    case _ => None
  }
}