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
package org.opencypher.v9_0.ast.generator

import java.nio.charset.StandardCharsets

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.AccessDatabaseAction
import org.opencypher.v9_0.ast.ActionResource
import org.opencypher.v9_0.ast.AdministrationCommand
import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.AllConstraintActions
import org.opencypher.v9_0.ast.AllDatabaseAction
import org.opencypher.v9_0.ast.AllDatabaseManagementActions
import org.opencypher.v9_0.ast.AllDatabasesQualifier
import org.opencypher.v9_0.ast.AllDatabasesScope
import org.opencypher.v9_0.ast.AllDbmsAction
import org.opencypher.v9_0.ast.AllGraphAction
import org.opencypher.v9_0.ast.AllGraphsScope
import org.opencypher.v9_0.ast.AllIndexActions
import org.opencypher.v9_0.ast.AllLabelResource
import org.opencypher.v9_0.ast.AllNodes
import org.opencypher.v9_0.ast.AllPrivilegeActions
import org.opencypher.v9_0.ast.AllPropertyResource
import org.opencypher.v9_0.ast.AllQualifier
import org.opencypher.v9_0.ast.AllRelationships
import org.opencypher.v9_0.ast.AllRoleActions
import org.opencypher.v9_0.ast.AllTokenActions
import org.opencypher.v9_0.ast.AllTransactionActions
import org.opencypher.v9_0.ast.AllUserActions
import org.opencypher.v9_0.ast.AlterUser
import org.opencypher.v9_0.ast.AlterUserAction
import org.opencypher.v9_0.ast.AscSortItem
import org.opencypher.v9_0.ast.AssignPrivilegeAction
import org.opencypher.v9_0.ast.AssignRoleAction
import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.Create
import org.opencypher.v9_0.ast.CreateConstraintAction
import org.opencypher.v9_0.ast.CreateDatabase
import org.opencypher.v9_0.ast.CreateDatabaseAction
import org.opencypher.v9_0.ast.CreateElementAction
import org.opencypher.v9_0.ast.CreateIndex
import org.opencypher.v9_0.ast.CreateIndexAction
import org.opencypher.v9_0.ast.CreateIndexNewSyntax
import org.opencypher.v9_0.ast.CreateNodeKeyConstraint
import org.opencypher.v9_0.ast.CreateNodeLabelAction
import org.opencypher.v9_0.ast.CreateNodePropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreatePropertyKeyAction
import org.opencypher.v9_0.ast.CreateRelationshipPropertyExistenceConstraint
import org.opencypher.v9_0.ast.CreateRelationshipTypeAction
import org.opencypher.v9_0.ast.CreateRole
import org.opencypher.v9_0.ast.CreateRoleAction
import org.opencypher.v9_0.ast.CreateUniquePropertyConstraint
import org.opencypher.v9_0.ast.CreateUser
import org.opencypher.v9_0.ast.CreateUserAction
import org.opencypher.v9_0.ast.DatabaseAction
import org.opencypher.v9_0.ast.DatabasePrivilegeQualifier
import org.opencypher.v9_0.ast.DbmsAction
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
import org.opencypher.v9_0.ast.ExecuteBoostedProcedureAction
import org.opencypher.v9_0.ast.ExecuteProcedureAction
import org.opencypher.v9_0.ast.Foreach
import org.opencypher.v9_0.ast.FromGraph
import org.opencypher.v9_0.ast.GrantPrivilege
import org.opencypher.v9_0.ast.GrantRolesToUsers
import org.opencypher.v9_0.ast.GraphAction
import org.opencypher.v9_0.ast.GraphPrivilegeQualifier
import org.opencypher.v9_0.ast.IfExistsDo
import org.opencypher.v9_0.ast.IfExistsDoNothing
import org.opencypher.v9_0.ast.IfExistsInvalidSyntax
import org.opencypher.v9_0.ast.IfExistsReplace
import org.opencypher.v9_0.ast.IfExistsThrowError
import org.opencypher.v9_0.ast.IndefiniteWait
import org.opencypher.v9_0.ast.LabelAllQualifier
import org.opencypher.v9_0.ast.LabelQualifier
import org.opencypher.v9_0.ast.LabelsResource
import org.opencypher.v9_0.ast.Limit
import org.opencypher.v9_0.ast.LoadCSV
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.MatchAction
import org.opencypher.v9_0.ast.Merge
import org.opencypher.v9_0.ast.MergeAction
import org.opencypher.v9_0.ast.MergeAdminAction
import org.opencypher.v9_0.ast.NamedDatabaseScope
import org.opencypher.v9_0.ast.NamedGraphScope
import org.opencypher.v9_0.ast.NoWait
import org.opencypher.v9_0.ast.NodeByIds
import org.opencypher.v9_0.ast.NodeByParameter
import org.opencypher.v9_0.ast.OnCreate
import org.opencypher.v9_0.ast.OnMatch
import org.opencypher.v9_0.ast.OrderBy
import org.opencypher.v9_0.ast.PeriodicCommitHint
import org.opencypher.v9_0.ast.PrivilegeCommand
import org.opencypher.v9_0.ast.ProcedureQualifier
import org.opencypher.v9_0.ast.ProcedureResult
import org.opencypher.v9_0.ast.ProcedureResultItem
import org.opencypher.v9_0.ast.PropertiesResource
import org.opencypher.v9_0.ast.Query
import org.opencypher.v9_0.ast.QueryPart
import org.opencypher.v9_0.ast.ReadAction
import org.opencypher.v9_0.ast.RelationshipAllQualifier
import org.opencypher.v9_0.ast.RelationshipByIds
import org.opencypher.v9_0.ast.RelationshipByParameter
import org.opencypher.v9_0.ast.RelationshipQualifier
import org.opencypher.v9_0.ast.Remove
import org.opencypher.v9_0.ast.RemoveItem
import org.opencypher.v9_0.ast.RemoveLabelAction
import org.opencypher.v9_0.ast.RemoveLabelItem
import org.opencypher.v9_0.ast.RemovePrivilegeAction
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.RemoveRoleAction
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItem
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.RevokeBothType
import org.opencypher.v9_0.ast.RevokeDenyType
import org.opencypher.v9_0.ast.RevokeGrantType
import org.opencypher.v9_0.ast.RevokePrivilege
import org.opencypher.v9_0.ast.RevokeRolesFromUsers
import org.opencypher.v9_0.ast.RevokeType
import org.opencypher.v9_0.ast.SchemaCommand
import org.opencypher.v9_0.ast.SeekOnly
import org.opencypher.v9_0.ast.SeekOrScan
import org.opencypher.v9_0.ast.SetClause
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetItem
import org.opencypher.v9_0.ast.SetLabelAction
import org.opencypher.v9_0.ast.SetLabelItem
import org.opencypher.v9_0.ast.SetOwnPassword
import org.opencypher.v9_0.ast.SetPasswordsAction
import org.opencypher.v9_0.ast.SetPropertyAction
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.SetUserStatusAction
import org.opencypher.v9_0.ast.ShowAllPrivileges
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.ShowPrivilegeAction
import org.opencypher.v9_0.ast.ShowPrivileges
import org.opencypher.v9_0.ast.ShowRoleAction
import org.opencypher.v9_0.ast.ShowRoles
import org.opencypher.v9_0.ast.ShowRolesPrivileges
import org.opencypher.v9_0.ast.ShowTransactionAction
import org.opencypher.v9_0.ast.ShowUserAction
import org.opencypher.v9_0.ast.ShowUserPrivileges
import org.opencypher.v9_0.ast.ShowUsers
import org.opencypher.v9_0.ast.ShowUsersPrivileges
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Skip
import org.opencypher.v9_0.ast.SortItem
import org.opencypher.v9_0.ast.Start
import org.opencypher.v9_0.ast.StartDatabase
import org.opencypher.v9_0.ast.StartDatabaseAction
import org.opencypher.v9_0.ast.StartItem
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.StopDatabase
import org.opencypher.v9_0.ast.StopDatabaseAction
import org.opencypher.v9_0.ast.SubQuery
import org.opencypher.v9_0.ast.TerminateTransactionAction
import org.opencypher.v9_0.ast.TimeoutAfter
import org.opencypher.v9_0.ast.TransactionManagementAction
import org.opencypher.v9_0.ast.TraverseAction
import org.opencypher.v9_0.ast.UnaliasedReturnItem
import org.opencypher.v9_0.ast.Union
import org.opencypher.v9_0.ast.UnionAll
import org.opencypher.v9_0.ast.UnionDistinct
import org.opencypher.v9_0.ast.UnresolvedCall
import org.opencypher.v9_0.ast.Unwind
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.ast.UserAllQualifier
import org.opencypher.v9_0.ast.UserQualifier
import org.opencypher.v9_0.ast.UsingHint
import org.opencypher.v9_0.ast.UsingIndexHint
import org.opencypher.v9_0.ast.UsingJoinHint
import org.opencypher.v9_0.ast.UsingScanHint
import org.opencypher.v9_0.ast.WaitUntilComplete
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.With
import org.opencypher.v9_0.ast.WriteAction
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.ast.YieldOrWhere
import org.opencypher.v9_0.ast.generator.AstGenerator.boolean
import org.opencypher.v9_0.ast.generator.AstGenerator.char
import org.opencypher.v9_0.ast.generator.AstGenerator.oneOrMore
import org.opencypher.v9_0.ast.generator.AstGenerator.tuple
import org.opencypher.v9_0.ast.generator.AstGenerator.zeroOrMore
import org.opencypher.v9_0.expressions.Add
import org.opencypher.v9_0.expressions.AllIterablePredicate
import org.opencypher.v9_0.expressions.AllPropertiesSelector
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.AnonymousPatternPart
import org.opencypher.v9_0.expressions.AnyIterablePredicate
import org.opencypher.v9_0.expressions.BooleanLiteral
import org.opencypher.v9_0.expressions.CaseExpression
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.Contains
import org.opencypher.v9_0.expressions.CountStar
import org.opencypher.v9_0.expressions.DecimalDoubleLiteral
import org.opencypher.v9_0.expressions.Divide
import org.opencypher.v9_0.expressions.EndsWith
import org.opencypher.v9_0.expressions.Equals
import org.opencypher.v9_0.expressions.Equivalent
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.ExistsSubClause
import org.opencypher.v9_0.expressions.ExplicitParameter
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.ExtractScope
import org.opencypher.v9_0.expressions.False
import org.opencypher.v9_0.expressions.FilterScope
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.GreaterThan
import org.opencypher.v9_0.expressions.GreaterThanOrEqual
import org.opencypher.v9_0.expressions.HasLabels
import org.opencypher.v9_0.expressions.In
import org.opencypher.v9_0.expressions.InvalidNotEquals
import org.opencypher.v9_0.expressions.IsNotNull
import org.opencypher.v9_0.expressions.IsNull
import org.opencypher.v9_0.expressions.IterablePredicateExpression
import org.opencypher.v9_0.expressions.LabelName
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
import org.opencypher.v9_0.expressions.ReduceScope
import org.opencypher.v9_0.expressions.RegexMatch
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.SensitiveAutoParameter
import org.opencypher.v9_0.expressions.SensitiveParameter
import org.opencypher.v9_0.expressions.SensitiveStringLiteral
import org.opencypher.v9_0.expressions.ShortestPathExpression
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.expressions.SignedDecimalIntegerLiteral
import org.opencypher.v9_0.expressions.SignedHexIntegerLiteral
import org.opencypher.v9_0.expressions.SignedIntegerLiteral
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
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.symbols.AnyType
import org.opencypher.v9_0.util.symbols.CTString
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.alphaLowerChar
import org.scalacheck.Gen.choose
import org.scalacheck.Gen.const
import org.scalacheck.Gen.frequency
import org.scalacheck.Gen.listOf
import org.scalacheck.Gen.listOfN
import org.scalacheck.Gen.lzy
import org.scalacheck.Gen.nonEmptyListOf
import org.scalacheck.Gen.oneOf
import org.scalacheck.Gen.option
import org.scalacheck.Gen.pick
import org.scalacheck.Gen.posNum
import org.scalacheck.Gen.sequence
import org.scalacheck.Gen.some
import org.scalacheck.util.Buildable

