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

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.DeprecatedSyntax
import org.opencypher.v9_0.ast.ExistsConstraints
import org.opencypher.v9_0.ast.HasCatalog
import org.opencypher.v9_0.ast.NodeExistsConstraints
import org.opencypher.v9_0.ast.RelExistsConstraints
import org.opencypher.v9_0.ast.ShowConstraintsClause
import org.opencypher.v9_0.ast.ShowIndexesClause
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.semantics.SemanticTable
import org.opencypher.v9_0.expressions.And
import org.opencypher.v9_0.expressions.Ands
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.ExtractExpression
import org.opencypher.v9_0.expressions.ExtractScope
import org.opencypher.v9_0.expressions.FilterExpression
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.IsNotNull
import org.opencypher.v9_0.expressions.ListComprehension
import org.opencypher.v9_0.expressions.ListLiteral
import org.opencypher.v9_0.expressions.Or
import org.opencypher.v9_0.expressions.Ors
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.ParameterWithOldSyntax
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SignedHexIntegerLiteral
import org.opencypher.v9_0.expressions.SignedOctalIntegerLiteral
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.expressions.functions.Exists
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.DeprecatedCatalogKeywordForAdminCommandSyntax
import org.opencypher.v9_0.util.DeprecatedCoercionOfListToBoolean
import org.opencypher.v9_0.util.DeprecatedCreateIndexSyntax
import org.opencypher.v9_0.util.DeprecatedCreatePropertyExistenceConstraintSyntax
import org.opencypher.v9_0.util.DeprecatedDefaultDatabaseSyntax
import org.opencypher.v9_0.util.DeprecatedDefaultGraphSyntax
import org.opencypher.v9_0.util.DeprecatedDropConstraintSyntax
import org.opencypher.v9_0.util.DeprecatedDropIndexSyntax
import org.opencypher.v9_0.util.DeprecatedFunctionNotification
import org.opencypher.v9_0.util.DeprecatedHexLiteralSyntax
import org.opencypher.v9_0.util.DeprecatedOctalLiteralSyntax
import org.opencypher.v9_0.util.DeprecatedParameterSyntax
import org.opencypher.v9_0.util.DeprecatedPatternExpressionOutsideExistsSyntax
import org.opencypher.v9_0.util.DeprecatedPropertyExistenceSyntax
import org.opencypher.v9_0.util.DeprecatedRelTypeSeparatorNotification
import org.opencypher.v9_0.util.DeprecatedShowExistenceConstraintSyntax
import org.opencypher.v9_0.util.DeprecatedShowSchemaSyntax
import org.opencypher.v9_0.util.DeprecatedVarLengthBindingNotification
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.InternalNotification
import org.opencypher.v9_0.util.LengthOnNonPathNotification
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTList

import scala.collection.immutable.TreeMap

object Deprecations {

  def propertyOf(propertyKey: String): Expression => Expression =
    e => Property(e, PropertyKeyName(propertyKey)(e.position))(e.position)

  def renameFunctionTo(newName: String): FunctionInvocation => FunctionInvocation =
    f => f.copy(functionName = FunctionName(newName)(f.functionName.position))(f.position)

