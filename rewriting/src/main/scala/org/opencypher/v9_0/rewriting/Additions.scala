/*
 * Copyright © 2002-2020 Neo4j Sweden AB (http://neo4j.com)
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

import org.opencypher.v9_0.ast.AlterUser
import org.opencypher.v9_0.ast.CreateIndexNewSyntax
import org.opencypher.v9_0.ast.CreateNodeKeyConstraint
import org.opencypher.v9_0.ast.CreateNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateUniquePropertyConstraint
import org.opencypher.v9_0.ast.CreateUser
import org.opencypher.v9_0.ast.DbmsPrivilege
import org.opencypher.v9_0.ast.DefaultGraphScope
import org.opencypher.v9_0.ast.DenyPrivilege
import org.opencypher.v9_0.ast.DropConstraintOnName
import org.opencypher.v9_0.ast.DropIndexOnName
import org.opencypher.v9_0.ast.ExecuteAdminProcedureAction
import org.opencypher.v9_0.ast.ExecuteBoostedProcedureAction
import org.opencypher.v9_0.ast.ExecuteProcedureAction
import org.opencypher.v9_0.ast.GrantPrivilege
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.RevokePrivilege
import org.opencypher.v9_0.ast.ShowPrivileges
import org.opencypher.v9_0.ast.ShowRolesPrivileges
import org.opencypher.v9_0.ast.ShowUsersPrivileges
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.util.CypherExceptionFactory

import scala.Option
import scala.Option

object Additions {

  // This is functionality that has been added in 4.0 and 4.1 and should not work when using CYPHER 3.5
  case object addedFeaturesIn4_x extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      case u: UseGraph =>
        throw cypherExceptionFactory.syntaxException("The USE clause is not supported in this Cypher version.", u.position)

      // CREATE INDEX [name] FOR (n:Label) ON (n.prop)
      case c: CreateIndexNewSyntax =>
        throw cypherExceptionFactory.syntaxException("Creating index using this syntax is not supported in this Cypher version.", c.position)

      // DROP INDEX name
      case d: DropIndexOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping index by name is not supported in this Cypher version.", d.position)

      // CREATE CONSTRAINT name ON ... IS NODE KEY
      case c@CreateNodeKeyConstraint(_, _, _, Some(_), _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node key constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ... IS UNIQUE
      case c@CreateUniquePropertyConstraint(_, _, _, Some(_),_, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named uniqueness constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON () ... EXISTS
      case c@CreateNodePropertyExistenceConstraint(_, _, _, Some(_),_, _) =>
        throw cypherExceptionFactory.syntaxException("Creating named node existence constraint is not supported in this Cypher version.", c.position)

      // CREATE CONSTRAINT name ON ()-[]-() ... EXISTS
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, Some(_), _,_) =>
        throw cypherExceptionFactory.syntaxException("Creating named relationship existence constraint is not supported in this Cypher version.", c.position)

      // DROP CONSTRAINT name
      case d: DropConstraintOnName =>
        throw cypherExceptionFactory.syntaxException("Dropping constraint by name is not supported in this Cypher version.", d.position)

      case e: ExistsSubClause =>
        throw cypherExceptionFactory.syntaxException("Existential subquery is not supported in this Cypher version.", e.position)

      // Administration commands against system database are checked in CompilerFactory to cover all of them at once
    }
  }

  // This is functionality that has been added in 4.2 and should not work when using CYPHER 3.5 and CYPHER 4.1
  case object addedFeaturesIn4_2 extends Additions {

    override def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = statement.treeExists {

      case c@CreateUser(_, true, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Creating a user with an encrypted password is not supported in this Cypher version.", c.position)

      case c@AlterUser(_, Some(true), _, _, _) =>
        throw cypherExceptionFactory.syntaxException("Updating a user with an encrypted password is not supported in this Cypher version.", c.position)

      // SHOW ROLE role1, role2 PRIVILEGES
      case s@ShowPrivileges(ShowRolesPrivileges(r), _, _) if r.size > 1 =>
        throw cypherExceptionFactory.syntaxException("Multiple roles in SHOW ROLE PRIVILEGE command is not supported in this Cypher version.", s.position)

      // SHOW USER user1, user2 PRIVILEGES
      case s@ShowPrivileges(ShowUsersPrivileges(u), _,  _) if u.size > 1 =>
        throw cypherExceptionFactory.syntaxException("Multiple users in SHOW USER PRIVILEGE command is not supported in this Cypher version.", s.position)

      case d: DefaultGraphScope => throw cypherExceptionFactory.syntaxException("Default graph is not supported in this Cypher version.", d.position)

      // GRANT EXECUTE [BOOSTED|ADMIN] PROCEDURES ...
      case p@GrantPrivilege(DbmsPrivilege(ExecuteProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE PROCEDURE is not supported in this Cypher version.", p.position)
      case p@GrantPrivilege(DbmsPrivilege(ExecuteBoostedProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE BOOSTED PROCEDURE is not supported in this Cypher version.", p.position)
      case p@GrantPrivilege(DbmsPrivilege(ExecuteAdminProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE ADMIN PROCEDURES is not supported in this Cypher version.", p.position)

      // DENY EXECUTE [BOOSTED|ADMIN] PROCEDURES ...
      case p@DenyPrivilege(DbmsPrivilege(ExecuteProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE PROCEDURE is not supported in this Cypher version.", p.position)
      case p@DenyPrivilege(DbmsPrivilege(ExecuteBoostedProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE BOOSTED PROCEDURE is not supported in this Cypher version.", p.position)
      case p@DenyPrivilege(DbmsPrivilege(ExecuteAdminProcedureAction), _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE ADMIN PROCEDURES is not supported in this Cypher version.", p.position)

      // REVOKE EXECUTE [BOOSTED|ADMIN] PROCEDURES ...
      case p@RevokePrivilege(DbmsPrivilege(ExecuteProcedureAction), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE PROCEDURE is not supported in this Cypher version.", p.position)
      case p@RevokePrivilege(DbmsPrivilege(ExecuteBoostedProcedureAction), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE BOOSTED PROCEDURE is not supported in this Cypher version.", p.position)
      case p@RevokePrivilege(DbmsPrivilege(ExecuteAdminProcedureAction), _, _, _, _, _) =>
        throw cypherExceptionFactory.syntaxException("EXECUTE ADMIN PROCEDURES is not supported in this Cypher version.", p.position)

      // CREATE OR REPLACE INDEX name ...
      // CREATE INDEX [name] IF NOT EXISTS ...
      case c@CreateIndexNewSyntax(_, _, _, _, ifExistsDo, _) if ifExistsDo != IfExistsThrowError =>
        throw cypherExceptionFactory.syntaxException("Creating index using `OR REPLACE` or `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // DROP INDEX name IF EXISTS
      case d@DropIndexOnName(_, true, _) =>
        throw cypherExceptionFactory.syntaxException("Dropping index using `IF EXISTS` is not supported in this Cypher version.", d.position)

      // CREATE OR REPLACE CONSTRAINT name ...
      // CREATE CONSTRAINT [name] IF NOT EXISTS ...
      case c@CreateNodeKeyConstraint(_, _, _, _, ifExistsDo, _) if ifExistsDo != IfExistsThrowError =>
        throw cypherExceptionFactory.syntaxException("Creating node key constraint using `OR REPLACE` or `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE OR REPLACE CONSTRAINT name ...
      // CREATE CONSTRAINT [name] IF NOT EXISTS ...
      case c@CreateUniquePropertyConstraint(_, _, _, _, ifExistsDo, _) if ifExistsDo != IfExistsThrowError =>
        throw cypherExceptionFactory.syntaxException("Creating uniqueness constraint using `OR REPLACE` or `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE OR REPLACE CONSTRAINT name ...
      // CREATE CONSTRAINT [name] IF NOT EXISTS ...
      case c@CreateNodePropertyExistenceConstraint(_, _, _, _, ifExistsDo, _) if ifExistsDo != IfExistsThrowError =>
        throw cypherExceptionFactory.syntaxException("Creating node existence constraint using `OR REPLACE` or `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // CREATE OR REPLACE CONSTRAINT name ...
      // CREATE CONSTRAINT [name] IF NOT EXISTS ...
      case c@CreateRelationshipPropertyExistenceConstraint(_, _, _, _, ifExistsDo, _) if ifExistsDo != IfExistsThrowError =>
        throw cypherExceptionFactory.syntaxException("Creating relationship existence constraint using `OR REPLACE` or `IF NOT EXISTS` is not supported in this Cypher version.", c.position)

      // DROP CONSTRAINT name IF EXISTS
      case d@DropConstraintOnName(_, true, _) =>
        throw cypherExceptionFactory.syntaxException("Dropping constraint using `IF EXISTS` is not supported in this Cypher version.", d.position)
    }
  }
}

trait Additions extends {
  def check(statement: Statement, cypherExceptionFactory: CypherExceptionFactory): Unit = {}
}
