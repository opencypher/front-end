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
import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.ast.NoOptions
import org.opencypher.v9_0.ast.Options
import org.opencypher.v9_0.ast.factory.ASTExceptionFactory
import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class IndexCommandsJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName with AstConstructionTestSupport {

  // Create node index (old syntax)

  test("CREATE INDEX ON :Person(name)") {
    assertSameAST(testName, comparePosition = false)
  }

  test("CREATE INDEX ON :Person(name,age)") {
    assertSameAST(testName, comparePosition = false)
  }

  test("CREATE INDEX my_index ON :Person(name)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX my_index ON :Person(name,age)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE INDEX ON :Person(name)") {
    assertJavaCCException(testName, "'REPLACE' is not allowed for this index syntax (line 1, column 1 (offset: 0))")
  }

  // Create index

  test("CREATe INDEX FOR (n1:Person) ON (n2.name)")
  {
    assertSameAST(testName)
  }

  Seq(
    ("(n1:Person)", btreeNodeIndex: CreateBtreeIndexFunction),
    ("()-[n1:R]-()", btreeRelIndex: CreateBtreeIndexFunction),
    ("()-[n1:R]->()", btreeRelIndex: CreateBtreeIndexFunction),
    ("()<-[n1:R]-()", btreeRelIndex: CreateBtreeIndexFunction)
  ).foreach {
    case (pattern, createIndex: CreateBtreeIndexFunction) =>
      test(s"CREATE INDEX FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"USE neo4j CREATE INDEX FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX FOR $pattern ON (n2.name, n3.age)") {
        assertSameAST(testName)
      }

      test(s"CREATE BTREE INDEX my_index FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX my_index FOR $pattern ON (n2.name, n3.age)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX `$$my_index` FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE INDEX FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE INDEX my_index FOR $pattern ON (n2.name, n3.age)") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE INDEX IF NOT EXISTS FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE INDEX my_index IF NOT EXISTS FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX IF NOT EXISTS FOR $pattern ON (n2.name, n3.age)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX my_index IF NOT EXISTS FOR $pattern ON (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX FOR $pattern ON (n2.name) OPTIONS {indexProvider : 'native-btree-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE BTREE INDEX FOR $pattern ON (n2.name) OPTIONS {indexProvider : 'lucene+native-3.0', indexConfig : {`spatial.cartesian.max`: [100.0,100.0], `spatial.cartesian.min`: [-100.0,-100.0] }}") {
        assertSameAST(testName)
      }

      test(s"CREATE BTREE INDEX FOR $pattern ON (n2.name) OPTIONS {indexConfig : {`spatial.cartesian.max`: [100.0,100.0], `spatial.cartesian.min`: [-100.0,-100.0] }, indexProvider : 'lucene+native-3.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE BTREE INDEX FOR $pattern ON (n2.name) OPTIONS {indexConfig : {`spatial.wgs-84.max`: [60.0,60.0], `spatial.wgs-84.min`: [-40.0,-40.0] }}") {
        assertSameAST(testName)
      }

      test(s"CREATE BTREE INDEX FOR $pattern ON (n2.name) OPTIONS $$options") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX FOR $pattern ON (n2.name) OPTIONS {nonValidOption : 42}") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX my_index FOR $pattern ON (n2.name) OPTIONS {}") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX $$my_index FOR $pattern ON (n2.name)") {
        assertJavaCCExceptionStart(testName, "Invalid input '$': expected an identifier")
      }

      test(s"CREATE INDEX FOR $pattern ON n2.name") {
        assertJavaCCAST(testName, createIndex(List(prop("n2", "name")), None, ast.IfExistsThrowError, NoOptions))
      }

      test(s"CREATE BTREE INDEX my_index FOR $pattern ON n2.name") {
        assertJavaCCAST(testName, createIndex(List(prop("n2", "name")), Some("my_index"), ast.IfExistsThrowError, NoOptions))
      }

      test(s"CREATE OR REPLACE INDEX IF NOT EXISTS FOR $pattern ON n2.name") {
        assertJavaCCAST(testName, createIndex(List(prop("n2", "name")), None, ast.IfExistsInvalidSyntax, NoOptions))
      }

      test(s"CREATE INDEX FOR $pattern ON (n2.name) {indexProvider : 'native-btree-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX FOR $pattern ON (n2.name) OPTIONS") {
        assertSameAST(testName)
      }
  }

  Seq(
    ("(n1)", "labels(n2)"),
    ("()-[r1]-()", "type(r2)"),
    ("()-[r1]->()", "type(r2)"),
    ("()<-[r1]-()", "type(r2)")
  ).foreach {
    case (pattern, function) =>
      test(s"CREATE LOOKUP INDEX FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"USE neo4j CREATE LOOKUP INDEX FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX my_index FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX `$$my_index` FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE LOOKUP INDEX FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE LOOKUP INDEX my_index FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE LOOKUP INDEX IF NOT EXISTS FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE LOOKUP INDEX my_index IF NOT EXISTS FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX IF NOT EXISTS FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX my_index IF NOT EXISTS FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX FOR $pattern ON EACH $function OPTIONS {anyOption : 42}") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX my_index FOR $pattern ON EACH $function OPTIONS {}") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX $$my_index FOR $pattern ON EACH $function") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX FOR $pattern ON EACH $function {indexProvider : 'native-btree-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE LOOKUP INDEX FOR $pattern ON EACH $function OPTIONS") {
        assertSameAST(testName)
      }
  }

  Seq(
    "(n1:Person)",
    "(n1:Person|Colleague|Friend)",
    "()-[n1:R]->()",
    "()<-[n1:R|S]-()"
  ).foreach {
    pattern =>
      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"USE neo4j CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name, n3.age]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX my_index FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX my_index FOR $pattern ON EACH [n2.name, n3.age]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX `$$my_index` FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE FULLTEXT INDEX FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE FULLTEXT INDEX my_index FOR $pattern ON EACH [n2.name, n3.age]") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE FULLTEXT INDEX IF NOT EXISTS FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE OR REPLACE FULLTEXT INDEX my_index IF NOT EXISTS FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX IF NOT EXISTS FOR $pattern ON EACH [n2.name, n3.age]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX my_index IF NOT EXISTS FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {indexProvider : 'fulltext-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {indexProvider : 'fulltext-1.0', indexConfig : {`fulltext.analyzer`: 'some_analyzer'}}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {indexConfig : {`fulltext.eventually_consistent`: false}, indexProvider : 'fulltext-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {indexConfig : {`fulltext.analyzer`: 'some_analyzer', `fulltext.eventually_consistent`: true}}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {nonValidOption : 42}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX my_index FOR $pattern ON EACH [n2.name] OPTIONS {}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX my_index FOR $pattern ON EACH [n2.name] OPTIONS $$options") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX $$my_index FOR $pattern ON EACH [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] {indexProvider : 'fulltext-1.0'}") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH (n2.name)") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH n2.name") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH []") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH") {
        assertSameAST(testName)
      }

      test(s"CREATE FULLTEXT INDEX FOR $pattern ON [n2.name]") {
        assertSameAST(testName)
      }

      test(s"CREATE INDEX FOR $pattern ON EACH [n2.name]") {
        assertJavaCCExceptionStart(testName, "Invalid input") //different failures depending on pattern
      }

      // Missing escaping around `fulltext.analyzer`
      test(s"CREATE FULLTEXT INDEX FOR $pattern ON EACH [n2.name] OPTIONS {indexConfig : {fulltext.analyzer: 'some_analyzer'}}") {
        assertJavaCCExceptionStart(testName, "Invalid input '{': expected \"+\" or \"-\"")
      }
  }

  test("CREATE LOOKUP INDEX FOR (x1) ON EACH labels(x2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[x1]-() ON EACH type(x2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR (n1) ON EACH count(n2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR (n1) ON EACH type(n2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR (n) ON EACH labels(x)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r1]-() ON EACH count(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r1]-() ON EACH labels(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r]-() ON EACH type(x)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r1]-() ON type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR n1:Person ON (n2.name)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR -[r1:R]-() ON (r2.name)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR ()-[r1:R]- ON (r2.name)") {
    //parboiled expects a space here, whereas java cc is whitespace ignorant
    assertJavaCCException(testName, "Invalid input 'ON': expected \"(\", \">\" or <ARROW_RIGHT_HEAD> (line 1, column 29 (offset: 28))")
  }

  test("CREATE INDEX FOR -[r1:R]- ON (r2.name)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR [r1:R] ON (r2.name)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR n1 ON EACH labels(n2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR -[r1]-() ON EACH type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r1]- ON EACH type(r2)") {
    //parboiled expects a space here, whereas java cc is whitespace ignorant
    assertJavaCCException(testName, "Invalid input 'ON': expected \"(\", \">\" or <ARROW_RIGHT_HEAD> (line 1, column 34 (offset: 33))")
  }

  test("CREATE LOOKUP INDEX FOR -[r1]- ON EACH type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR [r1] ON EACH type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR (n1) EACH labels(n2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR ()-[r1]-() EACH type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE LOOKUP INDEX FOR (n1) ON labels(n2)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR (n1) ON EACH labels(n2)") {
    assertSameAST(testName)
  }

  test("CREATE INDEX FOR ()-[r1]-() ON EACH type(r2)") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1|:A) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1|:R]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1:A|:B) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1:R|:S]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1:A||B) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1:R||S]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1:A:B) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1:R:S]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1:A&B) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1:R&S]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR (n1:A B) ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  test("CREATE FULLTEXT INDEX FOR ()-[n1:R S]-() ON EACH [n2.x]") {
    assertSameAST(testName)
  }

  // Drop index

  test("DROP INDEX ON :Person(name)") {
    assertSameAST(testName, comparePosition = false)
  }

  test("DROP INDEX ON :Person(name, age)") {
    assertSameAST(testName, comparePosition = false)
  }

  test("DROP INDEX my_index") {
    assertSameAST(testName)
  }

  test("DROP INDEX `$my_index`") {
    assertSameAST(testName)
  }

  test("DROP INDEX my_index IF EXISTS") {
    assertSameAST(testName)
  }

  test("DROP INDEX $my_index") {
    assertSameAST(testName)
  }

  test("DROP INDEX my_index ON :Person(name)") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON (:Person(name))") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON (:Person {name})") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON [:Person(name)]") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON -[:Person(name)]-") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON ()-[:Person(name)]-()") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON [:Person {name}]") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON -[:Person {name}]-") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON ()-[:Person {name}]-()") {
    assertSameAST(testName)
  }

  test("DROP INDEX on IF EXISTS") {
    assertSameAST(testName)
  }

  test("DROP INDEX on") {
    assertSameAST(testName)
  }

  test("DROP INDEX ON :if(exists)") {
    assertSameAST(testName, comparePosition = false)
  }

  test("CATALOG DROP INDEX name") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.invalidCatalogStatement))
  }

  // help methods
  type CreateBtreeIndexFunction = (List[expressions.Property], Option[String], ast.IfExistsDo, Options) => InputPosition => ast.CreateIndex

  private def btreeNodeIndex(props: List[expressions.Property],
                             name: Option[String],
                             ifExistsDo: ast.IfExistsDo,
                             options: Options): InputPosition => ast.CreateIndex =
    ast.CreateBtreeNodeIndex(varFor("n1"), labelName("Person"), props, name, ifExistsDo, options)

  private def btreeRelIndex(props: List[expressions.Property],
                            name: Option[String],
                            ifExistsDo: ast.IfExistsDo,
                            options: Options): InputPosition => ast.CreateIndex =
    ast.CreateBtreeRelationshipIndex(varFor("n1"), relTypeName("R"), props, name, ifExistsDo, options)
}
