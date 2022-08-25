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
import org.opencypher.v9_0.ast.DestroyData
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.IfExistsReplace
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.IndefiniteWait
import org.opencypher.v9_0.ast.NoWait
import org.opencypher.v9_0.ast.TimeoutAfter

class CompositeDatabaseParserTest extends AdministrationAndSchemaCommandParserTestBase {

  test("CREATE COMPOSITE DATABASE name") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsThrowError, NoWait))
  }

  test("CREATE COMPOSITE DATABASE $name") {
    yields(ast.CreateCompositeDatabase(stringParamName("name"), IfExistsThrowError, NoWait))
  }

  test("CREATE COMPOSITE DATABASE `db.name`") {
    yields(ast.CreateCompositeDatabase(namespacedName("db.name"), IfExistsThrowError, NoWait))
  }

  test("CREATE COMPOSITE DATABASE db.name") {
    yields(ast.CreateCompositeDatabase(namespacedName("db", "name"), IfExistsThrowError, NoWait))
  }

  test("CREATE COMPOSITE DATABASE name IF NOT EXISTS") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsDoNothing, NoWait))
  }

  test("CREATE OR REPLACE COMPOSITE DATABASE name") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsReplace, NoWait))
  }

  test("CREATE COMPOSITE DATABASE name OPTIONS {}") {
    assertFailsWithMessage(
      testName,
      """Invalid input 'OPTIONS': expected
        |  "."
        |  "IF"
        |  "NOWAIT"
        |  "WAIT"
        |  <EOF> (line 1, column 32 (offset: 31))""".stripMargin
    )
  }

  test("CREATE COMPOSITE DATABASE name WAIT") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsThrowError, IndefiniteWait))
  }

  test("CREATE COMPOSITE DATABASE name NOWAIT") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsThrowError, NoWait))
  }

  test("CREATE COMPOSITE DATABASE name WAIT 10 SECONDS") {
    yields(ast.CreateCompositeDatabase(namespacedName("name"), IfExistsThrowError, TimeoutAfter(10)))
  }

  test("DROP COMPOSITE DATABASE name") {
    yields(ast.DropDatabase(namespacedName("name"), ifExists = false, composite = true, DestroyData, NoWait))
  }

  test("DROP COMPOSITE DATABASE `db.name`") {
    yields(ast.DropDatabase(namespacedName("db.name"), ifExists = false, composite = true, DestroyData, NoWait))
  }

  test("DROP COMPOSITE DATABASE db.name") {
    yields(ast.DropDatabase(namespacedName("db", "name"), ifExists = false, composite = true, DestroyData, NoWait))
  }

  test("DROP COMPOSITE DATABASE $name") {
    yields(ast.DropDatabase(stringParamName("name"), ifExists = false, composite = true, DestroyData, NoWait))
  }

  test("DROP COMPOSITE DATABASE name IF EXISTS") {
    yields(ast.DropDatabase(namespacedName("name"), ifExists = true, composite = true, DestroyData, NoWait))
  }

  test("DROP COMPOSITE DATABASE name WAIT") {
    yields(ast.DropDatabase(
      namespacedName("name"),
      ifExists = false,
      composite = true,
      DestroyData,
      IndefiniteWait
    ))
  }

  test("DROP COMPOSITE DATABASE name WAIT 10 SECONDS") {
    yields(ast.DropDatabase(
      namespacedName("name"),
      ifExists = false,
      composite = true,
      DestroyData,
      TimeoutAfter(10)
    ))
  }

  test("DROP COMPOSITE DATABASE name NOWAIT") {
    yields(ast.DropDatabase(
      namespacedName("name"),
      ifExists = false,
      composite = true,
      DestroyData,
      NoWait
    ))
  }
}
