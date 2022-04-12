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
import org.opencypher.v9_0.ast.Options
import org.opencypher.v9_0.ast.OptionsMap
import org.opencypher.v9_0.ast.semantics.SemanticTable
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.MapExpression
import org.opencypher.v9_0.expressions.Namespace
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PatternExpression
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SignedHexIntegerLiteral
import org.opencypher.v9_0.expressions.SignedOctalIntegerLiteral
import org.opencypher.v9_0.expressions.StringLiteral
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.DeprecatedBtreeIndexSyntax
import org.opencypher.v9_0.util.DeprecatedCoercionOfListToBoolean
import org.opencypher.v9_0.util.DeprecatedHexLiteralSyntax
import org.opencypher.v9_0.util.DeprecatedOctalLiteralSyntax
import org.opencypher.v9_0.util.DeprecatedVarLengthBindingNotification
import org.opencypher.v9_0.util.InternalNotification
import org.opencypher.v9_0.util.Ref
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.symbols.CTBoolean
import org.opencypher.v9_0.util.symbols.CTList

object Deprecations {

  def propertyOf(propertyKey: String): Expression => Expression =
    e => Property(e, PropertyKeyName(propertyKey)(e.position))(e.position)

  def renameFunctionTo(newName: String): FunctionInvocation => FunctionInvocation =
    f => f.copy(functionName = FunctionName(newName)(f.functionName.position))(f.position)

  def renameFunctionTo(newNamespace: Namespace, newName: String): FunctionInvocation => FunctionInvocation =
    f => f.copy(namespace = newNamespace, functionName = FunctionName(newName)(f.functionName.position))(f.position)

  case object syntacticallyDeprecatedFeaturesIn4_X extends SyntacticDeprecations {
    override val find: PartialFunction[Any, Deprecation] = {

      // old octal literal syntax, don't support underscores
      case p@SignedOctalIntegerLiteral(stringVal) if stringVal.charAt(stringVal.indexOf('0') + 1) != 'o' && stringVal.charAt(stringVal.indexOf('0') + 1) != '_' =>
        Deprecation(
          Some(Ref(p) -> SignedOctalIntegerLiteral(stringVal.patch(stringVal.indexOf('0') + 1, "o", 0))(p.position)),
          Some(DeprecatedOctalLiteralSyntax(p.position))
        )

      // old hex literal syntax
      case p@SignedHexIntegerLiteral(stringVal) if stringVal.charAt(stringVal.indexOf('0') + 1) == 'X' =>
        Deprecation(
          Some(Ref(p) -> SignedHexIntegerLiteral(stringVal.toLowerCase)(p.position)),
          Some(DeprecatedHexLiteralSyntax(p.position))
        )

      // timestamp
      case f@FunctionInvocation(namespace, FunctionName(name), _, _) if namespace.parts.isEmpty && name.equalsIgnoreCase("timestamp") =>
        Deprecation(
          Some(Ref(f) -> renameFunctionTo("datetime").andThen(propertyOf("epochMillis"))(f)),
          None
        )

      // var-length binding
      case p@RelationshipPattern(Some(variable), _, Some(_), _, _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedVarLengthBindingNotification(p.position, variable.name))
        )

        // CREATE BTREE INDEX ...
      case i: ast.CreateBtreeNodeIndex =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(i.position))
        )

        // CREATE BTREE INDEX ...
      case i: ast.CreateBtreeRelationshipIndex =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(i.position))
        )

      // CREATE INDEX ... OPTIONS {<btree options>}
      case i: ast.CreateRangeNodeIndex if i.fromDefault && hasBtreeOptions(i.options) =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(i.position))
        )

      // CREATE INDEX ... OPTIONS {<btree options>}
      case i: ast.CreateRangeRelationshipIndex if i.fromDefault && hasBtreeOptions(i.options) =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(i.position))
        )

      // CREATE CONSTRAINT ... OPTIONS {<btree options>}
      case c: ast.CreateNodeKeyConstraint if hasBtreeOptions(c.options) =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(c.position))
        )

      // CREATE CONSTRAINT ... OPTIONS {<btree options>}
      case c: ast.CreateUniquePropertyConstraint if hasBtreeOptions(c.options) =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(c.position))
        )

      case i: ast.ShowIndexesClause if i.indexType == ast.BtreeIndexes =>
        Deprecation(
          None,
          Some(DeprecatedBtreeIndexSyntax(i.position))
        )
    }

    private def hasBtreeOptions(options: Options): Boolean = options match {
      case OptionsMap(opt) => opt.exists {
        case (key, value: StringLiteral) if key.equalsIgnoreCase("indexProvider") =>
          // Can't reach the GenericNativeIndexProvider and NativeLuceneFusionIndexProviderFactory30
          // so have to hardcode the btree providers instead
          value.value.equalsIgnoreCase("native-btree-1.0") || value.value.equalsIgnoreCase("lucene+native-3.0")

        case (key, value: MapExpression) if key.equalsIgnoreCase("indexConfig") =>
          // Can't reach the settings so have to hardcode them instead, only checks start of setting names
          //  spatial.cartesian.{min | max}
          //  spatial.cartesian-3d.{min | max}
          //  spatial.wgs-84.{min | max}
          //  spatial.wgs-84-3d.{min | max}
          val settings = value.items.map(_._1.name)
          settings.exists(name => name.toLowerCase.startsWith("spatial.cartesian") || name.toLowerCase.startsWith("spatial.wgs-84"))

        case _ => false
      }
      case _ => false
    }
  }

  case object semanticallyDeprecatedFeaturesIn4_X extends SemanticDeprecations {

    private def isExpectedTypeBoolean(semanticTable: SemanticTable, e: Expression) = semanticTable.types.get(e).exists(
      typeInfo => typeInfo.expected.fold(false)(CTBoolean.covariant.containsAll)
    )

    private def isListCoercedToBoolean(semanticTable: SemanticTable, e: Expression): Boolean = semanticTable.types.get(e).exists(
      typeInfo =>
        CTList(CTAny).covariant.containsAll(typeInfo.specified) && isExpectedTypeBoolean(semanticTable, e)
    )

    override def find(semanticTable: SemanticTable): PartialFunction[Any, Deprecation] = {
      case e: Expression if isListCoercedToBoolean(semanticTable, e) && !e.isInstanceOf[PatternExpression] =>
        Deprecation(
          None,
          Some(DeprecatedCoercionOfListToBoolean(e.position))
        )
    }
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
case class Deprecation(replacement: Option[(Ref[ASTNode], ASTNode)], notification: Option[InternalNotification])

sealed trait Deprecations

trait SyntacticDeprecations extends Deprecations {
  def find: PartialFunction[Any, Deprecation]
  def findWithContext(statement: ast.Statement): Set[Deprecation] = Set.empty
}

trait SemanticDeprecations extends Deprecations {
  def find(semanticTable: SemanticTable): PartialFunction[Any, Deprecation]
}
