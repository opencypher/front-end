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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.CallClause
import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.Create
import org.opencypher.v9_0.ast.Limit
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.Merge
import org.opencypher.v9_0.ast.PeriodicCommitHint
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.SetClause
import org.opencypher.v9_0.ast.Unwind
import org.opencypher.v9_0.ast.With
import org.opencypher.v9_0.expressions.AutoExtractedParameter
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.DoubleLiteral
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.IntegerLiteral
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.ListOfLiteralWriter
import org.opencypher.v9_0.expressions.Literal
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.Foldable
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.IdentityMap
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTFloat
import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTString

object literalReplacement {

  case class LiteralReplacement(parameter: Parameter, value: AnyRef)
  type LiteralReplacements = IdentityMap[Expression, LiteralReplacement]

  case class ExtractParameterRewriter(replaceableLiterals: LiteralReplacements) extends Rewriter {
    def apply(that: AnyRef): AnyRef = rewriter.apply(that)

    private val rewriter: Rewriter = bottomUp(Rewriter.lift {
      case l: Expression if replaceableLiterals.contains(l) => replaceableLiterals(l).parameter
    })
  }

  private val literalMatcher: PartialFunction[Any, LiteralReplacements => Foldable.FoldingBehavior[LiteralReplacements]] = {
    case _: Match |
         _: Create |
         _: Merge |
         _: SetClause |
         _: Return |
         _: With |
         _: Unwind |
         _: CallClause =>
      acc => TraverseChildren(acc)
    case _: Clause |
         _: PeriodicCommitHint |
         _: Limit =>
      acc => SkipChildren(acc)
    case n: NodePattern =>
      acc => SkipChildren(n.properties.treeFold(acc)(literalMatcher))
    case r: RelationshipPattern =>
      acc => SkipChildren(r.properties.treeFold(acc)(literalMatcher))
    case ContainerIndex(_, _: StringLiteral) =>
      acc => SkipChildren(acc)
    case l: StringLiteral =>
      acc =>
        if (acc.contains(l)) SkipChildren(acc) else {
          val parameter = AutoExtractedParameter(s"  AUTOSTRING${acc.size}", CTString, l)(l.position)
          SkipChildren(acc + (l -> LiteralReplacement(parameter, l.value)))
        }
    case l: IntegerLiteral =>
      acc =>
        if (acc.contains(l)) SkipChildren(acc) else {
          val parameter = AutoExtractedParameter(s"  AUTOINT${acc.size}", CTInteger, l)(l.position)
          SkipChildren(acc + (l -> LiteralReplacement(parameter, l.value)))
        }
    case l: DoubleLiteral =>
      acc =>
        if (acc.contains(l)) SkipChildren(acc) else {
          val parameter = AutoExtractedParameter(s"  AUTODOUBLE${acc.size}", CTFloat, l)(l.position)
          SkipChildren(acc + (l -> LiteralReplacement(parameter, l.value)))
        }
    case l: ListLiteral if l.expressions.forall(_.isInstanceOf[Literal]) =>
      acc =>
        if (acc.contains(l)) SkipChildren(acc) else {
          val literals = l.expressions.map(_.asInstanceOf[Literal])
          val parameter = AutoExtractedParameter(s"  AUTOLIST${acc.size}", CTList(CTAny), ListOfLiteralWriter(literals))(l.position)
          SkipChildren(acc + (l -> LiteralReplacement(parameter, literals.map(_.value))))
        }
  }

  private def doIt(term: ASTNode) = {
    val replaceableLiterals = term.treeFold(IdentityMap.empty: LiteralReplacements)(literalMatcher)

    val extractedParams: Map[String, AnyRef] = replaceableLiterals.map {
      case (_, LiteralReplacement(parameter, value)) => (parameter.name, value)
    }

    (ExtractParameterRewriter(replaceableLiterals), extractedParams)
  }

  def apply(term: ASTNode, paramExtraction: LiteralExtraction): (Rewriter, Map[String, Any]) = paramExtraction match {
    case Never =>
      Rewriter.noop -> Map.empty
    case Forced =>
      doIt(term)
    case IfNoParameter =>
      val containsParameter: Boolean = term.treeExists {
        case _: Parameter => true
      }

      if (containsParameter) Rewriter.noop -> Map.empty
      else doIt(term)
  }
}

sealed trait LiteralExtraction
case object Forced extends LiteralExtraction
case object IfNoParameter extends LiteralExtraction
case object Never extends LiteralExtraction
