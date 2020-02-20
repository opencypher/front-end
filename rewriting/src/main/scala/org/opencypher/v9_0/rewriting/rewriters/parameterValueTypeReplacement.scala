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

import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.IdentityMap
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.symbols.CTAny

object parameterValueTypeReplacement {

  case class ParameterValueTypeReplacement(parameter: Parameter, value: AnyRef)
  type ParameterValueTypeReplacements = IdentityMap[Expression, ParameterValueTypeReplacement]

  case class ExtractParameterRewriter(replaceableParameters: ParameterValueTypeReplacements) extends Rewriter {
    def apply(that: AnyRef): AnyRef = rewriter.apply(that)

    private val rewriter: Rewriter = bottomUp(Rewriter.lift {
      case p: Parameter => replaceableParameters(p).parameter
    })
  }

  private def rewriteParameterValueTypes(term: ASTNode, paramTypes: Map[String, CypherType]) = {
    val replaceableParameters = term.treeFold(IdentityMap.empty: ParameterValueTypeReplacements){
      case p@Parameter(_, CTAny) =>
        acc =>
          if (acc.contains(p)) (acc, None) else {
            val cypherType = paramTypes.getOrElse(p.name, CTAny)
            (acc + (p -> ParameterValueTypeReplacement(Parameter(p.name, cypherType)(p.position), p.name)), None)
          }
    }
    ExtractParameterRewriter(replaceableParameters)
  }

  def apply(term: ASTNode, parameterTypeMapping: Map[String, CypherType]): Rewriter =
    rewriteParameterValueTypes(term, parameterTypeMapping)
}