object AstGenerator {
  val OR_MORE_UPPER_BOUND = 3

  def zeroOrMore[T](gen: Gen[T]): Gen[List[T]] =
    choose(0, OR_MORE_UPPER_BOUND).flatMap(listOfN(_, gen))

  def zeroOrMore[T](seq: Seq[T]): Gen[Seq[T]] =
    choose(0, Math.min(OR_MORE_UPPER_BOUND, seq.size)).flatMap(pick(_, seq))

  def oneOrMore[T](gen: Gen[T]): Gen[List[T]] =
    choose(1, OR_MORE_UPPER_BOUND).flatMap(listOfN(_, gen))

  def oneOrMore[T](seq: Seq[T]): Gen[Seq[T]] =
    choose(1, Math.min(OR_MORE_UPPER_BOUND, seq.size)).flatMap(pick(_, seq))

  def tuple[A, B](ga: Gen[A], gb: Gen[B]): Gen[(A, B)] = for {
    a <- ga
    b <- gb
  } yield (a, b)

  def boolean: Gen[Boolean] =
    Arbitrary.arbBool.arbitrary

  def char: Gen[Char] =
    Arbitrary.arbChar.arbitrary.suchThat(acceptedByParboiled)

  def acceptedByParboiled(c: Char): Boolean = {
    val DEL_ERROR = '\ufdea'
    val INS_ERROR = '\ufdeb'
    val RESYNC = '\ufdec'
    val RESYNC_START = '\ufded'
    val RESYNC_END = '\ufdee'
    val RESYNC_EOI = '\ufdef'
    val EOI = '\uffff'

    c match {
      case DEL_ERROR    => false
      case INS_ERROR    => false
      case RESYNC       => false
      case RESYNC_START => false
      case RESYNC_END   => false
      case RESYNC_EOI   => false
      case EOI          => false
      case _            => true
    }
  }

}

/**
 * Random query generation
 * Implements instances of Gen[T] for all query ast nodes
 * Generated queries are syntactically (but not semantically) valid
 */
class AstGenerator(simpleStrings: Boolean = true, allowedVarNames: Option[Seq[String]] = None) {

  // HELPERS
  // ==========================================================================

  protected var paramCount = 0
  protected val pos : InputPosition = InputPosition.NONE

