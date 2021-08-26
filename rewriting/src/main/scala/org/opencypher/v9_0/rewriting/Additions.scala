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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.ConstraintVersion1
import org.opencypher.v9_0.ast.ConstraintVersion2
import org.opencypher.v9_0.ast.CreateBtreeNodeIndex
import org.opencypher.v9_0.ast.CreateBtreeRelationshipIndex
import org.opencypher.v9_0.ast.CreateFulltextNodeIndex
import org.opencypher.v9_0.ast.CreateFulltextRelationshipIndex
import org.opencypher.v9_0.ast.CreateLookupIndex
import org.opencypher.v9_0.ast.CreateNodeKeyConstraint
import org.opencypher.v9_0.ast.CreateNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateTextNodeIndex
import org.opencypher.v9_0.ast.CreateTextRelationshipIndex
import org.opencypher.v9_0.ast.CreateUniquePropertyConstraint
import org.opencypher.v9_0.ast.DropConstraintOnName
import org.opencypher.v9_0.ast.DropIndexOnName
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.NoOptions
import org.opencypher.v9_0.ast.ShowConstraintsClause
import org.opencypher.v9_0.ast.ShowFunctionsClause
import org.opencypher.v9_0.ast.ShowIndexesClause
import org.opencypher.v9_0.ast.ShowProceduresClause
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.TextIndexes
import org.opencypher.v9_0.ast.UniquePropertyConstraintCommand
import org.opencypher.v9_0.ast.UnresolvedCall
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.util.CypherExceptionFactory

object Additions {

  // This is functionality that has been added earlier in 4.x and should not work when using CYPHER 3.5
  case object addedFeaturesIn4_x extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      case u: UseGraph =>
        throw cypherExceptionFactory.syntaxException("The USE clause is not supported in this Cypher version.", u.position)

      case c: UnresolvedCall if c.yieldAll =>
        throw cypherExceptionFactory.syntaxException("Procedure call using `YIELD *` is not supported in this Cypher version.", c.position)

      // CREATE INDEX [name] [IF NOT EXISTS] FOR (n:Label) ON (n.prop) [OPTIONS {...}]
      case c: CreateBtreeNodeIndex =>
        throw cypherExceptionFactory.syntaxException("Creating index using this syntax is not supported in this Cypher version.", c.position)

      // CREATE INDEX [name] [IF NOT EXISTS] FOR ()-[n:RelType]-() ON (n.prop) [OPTIONS {...}]
      case c: CreateBtreeRelationshipIndex =>
        throw cypherExceptionFactory.syntaxException("Relationship property indexes are not supported in this Cypher version.", c.position)

      // CREATE LOOKUP INDEX ...
      case c: CreateLookupIndex =>
        throw cypherExceptionFactory.syntaxException("Lookup indexes are not supported in this Cypher version.", c.position)

      // CREATE FULLTEXT INDEX ...
      case c: CreateFulltextNodeIndex =>
        throw cypherExceptionFactory.syntaxException("Fulltext indexes can only be created using procedures in this Cypher version.", c.position)
      case c: CreateFulltextRelationshipIndex =>
        throw cypherExceptionFactory.syntaxException("Fulltext indexes can only be created using procedures in this Cypher version.", c.position)

      // DROP INDEX name
      case d: DropIndexOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping index by name is not supported in this Cypher version.", d.position)

