/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.openCypher.v9_0.internal.frontend.ast

import org.openCypher.v9_0.internal.expressions.{Parameter, StringLiteral}
import org.openCypher.v9_0.internal.frontend.SemanticCheck
import org.openCypher.v9_0.internal.frontend.semantics.{SemanticAnalysisTooling, SemanticCheckable, SemanticExpressionCheck}
import org.openCypher.v9_0.internal.util.{ASTNode, InputPosition}
import org.openCypher.v9_0.internal.util.symbols._

final case class GraphUrl(url: Either[Parameter, StringLiteral])(val position: InputPosition)
  extends ASTNode with SemanticCheckable with SemanticAnalysisTooling {

  override def semanticCheck: SemanticCheck = url match {
    case Left(parameter) =>
      SemanticExpressionCheck.simple(parameter) chain
        expectType(CTString.covariant, parameter)

    case Right(literal) =>
      SemanticExpressionCheck.simple(literal) chain
        expectType(CTString.covariant, literal)
  }
}
