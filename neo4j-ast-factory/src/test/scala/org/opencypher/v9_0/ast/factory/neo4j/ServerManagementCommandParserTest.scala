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

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.Yield

class ServerManagementCommandParserTest extends AdministrationAndSchemaCommandParserTestBase {
  // SHOW

  test("SHOW SERVERS") {
    assertAst(ast.ShowServers(None)(defaultPos))
  }

  test("SHOW SERVERS YIELD *") {
    val yieldOrWhere = Left((yieldClause(returnAllItems), None))
    assertAst(ast.ShowServers(Some(yieldOrWhere))(defaultPos))
  }

  test("SHOW SERVERS YIELD address") {
    val columns = yieldClause(returnItems(variableReturnItem("address")), None)
    val yieldOrWhere = Some(Left((columns, None)))
    assertAst(ast.ShowServers(yieldOrWhere)(defaultPos))
  }

  test("SHOW SERVERS YIELD address ORDER BY name") {
    val orderByClause = Some(orderBy(sortItem(varFor("name"))))
    val columns = yieldClause(returnItems(variableReturnItem("address")), orderByClause)
    val yieldOrWhere = Some(Left((columns, None)))
    assertAst(ast.ShowServers(yieldOrWhere)(defaultPos))
  }

  test("SHOW SERVERS YIELD address ORDER BY name SKIP 1 LIMIT 2 WHERE name = 'badger' RETURN *") {
    val orderByClause = orderBy(sortItem(varFor("name")))
    val whereClause = where(equals(varFor("name"), literalString("badger")))
    val columns = yieldClause(
      returnItems(variableReturnItem("address")),
      Some(orderByClause),
      Some(skip(1)),
      Some(limit(2)),
      Some(whereClause)
    )
    val yieldOrWhere = Some(Left((columns, Some(returnAll))))
    assertAst(ast.ShowServers(yieldOrWhere)(defaultPos))
  }

  test("SHOW SERVERS YIELD * RETURN id") {
    val yieldOrWhere: Left[(Yield, Some[Return]), Nothing] =
      Left((yieldClause(returnAllItems), Some(return_(variableReturnItem("id")))))
    assertAst(ast.ShowServers(Some(yieldOrWhere))(defaultPos))
  }

  test("SHOW SERVERS WHERE name = 'badger'") {
    val yieldOrWhere = Right(where(equals(varFor("name"), literalString("badger"))))
    assertAst(ast.ShowServers(Some(yieldOrWhere))(defaultPos))
  }

  test("SHOW SERVERS RETURN *") {
    assertFailsWithMessage(
      testName,
      "Invalid input 'RETURN': expected \"WHERE\", \"YIELD\" or <EOF> (line 1, column 14 (offset: 13))"
    )
  }

  // DROP

  test("DROP SERVER 'name'") {
    assertAst(ast.DropServer(literal("name"))(defaultPos))
  }

  test("DROP SERVER $name") {
    assertAst(ast.DropServer(stringParam("name"))(defaultPos))
  }

  test("DROP SERVER name") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'name': expected "\"", "\'" or a parameter (line 1, column 13 (offset: 12))"""
    )
  }

  // DEALLOCATE

  test("DEALLOCATE DATABASES FROM SERVER 'badger', 'snake'") {
    assertAst(ast.DeallocateServers(Seq(literal("badger"), literal("snake")))(defaultPos))
  }

  test("DEALLOCATE DATABASES FROM SERVER $name") {
    assertAst(ast.DeallocateServers(Seq(stringParam("name")))(defaultPos))
  }

  test("DEALLOCATE DATABASE FROM SERVERS $name, 'foo'") {
    assertAst(ast.DeallocateServers(Seq(stringParam("name"), literal("foo")))(defaultPos))
  }
}
