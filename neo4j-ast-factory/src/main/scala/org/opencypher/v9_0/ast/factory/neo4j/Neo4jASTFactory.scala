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
import org.opencypher.v9_0.ast.AccessDatabaseAction
import org.opencypher.v9_0.ast.ActionResource
import org.opencypher.v9_0.ast.AdministrationAction
import org.opencypher.v9_0.ast.AdministrationCommand
import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.AllConstraintActions
import org.opencypher.v9_0.ast.AllConstraints
import org.opencypher.v9_0.ast.AllDatabaseAction
import org.opencypher.v9_0.ast.AllDatabaseManagementActions
import org.opencypher.v9_0.ast.AllDatabasesQualifier
import org.opencypher.v9_0.ast.AllDatabasesScope
import org.opencypher.v9_0.ast.AllDbmsAction
import org.opencypher.v9_0.ast.AllFunctions
import org.opencypher.v9_0.ast.AllGraphAction
import org.opencypher.v9_0.ast.AllGraphsScope
import org.opencypher.v9_0.ast.AllIndexActions
import org.opencypher.v9_0.ast.AllIndexes
import org.opencypher.v9_0.ast.AllLabelResource
import org.opencypher.v9_0.ast.AllPrivilegeActions
import org.opencypher.v9_0.ast.AllPropertyResource
import org.opencypher.v9_0.ast.AllQualifier
import org.opencypher.v9_0.ast.AllRoleActions
import org.opencypher.v9_0.ast.AllTokenActions
import org.opencypher.v9_0.ast.AllTransactionActions
import org.opencypher.v9_0.ast.AllUserActions
import org.opencypher.v9_0.ast.AlterDatabase
import org.opencypher.v9_0.ast.AlterDatabaseAction
import org.opencypher.v9_0.ast.AlterDatabaseAlias
import org.opencypher.v9_0.ast.AlterUser
import org.opencypher.v9_0.ast.AlterUserAction
import org.opencypher.v9_0.ast.AscSortItem
import org.opencypher.v9_0.ast.AssignPrivilegeAction
import org.opencypher.v9_0.ast.AssignRoleAction
import org.opencypher.v9_0.ast.BtreeIndexes
import org.opencypher.v9_0.ast.BuiltInFunctions
import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.ConstraintVersion0
import org.opencypher.v9_0.ast.ConstraintVersion1
import org.opencypher.v9_0.ast.ConstraintVersion2
import org.opencypher.v9_0.ast.Create
import org.opencypher.v9_0.ast.CreateBtreeNodeIndex
import org.opencypher.v9_0.ast.CreateBtreeRelationshipIndex
import org.opencypher.v9_0.ast.CreateConstraintAction
import org.opencypher.v9_0.ast.CreateDatabase
import org.opencypher.v9_0.ast.CreateDatabaseAction
import org.opencypher.v9_0.ast.CreateDatabaseAlias
import org.opencypher.v9_0.ast.CreateElementAction
import org.opencypher.v9_0.ast.CreateFulltextNodeIndex
import org.opencypher.v9_0.ast.CreateFulltextRelationshipIndex
import org.opencypher.v9_0.ast.CreateIndex
import org.opencypher.v9_0.ast.CreateIndexAction
import org.opencypher.v9_0.ast.CreateIndexOldSyntax
import org.opencypher.v9_0.ast.CreateLookupIndex
import org.opencypher.v9_0.ast.CreateNodeLabelAction
import org.opencypher.v9_0.ast.CreatePointNodeIndex
import org.opencypher.v9_0.ast.CreatePointRelationshipIndex
import org.opencypher.v9_0.ast.CreatePropertyKeyAction
import org.opencypher.v9_0.ast.CreateRangeNodeIndex
import org.opencypher.v9_0.ast.CreateRangeRelationshipIndex
import org.opencypher.v9_0.ast.CreateRelationshipTypeAction
import org.opencypher.v9_0.ast.CreateRole
import org.opencypher.v9_0.ast.CreateRoleAction
import org.opencypher.v9_0.ast.CreateTextNodeIndex
import org.opencypher.v9_0.ast.CreateTextRelationshipIndex
import org.opencypher.v9_0.ast.CreateUser
import org.opencypher.v9_0.ast.CreateUserAction
import org.opencypher.v9_0.ast.CurrentUser
import org.opencypher.v9_0.ast.DatabaseAction
import org.opencypher.v9_0.ast.DatabasePrivilege
import org.opencypher.v9_0.ast.DatabaseResource
import org.opencypher.v9_0.ast.DatabaseScope
import org.opencypher.v9_0.ast.DbmsAction
import org.opencypher.v9_0.ast.DbmsPrivilege
import org.opencypher.v9_0.ast.DefaultDatabaseScope
import org.opencypher.v9_0.ast.DefaultGraphScope
import org.opencypher.v9_0.ast.Delete
import org.opencypher.v9_0.ast.DeleteElementAction
import org.opencypher.v9_0.ast.DenyPrivilege
import org.opencypher.v9_0.ast.DescSortItem
import org.opencypher.v9_0.ast.DestroyData
import org.opencypher.v9_0.ast.DropConstraintAction
import org.opencypher.v9_0.ast.DropConstraintOnName
import org.opencypher.v9_0.ast.DropDatabase
import org.opencypher.v9_0.ast.DropDatabaseAction
import org.opencypher.v9_0.ast.DropDatabaseAdditionalAction
import org.opencypher.v9_0.ast.DropDatabaseAlias
import org.opencypher.v9_0.ast.DropIndex
import org.opencypher.v9_0.ast.DropIndexAction
import org.opencypher.v9_0.ast.DropIndexOnName
import org.opencypher.v9_0.ast.DropNodeKeyConstraint
import org.opencypher.v9_0.ast.DropNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.DropRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.DropRole
import org.opencypher.v9_0.ast.DropRoleAction
import org.opencypher.v9_0.ast.DropUniquePropertyConstraint
import org.opencypher.v9_0.ast.DropUser
import org.opencypher.v9_0.ast.DropUserAction
import org.opencypher.v9_0.ast.DumpData
import org.opencypher.v9_0.ast.ElementQualifier
import org.opencypher.v9_0.ast.ElementsAllQualifier
import org.opencypher.v9_0.ast.ExecuteAdminProcedureAction
import org.opencypher.v9_0.ast.ExecuteBoostedFunctionAction
import org.opencypher.v9_0.ast.ExecuteBoostedProcedureAction
import org.opencypher.v9_0.ast.ExecuteFunctionAction
import org.opencypher.v9_0.ast.ExecuteProcedureAction
import org.opencypher.v9_0.ast.ExistsConstraints
import org.opencypher.v9_0.ast.Foreach
import org.opencypher.v9_0.ast.FulltextIndexes
import org.opencypher.v9_0.ast.FunctionQualifier
import org.opencypher.v9_0.ast.GrantPrivilege
import org.opencypher.v9_0.ast.GrantRolesToUsers
import org.opencypher.v9_0.ast.GraphAction
import org.opencypher.v9_0.ast.GraphPrivilege
import org.opencypher.v9_0.ast.GraphScope
import org.opencypher.v9_0.ast.HomeDatabaseScope
import org.opencypher.v9_0.ast.HomeGraphScope
import org.opencypher.v9_0.ast.IfExistsDo
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.IfExistsInvalidSyntax
import org.opencypher.v9_0.ast.IfExistsReplace
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.ImpersonateUserAction
import org.opencypher.v9_0.ast.IndefiniteWait
import org.opencypher.v9_0.ast.LabelAllQualifier
import org.opencypher.v9_0.ast.LabelQualifier
import org.opencypher.v9_0.ast.LabelsResource
import org.opencypher.v9_0.ast.Limit
import org.opencypher.v9_0.ast.LoadCSV
import org.opencypher.v9_0.ast.LookupIndexes
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.MatchAction
import org.opencypher.v9_0.ast.Merge
import org.opencypher.v9_0.ast.MergeAdminAction
import org.opencypher.v9_0.ast.NamedDatabaseScope
import org.opencypher.v9_0.ast.NamedGraphScope
import org.opencypher.v9_0.ast.NoOptions
import org.opencypher.v9_0.ast.NoResource
import org.opencypher.v9_0.ast.NoWait
import org.opencypher.v9_0.ast.NodeExistsConstraints
import org.opencypher.v9_0.ast.NodeKeyConstraints
import org.opencypher.v9_0.ast.OnCreate
import org.opencypher.v9_0.ast.OnMatch
import org.opencypher.v9_0.ast.OptionsMap
import org.opencypher.v9_0.ast.OptionsParam
import org.opencypher.v9_0.ast.OrderBy
import org.opencypher.v9_0.ast.PeriodicCommitHint
import org.opencypher.v9_0.ast.PointIndexes
import org.opencypher.v9_0.ast.PrivilegeQualifier
import org.opencypher.v9_0.ast.PrivilegeType
import org.opencypher.v9_0.ast.ProcedureQualifier
import org.opencypher.v9_0.ast.ProcedureResult
import org.opencypher.v9_0.ast.ProcedureResultItem
import org.opencypher.v9_0.ast.PropertiesResource
import org.opencypher.v9_0.ast.Query
import org.opencypher.v9_0.ast.RangeIndexes
import org.opencypher.v9_0.ast.ReadAction
import org.opencypher.v9_0.ast.ReadAdministrationCommand
import org.opencypher.v9_0.ast.ReadOnlyAccess
import org.opencypher.v9_0.ast.ReadWriteAccess
import org.opencypher.v9_0.ast.RelExistsConstraints
import org.opencypher.v9_0.ast.RelationshipAllQualifier
import org.opencypher.v9_0.ast.RelationshipQualifier
import org.opencypher.v9_0.ast.Remove
import org.opencypher.v9_0.ast.RemoveHomeDatabaseAction
import org.opencypher.v9_0.ast.RemoveItem
import org.opencypher.v9_0.ast.RemoveLabelAction
import org.opencypher.v9_0.ast.RemoveLabelItem
import org.opencypher.v9_0.ast.RemovePrivilegeAction
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.RemoveRoleAction
import org.opencypher.v9_0.ast.RemovedSyntax
import org.opencypher.v9_0.ast.RenameRole
import org.opencypher.v9_0.ast.RenameRoleAction
import org.opencypher.v9_0.ast.RenameUser
import org.opencypher.v9_0.ast.RenameUserAction
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItem
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.RevokeBothType
import org.opencypher.v9_0.ast.RevokeDenyType
import org.opencypher.v9_0.ast.RevokeGrantType
import org.opencypher.v9_0.ast.RevokePrivilege
import org.opencypher.v9_0.ast.RevokeRolesFromUsers
import org.opencypher.v9_0.ast.SchemaCommand
import org.opencypher.v9_0.ast.SeekOnly
import org.opencypher.v9_0.ast.SeekOrScan
import org.opencypher.v9_0.ast.SetClause
import org.opencypher.v9_0.ast.SetDatabaseAccessAction
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetHomeDatabaseAction
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetItem
import org.opencypher.v9_0.ast.SetLabelAction
import org.opencypher.v9_0.ast.SetLabelItem
import org.opencypher.v9_0.ast.SetOwnPassword
import org.opencypher.v9_0.ast.SetPasswordsAction
import org.opencypher.v9_0.ast.SetPropertyAction
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.SetUserHomeDatabaseAction
import org.opencypher.v9_0.ast.SetUserStatusAction
import org.opencypher.v9_0.ast.ShowAllPrivileges
import org.opencypher.v9_0.ast.ShowConstraintAction
import org.opencypher.v9_0.ast.ShowConstraintType
import org.opencypher.v9_0.ast.ShowConstraintsClause
import org.opencypher.v9_0.ast.ShowCurrentUser
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.ShowFunctionsClause
import org.opencypher.v9_0.ast.ShowIndexAction
import org.opencypher.v9_0.ast.ShowIndexesClause
import org.opencypher.v9_0.ast.ShowPrivilegeAction
import org.opencypher.v9_0.ast.ShowPrivilegeCommands
import org.opencypher.v9_0.ast.ShowPrivilegeScope
import org.opencypher.v9_0.ast.ShowPrivileges
import org.opencypher.v9_0.ast.ShowProceduresClause
import org.opencypher.v9_0.ast.ShowRoleAction
import org.opencypher.v9_0.ast.ShowRoles
import org.opencypher.v9_0.ast.ShowRolesPrivileges
import org.opencypher.v9_0.ast.ShowTransactionAction
import org.opencypher.v9_0.ast.ShowTransactionsClause
import org.opencypher.v9_0.ast.ShowUserAction
import org.opencypher.v9_0.ast.ShowUserPrivileges
import org.opencypher.v9_0.ast.ShowUsers
import org.opencypher.v9_0.ast.ShowUsersPrivileges
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Skip
import org.opencypher.v9_0.ast.SortItem
import org.opencypher.v9_0.ast.StartDatabase
import org.opencypher.v9_0.ast.StartDatabaseAction
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.StatementWithGraph
import org.opencypher.v9_0.ast.StopDatabase
import org.opencypher.v9_0.ast.StopDatabaseAction
import org.opencypher.v9_0.ast.SubqueryCall
import org.opencypher.v9_0.ast.TerminateTransactionAction
import org.opencypher.v9_0.ast.TerminateTransactionsClause
import org.opencypher.v9_0.ast.TextIndexes
import org.opencypher.v9_0.ast.TimeoutAfter
import org.opencypher.v9_0.ast.TraverseAction
import org.opencypher.v9_0.ast.UnaliasedReturnItem
import org.opencypher.v9_0.ast.UnionAll
import org.opencypher.v9_0.ast.UnionDistinct
import org.opencypher.v9_0.ast.UniqueConstraints
import org.opencypher.v9_0.ast.UnresolvedCall
import org.opencypher.v9_0.ast.Unwind
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.ast.User
import org.opencypher.v9_0.ast.UserAllQualifier
import org.opencypher.v9_0.ast.UserDefinedFunctions
import org.opencypher.v9_0.ast.UserOptions
import org.opencypher.v9_0.ast.UserQualifier
import org.opencypher.v9_0.ast.UsingAnyIndexType
import org.opencypher.v9_0.ast.UsingBtreeIndexType
import org.opencypher.v9_0.ast.UsingHint
import org.opencypher.v9_0.ast.UsingIndexHintType
import org.opencypher.v9_0.ast.UsingJoinHint
import org.opencypher.v9_0.ast.UsingScanHint
import org.opencypher.v9_0.ast.UsingTextIndexType
import org.opencypher.v9_0.ast.ValidSyntax
import org.opencypher.v9_0.ast.WaitUntilComplete
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.With
import org.opencypher.v9_0.ast.WriteAction
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.ast.factory.ASTExceptionFactory
import org.opencypher.v9_0.ast.factory.ASTFactory
import org.opencypher.v9_0.ast.factory.ASTFactory.MergeActionType
import org.opencypher.v9_0.ast.factory.ASTFactory.StringPos
import org.opencypher.v9_0.ast.factory.AccessType
import org.opencypher.v9_0.ast.factory.AccessType.READ_ONLY
import org.opencypher.v9_0.ast.factory.AccessType.READ_WRITE
import org.opencypher.v9_0.ast.factory.ActionType
import org.opencypher.v9_0.ast.factory.ConstraintType
import org.opencypher.v9_0.ast.factory.ConstraintVersion
import org.opencypher.v9_0.ast.factory.CreateIndexTypes
import org.opencypher.v9_0.ast.factory.HintIndexType
import org.opencypher.v9_0.ast.factory.ParameterType
import org.opencypher.v9_0.ast.factory.ScopeType
import org.opencypher.v9_0.ast.factory.ShowCommandFilterTypes
import org.opencypher.v9_0.ast.factory.SimpleEither
import org.opencypher.v9_0.expressions.Add
import org.opencypher.v9_0.expressions.AllIterablePredicate
import org.opencypher.v9_0.expressions.AllPropertiesSelector
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.AnonymousPatternPart
import org.opencypher.v9_0.expressions.AnyIterablePredicate
import org.opencypher.v9_0.expressions.CaseExpression
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.Contains
import org.opencypher.v9_0.expressions.CountStar
import org.opencypher.v9_0.expressions.DecimalDoubleLiteral
import org.opencypher.v9_0.expressions.Divide
import org.opencypher.v9_0.expressions.EndsWith
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.expressions.ExplicitParameter
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.False
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.GreaterThanOrEqual
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.In
import org.opencypher.v9_0.expressions.InvalidNotEquals
import org.opencypher.v9_0.expressions.IsNotNull
import org.opencypher.v9_0.expressions.IsNull
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.LabelOrRelTypeName
import org.opencypher.v9_0.expressions.LessThan
import org.opencypher.v9_0.expressions.LessThanOrEqual
import org.opencypher.v9_0.expressions.ListComprehension
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.ListSlice
import org.opencypher.v9_0.expressions.LiteralEntry
import org.opencypher.v9_0.expressions.MapExpression
import org.opencypher.v9_0.expressions.MapProjection
import org.opencypher.v9_0.expressions.MapProjectionElement
import org.opencypher.v9_0.expressions.Modulo
import org.opencypher.v9_0.expressions.Multiply
import org.opencypher.v9_0.expressions.NamedPatternPart
import org.opencypher.v9_0.expressions.Namespace
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.NoneIterablePredicate
import org.opencypher.v9_0.expressions.Not
import org.opencypher.v9_0.expressions.NotEquals
import org.opencypher.v9_0.expressions.Null
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.PatternPart
import org.opencypher.v9_0.expressions.Pow
import org.opencypher.v9_0.expressions.ProcedureName
import org.opencypher.v9_0.expressions.ProcedureOutput
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.PropertySelector
import org.opencypher.v9_0.expressions.Range
import org.opencypher.v9_0.expressions.ReduceExpression
import org.opencypher.v9_0.expressions.RegexMatch
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.SensitiveParameter
import org.opencypher.v9_0.expressions.SensitiveStringLiteral
import org.opencypher.v9_0.expressions.ShortestPathExpression
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.SignedHexIntegerLiteral
import org.opencypher.v9_0.expressions.SignedOctalIntegerLiteral
import org.opencypher.v9_0.expressions.SingleIterablePredicate
import org.opencypher.v9_0.expressions.StartsWith
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.Subtract
import org.opencypher.v9_0.expressions.True
import org.opencypher.v9_0.expressions.UnaryAdd
import org.opencypher.v9_0.expressions.UnarySubtract
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.expressions.VariableSelector
import org.opencypher.v9_0.expressions.Xor
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTString

