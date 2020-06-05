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

import org.opencypher.v9_0.expressions.AutoExtractedParameter
import org.opencypher.v9_0.expressions.SensitiveAutoParameter
import org.opencypher.v9_0.expressions.SensitiveStringLiteral
import org.opencypher.v9_0.rewriting.rewriters.literalReplacement.ExtractParameterRewriter
import org.opencypher.v9_0.rewriting.rewriters.literalReplacement.LiteralReplacement
import org.opencypher.v9_0.rewriting.rewriters.literalReplacement.LiteralReplacements
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.IdentityMap
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.symbols.CTString

object sensitiveLiteralReplacement {

  private val sensitiveliteralMatcher: PartialFunction[Any, LiteralReplacements => (LiteralReplacements, Option[LiteralReplacements => LiteralReplacements])] = {
    case l: SensitiveStringLiteral =>
      acc =>
        if (acc.contains(l)) {
          (acc, None)
        } else {
          val parameter = new AutoExtractedParameter(s"  AUTOSTRING${acc.size}", CTString, l)(l.position) with SensitiveAutoParameter
          (acc + (l -> LiteralReplacement(parameter, l.value)), None)
        }
  }

  def apply(term: ASTNode): (Rewriter, Map[String, Any]) = {
    val replaceableLiterals = term.treeFold(IdentityMap.empty: LiteralReplacements)(sensitiveliteralMatcher)

    val extractedParams: Map[String, AnyRef] = replaceableLiterals.map {
      case (_, LiteralReplacement(parameter, value)) => (parameter.name, value)
    }

    (ExtractParameterRewriter(replaceableLiterals), extractedParams)
  }
}
