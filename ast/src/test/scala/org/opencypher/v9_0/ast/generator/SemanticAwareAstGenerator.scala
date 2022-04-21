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
package org.opencypher.v9_0.ast.generator

import org.opencypher.v9_0.ast.generator.AstGenerator.boolean
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.MapProjection
import org.opencypher.v9_0.expressions.functions.Avg
import org.opencypher.v9_0.expressions.functions.Collect
import org.opencypher.v9_0.expressions.functions.Count
import org.opencypher.v9_0.expressions.functions.Max
import org.opencypher.v9_0.expressions.functions.Min
import org.opencypher.v9_0.expressions.functions.PercentileCont
import org.opencypher.v9_0.expressions.functions.PercentileDisc
import org.opencypher.v9_0.expressions.functions.StdDev
import org.opencypher.v9_0.expressions.functions.StdDevP
import org.opencypher.v9_0.expressions.functions.Sum
import org.scalacheck.Gen
import org.scalacheck.Gen.frequency
import org.scalacheck.Gen.listOfN
import org.scalacheck.Gen.oneOf

/**
 * Prototype of a generator that generates semantically valid expressions/ASTs.
 */
class SemanticAwareAstGenerator(simpleStrings: Boolean = true, allowedVarNames: Option[Seq[String]] = None)
    extends AstGenerator(simpleStrings, allowedVarNames) {

  private val supportedAggregationFunctions =
    Seq(Avg, Collect, Count, Max, Min, PercentileCont, PercentileDisc, StdDev, StdDevP, Sum)

  // FIXME this generates too many invalid combinations
  def aggregationFunctionInvocation: Gen[FunctionInvocation] = for {
    function <- oneOf(supportedAggregationFunctions)
    signature <- oneOf(function.signatures)
    numArgs = signature.argumentTypes.length
    distinct <- boolean
    args <- listOfN(numArgs, nonAggregatingExpression)
    (ns, name) = function.asFunctionName(pos)
  } yield FunctionInvocation(ns, name, distinct, args.toIndexedSeq)(pos)

  def aggregatingExpression: Gen[Expression] =
    frequency(
      supportedAggregationFunctions.size ->
        aggregationFunctionInvocation,
      1 ->
        _countStar
    )

  def nonAggregatingExpression: Gen[Expression] =
    _expression.suchThat { expr =>
      !expr.containsAggregate && !expr.folder.treeExists {
        case _: FunctionInvocation =>
          true // not interested in randomly-generated functions, we'll just end up with `scala.MatchError: UnresolvedFunction`
        case _: MapProjection =>
          true // org.neo4j.exceptions.InternalException: `MapProjection` should have been rewritten away
      }
    }
}