  def string: Gen[String] =
    if (simpleStrings) alphaLowerChar.map(_.toString)
    else listOf(char).map(_.mkString)


  // IDENTIFIERS
  // ==========================================================================

  def _identifier: Gen[String] =
    if (simpleStrings) alphaLowerChar.map(_.toString)
    else nonEmptyListOf(char).map(_.mkString)

  def _labelName: Gen[LabelName] =
    _identifier.map(LabelName(_)(pos))

  def _relTypeName: Gen[RelTypeName] =
    _identifier.map(RelTypeName(_)(pos))

  def _propertyKeyName: Gen[PropertyKeyName] =
    _identifier.map(PropertyKeyName(_)(pos))

  // EXPRESSIONS
  // ==========================================================================

  // LEAFS
  // ----------------------------------

  def _nullLit: Gen[Null] =
    const(Null.NULL)

  def _stringLit: Gen[StringLiteral] =
    string.flatMap(StringLiteral(_)(pos))

  def _sensitiveStringLiteral: Gen[SensitiveStringLiteral] =
    // Needs to be '******' since all sensitive strings get rendered as such
    // Would normally get rewritten as SensitiveAutoParameter which can be generated as parameter when needed
    const(SensitiveStringLiteral("******".getBytes(StandardCharsets.UTF_8))(pos))

  def _booleanLit: Gen[BooleanLiteral] =
    oneOf(True()(pos), False()(pos))

  def _unsignedIntString(prefix: String, radix: Int): Gen[String] = for {
    num <- posNum[Int]
    str = Integer.toString(num, radix)
  } yield List(prefix, str).mkString

  def _signedIntString(prefix: String, radix: Int): Gen[String] = for {
    str <- _unsignedIntString(prefix, radix)
    neg <- boolean
    sig = if (neg) "-" else ""
  } yield List(sig, str).mkString

  def _unsignedDecIntLit: Gen[UnsignedDecimalIntegerLiteral] =
    _unsignedIntString("", 10).map(UnsignedDecimalIntegerLiteral(_)(pos))

  def _signedDecIntLit: Gen[SignedDecimalIntegerLiteral] =
    _signedIntString("", 10).map(SignedDecimalIntegerLiteral(_)(pos))

  def _signedHexIntLit: Gen[SignedHexIntegerLiteral] =
    _signedIntString("0x", 16).map(SignedHexIntegerLiteral(_)(pos))

  def _signedOctIntLitOldSyntax: Gen[SignedOctalIntegerLiteral] =
    _signedIntString("0", 8).map(SignedOctalIntegerLiteral(_)(pos))

  def _signedOctIntLit: Gen[SignedOctalIntegerLiteral] =
    _signedIntString("0o", 8).map(SignedOctalIntegerLiteral(_)(pos))

  def _signedIntLit: Gen[SignedIntegerLiteral] = oneOf(
    _signedDecIntLit,
    _signedHexIntLit,
    _signedOctIntLitOldSyntax,
    _signedOctIntLit
  )

  def _doubleLit: Gen[DecimalDoubleLiteral] =
    Arbitrary.arbDouble.arbitrary.map(_.toString).map(DecimalDoubleLiteral(_)(pos))

  def _parameter: Gen[Parameter] =
    _identifier.map(Parameter(_, AnyType.instance)(pos))

  def _stringParameter: Gen[Parameter] = _identifier.map(Parameter(_, CTString)(pos))

  def _sensitiveStringParameter: Gen[Parameter with SensitiveParameter] =
    _identifier.map(new ExplicitParameter(_, CTString)(pos) with SensitiveParameter)

  def _sensitiveAutoStringParameter: Gen[Parameter with SensitiveAutoParameter] =
    _identifier.map(new ExplicitParameter(_, CTString)(pos) with SensitiveAutoParameter)

  def _variable: Gen[Variable] = {
    val nameGen = allowedVarNames match {
      case None => _identifier
      case Some(Seq()) => const("").suchThat(_ => false)
      case Some(names) =>  oneOf(names)
    }
    for {
      name <- nameGen
    } yield Variable(name)(pos)
  }

  // Predicates
  // ----------------------------------

  def _predicateComparisonPar(l: Expression, r: Expression): Gen[Expression] = oneOf(
    GreaterThanOrEqual(l, r)(pos),
    GreaterThan(l, r)(pos),
    LessThanOrEqual(l, r)(pos),
    LessThan(l, r)(pos),
    Equals(l, r)(pos),
    Equivalent(l, r)(pos),
    NotEquals(l, r)(pos),
    InvalidNotEquals(l, r)(pos)
  )

  def _predicateComparison: Gen[Expression] = for {
    l <- _expression
    r <- _expression
    res <- _predicateComparisonPar(l, r)
  } yield res

  def _predicateComparisonChain: Gen[Expression] = for {
    exprs <- listOfN(4, _expression)
    pairs = exprs.sliding(2)
    gens = pairs.map(p => _predicateComparisonPar(p.head, p.last)).toList
    chain <- sequence(gens)(Buildable.buildableCanBuildFrom)
  } yield Ands(chain)(pos)

  def _predicateUnary: Gen[Expression] = for {
    r <- _expression
    res <- oneOf(
      Not(r)(pos),
      IsNull(r)(pos),
      IsNotNull(r)(pos)
    )
  } yield res

  def _predicateBinary: Gen[Expression] = for {
    l <- _expression
    r <- _expression
    res <- oneOf(
      And(l, r)(pos),
      Or(l, r)(pos),
      Xor(l, r)(pos),
      RegexMatch(l, r)(pos),
      In(l, r)(pos),
      StartsWith(l, r)(pos),
      EndsWith(l, r)(pos),
      Contains(l, r)(pos)
    )
  } yield res

  def _hasLabels: Gen[HasLabels] = for {
    expression <- _expression
    labels <- oneOrMore(_labelName)
  } yield HasLabels(expression, labels)(pos)

  // Collections
  // ----------------------------------

  def _map: Gen[MapExpression] = for {
    items <- zeroOrMore(tuple(_propertyKeyName, _expression))
  } yield MapExpression(items)(pos)

  def _property: Gen[Property] = for {
    map <- _expression
    key <- _propertyKeyName
  } yield Property(map, key)(pos)

  def _mapProjectionElement: Gen[MapProjectionElement] =
    oneOf(
      for {key <- _propertyKeyName; exp <- _expression} yield LiteralEntry(key, exp)(pos),
      for {id <- _variable} yield VariableSelector(id)(pos),
      for {id <- _variable} yield PropertySelector(id)(pos),
      const(AllPropertiesSelector()(pos))
    )

  def _mapProjection: Gen[MapProjection] = for {
    name <- _variable
    items <- oneOrMore(_mapProjectionElement)
  } yield MapProjection(name, items)(pos)

  def _list: Gen[ListLiteral] =
    _listOf(_expression)

  def _listOf(expressionGen: Gen[Expression]): Gen[ListLiteral] = for {
    parts <- zeroOrMore(expressionGen)
  } yield ListLiteral(parts)(pos)

  def _listSlice: Gen[ListSlice] = for {
    list <- _expression
    from <- option(_expression)
    to <- option(_expression)
  } yield ListSlice(list, from, to)(pos)

