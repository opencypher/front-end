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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.test_helpers.TestName

trait JavaccParserAstTestBase[AST] extends JavaccParserTestBase[AST, AST] with TestName with AstConstructionTestSupport {

  final override def convert(astNode: AST): AST = astNode

  final def yields(expr: InputPosition => AST)(implicit parser: JavaccRule[AST]): Unit = parsing(testName) shouldGive expr

  final def gives(ast: AST)(implicit parser: JavaccRule[AST]): Unit = parsing(testName) shouldGive ast

  final def failsToParse(implicit parser: JavaccRule[AST]): Unit = assertFails(testName)

  final def id(id: String): Variable = varFor(id)

  final def lt(lhs: Expression, rhs: Expression): Expression = lessThan(lhs, rhs)

  final def lte(lhs: Expression, rhs: Expression): Expression = lessThanOrEqual(lhs, rhs)

  final def gt(lhs: Expression, rhs: Expression): Expression = greaterThan(lhs, rhs)

  final def gte(lhs: Expression, rhs: Expression): Expression = greaterThanOrEqual(lhs, rhs)

  final def eq(lhs: Expression, rhs: Expression): Expression = equals(lhs, rhs)

  final def ne(lhs: Expression, rhs: Expression): Expression = notEquals(lhs, rhs)
}
