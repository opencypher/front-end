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

import org.opencypher.v9_0.ast.factory.ASTExceptionFactory
import org.opencypher.v9_0.ast.factory.ConstraintType
import org.opencypher.v9_0.ast.factory.CreateIndexTypes
import org.opencypher.v9_0.ast.factory.ShowCommandFilterTypes
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class Neo4jASTFactoryTest extends CypherFunSuite {

  test("invalidDropCommand") {
    ASTExceptionFactory.invalidDropCommand shouldBe "Unsupported drop constraint command: Please delete the constraint by name instead"
  }

  test("invalidCatalogStatement") {
    ASTExceptionFactory.invalidCatalogStatement shouldBe "CATALOG is not allowed for this statement"
  }

  test("relationShipPattternNotAllowed") {
    ASTExceptionFactory.relationshipPattternNotAllowed(ConstraintType.UNIQUE) shouldBe "'IS UNIQUE' does not allow relationship patterns"
  }

  test("onlySinglePropertyAllowed") {
    ASTExceptionFactory.onlySinglePropertyAllowed(ConstraintType.NODE_EXISTS) shouldBe "'EXISTS' does not allow multiple properties"
  }

  test("invalidShowFilterType") {
    ASTExceptionFactory.invalidShowFilterType("indexes", ShowCommandFilterTypes.INVALID) shouldBe "Filter type INVALID is not defined for show indexes command."
  }

  test("invalidCreateIndexType") {
    ASTExceptionFactory.invalidCreateIndexType(CreateIndexTypes.INVALID) shouldBe "Index type INVALID is not defined for create index command."
  }
}
