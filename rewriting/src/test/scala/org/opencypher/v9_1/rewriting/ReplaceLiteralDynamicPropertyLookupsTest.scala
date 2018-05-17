/*
 * Copyright © 2002-2018 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_1.rewriting

import org.opencypher.v9_1.ast.AstConstructionTestSupport
import org.opencypher.v9_1.expressions.{ContainerIndex, Property, StringLiteral}
import org.opencypher.v9_1.util.ASTNode
import org.opencypher.v9_1.util.test_helpers.CypherFunSuite
import org.opencypher.v9_1.expressions.PropertyKeyName
import org.opencypher.v9_1.rewriting.rewriters.replaceLiteralDynamicPropertyLookups

class ReplaceLiteralDynamicPropertyLookupsTest extends CypherFunSuite with AstConstructionTestSupport {

  test("Replaces literal dynamic property lookups") {
    val input: ASTNode = ContainerIndex(varFor("a"), StringLiteral("name")_)_
    val output: ASTNode = Property(varFor("a"), PropertyKeyName("name")_)_

    replaceLiteralDynamicPropertyLookups(input) should equal(output)
  }

  test("Does not replaces non-literal dynamic property lookups") {
    val input: ASTNode = ContainerIndex(varFor("a"), varFor("b"))_

    replaceLiteralDynamicPropertyLookups(input) should equal(input)
  }
}