  def _containerIndex: Gen[ContainerIndex] = for {
    expr <- _expression
    idx <- _expression
  } yield ContainerIndex(expr, idx)(pos)

  def _filterScope: Gen[FilterScope] = for {
    variable <- _variable
    innerPredicate <- option(_expression)
  } yield FilterScope(variable, innerPredicate)(pos)

  def _extractScope: Gen[ExtractScope] = for {
    variable <- _variable
    innerPredicate <- option(_expression)
    extractExpression <- option(_expression)
  } yield ExtractScope(variable, innerPredicate, extractExpression)(pos)

  def _listComprehension: Gen[ListComprehension] = for {
    scope <- _extractScope
    expression <- _expression
  } yield ListComprehension(scope, expression)(pos)

  def _iterablePredicate: Gen[IterablePredicateExpression] = for {
    scope <- _filterScope
    expression <- _expression
    predicate <- oneOf(
      AllIterablePredicate(scope, expression)(pos),
      AnyIterablePredicate(scope, expression)(pos),
      NoneIterablePredicate(scope, expression)(pos),
      SingleIterablePredicate(scope, expression)(pos)
    )
  } yield predicate

  def _reduceScope: Gen[ReduceScope] = for {
    accumulator <- _variable
    variable <- _variable
    expression <- _expression
  } yield ReduceScope(accumulator, variable, expression)(pos)

  def _reduceExpr: Gen[ReduceExpression] = for {
    scope <- _reduceScope
    init <- _expression
    list <- _expression
  } yield ReduceExpression(scope, init, list)(pos)

  // Arithmetic
  // ----------------------------------

  def _arithmeticUnary: Gen[Expression] = for {
    r <- _expression
    exp <- oneOf(
      UnaryAdd(r)(pos),
      UnarySubtract(r)(pos)
    )
  } yield exp

  def _arithmeticBinary: Gen[Expression] = for {
    l <- _expression
    r <- _expression
    exp <- oneOf(
      Add(l, r)(pos),
      Multiply(l, r)(pos),
      Divide(l, r)(pos),
      Pow(l, r)(pos),
      Modulo(l, r)(pos),
      Subtract(l, r)(pos)
    )
  } yield exp

  def _case: Gen[CaseExpression] = for {
    expression <- option(_expression)
    alternatives <- oneOrMore(tuple(_expression, _expression))
    default <- option(_expression)
  } yield CaseExpression(expression, alternatives, default)(pos)

  // Functions
  // ----------------------------------

  def _namespace: Gen[Namespace] = for {
    parts <- zeroOrMore(_identifier)
  } yield Namespace(parts)(pos)

  def _functionName: Gen[FunctionName] = for {
    name <- _identifier
  } yield FunctionName(name)(pos)

  def _functionInvocation: Gen[FunctionInvocation] = for {
    namespace <- _namespace
    functionName <- _functionName
    distinct <- boolean
    args <- zeroOrMore(_expression)
  } yield FunctionInvocation(namespace, functionName, distinct, args.toIndexedSeq)(pos)

  def _countStar: Gen[CountStar] =
    const(CountStar()(pos))

  // Patterns
  // ----------------------------------

  def _relationshipsPattern: Gen[RelationshipsPattern] = for {
    chain <- _relationshipChain
  } yield RelationshipsPattern(chain)(pos)

  def _patternExpr: Gen[PatternExpression] = for {
    pattern <- _relationshipsPattern
  } yield PatternExpression(pattern)(Set.empty)

  def _shortestPaths: Gen[ShortestPaths] = for {
    element <- _patternElement
    single <- boolean
  } yield ShortestPaths(element, single)(pos)

  def _shortestPathExpr: Gen[ShortestPathExpression] = for {
    pattern <- _shortestPaths
  } yield ShortestPathExpression(pattern)

  def _existsSubClause: Gen[ExistsSubClause] = for {
    pattern <- _pattern
    where <- option(_expression)
    outerScope <- zeroOrMore(_variable)
  } yield ExistsSubClause(pattern, where)(pos, outerScope.toSet)

  def _patternComprehension: Gen[PatternComprehension] = for {
    namedPath <- option(_variable)
    pattern <- _relationshipsPattern
    predicate <- option(_expression)
    projection <- _expression
    outerScope <- zeroOrMore(_variable)
  } yield PatternComprehension(namedPath, pattern, predicate, projection)(pos, outerScope.toSet)

  // Expression
  // ----------------------------------

  def _expression: Gen[Expression] =
    frequency(
      5 -> oneOf(
        lzy(_nullLit),
        lzy(_stringLit),
        lzy(_booleanLit),
        lzy(_signedDecIntLit),
        lzy(_signedHexIntLit),
        lzy(_signedOctIntLitOldSyntax),
        lzy(_signedOctIntLit),
        lzy(_doubleLit),
        lzy(_variable),
        lzy(_parameter)
      ),
      1 -> oneOf(
        lzy(_predicateComparison),
        lzy(_predicateUnary),
        lzy(_predicateBinary),
        lzy(_predicateComparisonChain),
        lzy(_iterablePredicate),
        lzy(_hasLabels),
        lzy(_arithmeticUnary),
        lzy(_arithmeticBinary),
        lzy(_case),
        lzy(_functionInvocation),
        lzy(_countStar),
        lzy(_reduceExpr),
        lzy(_shortestPathExpr),
        lzy(_patternExpr),
        lzy(_map),
        lzy(_mapProjection),
        lzy(_property),
        lzy(_list),
        lzy(_listSlice),
        lzy(_listComprehension),
        lzy(_containerIndex),
        lzy(_existsSubClause),
        lzy(_patternComprehension)
      )
    )

  // PATTERNS
  // ==========================================================================

  def _nodePattern: Gen[NodePattern] = for {
    variable <- option(_variable)
    labels <- zeroOrMore(_labelName)
    properties <- option(oneOf(_map, _parameter))
    baseNode <- option(_variable)
  } yield NodePattern(variable, labels, properties, baseNode)(pos)

  def _range: Gen[Range] = for {
    lower <- option(_unsignedDecIntLit)
    upper <- option(_unsignedDecIntLit)
  } yield Range(lower, upper)(pos)

  def _semanticDirection: Gen[SemanticDirection] =
    oneOf(
      SemanticDirection.OUTGOING,
      SemanticDirection.INCOMING,
      SemanticDirection.BOTH
    )

  def _relationshipPattern: Gen[RelationshipPattern] = for {
    variable <- option(_variable)
    types <- zeroOrMore(_relTypeName)
    length <- option(option(_range))
    properties <- option(oneOf(_map, _parameter))
    direction <- _semanticDirection
    baseRel <- option(_variable)
  } yield RelationshipPattern(variable, types, length, properties, direction, legacyTypeSeparator = false, baseRel)(pos)

  def _relationshipChain: Gen[RelationshipChain] = for {
    element <- _patternElement
    relationship <- _relationshipPattern
    rightNode <- _nodePattern
  } yield RelationshipChain(element, relationship, rightNode)(pos)

  def _patternElement: Gen[PatternElement] = oneOf(
    _nodePattern,
    lzy(_relationshipChain)
  )

  def _anonPatternPart: Gen[AnonymousPatternPart] = for {
    element <- _patternElement
    single <- boolean
    part <- oneOf(
      EveryPath(element),
      ShortestPaths(element, single)(pos)
    )
  } yield part