      // CREATE CONSTRAINT name ON ... IS NODE KEY
      case c@CreateNodeKeyConstraint(_, _, _, Some(_), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node key constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ... IS NODE KEY
      case c@CreateNodeKeyConstraint(_, _, _, _, IfExistsDoNothing, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT ... IS NODE KEY OPTIONS {...}
      case c@CreateNodeKeyConstraint(_, _, _, _, _, options, _, _, _) if options != NoOptions =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint with options is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ... IS UNIQUE
      case c@CreateUniquePropertyConstraint(_, _, _, Some(_),_, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named uniqueness constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ... IS UNIQUE
      case c@CreateUniquePropertyConstraint(_, _, _, _, IfExistsDoNothing, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT ... IS UNIQUE OPTIONS {...}
      case c@CreateUniquePropertyConstraint(_, _, _, _, _, options, _, _, _) if options != NoOptions =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint with options is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON () ... EXISTS
      case c@CreateNodePropertyExistenceConstraint(_, _, _, Some(_), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node existence constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON () ... EXISTS
      case c@CreateNodePropertyExistenceConstraint(_, _, _, _, IfExistsDoNothing, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ()-[]-() ... EXISTS
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, Some(_), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named relationship existence constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] IF NOT EXISTS ON ()-[]-() ... EXISTS
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, _, IfExistsDoNothing, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] ON (node:Label) ASSERT node.prop IS NOT NULL
      case c: CreateNodePropertyExistenceConstraint if c.constraintVersion == ConstraintVersion1 =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `IS NOT NULL` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] ON ()-[r:R]-() ASSERT r.prop IS NOT NULL
      case c: CreateRelationshipPropertyExistenceConstraint if c.constraintVersion == ConstraintVersion1 =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `IS NOT NULL` is not supported in this Cypher version.", c.position)

      // DROP CONSTRAINT name
      case d: DropConstraintOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping constraint by name is not supported in this Cypher version.", d.position)

      case e: ExistsSubClause =>
        throw cypherExceptionFactory.syntaxException("Existential subquery is not supported in this Cypher version.", e.position)

      // SHOW [ALL|BTREE|FULLTEXT|LOOKUP] INDEX[ES] [WHERE clause|YIELD clause]
      case s: ShowIndexesClause =>
        throw cypherExceptionFactory.syntaxException("SHOW INDEXES is not supported in this Cypher version.", s.position)

      // SHOW [ALL|UNIQUE|NODE [PROPERTY] EXIST[ENCE]|RELATIONSHIP [PROPERTY] EXIST[ENCE]|EXIST[ENCE]|NODE KEY] CONSTRAINT[S] [WHERE clause|YIELD clause]
      case s: ShowConstraintsClause =>
        throw cypherExceptionFactory.syntaxException("SHOW CONSTRAINTS is not supported in this Cypher version.", s.position)

      // SHOW PROCEDURE[S] [EXECUTABLE [BY {CURRENT USER | username}]] [WHERE clause | YIELD clause]
      case c: ShowProceduresClause =>
        throw cypherExceptionFactory.syntaxException("`SHOW PROCEDURES` is not supported in this Cypher version.", c.position)

      // SHOW [ALL | BUILT IN | USER DEFINED] FUNCTION[S] [EXECUTABLE [BY {CURRENT USER | username}]] [WHERE clause | YIELD clause]
      case c: ShowFunctionsClause =>
        throw cypherExceptionFactory.syntaxException("`SHOW FUNCTIONS` is not supported in this Cypher version.", c.position)

      // Administration commands against system database are not supported at all in CYPHER 3.5.
      // This is checked in CompilerFactory, so separate checks for such commands are not needed here.
    }
  }

  // This is functionality that has been added in 4.4 and should not work when using CYPHER 3.5 and CYPHER 4.3
  case object addedFeaturesIn4_4 extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      case c: UniquePropertyConstraintCommand if c.properties.size > 1 =>
        throw cypherExceptionFactory.syntaxException("Multi-property uniqueness constraints are not supported in this Cypher version.", c.position)

      // CREATE TEXT INDEX ...
      case c: CreateTextNodeIndex =>
        throw cypherExceptionFactory.syntaxException("Text indexes are not supported in this Cypher version.", c.position)
      case c: CreateTextRelationshipIndex =>
        throw cypherExceptionFactory.syntaxException("Text indexes are not supported in this Cypher version.", c.position)

      // SHOW TEXT INDEXES
      case s: ShowIndexesClause if s.indexType == TextIndexes =>
        throw cypherExceptionFactory.syntaxException("Filtering on text indexes in SHOW INDEXES is not supported in this Cypher version.", s.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] FOR (node:Label) REQUIRE node.prop IS NOT NULL
      case c: CreateNodePropertyExistenceConstraint if c.constraintVersion == ConstraintVersion2 =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `FOR ... REQUIRE` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] FOR ()-[r:R]-() REQUIRE r.prop IS NOT NULL
      case c: CreateRelationshipPropertyExistenceConstraint if c.constraintVersion == ConstraintVersion2 =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `FOR ... REQUIRE` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] FOR (node:Label) REQUIRE node.prop IS NODE KEY
      case c: CreateNodeKeyConstraint if c.constraintVersion == ConstraintVersion2 =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint using `FOR ... REQUIRE` is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT [name] [IF NOT EXISTS] FOR (node:Label) REQUIRE node.prop IS UNIQUE
      case c: CreateUniquePropertyConstraint if c.constraintVersion == ConstraintVersion2 =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint using `FOR ... REQUIRE` is not supported in this Cypher version.", c.position)

    }
  }

}

trait Additions extends {
  def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = {}
}
