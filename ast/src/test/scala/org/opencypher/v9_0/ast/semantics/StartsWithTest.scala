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

import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.util.DummyPosition
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTString

class StartsWithTest extends InfixExpressionTestBase(expressions.StartsWith(_, _)(DummyPosition(0))) {

  test("should combine strings and possible strings") {
    testValidTypes(CTString, CTString)(CTBoolean)
    testValidTypes(CTAny, CTString)(CTBoolean)
    testValidTypes(CTString, CTAny)(CTBoolean)
    testValidTypes(CTAny, CTAny)(CTBoolean)
  }
}