  def _namedPatternPart: Gen[NamedPatternPart] = for {
    variable <- _variable
    part <- _anonPatternPart
  } yield NamedPatternPart(variable, part)(pos)

  def _patternPart: Gen[PatternPart] =
    oneOf(
      _anonPatternPart,
      _namedPatternPart
    )

  def _pattern: Gen[Pattern] = for {
    parts <- oneOrMore(_patternPart)
  } yield Pattern(parts)(pos)

  def _patternSingle: Gen[Pattern] = for {
    part <- _patternPart
  } yield Pattern(Seq(part))(pos)

  // CLAUSES
  // ==========================================================================

  def _returnItem: Gen[ReturnItem] = for {
    expr <- _expression
    variable <- _variable
    item <- oneOf(
      UnaliasedReturnItem(expr, "")(pos),
      AliasedReturnItem(expr, variable)(pos)
    )
  } yield item

  def _sortItem: Gen[SortItem] = for {
    expr <- _expression
    item <- oneOf(
      AscSortItem(expr)(pos),
      DescSortItem(expr)(pos)
    )
  } yield item

  def _orderBy: Gen[OrderBy] = for {
    items <- oneOrMore(_sortItem)
  } yield OrderBy(items)(pos)

  def _skip: Gen[Skip] =
    _expression.map(Skip(_)(pos))

  def _limit: Gen[Limit] =
    _expression.map(Limit(_)(pos))

  def _where: Gen[Where] =
    _expression.map(Where(_)(pos))

  def _returnItems1: Gen[ReturnItems] = for {
    retItems <- oneOrMore(_returnItem)
  } yield ReturnItems(includeExisting = false, retItems)(pos)

  def _returnItems2: Gen[ReturnItems] = for {
    retItems <- zeroOrMore(_returnItem)
  } yield ReturnItems(includeExisting = true, retItems)(pos)

  def _returnItems: Gen[ReturnItems] =
    oneOf(_returnItems1, _returnItems2)

  def _with: Gen[With] = for {
    distinct <- boolean
    inclExisting <- boolean
    retItems <- oneOrMore(_returnItem)
    orderBy <- option(_orderBy)
    skip <- option(_skip)
    limit <- option(_limit)
    where <- option(_where)
  } yield With(distinct, ReturnItems(inclExisting, retItems)(pos), orderBy, skip, limit, where)(pos)

  def _return: Gen[Return] = for {
    distinct <- boolean
    inclExisting <- boolean
    retItems <- oneOrMore(_returnItem)
    orderBy <- option(_orderBy)
    skip <- option(_skip)
    limit <- option(_limit)
  } yield Return(distinct, ReturnItems(inclExisting, retItems)(pos), orderBy, skip, limit)(pos)

  def _yield: Gen[Yield] = for {
    retItems <- oneOrMore(_yieldItem)
    orderBy <- option(_orderBy)
    skip <- option(_signedDecIntLit.map(Skip(_)(pos)))
    limit <- option(_signedDecIntLit.map(Limit(_)(pos)))
    where <- option(_where)
  } yield Yield(ReturnItems(includeExisting = false, retItems)(pos), orderBy, skip, limit, where)(pos)

  def _yieldItem: Gen[ReturnItem] = for {
    var1 <- _variable
    item <- UnaliasedReturnItem(var1, "")(pos)
  }  yield item

  def _match: Gen[Match] = for {
    optional <- boolean
    pattern <- _pattern
    hints <- zeroOrMore(_hint)
    where <- option(_where)
  } yield Match(optional, pattern, hints, where)(pos)

  def _create: Gen[Create] = for {
    pattern <- _pattern
  } yield Create(pattern)(pos)

  def _unwind: Gen[Unwind] = for {
    expression <- _expression
    variable <- _variable
  } yield Unwind(expression, variable)(pos)

  def _setItem: Gen[SetItem] = for {
    variable <- _variable
    labels <- oneOrMore(_labelName)
    property <- _property
    expression <- _expression
    item <- oneOf(
      SetLabelItem(variable, labels)(pos),
      SetPropertyItem(property, expression)(pos),
      SetExactPropertiesFromMapItem(variable, expression)(pos),
      SetIncludingPropertiesFromMapItem(variable, expression)(pos)
    )
  } yield item

  def _removeItem: Gen[RemoveItem] = for {
    variable <- _variable
    labels <- oneOrMore(_labelName)
    property <- _property
    item <- oneOf(
      RemoveLabelItem(variable, labels)(pos),
      RemovePropertyItem(property)
    )
  } yield item

  def _set: Gen[SetClause] = for {
    items <- oneOrMore(_setItem)
  } yield SetClause(items)(pos)

  def _remove: Gen[Remove] = for {
    items <- oneOrMore(_removeItem)
  } yield Remove(items)(pos)

  def _delete: Gen[Delete] = for {
    expressions <- oneOrMore(_expression)
    forced <- boolean
  } yield Delete(expressions, forced)(pos)


  def _mergeAction: Gen[MergeAction] = for {
    set <- _set
    action <- oneOf(
      OnCreate(set)(pos),
      OnMatch(set)(pos)
    )
  } yield action

  def _merge: Gen[Merge] = for {
    pattern <- _patternSingle
    actions <- oneOrMore(_mergeAction)
  } yield Merge(pattern, actions)(pos)

  def _procedureName: Gen[ProcedureName] = for {
    name <- _identifier
  } yield ProcedureName(name)(pos)

  def _procedureOutput: Gen[ProcedureOutput] = for {
    name <- _identifier
  } yield ProcedureOutput(name)(pos)

  def _procedureResultItem: Gen[ProcedureResultItem] = for {
    output <- option(_procedureOutput)
    variable <- _variable
  } yield ProcedureResultItem(output, variable)(pos)

  def _procedureResult: Gen[ProcedureResult] = for {
    items <- oneOrMore(_procedureResultItem)
    where <- option(_where)
  } yield ProcedureResult(items.toIndexedSeq, where)(pos)

  def _call: Gen[UnresolvedCall] = for {
    procedureNamespace <- _namespace
    procedureName <- _procedureName
    declaredArguments <- option(zeroOrMore(_expression))
    declaredResult <- option(_procedureResult)
  } yield UnresolvedCall(procedureNamespace, procedureName, declaredArguments, declaredResult)(pos)

  def _foreach: Gen[Foreach] = for {
    variable <- _variable
    expression <- _expression
    updates <- oneOrMore(_clause)
  } yield Foreach(variable, expression, updates)(pos)

  def _loadCsv: Gen[LoadCSV] = for {
    withHeaders <- boolean
    urlString <- _expression
    variable <- _variable
    fieldTerminator <- option(_stringLit)
  } yield LoadCSV(withHeaders, urlString, variable, fieldTerminator)(pos)

  def _startItem: Gen[StartItem] = for {
    variable <- _variable
    parameter <- _parameter
    ids <- oneOrMore(_unsignedDecIntLit)
    item <- oneOf(
      NodeByParameter(variable, parameter)(pos),
      AllNodes(variable)(pos),
      NodeByIds(variable, ids)(pos),
      RelationshipByIds(variable, ids)(pos),
      RelationshipByParameter(variable, parameter)(pos),
      AllRelationships(variable)(pos)
    )
  } yield item