import java.lang
import java.nio.charset.StandardCharsets
import java.util
import java.util.stream.Collectors
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsScalaMap
import scala.util.Either

final case class Privilege(privilegeType: PrivilegeType, resource: ActionResource, qualifier: util.List[PrivilegeQualifier])

trait DecorateTuple {
  class AsScala[A](op: => A) {
    def asScala: A = op
  }

  implicit def asScalaEither[L, R](i: SimpleEither[L, R]): AsScala[Either[L, R]] = {
    new AsScala(if (i.getRight == null) Left[L, R](i.getLeft) else Right[L, R](i.getRight))
  }
}

object TupleConverter extends DecorateTuple

import org.opencypher.v9_0.ast.factory.neo4j.TupleConverter.asScalaEither

class Neo4jASTFactory(query: String, anonymousVariableNameGenerator: AnonymousVariableNameGenerator)
  extends ASTFactory[Statement,
    Query,
    Clause,
    Return,
    ReturnItem,
    ReturnItems,
    SortItem,
    PatternPart,
    NodePattern,
    RelationshipPattern,
    Option[Range],
    SetClause,
    SetItem,
    RemoveItem,
    ProcedureResultItem,
    UsingHint,
    Expression,
    Parameter,
    Variable,
    Property,
    MapProjectionElement,
    UseGraph,
    StatementWithGraph,
    AdministrationCommand,
    SchemaCommand,
    Yield,
    Where,
    DatabaseScope,
    WaitUntilComplete,
    AdministrationAction,
    GraphScope,
    Privilege,
    ActionResource,
    PrivilegeQualifier,
    SubqueryCall.InTransactionsParameters,
    InputPosition] {

  override def newSingleQuery(p: InputPosition, clauses: util.List[Clause]): Query = {
    if (clauses.isEmpty) {
      throw new Neo4jASTConstructionException("A valid Cypher query has to contain at least 1 clause")
    }
    Query(None, SingleQuery(clauses.asScala.toList)(p))(p)
  }

  override def newSingleQuery(clauses: util.List[Clause]): Query = {
    if (clauses.isEmpty) {
      throw new Neo4jASTConstructionException("A valid Cypher query has to contain at least 1 clause")
    }
    val pos = clauses.get(0).position
    Query(None, SingleQuery(clauses.asScala.toList)(pos))(pos)
  }

  override def newUnion(p: InputPosition,
                        lhs: Query,
                        rhs: Query,
                        all: Boolean): Query = {
    val rhsQuery =
      rhs.part match {
        case x: SingleQuery => x
        case other =>
          throw new Neo4jASTConstructionException(
            s"The Neo4j AST encodes Unions as a left-deep tree, so the rhs query must always be a SingleQuery. Got `$other`")
      }

    val union =
      if (all) UnionAll(lhs.part, rhsQuery)(p)
      else UnionDistinct(lhs.part, rhsQuery)(p)
    Query(None, union)(lhs.position)
  }

  override def periodicCommitQuery(p: InputPosition,
                                   periodicCommitPosition: InputPosition,
                                   batchSize: String,
                                   loadCsv: Clause,
                                   queryBody: util.List[Clause]): Query =
    Query(Some(PeriodicCommitHint(Option(batchSize).map(UnsignedDecimalIntegerLiteral(_)(periodicCommitPosition)))(periodicCommitPosition)),
      SingleQuery(loadCsv +: queryBody.asScala)(loadCsv.position)
    )(p)

  override def useClause(p: InputPosition,
                         e: Expression): UseGraph = UseGraph(e)(p)

  override def newReturnClause(p: InputPosition,
                               distinct: Boolean,
                               returnItems: ReturnItems,
                               order: util.List[SortItem],
                               orderPosition: InputPosition,
                               skip: Expression,
                               skipPosition: InputPosition,
                               limit: Expression,
                               limitPosition: InputPosition): Return = {
    val orderList = order.asScala.toList
    Return(distinct,
      returnItems,
      if (order.isEmpty) None else Some(OrderBy(orderList)(orderPosition)),
      Option(skip).map(e => Skip(e)(skipPosition)),
      Option(limit).map(e => Limit(e)(limitPosition)))(p)
  }

  override def newReturnItems(p: InputPosition, returnAll: Boolean, returnItems: util.List[ReturnItem]): ReturnItems = {
    ReturnItems(returnAll, returnItems.asScala.toList)(p)
  }

  override def newReturnItem(p: InputPosition, e: Expression, v: Variable): ReturnItem = {
    AliasedReturnItem(e, v)(p, isAutoAliased = false)
  }

  override def newReturnItem(p: InputPosition,
                             e: Expression,
                             eStartOffset: Int,
                             eEndOffset: Int): ReturnItem = {

    val name = query.substring(eStartOffset, eEndOffset + 1)
    UnaliasedReturnItem(e, name)(p)
  }

  override def orderDesc(p: InputPosition, e: Expression): SortItem = DescSortItem(e)(p)

  override def orderAsc(p: InputPosition, e: Expression): SortItem = AscSortItem(e)(p)

  override def createClause(p: InputPosition, patterns: util.List[PatternPart]): Clause =
    Create(Pattern(patterns.asScala.toList)(patterns.asScala.map(_.position).minBy(_.offset)))(p)

  override def matchClause(p: InputPosition,
                           optional: Boolean,
                           patterns: util.List[PatternPart],
                           patternPos: InputPosition,
                           hints: util.List[UsingHint],
                           where: Where): Clause = {
    val patternList = patterns.asScala.toList
    Match(optional, Pattern(patternList)(patternPos), if (hints == null) Nil else hints.asScala.toList, Option(where))(p)
  }

  override def usingIndexHint(p: InputPosition,
                              v: Variable,
                              labelOrRelType: String,
                              properties: util.List[String],
                              seekOnly: Boolean,
                              indexType: HintIndexType): UsingHint =
    ast.UsingIndexHint(v,
      LabelOrRelTypeName(labelOrRelType)(p),
      properties.asScala.toList.map(PropertyKeyName(_)(p)),
      if (seekOnly) SeekOnly else SeekOrScan,
      usingIndexType(indexType))(p)

  private def usingIndexType(indexType: HintIndexType): UsingIndexHintType = indexType match {
    case HintIndexType.ANY   => UsingAnyIndexType
    case HintIndexType.BTREE => UsingBtreeIndexType
    case HintIndexType.TEXT  => UsingTextIndexType
  }

  override def usingJoin(p: InputPosition, joinVariables: util.List[Variable]): UsingHint =
    UsingJoinHint(joinVariables.asScala.toList)(p)

  override def usingScan(p: InputPosition,
                         v: Variable,
                         labelOrRelType: String): UsingHint =
    UsingScanHint(v, LabelOrRelTypeName(labelOrRelType)(p))(p)

  override def withClause(p: InputPosition,
                          r: Return,
                          where: Where): Clause =
    With(r.distinct,
      r.returnItems,
      r.orderBy,
      r.skip,
      r.limit,
      Option(where))(p)

  override def whereClause(p: InputPosition,
                           where: Expression): Where =
    Where(where)(p)

  override def setClause(p: InputPosition, setItems: util.List[SetItem]): SetClause =
    SetClause(setItems.asScala.toList)(p)

  override def setProperty(property: Property,
                           value: Expression): SetItem = SetPropertyItem(property, value)(property.position)

  override def setVariable(variable: Variable,
                           value: Expression): SetItem = SetExactPropertiesFromMapItem(variable, value)(variable.position)

  override def addAndSetVariable(variable: Variable,
                                 value: Expression): SetItem = SetIncludingPropertiesFromMapItem(variable, value)(variable.position)

  override def setLabels(variable: Variable,
                         labels: util.List[StringPos[InputPosition]]): SetItem =
    SetLabelItem(variable, labels.asScala.toList.map(sp => LabelName(sp.string)(sp.pos)))(variable.position)

  override def removeClause(p: InputPosition, removeItems: util.List[RemoveItem]): Clause =
    Remove(removeItems.asScala.toList)(p)

  override def removeProperty(property: Property): RemoveItem = RemovePropertyItem(property)

  override def removeLabels(variable: Variable, labels: util.List[StringPos[InputPosition]]): RemoveItem =
    RemoveLabelItem(variable, labels.asScala.toList.map(sp => LabelName(sp.string)(sp.pos)))(variable.position)

  override def deleteClause(p: InputPosition,
                            detach: Boolean,
                            expressions: util.List[Expression]): Clause = Delete(expressions.asScala.toList, detach)(p)

  override def unwindClause(p: InputPosition,
                            e: Expression,
                            v: Variable): Clause = Unwind(e, v)(p)

  override def mergeClause(p: InputPosition,
                           pattern: PatternPart,
                           setClauses: util.List[SetClause],
                           actionTypes: util.List[MergeActionType],
                           positions: util.List[InputPosition]): Clause = {
    val clausesIter = setClauses.iterator()
    val positionItr = positions.iterator()
    val actions = actionTypes.asScala.toList.map {
      case MergeActionType.OnMatch =>
        OnMatch(clausesIter.next())(positionItr.next)
      case MergeActionType.OnCreate =>
        OnCreate(clausesIter.next())(positionItr.next)
    }

    Merge(pattern, actions)(p)
  }

  override def callClause(p: InputPosition,
                          namespacePosition: InputPosition,
                          procedureNamePosition: InputPosition,
                          procedureResultPosition: InputPosition,
                          namespace: util.List[String],
                          name: String,
                          arguments: util.List[Expression],
                          yieldAll: Boolean,
                          resultItems: util.List[ProcedureResultItem],
                          where: Where): Clause =
    UnresolvedCall(
      Namespace(namespace.asScala.toList)(namespacePosition),
      ProcedureName(name)(procedureNamePosition),
      if (arguments == null) None else Some(arguments.asScala.toList),
      Option(resultItems).map(items => ProcedureResult(items.asScala.toList.toIndexedSeq, Option(where))(procedureResultPosition)),
      yieldAll
    )(p)

  override def callResultItem(p: InputPosition,
                              name: String,
                              v: Variable): ProcedureResultItem =
    if (v == null) ProcedureResultItem(Variable(name)(p))(p)
    else ProcedureResultItem(ProcedureOutput(name)(v.position), v)(p)

  override def loadCsvClause(p: InputPosition,
                             headers: Boolean,
                             source: Expression,
                             v: Variable,
                             fieldTerminator: String): Clause =
    LoadCSV(headers, source, v, Option(fieldTerminator).map(StringLiteral(_)(p)))(p)

  override def foreachClause(p: InputPosition,
                             v: Variable,
                             list: Expression,
                             clauses: util.List[Clause]): Clause =
    Foreach(v, list, clauses.asScala.toList)(p)

  override def subqueryInTransactionsParams(p: InputPosition, batchSize: Expression): SubqueryCall.InTransactionsParameters = {
    SubqueryCall.InTransactionsParameters(Option(batchSize))(p)
  }

  override def subqueryClause(p: InputPosition, subquery: Query, inTransactions: SubqueryCall.InTransactionsParameters): Clause =
    SubqueryCall(subquery.part, Option(inTransactions))(p)

  // PATTERNS

  override def namedPattern(v: Variable,
                            pattern: PatternPart): PatternPart =
    NamedPatternPart(v, pattern.asInstanceOf[AnonymousPatternPart])(v.position)

  override def shortestPathPattern(p: InputPosition, pattern: PatternPart): PatternPart = ShortestPaths(pattern.element, single = true)(p)

  override def allShortestPathsPattern(p: InputPosition, pattern: PatternPart): PatternPart = ShortestPaths(pattern.element, single = false)(p)

  override def everyPathPattern(nodes: util.List[NodePattern],
                                relationships: util.List[RelationshipPattern]): PatternPart = {

    val nodeIter = nodes.iterator()
    val relIter = relationships.iterator()

    var patternElement: PatternElement = nodeIter.next()
    while (relIter.hasNext) {
      val relPattern = relIter.next()
      val rightNodePattern = nodeIter.next()
      patternElement = RelationshipChain(patternElement, relPattern, rightNodePattern)(patternElement.position)
    }
    EveryPath(patternElement)
  }

  override def nodePattern(p: InputPosition,
                           v: Variable,
                           labels: util.List[StringPos[InputPosition]],
                           properties: Expression,
                           predicate: Expression): NodePattern = {
//    // FIXME: Temporary hack
//    val maybeSingleLabelExpression = labelExpression match {
//      case LabelLeaf(label) => Some(LabelName(label.name)(label.position))
//      case _                => None
//    }
    NodePattern(Option(v), labels.asScala.toList.map(sp => LabelName(sp.string)(sp.pos)), None, Option(properties), Option(predicate))(p)
  }

  override def relationshipPattern(p: InputPosition,
                                   left: Boolean,
                                   right: Boolean,
                                   v: Variable,
                                   relTypes: util.List[StringPos[InputPosition]],
                                   pathLength: Option[Range],
                                   properties: Expression,
                                   predicate: Expression,
                                   legacyTypeSeparator: Boolean): RelationshipPattern = {
    val direction =
      if (left && !right) SemanticDirection.INCOMING
      else if (!left && right) SemanticDirection.OUTGOING
      else SemanticDirection.BOTH

    val range =
      pathLength match {
        case null => None
        case None => Some(None)
        case Some(r) => Some(Some(r))
      }

    RelationshipPattern(
      Option(v),
      relTypes.asScala.toList.map(sp => RelTypeName(sp.string)(sp.pos)),
      range,
      Option(properties),
      Option(predicate),
      direction,
      legacyTypeSeparator)(p)
  }

  override def pathLength(p: InputPosition, pMin: InputPosition, pMax: InputPosition, minLength: String, maxLength: String): Option[Range] = {
    if (minLength == null && maxLength == null) {
      None
    } else {
      val min = if (minLength == "") None else Some(UnsignedDecimalIntegerLiteral(minLength)(pMin))
      val max = if (maxLength == "") None else Some(UnsignedDecimalIntegerLiteral(maxLength)(pMax))
      Some(Range(min, max)(if (pMin != null) pMin else p))
    }
  }

  // EXPRESSIONS

  override def newVariable(p: InputPosition, name: String): Variable = Variable(name)(p)

  override def newParameter(p: InputPosition, v: Variable, t: ParameterType): Parameter = {
    Parameter(v.name, transformParameterType(t))(p)
  }

  override def newParameter(p: InputPosition, offset: String, t: ParameterType): Parameter = {
    Parameter(offset, transformParameterType(t))(p)
  }

  private def transformParameterType(t: ParameterType) = {
    t match {
      case ParameterType.ANY => CTAny
      case ParameterType.STRING => CTString
      case ParameterType.MAP => CTMap
      case _ => throw new IllegalArgumentException("unknown parameter type: " + t.toString)
    }
  }
  override def newSensitiveStringParameter(p: InputPosition, v: Variable): Parameter = new ExplicitParameter(v.name, CTString)(p) with SensitiveParameter

  override def newSensitiveStringParameter(p: InputPosition, offset: String): Parameter = new ExplicitParameter(offset, CTString)(p) with SensitiveParameter

  override def newDouble(p: InputPosition, image: String): Expression = DecimalDoubleLiteral(image)(p)

  override def newDecimalInteger(p: InputPosition, image: String, negated: Boolean): Expression =
    if (negated) SignedDecimalIntegerLiteral("-"+image)(p)
    else SignedDecimalIntegerLiteral(image)(p)

  override def newHexInteger(p: InputPosition, image: String, negated: Boolean): Expression =
    if (negated) SignedHexIntegerLiteral("-"+image)(p)
    else SignedHexIntegerLiteral(image)(p)

  override def newOctalInteger(p: InputPosition, image: String, negated: Boolean): Expression =
    if (negated) SignedOctalIntegerLiteral("-"+image)(p)
    else SignedOctalIntegerLiteral(image)(p)

  override def newString(p: InputPosition, image: String): Expression = StringLiteral(image)(p)

  override def newTrueLiteral(p: InputPosition): Expression = True()(p)

  override def newFalseLiteral(p: InputPosition): Expression = False()(p)

  override def newNullLiteral(p: InputPosition): Expression = Null()(p)

  override def listLiteral(p: InputPosition, values: util.List[Expression]): Expression = {
    ListLiteral(values.asScala.toList)(p)
  }

  override def mapLiteral(p: InputPosition,
                          keys: util.List[StringPos[InputPosition]],
                          values: util.List[Expression]): Expression = {

    if (keys.size() != values.size()) {
      throw new Neo4jASTConstructionException(
        s"Map have the same number of keys and values, but got keys `${pretty(keys)}` and values `${pretty(values)}`")
    }

    var i = 0
    val pairs = new Array[(PropertyKeyName, Expression)](keys.size())

    while (i < keys.size()) {
      val key = keys.get(i)
      pairs(i) = PropertyKeyName(key.string)(key.pos) -> values.get(i)
      i += 1
    }

    MapExpression(pairs)(p)
  }

  override def hasLabelsOrTypes(subject: Expression, labels: util.List[StringPos[InputPosition]]): Expression =
    HasLabelsOrTypes(subject, labels.asScala.toList.map(sp => LabelOrRelTypeName(sp.string)(sp.pos)))(subject.position)

  override def property(subject: Expression, propertyKeyName: StringPos[InputPosition]): Property =
    Property(subject, PropertyKeyName(propertyKeyName.string)(propertyKeyName.pos))(subject.position)

  override def or(p: InputPosition,
                  lhs: Expression,
                  rhs: Expression): Expression = Or(lhs, rhs)(p)

  override def xor(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = Xor(lhs, rhs)(p)

  override def and(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = And(lhs, rhs)(p)

  override def ands(exprs: util.List[Expression]): Expression = Ands(exprs.asScala.toList)(exprs.get(0).position)

  override def not(p: InputPosition, e: Expression): Expression = Not(e)(p)

  override def plus(p: InputPosition,
                    lhs: Expression,
                    rhs: Expression): Expression = Add(lhs, rhs)(p)

  override def minus(p: InputPosition,
                     lhs: Expression,
                     rhs: Expression): Expression = Subtract(lhs, rhs)(p)

  override def multiply(p: InputPosition,
                        lhs: Expression,
                        rhs: Expression): Expression = Multiply(lhs, rhs)(p)

  override def divide(p: InputPosition,
                      lhs: Expression,
                      rhs: Expression): Expression = Divide(lhs, rhs)(p)

  override def modulo(p: InputPosition,
                      lhs: Expression,
                      rhs: Expression): Expression = Modulo(lhs, rhs)(p)

  override def pow(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = Pow(lhs, rhs)(p)

  override def unaryPlus(e: Expression): Expression = unaryPlus(e.position, e)

  override def unaryPlus(p: InputPosition, e: Expression): Expression = UnaryAdd(e)(p)

  override def unaryMinus(p: InputPosition, e: Expression): Expression = UnarySubtract(e)(p)

  override def eq(p: InputPosition,
                  lhs: Expression,
                  rhs: Expression): Expression = Equals(lhs, rhs)(p)

  override def neq(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = InvalidNotEquals(lhs, rhs)(p)

  override def neq2(p: InputPosition,
                    lhs: Expression,
                    rhs: Expression): Expression = NotEquals(lhs, rhs)(p)

  override def lte(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = LessThanOrEqual(lhs, rhs)(p)

  override def gte(p: InputPosition,
                   lhs: Expression,
                   rhs: Expression): Expression = GreaterThanOrEqual(lhs, rhs)(p)

  override def lt(p: InputPosition,
                  lhs: Expression,
                  rhs: Expression): Expression = LessThan(lhs, rhs)(p)

  override def gt(p: InputPosition,
                  lhs: Expression,
                  rhs: Expression): Expression = GreaterThan(lhs, rhs)(p)

  override def regeq(p: InputPosition,
                     lhs: Expression,
                     rhs: Expression): Expression = RegexMatch(lhs, rhs)(p)

  override def startsWith(p: InputPosition,
                          lhs: Expression,
                          rhs: Expression): Expression = StartsWith(lhs, rhs)(p)

  override def endsWith(p: InputPosition,
                        lhs: Expression,
                        rhs: Expression): Expression = EndsWith(lhs, rhs)(p)

  override def contains(p: InputPosition,
                        lhs: Expression,
                        rhs: Expression): Expression = Contains(lhs, rhs)(p)

  override def in(p: InputPosition,
                  lhs: Expression,
                  rhs: Expression): Expression = In(lhs, rhs)(p)

  override def isNull(p: InputPosition, e: Expression): Expression = IsNull(e)(p)

  override def isNotNull(p: InputPosition, e: Expression): Expression = IsNotNull(e)(p)

  override def listLookup(list: Expression,
                          index: Expression): Expression = ContainerIndex(list, index)(index.position)

  override def listSlice(p: InputPosition,
                         list: Expression,
                         start: Expression,
                         end: Expression): Expression = {
    ListSlice(list, Option(start), Option(end))(p)
  }

  override def newCountStar(p: InputPosition): Expression = CountStar()(p)

  override def functionInvocation(p: InputPosition,
                                  functionNamePosition: InputPosition,
                                  namespace: util.List[String],
                                  name: String,
                                  distinct: Boolean,
                                  arguments: util.List[Expression]): Expression = {
    FunctionInvocation(Namespace(namespace.asScala.toList)(p),
      FunctionName(name)(functionNamePosition),
      distinct,
      arguments.asScala.toIndexedSeq)(p)
  }

  override def listComprehension(p: InputPosition,
                                 v: Variable,
                                 list: Expression,
                                 where: Expression,
                                 projection: Expression): Expression =
    ListComprehension(v, list, Option(where), Option(projection))(p)

  override def patternComprehension(p: InputPosition,
                                    relationshipPatternPosition: InputPosition,
                                    v: Variable,
                                    pattern: PatternPart,
                                    where: Expression,
                                    projection: Expression): Expression =
    PatternComprehension(Option(v),
      RelationshipsPattern(pattern.element.asInstanceOf[RelationshipChain])(relationshipPatternPosition),
      Option(where),
      projection)(p, Set.empty, anonymousVariableNameGenerator.nextName, anonymousVariableNameGenerator.nextName)

  override def reduceExpression(p: InputPosition,
                                acc: Variable,
                                accExpr: Expression,
                                v: Variable,
                                list: Expression,
                                innerExpr: Expression): Expression =
    ReduceExpression(acc, accExpr, v, list, innerExpr)(p)

  override def allExpression(p: InputPosition,
                             v: Variable,
                             list: Expression,
                             where: Expression): Expression =
    AllIterablePredicate(v, list, Option(where))(p)

  override def anyExpression(p: InputPosition,
                             v: Variable,
                             list: Expression,
                             where: Expression): Expression =
    AnyIterablePredicate(v, list, Option(where))(p)

  override def noneExpression(p: InputPosition,
                              v: Variable,
                              list: Expression,
                              where: Expression): Expression =
    NoneIterablePredicate(v, list, Option(where))(p)

  override def singleExpression(p: InputPosition,
                                v: Variable,
                                list: Expression,
                                where: Expression): Expression =
    SingleIterablePredicate(v, list, Option(where))(p)

  override def patternExpression(p: InputPosition, pattern: PatternPart): Expression =
    pattern match {
      case paths: ShortestPaths =>
        ShortestPathExpression(paths)
      case _ =>
        PatternExpression(RelationshipsPattern(pattern.element.asInstanceOf[RelationshipChain])(p))(Set.empty, anonymousVariableNameGenerator.nextName, anonymousVariableNameGenerator.nextName)
    }

  override def existsSubQuery(p: InputPosition,
                              patterns: util.List[PatternPart],
                              where: Expression): Expression =
    ExistsSubClause(Pattern(patterns.asScala.toList)(p), Option(where))(p, Set.empty)

  override def mapProjection(p: InputPosition,
                             v: Variable,
                             items: util.List[MapProjectionElement]): Expression =
    MapProjection(v, items.asScala.toList)(p)

  override def mapProjectionLiteralEntry(property: StringPos[InputPosition],
                                         value: Expression): MapProjectionElement =
    LiteralEntry(PropertyKeyName(property.string)(property.pos), value)(value.position)

  override def mapProjectionProperty(property: StringPos[InputPosition]): MapProjectionElement =
    PropertySelector(Variable(property.string)(property.pos))(property.pos)

  override def mapProjectionVariable(v: Variable): MapProjectionElement =
    VariableSelector(v)(v.position)

  override def mapProjectionAll(p: InputPosition): MapProjectionElement =
    AllPropertiesSelector()(p)

  override def caseExpression(p: InputPosition,
                              e: Expression,
                              whens: util.List[Expression],
                              thens: util.List[Expression],
                              elze: Expression): Expression = {

    if (whens.size() != thens.size()) {
      throw new Neo4jASTConstructionException(
        s"Case expressions have the same number of whens and thens, but got whens `${pretty(whens)}` and thens `${pretty(thens)}`")
    }

    val alternatives = new Array[(Expression, Expression)](whens.size())
    var i = 0
    while (i < whens.size()) {
      alternatives(i) = whens.get(i) -> thens.get(i)
      i += 1
    }
    CaseExpression(Option(e), alternatives, Option(elze))(p)
  }

  override def inputPosition(offset: Int, line: Int, column: Int): InputPosition = InputPosition(offset, line, column)

  // Commands

  override def useGraph(command: StatementWithGraph, graph: UseGraph): StatementWithGraph = {
    command.withGraph(Option(graph))
  }

  // Show Commands

  override def yieldClause(p: InputPosition,
                           returnAll: Boolean,
                           returnItemList: util.List[ReturnItem],
                           returnItemsP: InputPosition,
                           order: util.List[SortItem],
                           orderPos: InputPosition,
                           skip: Expression,
                           skipPosition: InputPosition,
                           limit: Expression,
                           limitPosition: InputPosition,
                           where: Where): Yield = {

    val returnItems = ReturnItems(returnAll, returnItemList.asScala.toList)(returnItemsP)

    Yield(returnItems,
      Option(order.asScala.toList).filter(_.nonEmpty).map(o => OrderBy(o)(orderPos)),
      Option(skip).map(s => Skip(s)(skipPosition)),
      Option(limit).map(l => Limit(l)(limitPosition)),
      Option(where)
    )(p)
  }

  override def showIndexClause(p: InputPosition,
                               initialIndexType: ShowCommandFilterTypes,
                               brief: Boolean,
                               verbose: Boolean,
                               where: Where,
                               hasYield: Boolean): Clause = {
    val indexType = initialIndexType match {
      case ShowCommandFilterTypes.ALL => AllIndexes
      case ShowCommandFilterTypes.BTREE => BtreeIndexes
      case ShowCommandFilterTypes.RANGE => RangeIndexes
      case ShowCommandFilterTypes.FULLTEXT => FulltextIndexes
      case ShowCommandFilterTypes.TEXT => TextIndexes
      case ShowCommandFilterTypes.POINT => PointIndexes
      case ShowCommandFilterTypes.LOOKUP => LookupIndexes
      case t => throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidShowFilterType("indexes", t))
    }
    ShowIndexesClause(indexType, brief, verbose, Option(where), hasYield)(p)
  }

  override def showConstraintClause(p: InputPosition,
                                    initialConstraintType: ShowCommandFilterTypes,
                                    brief: Boolean,
                                    verbose: Boolean,
                                    where: Where,
                                    hasYield: Boolean): Clause = {
    val constraintType: ShowConstraintType = initialConstraintType match {
      case ShowCommandFilterTypes.ALL => AllConstraints
      case ShowCommandFilterTypes.UNIQUE => UniqueConstraints
      case ShowCommandFilterTypes.NODE_KEY => NodeKeyConstraints
      case ShowCommandFilterTypes.EXIST  => ExistsConstraints(ValidSyntax)
      case ShowCommandFilterTypes.OLD_EXISTS => ExistsConstraints(RemovedSyntax)
      case ShowCommandFilterTypes.OLD_EXIST => ExistsConstraints(ValidSyntax)
      case ShowCommandFilterTypes.NODE_EXIST => NodeExistsConstraints(ValidSyntax)
      case ShowCommandFilterTypes.NODE_OLD_EXISTS => NodeExistsConstraints(RemovedSyntax)
      case ShowCommandFilterTypes.NODE_OLD_EXIST => NodeExistsConstraints(ValidSyntax)
      case ShowCommandFilterTypes.RELATIONSHIP_EXIST => RelExistsConstraints(ValidSyntax)
      case ShowCommandFilterTypes.RELATIONSHIP_OLD_EXISTS => RelExistsConstraints(RemovedSyntax)
      case ShowCommandFilterTypes.RELATIONSHIP_OLD_EXIST => RelExistsConstraints(ValidSyntax)
      case t => throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidShowFilterType("constraints", t))
    }
    ShowConstraintsClause(constraintType, brief, verbose, Option(where), hasYield)(p)
  }

  override def showProcedureClause(p: InputPosition,
                                   currentUser: Boolean,
                                   user: String,
                                   where: Where,
                                   hasYield: Boolean): Clause = {
    // either we have 'EXECUTABLE BY user', 'EXECUTABLE [BY CURRENT USER]' or nothing
    val executableBy = if (user != null) Some(User(user)) else if (currentUser) Some(CurrentUser) else None
    ShowProceduresClause(executableBy, Option(where), hasYield)(p)
  }

  override def showFunctionClause(p: InputPosition,
                                  initialFunctionType: ShowCommandFilterTypes,
                                  currentUser: Boolean,
                                  user: String,
                                  where: Where,
                                  hasYield: Boolean): Clause = {
    val functionType = initialFunctionType match {
      case ShowCommandFilterTypes.ALL   => AllFunctions
      case ShowCommandFilterTypes.BUILT_IN => BuiltInFunctions
      case ShowCommandFilterTypes.USER_DEFINED  => UserDefinedFunctions
      case t => throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidShowFilterType("functions", t))
    }

    // either we have 'EXECUTABLE BY user', 'EXECUTABLE [BY CURRENT USER]' or nothing
    val executableBy = if (user != null) Some(User(user)) else if (currentUser) Some(CurrentUser) else None
    ShowFunctionsClause(functionType, executableBy, Option(where), hasYield)(p)
  }

  override def showTransactionsClause(p: InputPosition,
                                      ids: SimpleEither[util.List[String], Parameter],
                                      where: Where,
                                      hasYield: Boolean): Clause = {
    val scalaIds = ids.asScala.left.map(_.asScala.toList) // if left: map the string list to scala, if right: changes nothing
    ShowTransactionsClause.apply(scalaIds, Option(where), hasYield)(p)
  }

  override def terminateTransactionsClause(p: InputPosition, ids: SimpleEither[util.List[String], Parameter]): Clause = {
    val scalaIds = ids.asScala.left.map(_.asScala.toList) // if left: map the string list to scala, if right: changes nothing
    TerminateTransactionsClause(scalaIds)(p)
  }

  // Schema Commands
  // Constraint Commands

  override def createConstraint(p: InputPosition,
                                constraintType: ConstraintType,
                                replace: Boolean,
                                ifNotExists: Boolean,
                                name: String,
                                variable: Variable,
                                label: StringPos[InputPosition],
                                javaProperties: util.List[Property],
                                options: SimpleEither[util.Map[String, Expression], Parameter],
                                containsOn: Boolean,
                                constraintVersion: ConstraintVersion
                               ): SchemaCommand = {
    // Convert ConstraintVersion from Java to Scala
    val constraintVersionScala = constraintVersion match {
      case ConstraintVersion.CONSTRAINT_VERSION_0 => ConstraintVersion0
      case ConstraintVersion.CONSTRAINT_VERSION_1 => ConstraintVersion1
      case ConstraintVersion.CONSTRAINT_VERSION_2 => ConstraintVersion2
    }

    val properties = javaProperties.asScala
    constraintType match {
      case ConstraintType.UNIQUE => ast.CreateUniquePropertyConstraint(variable, LabelName(label.string)(label.pos), properties, Option(name),
        ifExistsDo(replace, ifNotExists), asOptionsAst(options), containsOn, constraintVersionScala)(p)
      case ConstraintType.NODE_KEY => ast.CreateNodeKeyConstraint(variable, LabelName(label.string)(label.pos), properties, Option(name),
        ifExistsDo(replace, ifNotExists), asOptionsAst(options), containsOn, constraintVersionScala)(p)
      case ConstraintType.NODE_EXISTS | ConstraintType.NODE_IS_NOT_NULL =>
        validateSingleProperty(properties, constraintType)
        ast.CreateNodePropertyExistenceConstraint(variable, LabelName(label.string)(label.pos), properties.head, Option(name), ifExistsDo(replace, ifNotExists), asOptionsAst(options), containsOn, constraintVersionScala)(p)
      case ConstraintType.REL_EXISTS | ConstraintType.REL_IS_NOT_NULL =>
        validateSingleProperty(properties, constraintType)
        ast.CreateRelationshipPropertyExistenceConstraint(variable, RelTypeName(label.string)(label.pos), properties.head, Option(name), ifExistsDo(replace, ifNotExists), asOptionsAst(options), containsOn, constraintVersionScala)(p)
    }
  }

  override def dropConstraint(p: InputPosition, name: String, ifExists: Boolean): DropConstraintOnName = DropConstraintOnName(name, ifExists)(p)

  override def dropConstraint(p: InputPosition,
                              constraintType: ConstraintType,
                              variable: Variable,
                              label: StringPos[InputPosition],
                              javaProperties: util.List[Property]): SchemaCommand = {
    val properties = javaProperties.asScala
    constraintType match {
      case ConstraintType.UNIQUE => DropUniquePropertyConstraint(variable, LabelName(label.string)(label.pos), properties)(p)
      case ConstraintType.NODE_KEY => DropNodeKeyConstraint(variable, LabelName(label.string)(label.pos), properties)(p)
      case ConstraintType.NODE_EXISTS =>
        validateSingleProperty(properties, constraintType)
        DropNodePropertyExistenceConstraint(variable, LabelName(label.string)(label.pos), properties.head)(p)
      case ConstraintType.NODE_IS_NOT_NULL =>
        throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand)
      case ConstraintType.REL_EXISTS =>
        validateSingleProperty(properties, constraintType)
        DropRelationshipPropertyExistenceConstraint(variable, RelTypeName(label.string)(label.pos), properties.head)(p)
      case ConstraintType.REL_IS_NOT_NULL =>
        throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidDropCommand)
    }
  }

  private def validateSingleProperty(seq: Seq[_], constraintType:ConstraintType): Unit = {
    if (seq.size != 1) throw new Neo4jASTConstructionException(ASTExceptionFactory.onlySinglePropertyAllowed(constraintType))
  }

  // Index Commands

  override def createLookupIndex(p: InputPosition,
                                 replace: Boolean,
                                 ifNotExists:Boolean,
                                 isNode: Boolean,
                                 indexName: String,
                                 variable: Variable,
                                 functionName: StringPos[InputPosition],
                                 functionParameter: Variable,
                                 options: SimpleEither[util.Map[String, Expression], Parameter]
                                ): CreateLookupIndex = {
    val function = FunctionInvocation(FunctionName(functionName.string) (functionName.pos), distinct = false, IndexedSeq(functionParameter))(functionName.pos)
    CreateLookupIndex(variable, isNode, function, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
  }

  override def createIndexWithOldSyntax(p: InputPosition,
                                        label: StringPos[InputPosition],
                                        properties: util.List[StringPos[InputPosition]]): CreateIndexOldSyntax = {
    CreateIndexOldSyntax(LabelName(label.string)(label.pos), properties.asScala.toList.map(prop => PropertyKeyName(prop.string)(prop.pos)))(p)
  }

  override def createIndex(p: InputPosition,
                           replace: Boolean,
                           ifNotExists: Boolean,
                           isNode: Boolean,
                           indexName: String,
                           variable: Variable,
                           label: StringPos[InputPosition],
                           javaProperties: util.List[Property],
                           options: SimpleEither[util.Map[String, Expression], Parameter],
                           indexType: CreateIndexTypes): CreateIndex = {
    val properties = javaProperties.asScala.toList
    (indexType, isNode) match {
      case (CreateIndexTypes.DEFAULT, true)  =>
        CreateRangeNodeIndex(variable, LabelName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options), fromDefault = true)(p)
      case (CreateIndexTypes.DEFAULT, false) =>
        CreateRangeRelationshipIndex(variable, RelTypeName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options), fromDefault = true)(p)
      case (CreateIndexTypes.RANGE, true)    =>
        CreateRangeNodeIndex(variable, LabelName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options), fromDefault = false)(p)
      case (CreateIndexTypes.RANGE, false)   =>
        CreateRangeRelationshipIndex(variable, RelTypeName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options), fromDefault = false)(p)
      case (CreateIndexTypes.BTREE, true)    =>
        CreateBtreeNodeIndex(variable, LabelName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (CreateIndexTypes.BTREE, false)   =>
        CreateBtreeRelationshipIndex(variable, RelTypeName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (CreateIndexTypes.TEXT, true)     =>
        CreateTextNodeIndex(variable, LabelName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (CreateIndexTypes.TEXT, false)    =>
        CreateTextRelationshipIndex(variable, RelTypeName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (CreateIndexTypes.POINT, true)     =>
        CreatePointNodeIndex(variable, LabelName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (CreateIndexTypes.POINT, false)    =>
        CreatePointRelationshipIndex(variable, RelTypeName(label.string)(label.pos), properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
      case (t, _) =>
        throw new Neo4jASTConstructionException(ASTExceptionFactory.invalidCreateIndexType(t))
    }
  }

  override def createFulltextIndex(p: InputPosition,
                                   replace: Boolean,
                                   ifNotExists: Boolean,
                                   isNode: Boolean,
                                   indexName: String,
                                   variable: Variable,
                                   labels: util.List[StringPos[InputPosition]],
                                   javaProperties: util.List[Property],
                                   options: SimpleEither[util.Map[String, Expression], Parameter]): CreateIndex = {
    val properties = javaProperties.asScala.toList
    if (isNode) {
      val labelNames = labels.asScala.toList.map(stringPos => LabelName(stringPos.string)(stringPos.pos))
      CreateFulltextNodeIndex(variable, labelNames, properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
    } else {
      val relTypeNames = labels.asScala.toList.map(stringPos => RelTypeName(stringPos.string)(stringPos.pos))
      CreateFulltextRelationshipIndex(variable, relTypeNames, properties, Option(indexName), ifExistsDo(replace, ifNotExists), asOptionsAst(options))(p)
    }
  }

  override def dropIndex(p: InputPosition, name: String, ifExists: Boolean): DropIndexOnName = DropIndexOnName(name, ifExists)(p)

  override def dropIndex(p: InputPosition,
                         label: StringPos[InputPosition],
                         javaProperties: util.List[StringPos[InputPosition]]): DropIndex = {
    val properties = javaProperties.asScala.map(property => PropertyKeyName(property.string)(property.pos)).toList
    DropIndex(LabelName(label.string)(label.pos), properties)(p)
  }

  // Administration Commands
  // Role commands

  override def createRole(p: InputPosition,
                          replace: Boolean,
                          roleName: SimpleEither[String, Parameter],
                          from: SimpleEither[String, Parameter],
                          ifNotExists: Boolean): CreateRole = {
    CreateRole(roleName.asScala, Option(from).map(_.asScala), ifExistsDo(replace, ifNotExists))(p)
  }

  override def dropRole(p: InputPosition, roleName: SimpleEither[String, Parameter], ifExists: Boolean): DropRole = {
    DropRole(roleName.asScala, ifExists)(p)
  }

  override def renameRole(p: InputPosition, fromRoleName: SimpleEither[String, Parameter], toRoleName: SimpleEither[String, Parameter], ifExists: Boolean): RenameRole = {
    RenameRole(fromRoleName.asScala, toRoleName.asScala, ifExists)(p)
  }

  override def showRoles(p: InputPosition,
                         WithUsers: Boolean,
                         showAll: Boolean,
                         yieldExpr: Yield,
                         returnWithoutGraph: Return,
                         where: Where): ShowRoles = {
    ShowRoles(WithUsers, showAll, yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
  }

  override def grantRoles(p: InputPosition,
                          roles: util.List[SimpleEither[String, Parameter]],
                          users: util.List[SimpleEither[String, Parameter]]): GrantRolesToUsers = {
    GrantRolesToUsers(roles.asScala.map(_.asScala), users.asScala.map(_.asScala))(p)
  }

  override def revokeRoles(p: InputPosition,
                           roles: util.List[SimpleEither[String, Parameter]],
                           users: util.List[SimpleEither[String, Parameter]]): RevokeRolesFromUsers = {
    RevokeRolesFromUsers(roles.asScala.map(_.asScala), users.asScala.map(_.asScala))(p)
  }

  // User commands

  override def createUser(p: InputPosition,
                          replace: Boolean,
                          ifNotExists: Boolean,
                          username: SimpleEither[String, Parameter],
                          password: Expression,
                          encrypted: Boolean,
                          changeRequired: Boolean,
                          suspended: lang.Boolean,
                          homeDatabase: SimpleEither[String, Parameter]): AdministrationCommand = {
    val homeAction = if (homeDatabase == null) None else Some(SetHomeDatabaseAction(homeDatabase.asScala))
    val userOptions = UserOptions(Some(changeRequired), asBooleanOption(suspended), homeAction)
    CreateUser(username.asScala, encrypted, password, userOptions, ifExistsDo(replace, ifNotExists))(p)
  }

  override def dropUser(p: InputPosition, ifExists: Boolean, username: SimpleEither[String, Parameter]): DropUser = {
    DropUser(username.asScala, ifExists)(p)
  }

  override def renameUser(p: InputPosition, fromUserName: SimpleEither[String, Parameter], toUserName: SimpleEither[String, Parameter], ifExists: Boolean): RenameUser = {
    RenameUser(fromUserName.asScala, toUserName.asScala, ifExists)(p)
  }

  override def setOwnPassword(p: InputPosition, currentPassword: Expression, newPassword: Expression): SetOwnPassword = {
    SetOwnPassword(newPassword, currentPassword)(p)
  }

  override def alterUser(p: InputPosition,
                         ifExists: Boolean,
                         username: SimpleEither[String, Parameter],
                         password: Expression,
                         encrypted: Boolean,
                         changeRequired: lang.Boolean,
                         suspended: lang.Boolean,
                         homeDatabase: SimpleEither[String, Parameter],
                         removeHome: Boolean): AlterUser = {
    val maybePassword = Option(password)
    val isEncrypted = if (maybePassword.isDefined) Some(encrypted) else None
    val homeAction = if (removeHome) Some(RemoveHomeDatabaseAction) else if (homeDatabase == null) None else Some(SetHomeDatabaseAction(homeDatabase.asScala))
    val userOptions = UserOptions(asBooleanOption(changeRequired), asBooleanOption(suspended), homeAction)
    AlterUser(username.asScala, isEncrypted, maybePassword, userOptions, ifExists)(p)
  }

  override def passwordExpression(password: Parameter): Expression = new ExplicitParameter(password.name, CTString)(password.position) with SensitiveParameter

  override def passwordExpression(p: InputPosition, password: String): Expression = SensitiveStringLiteral(password.getBytes(StandardCharsets.UTF_8))(p)

  override def showUsers(p: InputPosition, yieldExpr: Yield, returnWithoutGraph: Return, where: Where): ShowUsers = {
    ShowUsers(yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
  }

  override def showCurrentUser(p: InputPosition, yieldExpr: Yield, returnWithoutGraph: Return, where: Where): ShowCurrentUser = {
    ShowCurrentUser(yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
  }

  // Privilege commands

  override def showAllPrivileges(p: InputPosition,
                                 asCommand: Boolean,
                                 asRevoke: Boolean,
                                 yieldExpr: Yield,
                                 returnWithoutGraph: Return,
                                 where: Where): ReadAdministrationCommand = {
    if ( asCommand ) {
      ShowPrivilegeCommands(ShowAllPrivileges()(p), asRevoke, yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    } else {
      ShowPrivileges(ShowAllPrivileges()(p), yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    }
  }

  override def showRolePrivileges(p: InputPosition,
                                  roles:  util.List[SimpleEither[String, Parameter]],
                                  asCommand: Boolean,
                                  asRevoke: Boolean,
                                  yieldExpr: Yield,
                                  returnWithoutGraph: Return,
                                  where: Where): ReadAdministrationCommand = {
    if ( asCommand ) {
      ShowPrivilegeCommands(ShowRolesPrivileges(roles.asScala.map(_.asScala).toList)(p), asRevoke, yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    } else {
      ShowPrivileges(ShowRolesPrivileges(roles.asScala.map(_.asScala).toList)(p), yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    }
  }

  override def showUserPrivileges(p: InputPosition,
                                  users:  util.List[SimpleEither[String, Parameter]],
                                  asCommand: Boolean,
                                  asRevoke: Boolean,
                                  yieldExpr: Yield,
                                  returnWithoutGraph: Return,
                                  where: Where): ReadAdministrationCommand = {
    if ( asCommand ) {
      ShowPrivilegeCommands(userPrivilegeScope(p, users), asRevoke, yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    } else {
      ShowPrivileges(userPrivilegeScope(p, users), yieldOrWhere(yieldExpr, returnWithoutGraph, where))(p)
    }
  }

  private def userPrivilegeScope(p: InputPosition, users: util.List[SimpleEither[String, Parameter]]): ShowPrivilegeScope = {
    if (Option(users).isDefined) {
      ShowUsersPrivileges(users.asScala.map(_.asScala).toList)(p)
    } else {
      ShowUserPrivileges(None)(p)
    }
  }

  override def grantPrivilege(p: InputPosition,
                              roles: util.List[SimpleEither[String, Parameter]],
                              privilege: Privilege): AdministrationCommand =
    GrantPrivilege(privilege.privilegeType, Option(privilege.resource), privilege.qualifier.asScala.toList, roles.asScala.map(_.asScala))(p)

  override def denyPrivilege(p: InputPosition, roles: util.List[SimpleEither[String, Parameter]], privilege: Privilege): AdministrationCommand =
    DenyPrivilege(privilege.privilegeType, Option(privilege.resource), privilege.qualifier.asScala.toList, roles.asScala.map(_.asScala))(p)

  override def revokePrivilege(p: InputPosition,
                               roles: util.List[SimpleEither[String, Parameter]],
                               privilege: Privilege,
                               revokeGrant: Boolean,
                               revokeDeny: Boolean): AdministrationCommand = (revokeGrant, revokeDeny) match {
    case (true, false) => RevokePrivilege(privilege.privilegeType, Option(privilege.resource), privilege.qualifier.asScala.toList, roles.asScala.map(_.asScala), RevokeGrantType()(p))(p)
    case (false, true) => RevokePrivilege(privilege.privilegeType, Option(privilege.resource), privilege.qualifier.asScala.toList, roles.asScala.map(_.asScala), RevokeDenyType()(p))(p)
    case _             => RevokePrivilege(privilege.privilegeType, Option(privilege.resource), privilege.qualifier.asScala.toList, roles.asScala.map(_.asScala), RevokeBothType()(p))(p)
  }

  override def databasePrivilege(p: InputPosition, action: AdministrationAction, scope: util.List[DatabaseScope], qualifier: util.List[PrivilegeQualifier]): Privilege =
    Privilege(DatabasePrivilege(action.asInstanceOf[DatabaseAction], scope.asScala.toList)(p), null, qualifier)

  override def dbmsPrivilege(p: InputPosition, action: AdministrationAction, qualifier: util.List[PrivilegeQualifier]): Privilege =
    Privilege(DbmsPrivilege(action.asInstanceOf[DbmsAction])(p), null, qualifier)

  override def graphPrivilege(p: InputPosition, action: AdministrationAction, scope: util.List[GraphScope], resource: ActionResource, qualifier: util.List[PrivilegeQualifier]): Privilege =
    Privilege(GraphPrivilege(action.asInstanceOf[GraphAction], scope.asScala.toList)(p), resource, qualifier)

  override def privilegeAction(action: ActionType): AdministrationAction = action match {
    case ActionType.DATABASE_ALL => AllDatabaseAction
    case ActionType.ACCESS => AccessDatabaseAction
    case ActionType.DATABASE_START => StartDatabaseAction
    case ActionType.DATABASE_STOP => StopDatabaseAction
    case ActionType.INDEX_ALL => AllIndexActions
    case ActionType.INDEX_CREATE => CreateIndexAction
    case ActionType.INDEX_DROP => DropIndexAction
    case ActionType.INDEX_SHOW => ShowIndexAction
    case ActionType.CONSTRAINT_ALL => AllConstraintActions
    case ActionType.CONSTRAINT_CREATE => CreateConstraintAction
    case ActionType.CONSTRAINT_DROP => DropConstraintAction
    case ActionType.CONSTRAINT_SHOW => ShowConstraintAction
    case ActionType.CREATE_TOKEN =>AllTokenActions
    case ActionType.CREATE_PROPERTYKEY => CreatePropertyKeyAction
    case ActionType.CREATE_LABEL => CreateNodeLabelAction
    case ActionType.CREATE_RELTYPE => CreateRelationshipTypeAction
    case ActionType.TRANSACTION_ALL => AllTransactionActions
    case ActionType.TRANSACTION_SHOW => ShowTransactionAction
    case ActionType.TRANSACTION_TERMINATE => TerminateTransactionAction

    case ActionType.DBMS_ALL => AllDbmsAction
    case ActionType.USER_ALL => AllUserActions
    case ActionType.USER_SHOW => ShowUserAction
    case ActionType.USER_ALTER => AlterUserAction
    case ActionType.USER_CREATE => CreateUserAction
    case ActionType.USER_DROP => DropUserAction
    case ActionType.USER_RENAME => RenameUserAction
    case ActionType.USER_PASSWORD => SetPasswordsAction
    case ActionType.USER_STATUS => SetUserStatusAction
    case ActionType.USER_HOME   => SetUserHomeDatabaseAction
    case ActionType.USER_IMPERSONATE => ImpersonateUserAction
    case ActionType.ROLE_ALL    => AllRoleActions
    case ActionType.ROLE_SHOW => ShowRoleAction
    case ActionType.ROLE_CREATE => CreateRoleAction
    case ActionType.ROLE_DROP => DropRoleAction
    case ActionType.ROLE_RENAME => RenameRoleAction
    case ActionType.ROLE_ASSIGN => AssignRoleAction
    case ActionType.ROLE_REMOVE => RemoveRoleAction
    case ActionType.DATABASE_MANAGEMENT => AllDatabaseManagementActions
    case ActionType.DATABASE_CREATE => CreateDatabaseAction
    case ActionType.DATABASE_DROP => DropDatabaseAction
    case ActionType.DATABASE_ALTER => AlterDatabaseAction
    case ActionType.SET_DATABASE_ACCESS => SetDatabaseAccessAction
    case ActionType.PRIVILEGE_ALL => AllPrivilegeActions
    case ActionType.PRIVILEGE_ASSIGN => AssignPrivilegeAction
    case ActionType.PRIVILEGE_REMOVE => RemovePrivilegeAction
    case ActionType.PRIVILEGE_SHOW => ShowPrivilegeAction
    case ActionType.EXECUTE_FUNCTION => ExecuteFunctionAction
    case ActionType.EXECUTE_BOOSTED_FUNCTION => ExecuteBoostedFunctionAction
    case ActionType.EXECUTE_PROCEDURE => ExecuteProcedureAction
    case ActionType.EXECUTE_BOOSTED_PROCEDURE => ExecuteBoostedProcedureAction
    case ActionType.EXECUTE_ADMIN_PROCEDURE => ExecuteAdminProcedureAction

    case ActionType.GRAPH_ALL => AllGraphAction
    case ActionType.GRAPH_WRITE => WriteAction
    case ActionType.GRAPH_CREATE => CreateElementAction
    case ActionType.GRAPH_MERGE => MergeAdminAction
    case ActionType.GRAPH_DELETE => DeleteElementAction
    case ActionType.GRAPH_LABEL_SET => SetLabelAction
    case ActionType.GRAPH_LABEL_REMOVE => RemoveLabelAction
    case ActionType.GRAPH_PROPERTY_SET => SetPropertyAction
    case ActionType.GRAPH_MATCH => MatchAction
    case ActionType.GRAPH_READ => ReadAction
    case ActionType.GRAPH_TRAVERSE => TraverseAction
  }

  // Resources

  override def propertiesResource(p: InputPosition, properties: util.List[String]): ActionResource = PropertiesResource(properties.asScala)(p)

  override def allPropertiesResource(p: InputPosition): ActionResource = AllPropertyResource()(p)

  override def labelsResource(p: InputPosition, labels: util.List[String]): ActionResource = LabelsResource(labels.asScala)(p)

  override def allLabelsResource(p: InputPosition): ActionResource = AllLabelResource()(p)

  override def databaseResource(p: InputPosition): ActionResource = DatabaseResource()(p)

  override def noResource(p: InputPosition): ActionResource = NoResource()(p)

  override def labelQualifier(p: InputPosition, label: String): PrivilegeQualifier = LabelQualifier(label)(p)

  override def relationshipQualifier(p: InputPosition, relationshipType: String): PrivilegeQualifier = RelationshipQualifier(relationshipType)(p)

  override def elementQualifier(p: InputPosition, name: String): PrivilegeQualifier = ElementQualifier(name)(p)

  override def allElementsQualifier(p: InputPosition): PrivilegeQualifier = ElementsAllQualifier()(p)

  override def allLabelsQualifier(p: InputPosition): PrivilegeQualifier = LabelAllQualifier()(p)

  override def allRelationshipsQualifier(p: InputPosition): PrivilegeQualifier = RelationshipAllQualifier()(p)

  override def allQualifier(): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    list.add(AllQualifier()(InputPosition.NONE))
    list
  }

  override def allDatabasesQualifier(): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    list.add(AllDatabasesQualifier()(InputPosition.NONE))
    list
  }

  override def userQualifier(users: util.List[SimpleEither[String, Parameter]]): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    users.forEach(u => list.add(UserQualifier(u.asScala)(InputPosition.NONE)))
    list
  }

  override def allUsersQualifier(): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    list.add(UserAllQualifier()(InputPosition.NONE))
    list
  }

  override def functionQualifier(p: InputPosition, functions: util.List[String]): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    functions.forEach(f => list.add(FunctionQualifier(f)(p)))
    list
  }

  override def procedureQualifier(p: InputPosition, procedures: util.List[String]): util.List[PrivilegeQualifier] = {
    val list = new util.ArrayList[PrivilegeQualifier]()
    procedures.forEach(proc => list.add(ProcedureQualifier(proc)(p)))
    list
  }

  override def graphScopes(p: InputPosition, graphNames: util.List[SimpleEither[String, Parameter]], scopeType: ScopeType): util.List[GraphScope] = {
    val list = new util.ArrayList[GraphScope]()
    scopeType match {
      case ScopeType.ALL     => list.add(AllGraphsScope()(p))
      case ScopeType.HOME    => list.add(HomeGraphScope()(p))
      case ScopeType.DEFAULT => list.add(DefaultGraphScope()(p))
      case ScopeType.NAMED   => graphNames.asScala.foreach(db => list.add(NamedGraphScope(db.asScala)(p)))
    }
    list
  }

  override def databaseScopes(p: InputPosition, databaseNames: util.List[SimpleEither[String, Parameter]], scopeType: ScopeType): util.List[DatabaseScope] = {
    val list = new util.ArrayList[DatabaseScope]()
    scopeType match {
      case ScopeType.ALL     => list.add(AllDatabasesScope()(p))
      case ScopeType.HOME    => list.add(HomeDatabaseScope()(p))
      case ScopeType.DEFAULT => list.add(DefaultDatabaseScope()(p))
      case ScopeType.NAMED   => databaseNames.asScala.foreach(db => list.add(NamedDatabaseScope(db.asScala)(p)))
    }
    list
  }

  // Database commands

  override def createDatabase(p: InputPosition,
                              replace: Boolean,
                              databaseName: SimpleEither[String, Parameter],
                              ifNotExists: Boolean,
                              wait: WaitUntilComplete,
                              options: SimpleEither[util.Map[String, Expression], Parameter]): CreateDatabase = {
    CreateDatabase(databaseName.asScala, ifExistsDo(replace, ifNotExists), asOptionsAst(options), wait)(p)
  }

  override def dropDatabase(p:InputPosition, databaseName: SimpleEither[String, Parameter], ifExists: Boolean, dumpData: Boolean, wait: WaitUntilComplete): DropDatabase = {
    val action: DropDatabaseAdditionalAction = if (dumpData) {
      DumpData
    } else {
      DestroyData
    }

    DropDatabase(databaseName.asScala, ifExists, action, wait)(p)
  }

  override def alterDatabase(p:InputPosition, databaseName: SimpleEither[String, Parameter], ifExists: Boolean, accessType: AccessType): AlterDatabase = {
    val access = accessType match {
      case READ_ONLY => ReadOnlyAccess
      case READ_WRITE => ReadWriteAccess
    }
    AlterDatabase(databaseName.asScala, ifExists, access)(p)
  }

  override def showDatabase(p: InputPosition,
                            scope: DatabaseScope,
                            yieldExpr: Yield,
                            returnWithoutGraph: Return,
                            where: Where): ShowDatabase = {
    if (yieldExpr != null) {
      ShowDatabase(scope, Some(Left((yieldExpr, Option(returnWithoutGraph)))))(p)
    } else {
      ShowDatabase(scope, Option(where).map(e => Right(where)))(p)
    }
  }

  override def databaseScope(p: InputPosition, databaseName: SimpleEither[String, Parameter], isDefault: Boolean, isHome: Boolean): DatabaseScope = {
    if (databaseName != null) {
      NamedDatabaseScope(databaseName.asScala)(p)
    } else if (isDefault) {
      DefaultDatabaseScope()(p)
    } else if (isHome) {
      HomeDatabaseScope()(p)
    } else {
      AllDatabasesScope()(p)
    }
  }

  override def startDatabase(p: InputPosition,
                             databaseName: SimpleEither[String, Parameter],
                             wait: WaitUntilComplete): StartDatabase = {
    StartDatabase(databaseName.asScala, wait)(p)
  }

  override def stopDatabase(p: InputPosition,
                            databaseName: SimpleEither[String, Parameter],
                            wait: WaitUntilComplete): StopDatabase = {
    StopDatabase(databaseName.asScala, wait)(p)
  }

  override def wait(wait: Boolean, seconds: Long): WaitUntilComplete = {
    if (!wait) {
      NoWait
    } else if (seconds > 0) {
      TimeoutAfter(seconds)
    } else {
      IndefiniteWait
    }
  }

  // Database commands

  override def createDatabaseAlias(p: InputPosition,
                           replace: Boolean,
                           aliasName: SimpleEither[String, Parameter],
                           targetName: SimpleEither[String, Parameter],
                           ifNotExists: Boolean): CreateDatabaseAlias = {
    CreateDatabaseAlias(aliasName.asScala, targetName.asScala, ifExistsDo(replace, ifNotExists))(p)
  }

  override def alterDatabaseAlias(p: InputPosition,
                                  aliasName: SimpleEither[String, Parameter],
                                  targetName: SimpleEither[String, Parameter],
                                  ifExists: Boolean): AlterDatabaseAlias = {
    AlterDatabaseAlias(aliasName.asScala, targetName.asScala, ifExists)(p)
  }

  override def dropAlias(p: InputPosition, aliasName: SimpleEither[String, Parameter], ifExists: Boolean): DropDatabaseAlias = {
    DropDatabaseAlias(aliasName.asScala, ifExists)(p)
  }

  private def ifExistsDo(replace: Boolean, ifNotExists: Boolean): IfExistsDo = {
    (replace, ifNotExists) match {
      case (true, true) => IfExistsInvalidSyntax
      case (true, false) => IfExistsReplace
      case (false, true) => IfExistsDoNothing
      case (false, false) => IfExistsThrowError
    }
  }

  private def yieldOrWhere(yieldExpr: Yield,
                           returnWithoutGraph: Return,
                           where: Where): Option[Either[(Yield, Option[Return]), Where]] = {
    if (yieldExpr != null) {
      Some(Left(yieldExpr -> Option(returnWithoutGraph)))
    } else if (where != null) {
      Some(Right(where))
    } else {
      None
    }
  }

  private def asBooleanOption(bool: lang.Boolean): Option[Boolean] = if (bool == null) None else Some(bool.booleanValue())

  private def asOptionsAst(options: SimpleEither[util.Map[String, Expression], Parameter]) =
    Option(options).map(_.asScala) match {
      case Some(Left(map)) => OptionsMap(mapAsScalaMap(map).toMap)
      case Some(Right(param)) => OptionsParam(param)
      case None => NoOptions
    }

  private def pretty[T <: AnyRef](ts: util.List[T]): String = {
    ts.stream().map[String](t => t.toString).collect(Collectors.joining(","))
  }
}

