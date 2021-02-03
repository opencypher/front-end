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
package org.opencypher.v9_0.ast.semantics

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.DecimalDoubleLiteral
import org.opencypher.v9_0.expressions.DummyExpression
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.SignedHexIntegerLiteral
import org.opencypher.v9_0.expressions.SignedOctalIntegerLiteral
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class SemanticFunSuite extends CypherFunSuite with SemanticAnalysisTooling with AstConstructionTestSupport {

  override protected def initTest(): Unit = {
    SemanticExpressionCheck.semanticCheckFallback =
      (ctx, e) =>
        e match {
          case x:DummyExpression =>
            specifyType(x.possibleTypes, x)

          case x:ErrorExpression =>
            s => SemanticCheckResult.error(s, x.error)

          case x:CustomExpression =>
            x.semanticCheck(ctx, x)

          case x:Expression =>
            SemanticExpressionCheck.crashOnUnknownExpression(ctx, x)
        }
  }

  def literal(x:String) = StringLiteral(x)(pos)
  def literal(x:Double) = DecimalDoubleLiteral(x.toString)(pos)
  def literal(x:Int) = SignedDecimalIntegerLiteral(x.toString)(pos)

  def unsignedDecimal(str:String) = UnsignedDecimalIntegerLiteral(str)(pos)
  def signedDecimal(str:String) = SignedDecimalIntegerLiteral(str)(pos)
  def decimalDouble(str:String) = DecimalDoubleLiteral(str)(pos)
  def signedOctal(str:String) = SignedOctalIntegerLiteral(str)(pos)
  def signedHex(str:String) = SignedHexIntegerLiteral(str)(pos)

  def variable(name: String): Variable = Variable(name)(pos)
  def propertyKeyName(name: String) = PropertyKeyName("prop")(pos)
  def property(variable:Variable, keyName:PropertyKeyName) = Property(variable, keyName)(pos)
}
