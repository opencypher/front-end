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
import org.opencypher.v9_0.ast.CreateNodeKeyConstraint
import org.opencypher.v9_0.ast.CreateNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.DropNodeKeyConstraint
import org.opencypher.v9_0.ast.DropNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.DropRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.NoOptions
import org.opencypher.v9_0.ast.factory.ASTExceptionFactory
import org.opencypher.v9_0.ast.factory.ConstraintType
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.test_helpers.TestName
import org.scalatest.FunSuiteLike

class ConstraintCommandJavaCcParserTest extends ParserComparisonTestBase with FunSuiteLike with TestName with AstConstructionTestSupport {

  // Create constraint: Without name

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {indexProvider : 'native-btree-1.0'}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {indexProvider : 'lucene+native-3.0', indexConfig : {`spatial.cartesian.max`: [100.0,100.0], `spatial.cartesian.min`: [-100.0,-100.0] }}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {indexConfig : {`spatial.wgs-84.max`: [60.0,60.0], `spatial.wgs-84.min`: [-40.0,-40.0] }}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {nonValidOption : 42}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:REL]-() ASSERT (r2.prop) IS NODE KEY") {
    assertJavaCCExceptionStart(testName, ASTExceptionFactory.relationshipPattternNotAllowed(ConstraintType.NODE_KEY) )
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT node2.prop IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON (node1:Label) ASSERT node2.prop IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT node2.prop IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop1,node.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS UNIQUE OPTIONS {indexConfig : {`spatial.wgs-84.max`: [60.0,60.0], `spatial.wgs-84.min`: [-40.0,-40.0]}, indexProvider : 'native-btree-1.0'}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]-() ASSERT (r2.prop) IS UNIQUE") {
    assertJavaCCExceptionStart(testName, ASTExceptionFactory.relationshipPattternNotAllowed(ConstraintType.UNIQUE))
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT EXISTS (node2.prop) OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]->() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()<-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON ()<-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON ()-[r1:R]->() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]->() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()<-[r1:R]-() ASSERT (r2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT ON ()<-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT IF NOT EXISTS ON ()-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT IF NOT EXISTS ON ()-[r1:R]->() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]-() ASSERT (r2.prop) IS NOT NULL OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT node2.prop IS NODE KEY") {
    assertJavaCCAST(testName,
      CreateNodeKeyConstraint(Variable("node1")(pos),
        LabelName("Label")(pos),
        Seq(Property(Variable("node2")(pos), PropertyKeyName("prop")(pos))(pos)),
        None,
        IfExistsThrowError,
        NoOptions,
        None
      )(pos)
    )
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY {indexProvider : 'native-btree-1.0'}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT node2.prop.part IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop.part) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS UNIQUE {indexProvider : 'native-btree-1.0'}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT (node2.prop1, node.prop2) IS UNIQUE OPTIONS") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT EXISTS node2.prop") {
    assertJavaCCAST(testName,
      CreateNodePropertyExistenceConstraint(
        Variable("node1")(pos),
        LabelName("Label")(pos),
        Property(Variable("node2")(pos),PropertyKeyName("prop")(pos))(pos),
        None,
        IfExistsThrowError,
        oldSyntax = true,
        NoOptions,
        None
      )(pos))
  }

  test("CREATE CONSTRAINT ON (node1:Label) ASSERT EXISTS (node2.prop1, node3.prop2)") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.onlySinglePropertyAllowed(ConstraintType.NODE_EXISTS)))
  }

  test("CREATE CONSTRAINT ON ()-[r1:REL]-() ASSERT EXISTS (r2.prop1, r3.prop2)") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.onlySinglePropertyAllowed(ConstraintType.REL_EXISTS)))
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT EXISTS (node2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT ON ()-[r1:R]-() ASSERT EXISTS r2.prop") {
    assertJavaCCAST(testName,
      CreateRelationshipPropertyExistenceConstraint(Variable("r1")(pos),
        RelTypeName("R")(pos),
        Property(Variable("r2")(pos), PropertyKeyName("prop")(pos))(pos),
        None,
        IfExistsThrowError,
        oldSyntax = true,
        NoOptions,
        None
      )(pos)
    )
  }

  test("CREATE CONSTRAINT my_constraint ON ()-[r1:R]-() ASSERT r2.prop IS NULL") {
    assertJavaCCException(testName, "Invalid input 'NULL': expected \"NODE\", \"NOT\" or \"UNIQUE\" (line 1, column 67 (offset: 66))")
  }

  // Create constraint: With name

  test("USE neo4j CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("USE neo4j CREATE OR REPLACE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node2.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS NODE KEY OPTIONS {indexProvider : 'native-btree-1.0', indexConfig : {`spatial.wgs-84.max`: [60.0,60.0], `spatial.wgs-84.min`: [-40.0,-40.0]}}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT node2.prop IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS UNIQUE OPTIONS {indexProvider : 'native-btree-1.0'}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS UNIQUE OPTIONS {indexProvider : 'lucene+native-3.0', indexConfig : {`spatial.cartesian.max`: [100.0,100.0], `spatial.cartesian.min`: [-100.0,-100.0] }}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS UNIQUE OPTIONS {indexConfig : {`spatial.wgs-84.max`: [60.0,60.0], `spatial.wgs-84.min`: [-40.0,-40.0] }}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS UNIQUE OPTIONS {nonValidOption : 42}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop2, node3.prop3) IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.onlySinglePropertyAllowed(ConstraintType.NODE_IS_NOT_NULL)))
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint ON ()-[r1:REL]-() ASSERT (r2.prop2, r3.prop3) IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.onlySinglePropertyAllowed(ConstraintType.REL_IS_NOT_NULL)))
  }

  test("CREATE OR REPLACE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint IF NOT EXISTS ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT node2.prop IS NOT NULL OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT `$my_constraint` ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON ()-[r1:R]-() ASSERT EXISTS (r2.prop) OPTIONS {}") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT `$my_constraint` ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT `$my_constraint` IF NOT EXISTS ON ()-[r1:R]->() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT `$my_constraint` IF NOT EXISTS ON ()<-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT `$my_constraint` ON ()-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON ()-[r1:R]-() ASSERT (r2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT `$my_constraint` ON ()-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE OR REPLACE CONSTRAINT `$my_constraint` IF NOT EXISTS ON ()-[r1:R]->() ASSERT (r2.prop) IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT `$my_constraint` IF NOT EXISTS ON ()<-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertSameAST(testName)
  }

  test("CREATE CONSTRAINT my_constraint ON (node1:Label) ASSERT node2.prop IS NULL") {
    assertJavaCCException(testName, "Invalid input 'NULL': expected \"NODE\", \"NOT\" or \"UNIQUE\" (line 1, column 71 (offset: 70))")
  }

  test("CREATE CONSTRAINT my_constraint ON ()-[r1:R]-() ASSERT EXISTS (r2.prop) IS NOT NULL") {
    assertJavaCCExceptionStart(testName,  ASTExceptionFactory.constraintTypeNotAllowed(ConstraintType.REL_IS_NOT_NULL, ConstraintType.REL_EXISTS))
  }

  test("CREATE CONSTRAINT $my_constraint ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertJavaCCException(testName, "Invalid input '$': expected \"IF\", \"ON\" or an identifier (line 1, column 19 (offset: 18))")
  }

  // Drop constraint

  test("DROP CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT node2.prop IS UNIQUE") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT r2.prop IS UNIQUE") {
    assertJavaCCExceptionStart(testName, ASTExceptionFactory.relationshipPattternNotAllowed(ConstraintType.UNIQUE))
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS UNIQUE") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT EXISTS (node2.prop)") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON ()-[r1:R]->() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON ()<-[r1:R]-() ASSERT EXISTS (r2.prop)") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT node2.prop IS NODE KEY") {
    assertJavaCCAST(testName,
      DropNodeKeyConstraint(Variable("node1")(pos),
        LabelName("Label")(pos),
        Seq(Property(Variable("node2")(pos), PropertyKeyName("prop")(pos))(pos)),
        None)(pos)
    )
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT EXISTS node2.prop") {
    assertJavaCCAST(testName,
      DropNodePropertyExistenceConstraint(Variable("node1")(pos),
        LabelName("Label")(pos),
        Property(Variable("node2")(pos), PropertyKeyName("prop")(pos))(pos),
        None)(pos)
    )
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT r2.prop IS NODE KEY") {
    assertJavaCCExceptionStart(testName,  ASTExceptionFactory.relationshipPattternNotAllowed(ConstraintType.NODE_KEY))
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT (node2.prop) IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand))
  }

  test("DROP CONSTRAINT ON (node1:Label) ASSERT node2.prop IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand))
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT EXISTS r2.prop") {
    assertJavaCCAST(testName,
      DropRelationshipPropertyExistenceConstraint(
        Variable("r1")(pos),
        RelTypeName("R")(pos),
        Property(Variable("r2")(pos),PropertyKeyName("prop")(pos))(pos),
        None)(pos)
    )
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT (r2.prop) IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand))
  }

  test("DROP CONSTRAINT ON ()-[r1:R]-() ASSERT r2.prop IS NOT NULL") {
    assertJavaCCException(testName, new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand))
  }

  test("DROP CONSTRAINT my_constraint") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT `$my_constraint`") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT my_constraint IF EXISTS") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT $my_constraint") {
    assertSameAST(testName)
  }

  test("DROP CONSTRAINT my_constraint ON (node1:Label) ASSERT (node2.prop1,node3.prop2) IS NODE KEY") {
    assertJavaCCException(testName, "Invalid input 'ON': expected \"IF\" or <EOF> (line 1, column 31 (offset: 30))")
  }
}
