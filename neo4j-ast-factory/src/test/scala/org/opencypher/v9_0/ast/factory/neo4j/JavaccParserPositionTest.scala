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

import org.opencypher.v9_0.ast.HasCatalog
import org.opencypher.v9_0.ast.LoadCSV
import org.opencypher.v9_0.ast.PeriodicCommitHint
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.ListSlice
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

import scala.util.Try

class JavaccParserPositionTest extends ParserComparisonTestBase with FunSuiteLike with TestName {
  private val exceptionFactory = new OpenCypherExceptionFactory(None)
  private val javaccAST = (query: String) => Try(JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator()))

  test("MATCH (n) RETURN n.prop") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[Property], InputPosition(17, 1, 18))
  }

  test("MATCH (n) SET n.prop = 1") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[SetPropertyItem], InputPosition(14, 1, 15))
  }

  test("MATCH (n) REMOVE n.prop") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[RemovePropertyItem], InputPosition(17, 1, 18))
  }

  test("LOAD CSV FROM 'url' AS line") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[LoadCSV], InputPosition(0, 1, 1))
  }

  test("USE GRAPH(x) RETURN 1 as y ") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[UseGraph], InputPosition(0, 1, 1))
  }

  test("CREATE (a)-[:X]->(b)") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[EveryPath], InputPosition(7, 1, 8))
  }

  test("CATALOG SHOW ALL ROLES YIELD role") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[Yield], InputPosition(23, 1, 24))
  }

  test("RETURN 3 IN list[0] AS r") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[ContainerIndex], InputPosition(17, 1, 18))
  }

  test("RETURN 3 IN [1, 2, 3][0..1] AS r") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[ListSlice], InputPosition(21, 1, 22))
  }

  test("MATCH (a) WHERE NOT (a:A)") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[HasLabelsOrTypes], InputPosition(21, 1, 22))
  }

  test("USING PERIODIC COMMIT LOAD CSV FROM 'url' AS line RETURN line") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[SingleQuery], InputPosition(22, 1, 23))
    validatePosition(testName, _.isInstanceOf[PeriodicCommitHint], InputPosition(6, 1, 7))
  }

  test("MATCH (n) SET n += {name: null}") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[SetIncludingPropertiesFromMapItem], InputPosition(14, 1, 15))
  }

  test("MATCH (n) SET n = {name: null}") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[SetExactPropertiesFromMapItem], InputPosition(14, 1, 15))
  }

  test("CATALOG SHOW ALL ROLES") {
    assertSameAST(testName)
    validatePosition(testName, _.isInstanceOf[HasCatalog], InputPosition(8, 1, 9))
  }

  Seq(
    "DATABASES",
    "DEFAULT DATABASE",
    "HOME DATABASE",
    "DATABASE $db",
    "DATABASE neo4j"
  ).foreach( name =>
    test(s"SHOW $name") {
      assertSameAST(testName)
      validatePosition(testName, _.isInstanceOf[ShowDatabase], InputPosition(0, 1, 1))
      validatePosition(testName, _.isInstanceOf[Variable], InputPosition(0, 1, 1))
    }
  )

  test("DROP INDEX ON :Person(name)") {
    // PropertyKeyName in this AST is not the same in JavaCC and parboiled
    validatePosition(testName, _.isInstanceOf[PropertyKeyName], InputPosition(22, 1, 23))
  }

  private def validatePosition(query: String, astToVerify: (ASTNode) => Boolean, pos: InputPosition): Unit = {
    val propAst = javaccAST(query).treeFind[ASTNode] {
      case ast if astToVerify(ast) => true
    }

    propAst.get.position shouldBe pos
  }
}
