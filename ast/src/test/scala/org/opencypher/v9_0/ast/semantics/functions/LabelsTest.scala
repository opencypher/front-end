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
package org.opencypher.v9_0.ast.semantics.functions

import org.opencypher.v9_0.util.symbols.CTInteger
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTString

class LabelsTest extends FunctionTestBase("labels") {

  test("shouldFailIfWrongArguments") {
    testInvalidApplication()("Insufficient parameters for function 'labels'")
    testInvalidApplication(CTNode, CTNode)("Too many parameters for function 'labels'")
  }

  test("shouldHaveCollectionOfStringsType") {
    testValidTypes(CTNode)(CTList(CTString))
  }

  test("shouldReturnErrorIfInvalidArgumentTypes") {
    testInvalidApplication(CTInteger)("Type mismatch: expected Node but was Integer")
  }
}
