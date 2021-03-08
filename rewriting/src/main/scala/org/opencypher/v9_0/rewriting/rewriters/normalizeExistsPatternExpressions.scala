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

import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.FilteringExpression
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.LessThan
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.functions.Exists
import org.opencypher.v9_0.expressions.functions.Size
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionAreWrappedInExists
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Foldable.FoldableAny
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.IdentityMap
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Condition
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols
import org.opencypher.v9_0.util.symbols.CypherType



/**
 * Adds an exists() around any pattern expression that is expected to produce a boolean e.g.
 *
 * MATCH (n) WHERE (n)-->(m) RETURN n
 *
 * is rewritten to
 *
 * MATCH (n) WHERE EXISTS((n)-->(m)) RETURN n
 *
 * Rewrite equivalent expressions with `size` or `length` to `exists`.
 * This rewrite normalizes this cases and make it easier to plan correctly.
 *
 * [[simplifyPredicates]]  takes care of rewriting the Not(Not(Exists(...))) which can be introduced by this rewriter.
 *
 * This rewriter needs to run before [[namePatternElements]], which rewrites pattern expressions. Otherwise we don't find them in the semantic table.
 */
case class normalizeExistsPatternExpressions(semanticState: SemanticState) extends Rewriter {

  override def apply(v: AnyRef): AnyRef = {
    val replacements: IdentityMap[AnyRef, AnyRef] = computeReplacements(v)
    createRewriter(replacements).apply(v)
  }

  private def computeReplacements(v: AnyRef): IdentityMap[AnyRef, AnyRef] = {
    v.treeFold(IdentityMap.empty[AnyRef, AnyRef]) {
      // find replacements for pattern expressions, ONLY if they are used in supported places (e.g. inside WHERE clause or inside a pattern comprehension)
      case w: Where =>
        acc => TraverseChildren(patternExpressionAsBooleanReplacements(w, acc))
      case e:FilteringExpression if e.innerPredicate.isDefined =>
        acc => SkipChildren(patternExpressionAsBooleanReplacements(e, acc))
      case p@PatternComprehension(_, _, Some(predicate), _) =>
        acc => SkipChildren(patternExpressionAsBooleanReplacements(p, acc))

      // other replacements for pattern expressions
      case g@GreaterThan(Size(p: PatternExpression), SignedDecimalIntegerLiteral("0")) =>
        acc => SkipChildren(acc.updated(g, Exists(p)(p.position)))
      case l@LessThan(SignedDecimalIntegerLiteral("0"), Size(p: PatternExpression)) =>
        acc => SkipChildren(acc.updated(l, Exists(p)(p.position)))
      case e@Equals(Size(p: PatternExpression), SignedDecimalIntegerLiteral("0")) =>
        acc => SkipChildren(acc.updated(e, Not(Exists(p)(p.position))(p.position)))
      case e@Equals(SignedDecimalIntegerLiteral("0"), Size(p: PatternExpression)) =>
        acc => SkipChildren(acc.updated(e, Not(Exists(p)(p.position))(p.position)))

      case _ =>
        acc => TraverseChildren(acc)
    }
  }

  private def patternExpressionAsBooleanReplacements(v: AnyRef, accumulator: IdentityMap[AnyRef, AnyRef]): IdentityMap[AnyRef, AnyRef] = {
    v.treeFold(accumulator) {
      case p: PatternExpression if semanticState.expressionType(p).expected.contains(symbols.CTBoolean.invariant) =>
        acc => SkipChildren(acc.updated(p, Exists(p)(p.position)))
      case _ =>
        acc => TraverseChildren(acc)
    }
  }

  private def createRewriter(replacements: IdentityMap[AnyRef, AnyRef]): Rewriter = {
    bottomUp(Rewriter.lift {
      case that => replacements.getOrElse(that, that)
    })
  }
}

object normalizeExistsPatternExpressions extends StepSequencer.Step with ASTRewriterFactory {
  override def preConditions: Set[Condition] = Set(
    PatternExpressionsHaveSemanticInfo // Looks up type of pattern expressions
  )

  override def postConditions: Set[Condition] = Set(PatternExpressionAreWrappedInExists)

  // TODO capture the dependency with simplifyPredicates
  override def invalidatedConditions: Set[Condition] = Set(
    ProjectionClausesHaveSemanticInfo // It can invalidate this condition by rewriting things inside WITH/RETURN.
  )

  override def getRewriter(innerVariableNamer: InnerVariableNamer,
                           semanticState: SemanticState,
                           parameterTypeMapping: Map[String, CypherType],
                           cypherExceptionFactory: CypherExceptionFactory): Rewriter = normalizeExistsPatternExpressions(semanticState)
}