  def _start: Gen[Start] = for {
    items <- oneOrMore(_startItem)
    where <- option(_where)
  } yield Start(items, where)(pos)

  // Hints
  // ----------------------------------

  def _usingIndexHint: Gen[UsingIndexHint] = for {
    variable <- _variable
    label <- _labelName
    properties <- oneOrMore(_propertyKeyName)
    spec <- oneOf(SeekOnly, SeekOrScan)
  } yield UsingIndexHint(variable, label, properties, spec)(pos)

  def _usingJoinHint: Gen[UsingJoinHint] = for {
    variables <- oneOrMore(_variable)
  } yield UsingJoinHint(variables)(pos)

  def _usingScanHint: Gen[UsingScanHint] = for {
    variable <- _variable
    label <- _labelName
  } yield UsingScanHint(variable, label)(pos)

  def _hint: Gen[UsingHint] = oneOf(
    _usingIndexHint,
    _usingJoinHint,
    _usingScanHint
  )

  // Queries
  // ----------------------------------

  def _use: Gen[UseGraph] = for {
    expression <- _expression
  } yield UseGraph(expression)(pos)

  def _from: Gen[FromGraph] = for {
    expression <- _expression
  } yield FromGraph(expression)(pos)

  def _subQuery: Gen[SubQuery] = for {
    part <- _queryPart
  } yield SubQuery(part)(pos)

  def _clause: Gen[Clause] = oneOf(
    lzy(_use),
    lzy(_from),
    lzy(_with),
    lzy(_return),
    lzy(_match),
    lzy(_create),
    lzy(_unwind),
    lzy(_set),
    lzy(_remove),
    lzy(_delete),
    lzy(_merge),
    lzy(_call),
    lzy(_foreach),
    lzy(_loadCsv),
    lzy(_start),
    lzy(_subQuery),
  )

  def _singleQuery: Gen[SingleQuery] = for {
    s <- choose(1, 1)
    clauses <- listOfN(s, _clause)
  } yield SingleQuery(clauses)(pos)

  def _union: Gen[Union] = for {
    part <- _queryPart
    single <- _singleQuery
    union <- oneOf(
      UnionDistinct(part, single)(pos),
      UnionAll(part, single)(pos)
    )
  } yield union

  def _queryPart: Gen[QueryPart] = frequency(
    5 -> lzy(_singleQuery),
    1 -> lzy(_union)
  )

  def _regularQuery: Gen[Query] = for {
    part <- _queryPart
  } yield Query(None, part)(pos)

  def _periodicCommitHint: Gen[PeriodicCommitHint] = for {
    size <- option(_signedIntLit)
  } yield PeriodicCommitHint(size)(pos)

  def _bulkImportQuery: Gen[Query] = for {
    periodicCommitHint <- option(_periodicCommitHint)
    load <- _loadCsv
  } yield Query(periodicCommitHint, SingleQuery(Seq(load))(pos))(pos)

  def _query: Gen[Query] = frequency(
    10 -> _regularQuery,
    1 -> _bulkImportQuery
  )

  // Schema commands
  // ----------------------------------

  def _variableProperty: Gen[Property] = for {
    map <- _variable
    key <- _propertyKeyName
  } yield Property(map, key)(pos)

  def _listOfProperties: Gen[List[Property]] = for {
    props <- oneOrMore(_variableProperty)
  } yield props

  def _createIndex: Gen[CreateIndexNewSyntax] = for {
    variable   <- _variable
    labelName  <- _labelName
    props      <- _listOfProperties
    name       <- option(_identifier)
    ifExistsDo <- _ifExistsDo
    use        <- option(_use)
  } yield CreateIndexNewSyntax(variable, labelName, props, name, ifExistsDo, use)(pos)

  def _dropIndex: Gen[DropIndexOnName] = for {
    name     <- _identifier
    ifExists <- boolean
    use      <- option(_use)
  } yield DropIndexOnName(name, ifExists, use)(pos)

  def _indexCommandsOldSyntax: Gen[SchemaCommand] = for {
    labelName <- _labelName
    props     <- oneOrMore(_propertyKeyName)
    use       <- option(_use)
    command   <- oneOf(CreateIndex(labelName, props, use)(pos), DropIndex(labelName, props, use)(pos))
  } yield command

  def _createConstraint: Gen[SchemaCommand] = for {
    variable            <- _variable
    labelName           <- _labelName
    relTypeName         <- _relTypeName
    props               <- _listOfProperties
    prop                <- _variableProperty
    name                <- option(_identifier)
    ifExistsDo          <- _ifExistsDo
    use                 <- option(_use)
    nodeKey             = CreateNodeKeyConstraint(variable, labelName, props, name, ifExistsDo, use)(pos)
    uniqueness          = CreateUniquePropertyConstraint(variable, labelName, Seq(prop), name, ifExistsDo, use)(pos)
    compositeUniqueness = CreateUniquePropertyConstraint(variable, labelName, props, name, ifExistsDo, use)(pos)
    nodeExistence       = CreateNodePropertyExistenceConstraint(variable, labelName, prop, name, ifExistsDo, use)(pos)
    relExistence        = CreateRelationshipPropertyExistenceConstraint(variable, relTypeName, prop, name, ifExistsDo, use)(pos)
    command             <- oneOf(nodeKey, uniqueness, compositeUniqueness, nodeExistence, relExistence)
  } yield command

  def _dropConstraintOldSyntax: Gen[SchemaCommand] = for {
    variable            <- _variable
    labelName           <- _labelName
    relTypeName         <- _relTypeName
    props               <- _listOfProperties
    prop                <- _variableProperty
    use                 <- option(_use)
    nodeKey             = DropNodeKeyConstraint(variable, labelName, props, use)(pos)
    uniqueness          = DropUniquePropertyConstraint(variable, labelName, Seq(prop), use)(pos)
    compositeUniqueness = DropUniquePropertyConstraint(variable, labelName, props, use)(pos)
    nodeExistence       = DropNodePropertyExistenceConstraint(variable, labelName, prop, use)(pos)
    relExistence        = DropRelationshipPropertyExistenceConstraint(variable, relTypeName, prop, use)(pos)
    command             <- oneOf(nodeKey, uniqueness, compositeUniqueness, nodeExistence, relExistence)
  } yield command

  def _dropConstraint: Gen[DropConstraintOnName] = for {
    name     <- _identifier
    ifExists <- boolean
    use      <- option(_use)
  } yield DropConstraintOnName(name, ifExists, use)(pos)

  def _indexCommand: Gen[SchemaCommand] = oneOf(_createIndex, _dropIndex, _indexCommandsOldSyntax)

  def _constraintCommand: Gen[SchemaCommand] = oneOf(_createConstraint, _dropConstraint, _dropConstraintOldSyntax)

  def _schemaCommand: Gen[SchemaCommand] = oneOf(_indexCommand, _constraintCommand)

  // Administration commands
  // ----------------------------------

  def _nameAsEither: Gen[Either[String, Parameter]] = for {
    name  <- _identifier
    param <- _stringParameter
    finalName <- oneOf(Left(name), Right(param))
  } yield finalName

  def _listOfNameOfEither: Gen[List[Either[String, Parameter]]] = for {
    names <- oneOrMore(_nameAsEither)
  } yield names

