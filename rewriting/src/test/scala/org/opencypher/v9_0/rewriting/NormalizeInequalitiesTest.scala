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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.InequalityExpression
import org.opencypher.v9_0.expressions.LessThan
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.rewriting.rewriters.normalizeInequalities
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class NormalizeInequalitiesTest extends CypherFunSuite with AstConstructionTestSupport {

  val expression1: Expression = Variable("foo1")(pos)
  val expression2: Expression = Variable("foo2")(pos)
  val comparisons = List(
    Or(Equals(expression1, expression2)(pos), LessThan(expression1, expression2)(pos))(pos),
    Or(Equals(expression2, expression1)(pos), LessThan(expression1, expression2)(pos))(pos),
    Or(LessThan(expression1, expression2)(pos), Equals(expression1, expression2)(pos))(pos),
    Or(LessThan(expression1, expression2)(pos), Equals(expression2, expression1)(pos))(pos),
    Or(Equals(expression1, expression2)(pos), GreaterThan(expression1, expression2)(pos))(pos),
    Or(Equals(expression2, expression1)(pos), GreaterThan(expression1, expression2)(pos))(pos),
    Or(GreaterThan(expression1, expression2)(pos), Equals(expression1, expression2)(pos))(pos),
    Or(GreaterThan(expression1, expression2)(pos), Equals(expression2, expression1)(pos))(pos)
  )

  comparisons.foreach { exp =>
    test(exp.toString) {
      val rewritten = exp.rewrite(normalizeInequalities)
      rewritten shouldBe a[InequalityExpression]
    }
  }
}