  case object deprecatedFeaturesIn4_X extends Deprecations {

    override val find: PartialFunction[Any, Deprecation] = {

      // old octal literal syntax
      case p@SignedOctalIntegerLiteral(stringVal) if stringVal.charAt(1) != 'o' =>
        Deprecation(
          None,
          Some(DeprecatedOctalLiteralSyntax(p.position))
        )

      // old hex literal syntax
      case p@SignedHexIntegerLiteral(stringVal) if stringVal.charAt(1) == 'X' =>
        Deprecation(
          Some(p -> SignedHexIntegerLiteral(stringVal.toLowerCase)(p.position)),
          Some(DeprecatedHexLiteralSyntax(p.position))
        )

      // timestamp
      case f@FunctionInvocation(_, FunctionName(name), _, _) if name.equalsIgnoreCase("timestamp") =>
        Deprecation(
          Some(f -> renameFunctionTo("datetime").andThen(propertyOf("epochMillis"))(f)),
          None
        )

      // var-length binding
      case p@RelationshipPattern(Some(variable), _, Some(_), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedVarLengthBindingNotification(p.position, variable.name))
        )

      case i: ast.CreateIndexOldSyntax =>
        Deprecation(
          None,
          Some(DeprecatedCreateIndexSyntax(i.position))
        )

      case i: ast.DropIndex =>
        Deprecation(
          None,
          Some(DeprecatedDropIndexSyntax(i.position))
        )

      case c: ast.DropNodeKeyConstraint =>
        Deprecation(
          None,
          Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropUniquePropertyConstraint =>
        Deprecation(
          None,
          Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropNodePropertyExistenceConstraint =>
        Deprecation(
          None,
          Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropRelationshipPropertyExistenceConstraint =>
        Deprecation(
          None,
          Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.CreateNodePropertyExistenceConstraint if c.oldSyntax =>
        Deprecation(
          None,
          Some(DeprecatedCreatePropertyExistenceConstraintSyntax(c.position))
        )

      case c: ast.CreateRelationshipPropertyExistenceConstraint if c.oldSyntax =>
        Deprecation(
          None,
          Some(DeprecatedCreatePropertyExistenceConstraintSyntax(c.position))
        )

      case e@Exists(_: Property | _: ContainerIndex) =>
        Deprecation(
          None,
          Some(DeprecatedPropertyExistenceSyntax(e.position))
        )

      case s: ShowIndexesClause if s.verbose || s.brief =>
        Deprecation(
          None,
          Some(DeprecatedShowSchemaSyntax(s.position))
        )

      case c@ast.GrantPrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.DenyPrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.RevokePrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.GrantPrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ast.DenyPrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ast.RevokePrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, ExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, NodeExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, RelExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case s: ShowConstraintsClause if s.verbose || s.brief =>
        Deprecation(
          None,
          Some(DeprecatedShowSchemaSyntax(s.position))
        )

      case c: HasCatalog =>
        Deprecation(
          Some(c -> c.source),
          Some(DeprecatedCatalogKeywordForAdminCommandSyntax(c.position))
        )
    }

    override def findWithContext(statement: Statement,
                                 semanticTable: Option[SemanticTable]): Set[Deprecation] = {
      statement.treeFold[Set[Deprecation]](Set.empty) {
        case w: Where =>
          val deprecations = w.treeFold[Set[Deprecation]](Set.empty) {
            case _: Where | _: And | _: Ands | _: Set[_] | _: Seq[_] | _: Or | _: Ors =>
              acc => TraverseChildren(acc)

            case e@Exists(p@(_: Property | _: ContainerIndex)) =>
              val deprecation = Deprecation(
                Some(e -> IsNotNull(p)(e.position)),
                None
              )
              acc => SkipChildren(acc + deprecation)

            case _ =>
              acc => SkipChildren(acc)
          }
          acc => SkipChildren(acc ++ deprecations)
      }
    }
  }

  case object deprecatedFeaturesIn4_XAfterRewrite extends Deprecations {
    override def find: PartialFunction[Any, Deprecation] = PartialFunction.empty

    override def findWithContext(statement: Statement, semanticTable: Option[SemanticTable]): Set[Deprecation] = {
      val deprecationsOfPatternExpressionsOutsideExists = statement.treeFold[Set[Deprecation]](Set.empty) {
        case Exists(_) =>
          // Don't look inside exists()
          deprecations => SkipChildren(deprecations)

        case p: PatternExpression =>
          val deprecation = Deprecation(
            None,
            Some(DeprecatedPatternExpressionOutsideExistsSyntax(p.position))
          )
          deprecations => SkipChildren(deprecations + deprecation)
      }

      val deprecationsOfCoercingListToBoolean = semanticTable.map { table =>
          def isListCoercedToBoolean(e: Expression) = table.types.get(e).exists(
            typeInfo => CTList(CTAny).covariant.containsAll(typeInfo.specified) && CTBoolean.covariant.containsAll(typeInfo.actual)
          )

          statement.treeFold[Set[Deprecation]](Set.empty) {
            case e: Expression if isListCoercedToBoolean(e) =>
              val deprecation = Deprecation(
                None,
                Some(DeprecatedCoercionOfListToBoolean(e.position))
              )
              deprecations => SkipChildren(deprecations + deprecation)
          }
      }.getOrElse(Set.empty)

      deprecationsOfPatternExpressionsOutsideExists ++ deprecationsOfCoercingListToBoolean
    }
  }

  // This is functionality that has been removed in 4.0 but still should work (but be deprecated) when using CYPHER 3.5
  case object removedFeaturesIn4_0 extends Deprecations {
    val removedFunctionsRenames: Map[String, String] =
      TreeMap(
        "toInt" -> "toInteger",
        "upper" -> "toUpper",
        "lower" -> "toLower",
        "rels" -> "relationships"
      )(CaseInsensitiveOrdered)

    override def find: PartialFunction[Any, Deprecation] = {

      case f@FunctionInvocation(_, FunctionName(name), _, _) if removedFunctionsRenames.contains(name) =>
        Deprecation(
          Some(f -> renameFunctionTo(removedFunctionsRenames(name))(f)),
          Some(DeprecatedFunctionNotification(f.position, name, removedFunctionsRenames(name)))
        )

      // extract => list comprehension
      case e@ExtractExpression(scope, expression) =>
        Deprecation(
          Some(e -> ListComprehension(scope, expression)(e.position)),
          Some(DeprecatedFunctionNotification(e.position, "extract(...)", "[...]"))
        )

      // filter => list comprehension
      case e@FilterExpression(scope, expression) =>
        Deprecation(
          Some(e -> ListComprehension(ExtractScope(scope.variable, scope.innerPredicate, None)(scope.position), expression)(e.position)),
          Some(DeprecatedFunctionNotification(e.position, "filter(...)", "[...]"))
        )

      // old parameter syntax
      case p@ParameterWithOldSyntax(name, parameterType) =>
        Deprecation(
          Some(p -> Parameter(name, parameterType)(p.position)),
          Some(DeprecatedParameterSyntax(p.position))
        )

      // length of a string, collection or pattern expression
      case f@FunctionInvocation(_, FunctionName(name), _, args)
        if name.toLowerCase.equals("length") && args.nonEmpty &&
          (args.head.isInstanceOf[StringLiteral] || args.head.isInstanceOf[ListLiteral] || args.head.isInstanceOf[PatternExpression]) =>
        Deprecation(
          Some(f -> renameFunctionTo("size")(f)),
          Some(LengthOnNonPathNotification(f.position))
        )

      // legacy type separator
      case p@RelationshipPattern(variable, _, length, properties, _, true) if variable.isDefined || length.isDefined || properties.isDefined =>
        Deprecation(
          Some(p -> p.copy(legacyTypeSeparator = false)(p.position)),
          Some(DeprecatedRelTypeSeparatorNotification(p.position))
        )
    }
  }

  object CaseInsensitiveOrdered extends Ordering[String] {
    def compare(x: String, y: String): Int =
      x.compareToIgnoreCase(y)
  }
}

/**
 * One deprecation.
 *
 * This class holds both the ability to replace a part of the AST with the preferred non-deprecated variant, and
 * the ability to generate an optional notification to the user that they are using a deprecated feature.
 *
 * @param replacement  an optional replacement tuple with the ASTNode to be replaced and its replacement.
 * @param notification optional appropriate deprecation notification
 */
case class Deprecation(replacement: Option[(ASTNode, ASTNode)], notification: Option[InternalNotification])

trait Deprecations extends {
  def find: PartialFunction[Any, Deprecation]
  def findWithContext(statement: Statement, semanticTable: Option[SemanticTable]): Set[Deprecation] = Set.empty
}