  def _password: Gen[Expression] = oneOf(_sensitiveStringParameter, _sensitiveAutoStringParameter, _sensitiveStringLiteral)

  def _ifExistsDo: Gen[IfExistsDo] = oneOf(IfExistsReplace, IfExistsDoNothing, IfExistsThrowError, IfExistsInvalidSyntax)

  // User commands

  def _showUsers: Gen[ShowUsers] = for {
    yields <- option(_eitherYieldOrWhere)
  } yield ShowUsers(yields)(pos)

  def _eitherYieldOrWhere: Gen[Either[(ast.Yield, Option[ast.Return]), ast.Where]] = for {
    yields <- _yield
    where <- _where
    returns <- option(_return)
    eyw <- oneOf(Seq(Left((yields,returns)), Right(where)))
  } yield eyw

  def _createUser: Gen[CreateUser] = for {
    userName              <- _nameAsEither
    isEncryptedPassword   <- boolean
    password              <- _password
    requirePasswordChange <- boolean
    suspended             <- option(boolean)
    ifExistsDo            <- _ifExistsDo
  } yield CreateUser(userName, isEncryptedPassword, password, requirePasswordChange, suspended, ifExistsDo)(pos)

  def _dropUser: Gen[DropUser] = for {
    userName <- _nameAsEither
    ifExists <- boolean
  } yield DropUser(userName, ifExists)(pos)

  def _alterUser: Gen[AlterUser] = for {
    userName              <- _nameAsEither
    password              <- option(_password)
    requirePasswordChange <- option(boolean)
    isEncryptedPassword   <- if (password.isEmpty) const(None) else some(boolean)
    suspended             <- if (password.isEmpty && requirePasswordChange.isEmpty) some(boolean) else option(boolean) // All three are not allowed to be None
  } yield AlterUser(userName, isEncryptedPassword, password, requirePasswordChange, suspended)(pos)

  def _setOwnPassword: Gen[SetOwnPassword] = for {
    newPassword <- _password
    oldPassword <- _password
  } yield SetOwnPassword(newPassword, oldPassword)(pos)

  def _userCommand: Gen[AdministrationCommand] = oneOf(
    _showUsers,
    _createUser,
    _dropUser,
    _alterUser,
    _setOwnPassword
  )

  // Role commands

  def _showRoles: Gen[ShowRoles] = for {
    withUsers <- boolean
    showAll   <- boolean
    yields <- option(_eitherYieldOrWhere)
  } yield ShowRoles(withUsers, showAll, yields)(pos)

  def _createRole: Gen[CreateRole] = for {
    roleName     <- _nameAsEither
    fromRoleName <- option(_nameAsEither)
    ifExistsDo   <- _ifExistsDo
  } yield CreateRole(roleName, fromRoleName, ifExistsDo)(pos)

  def _dropRole: Gen[DropRole] = for {
    roleName <- _nameAsEither
    ifExists <- boolean
  } yield DropRole(roleName, ifExists)(pos)

  def _grantRole: Gen[GrantRolesToUsers] = for {
    roleNames <- _listOfNameOfEither
    userNames <- _listOfNameOfEither
  } yield GrantRolesToUsers(roleNames, userNames)(pos)

  def _revokeRole: Gen[RevokeRolesFromUsers] = for {
    roleNames <- _listOfNameOfEither
    userNames <- _listOfNameOfEither
  } yield RevokeRolesFromUsers(roleNames, userNames)(pos)

  def _roleCommand: Gen[AdministrationCommand] = oneOf(
    _showRoles,
    _createRole,
    _dropRole,
    _grantRole,
    _revokeRole
  )

  // Privilege commands

  def _revokeType: Gen[RevokeType] = oneOf(RevokeGrantType()(pos), RevokeDenyType()(pos), RevokeBothType()(pos))

  def _graphAction: Gen[GraphAction] = oneOf(
    TraverseAction, ReadAction, MatchAction, MergeAdminAction, CreateElementAction, DeleteElementAction, WriteAction, RemoveLabelAction, SetLabelAction, SetPropertyAction, AllGraphAction
  )

  def _databaseAction: Gen[DatabaseAction] = oneOf(
    StartDatabaseAction, StopDatabaseAction,
    AllDatabaseAction, AccessDatabaseAction,
    AllIndexActions, CreateIndexAction, DropIndexAction,
    AllConstraintActions, CreateConstraintAction, DropConstraintAction,
    AllTokenActions, CreateNodeLabelAction, CreateRelationshipTypeAction, CreatePropertyKeyAction,
    AllTransactionActions, ShowTransactionAction, TerminateTransactionAction
  )

  def _dbmsAction: Gen[DbmsAction] = oneOf(
    AllDbmsAction, ExecuteAdminProcedureAction,
    AllUserActions, ShowUserAction, CreateUserAction, SetUserStatusAction, SetPasswordsAction, AlterUserAction, DropUserAction,
    AllRoleActions, ShowRoleAction, CreateRoleAction, DropRoleAction, AssignRoleAction, RemoveRoleAction,
    AllDatabaseManagementActions, CreateDatabaseAction, DropDatabaseAction,
    AllPrivilegeActions, ShowPrivilegeAction, AssignPrivilegeAction, RemovePrivilegeAction
  )

  def _databaseQualifier(haveUserQualifier: Boolean): Gen[List[DatabasePrivilegeQualifier]] =
    if (haveUserQualifier) {
      for {
        userNames <- _listOfNameOfEither
        qualifier <- oneOf(List(UserAllQualifier()(pos)), userNames.map(UserQualifier(_)(pos)))
      } yield qualifier
    } else {
      List(AllDatabasesQualifier()(pos))
    }

  def _graphQualifier: Gen[List[GraphPrivilegeQualifier]] = for {
    qualifierNames <- oneOrMore(_identifier)
    qualifier <- oneOf(qualifierNames.map(RelationshipQualifier(_)(pos)), List(RelationshipAllQualifier()(pos)),
                       qualifierNames.map(LabelQualifier(_)(pos)), List(LabelAllQualifier()(pos)),
                       qualifierNames.map(ElementQualifier(_)(pos)), List(ElementsAllQualifier()(pos)))
  } yield qualifier

  def _graphQualifierAndResource(graphAction: GraphAction): Gen[(List[GraphPrivilegeQualifier], Option[ActionResource])] =
    if (graphAction == AllGraphAction) {
      // ALL GRAPH PRIVILEGES has AllQualifier and no resource
      (List(AllQualifier()(pos)), None)
    } else if (graphAction == WriteAction) {
      // WRITE has AllElementsQualifier and no resource
      (List(ElementsAllQualifier()(pos)), None)
    } else if (graphAction == SetLabelAction || graphAction == RemoveLabelAction) {
      // SET/REMOVE LABEL have AllLabelQualifier and label resource
      for {
        resourceNames  <- oneOrMore(_identifier)
        resource       <- oneOf(LabelsResource(resourceNames)(pos), AllLabelResource()(pos))
      } yield (List(LabelAllQualifier()(pos)), Some(resource))
    } else if (graphAction == TraverseAction || graphAction == CreateElementAction || graphAction == DeleteElementAction) {
      // TRAVERSE, CREATE/DELETE ELEMENT have any graph qualifier and no resource
      for {
        qualifier      <- _graphQualifier
      } yield (qualifier, None)
    } else {
      // READ, MATCH, MERGE, SET PROPERTY have any graph qualifier and property resource
      for {
        qualifier      <- _graphQualifier
        resourceNames  <- oneOrMore(_identifier)
        resource       <- oneOf(PropertiesResource(resourceNames)(pos), AllPropertyResource()(pos))
      } yield (qualifier, Some(resource))
    }

