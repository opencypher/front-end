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

import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode

class EscapedSymbolicNameParserTest extends JavaccParserAstTestBase[ASTNode] {

  test("escaped variable name") {
    implicit val parser: JavaccRule[Variable] = JavaccRule.Variable

    parsing("`This isn\\'t a common variable`") shouldGive varFor("This isn\\'t a common variable")
    parsing("`a``b`") shouldGive varFor("a`b")
  }

  test("escaped label name") {
    implicit val parser: JavaccRule[NodePattern] = JavaccRule.NodePattern

    parsing("(n:`Label`)") shouldGive nodePat(Some("n"), Some(labelAtom("Label")))
    parsing("(n:`Label``123`)") shouldGive nodePat(Some("n"), Some(labelAtom("Label`123")))
    parsing("(n:`````Label```)") shouldGive nodePat(Some("n"), Some(labelAtom("``Label`")))

    assertFails("(n:`L`abel`)")
  }
}