  def _showPrivileges: Gen[ShowPrivileges] = for {
    names      <- _listOfNameOfEither
    showRole   = ShowRolesPrivileges(names)(pos)
    showUser1  = ShowUsersPrivileges(names)(pos)
    showUser2  = ShowUserPrivileges(None)(pos)
    showAll    = ShowAllPrivileges()(pos)
    scope      <- oneOf(showRole, showUser1, showUser2, showAll)
    yields     <- option(_eitherYieldOrWhere)
  } yield ShowPrivileges(scope, yields)(pos)

  def _dbmsPrivilege: Gen[PrivilegeCommand] = for {
    dbmsAction      <- _dbmsAction
    roleNames       <- _listOfNameOfEither
    revokeType      <- _revokeType
    dbmsGrant       = GrantPrivilege.dbmsAction(dbmsAction, roleNames)(pos)
    dbmsDeny        = DenyPrivilege.dbmsAction(dbmsAction, roleNames)(pos)
    dbmsRevoke      = RevokePrivilege.dbmsAction(dbmsAction, roleNames, revokeType)(pos)
    dbms            <- oneOf(dbmsGrant, dbmsDeny, dbmsRevoke)
  } yield dbms

  def _qualifiedDbmsPrivilege: Gen[PrivilegeCommand] = for {
    dbmsAction         <- oneOf(ExecuteProcedureAction, ExecuteBoostedProcedureAction)
    procedureNamespace <- _namespace
    procedureName      <- _procedureName
    procedures         <- oneOrMore(ProcedureQualifier(procedureNamespace, procedureName)(pos))
    qualifier          <- frequency(7 -> procedures, 3 -> List(ProcedureQualifier(Namespace()(pos), ProcedureName("*")(pos))(pos)))
    roleNames          <- _listOfNameOfEither
    revokeType         <- _revokeType
    dbmsGrant          = GrantPrivilege.dbmsAction(dbmsAction, roleNames, qualifier)(pos)
    dbmsDeny           = DenyPrivilege.dbmsAction(dbmsAction, roleNames, qualifier)(pos)
    dbmsRevoke         = RevokePrivilege.dbmsAction(dbmsAction, roleNames, revokeType, qualifier)(pos)
    dbms               <- oneOf(dbmsGrant, dbmsDeny, dbmsRevoke)
  } yield dbms

  def _databasePrivilege: Gen[PrivilegeCommand] = for {
    databaseAction      <- _databaseAction
    namedScope          <- _listOfNameOfEither.map(_.map(n => NamedDatabaseScope(n)(pos)))
    databaseScope       <- oneOf(namedScope, List(AllDatabasesScope()(pos)), List(DefaultDatabaseScope()(pos)))
    databaseQualifier   <- _databaseQualifier(databaseAction.isInstanceOf[TransactionManagementAction])
    roleNames           <- _listOfNameOfEither
    revokeType          <- _revokeType
    databaseGrant       = GrantPrivilege.databaseAction(databaseAction, databaseScope, roleNames, databaseQualifier)(pos)
    databaseDeny        = DenyPrivilege.databaseAction(databaseAction, databaseScope, roleNames, databaseQualifier)(pos)
    databaseRevoke      = RevokePrivilege.databaseAction(databaseAction, databaseScope, roleNames, revokeType, databaseQualifier)(pos)
    database            <- oneOf(databaseGrant, databaseDeny, databaseRevoke)
  } yield database

  def _graphPrivilege: Gen[PrivilegeCommand] = for {
    graphAction                 <- _graphAction
    namedScope                  <- _listOfNameOfEither.map(_.map(n => NamedGraphScope(n)(pos)))
    graphScope                  <- oneOf(namedScope, List(AllGraphsScope()(pos)), List(DefaultGraphScope()(pos)))
    (qualifier, maybeResource)  <- _graphQualifierAndResource(graphAction)
    roleNames                   <- _listOfNameOfEither
    revokeType                  <- _revokeType
    graphGrant                  = GrantPrivilege.graphAction(graphAction, maybeResource, graphScope, qualifier, roleNames)(pos)
    graphDeny                   = DenyPrivilege.graphAction(graphAction, maybeResource, graphScope, qualifier, roleNames)(pos)
    graphRevoke                 = RevokePrivilege.graphAction(graphAction, maybeResource, graphScope, qualifier, roleNames, revokeType)(pos)
    graph                       <- oneOf(graphGrant, graphDeny, graphRevoke)
  } yield graph

  def _privilegeCommand: Gen[AdministrationCommand] = oneOf(
    _showPrivileges,
    _dbmsPrivilege,
    _qualifiedDbmsPrivilege,
    _databasePrivilege,
    _graphPrivilege
  )

  // Database commands

  def _showDatabase: Gen[ShowDatabase] = for {
    dbName <- _nameAsEither
    scope  <- oneOf(NamedDatabaseScope(dbName)(pos), AllDatabasesScope()(pos), DefaultDatabaseScope()(pos))
    yields <- option(_eitherYieldOrWhere)
  } yield ShowDatabase(scope, yields)(pos)

  def _createDatabase: Gen[CreateDatabase] = for {
    dbName <- _nameAsEither
    ifExistsDo <- _ifExistsDo
    wait <- _waitUntilComplete
  } yield CreateDatabase(dbName, ifExistsDo, wait)(pos)

  def _dropDatabase: Gen[DropDatabase] = for {
    dbName <- _nameAsEither
    ifExists <- boolean
    additionalAction <- Gen.oneOf( DumpData, DestroyData )
    wait <- _waitUntilComplete
  } yield DropDatabase(dbName, ifExists, additionalAction, wait)(pos)

  def _startDatabase: Gen[StartDatabase] = for {
    dbName <- _nameAsEither
    wait <- _waitUntilComplete
  } yield StartDatabase(dbName, wait)(pos)

  def _stopDatabase: Gen[StopDatabase] = for {
    dbName <- _nameAsEither
    wait <- _waitUntilComplete
  } yield StopDatabase(dbName, wait)(pos)

  def _multiDatabaseCommand: Gen[AdministrationCommand] = oneOf(
    _showDatabase,
    _createDatabase,
    _dropDatabase,
    _startDatabase,
    _stopDatabase
  )

  def _waitUntilComplete: Gen[WaitUntilComplete] = for {
    timeout <- posNum[Long]
    wait <- oneOf(NoWait, IndefiniteWait, TimeoutAfter(timeout))
  } yield wait

  // Top level administration command

  def _adminCommand: Gen[AdministrationCommand] = for {
    command <- oneOf(_userCommand, _roleCommand, _privilegeCommand, _multiDatabaseCommand)
    use     <- frequency(1 -> some(_use), 9 -> const(None))
  } yield command.withGraph(use)

  // Top level statement
  // ----------------------------------

  def _statement: Gen[Statement] = oneOf(
    _query,
    _schemaCommand,
    _adminCommand
  )
}
