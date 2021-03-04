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
package org.opencypher.v9_0.ast.semantics

import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.InvalidNodePattern
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.LogicalVariable
import org.opencypher.v9_0.expressions.MapExpression
import org.opencypher.v9_0.expressions.NamedPatternPart
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.Parameter
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.Pattern.SemanticContext
import org.opencypher.v9_0.expressions.Pattern.SemanticContext.Match
import org.opencypher.v9_0.expressions.Pattern.SemanticContext.name
import org.opencypher.v9_0.expressions.PatternElement
import org.opencypher.v9_0.expressions.PatternPart
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.Range
import org.opencypher.v9_0.expressions.RelTypeName
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.AllNameGenerators
import org.opencypher.v9_0.util.DeprecatedRepeatedRelVarInPatternExpression
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.UnboundedShortestPathNotification
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTPath
import org.opencypher.v9_0.util.symbols.CTRelationship

object SemanticPatternCheck extends SemanticAnalysisTooling {

  def check(ctx: SemanticContext, pattern: Pattern): SemanticCheck =
        semanticCheckFold(pattern.patternParts)(declareVariables(ctx)) chain
          semanticCheckFold(pattern.patternParts)(check(ctx)) chain
          ensureNoDuplicateRelationships(pattern, error = true)

  def check(ctx: SemanticContext, pattern: RelationshipsPattern): SemanticCheck =
    declareVariables(ctx, pattern.element) chain
      check(ctx, pattern.element) chain
      ensureNoDuplicateRelationships(pattern, error = false)

  def declareVariables(ctx: SemanticContext)(part: PatternPart): SemanticCheck =
    part match {
      case x: NamedPatternPart =>
        declareVariables(ctx)(x.patternPart) chain
          declareVariable(x.variable, CTPath)

      case x: EveryPath =>
        (x.element, ctx) match {
          case (_: NodePattern, SemanticContext.Match) =>
            declareVariables(ctx, x.element)
          case (n: NodePattern, _) =>
            n.variable.fold(SemanticCheckResult.success)(declareVariable(_, CTNode)) chain
              declareVariables(ctx, n)
          case _ =>
            declareVariables(ctx, x.element)
        }

      case x: ShortestPaths =>
        declareVariables(ctx, x.element)
    }

  @scala.annotation.tailrec
  def check(ctx: SemanticContext)(part: PatternPart): SemanticCheck =
    part match {
      case x: NamedPatternPart =>
        check(ctx)(x.patternPart)

      case x: EveryPath =>
        check(ctx, x.element)

      case x: ShortestPaths =>
        def checkContext: SemanticCheck =
          ctx match {
            case SemanticContext.Merge =>
              SemanticError(s"${
                x.name
              }(...) cannot be used to MERGE", x.position)
            case SemanticContext.Create =>
              SemanticError(s"${
                x.name
              }(...) cannot be used to CREATE", x.position)
            case _ =>
              None
          }

        def checkContainsSingle: SemanticCheck =
          x.element match {
            case RelationshipChain(_: NodePattern, r, _: NodePattern) =>
              r.properties.map {
                props =>
                  SemanticError(s"${
                    x.name
                  }(...) contains properties $props. This is currently not supported.", x.position)
              }
            case _ =>
              SemanticError(s"${
                x.name
              }(...) requires a pattern containing a single relationship", x.position)
          }

        def checkKnownEnds: SemanticCheck =
          (ctx, x.element) match {
            case (Match, _) => None
            case (_, RelationshipChain(l: NodePattern, _, r: NodePattern)) =>
              if (l.variable.isEmpty)
                SemanticError(s"A ${
                  x.name
                }(...) requires bound nodes when not part of a MATCH clause.", x.position)
              else if (r.variable.isEmpty)
                SemanticError(s"A ${
                  x.name
                }(...) requires bound nodes when not part of a MATCH clause.", x.position)
              else
                None
            case (_, _) =>
              None
          }

        def checkLength: SemanticCheck =
          (state: SemanticState) =>
            x.element match {
              case RelationshipChain(_, rel, _) =>
                rel.length match {
                  case Some(Some(Range(Some(min), _))) if min.value < 0 || min.value > 1 =>
                    SemanticCheckResult(state, Seq(SemanticError(s"${
                      x.name
                    }(...) does not support a minimal length different " +
                      s"from 0 or 1", x.position)))

                  case Some(None) =>
                    val newState = state.addNotification(UnboundedShortestPathNotification(x.element.position))
                    SemanticCheckResult(newState, Seq.empty)
                  case _ => SemanticCheckResult(state, Seq.empty)
                }
              case _ => SemanticCheckResult(state, Seq.empty)
            }

        def checkRelVariablesUnknown: SemanticCheck =
          state => {
            x.element match {
              case RelationshipChain(_, rel, _) =>
                rel.variable.flatMap(id => state.symbol(id.name)) match {
                  case Some(symbol) if symbol.positions.size > 1 => {
                    SemanticCheckResult.error(state, SemanticError(s"Bound relationships not allowed in ${
                      x.name
                    }(...)", rel.position))
                  }
                  case _ =>
                    SemanticCheckResult.success(state)
                }
              case _ =>
                SemanticCheckResult.success(state)
            }
          }

        checkContext chain
          checkContainsSingle chain
          checkKnownEnds chain
          checkLength chain
          checkRelVariablesUnknown chain
          check(ctx, x.element)
    }

  def check(ctx: SemanticContext, element: PatternElement): SemanticCheck =
    element match {
      case x: RelationshipChain =>
        check(ctx, x.element) chain
          check(ctx, x.relationship) chain
          check(ctx, x.rightNode)

      case x: InvalidNodePattern =>
        checkNodeProperties(ctx, x.properties) chain
          error(s"Parentheses are required to identify nodes in patterns, i.e. (${x.id.name})", x.position)

      case x: NodePattern =>
        checkNodeProperties(ctx, x.properties) chain
          checkValidLabels(x.labels, x.position)

    }

  def check(ctx: SemanticContext, x: RelationshipPattern): SemanticCheck = {
    def checkNotUndirectedWhenCreating: SemanticCheck = {
      ctx match {
        case SemanticContext.Create if x.direction == SemanticDirection.BOTH =>
          error(s"Only directed relationships are supported in ${name(ctx)}", x.position)
        case _ =>
          SemanticCheckResult.success
      }
    }

    def checkNoVarLengthWhenUpdating: SemanticCheck =
      when(!x.isSingleLength) {
        ctx match {
          case SemanticContext.Merge | SemanticContext.Create =>
            error(s"Variable length relationships cannot be used in ${name(ctx)}", x.position)
          case _ =>
            None
        }
      }

    def checkProperties: SemanticCheck =
      SemanticExpressionCheck.simple(x.properties) chain
        expectType(CTMap.covariant, x.properties)

    def checkForLegacyTypeSeparator: SemanticCheck = x match {
      case RelationshipPattern(variable, _, length, properties, _, true) if (variable.isDefined && !variableIsGenerated(variable.get)) || length.isDefined || properties.isDefined =>
        error(
          """The semantics of using colon in the separation of alternative relationship types in conjunction with
            |the use of variable binding, inlined property predicates, or variable length is no longer supported.
            |Please separate the relationships types using `:A|B|C` instead""".stripMargin, x.position)
      case _ =>
        None
    }

    checkNoVarLengthWhenUpdating chain
      checkForLegacyTypeSeparator chain
      checkNoParamMapsWhenMatching(x.properties, ctx) chain
      checkProperties chain
      checkValidPropertyKeyNamesInPattern(x.properties) chain
      checkValidRelTypes(x.types, x.position) chain
      checkNotUndirectedWhenCreating
  }

  def variableIsGenerated(variable: LogicalVariable): Boolean = !AllNameGenerators.isNamed(variable.name)

  def declareVariables(ctx: SemanticContext, element: PatternElement): SemanticCheck =
    element match {
      case x: RelationshipChain =>
        declareVariables(ctx, x.element) chain
          declareVariables(ctx, x.relationship) chain
          declareVariables(ctx, x.rightNode)

      case x: NodePattern =>
        x.variable.fold(SemanticCheckResult.success) {
          variable =>
            ctx match {
              case SemanticContext.Expression =>
                ensureDefined(variable) chain
                  expectType(CTNode.covariant, variable)
              case _ =>
                implicitVariable(variable, CTNode)
            }
        }
    }

  def declareVariables(ctx: SemanticContext, x: RelationshipPattern): SemanticCheck =
    x.variable.fold(SemanticCheckResult.success) {
      variable =>
        val possibleType = if (x.length.isEmpty) CTRelationship else CTList(CTRelationship)

        ctx match {
          case SemanticContext.Match =>
            implicitVariable(variable, possibleType)
          case SemanticContext.Expression =>
            ensureDefined(variable) chain
              expectType(possibleType.covariant, variable)
          case _ =>
            declareVariable(variable, possibleType)
        }
    }

  /**
   * Traverse the sub-tree at astNode. Warn or fail if any duplicate relationships are found at that sub-tree.
   *
   * @param astNode the sub-tree to traverse.
   * @param error if true return an error, otherwise a warning.
   */
  private def ensureNoDuplicateRelationships(astNode: ASTNode, error: Boolean): SemanticCheck = {
    def perDuplicate(name: String, pos: InputPosition): SemanticCheck =
      if(error)
        SemanticError(s"Cannot use the same relationship variable '$name' for multiple patterns", pos)
      else
        state => SemanticCheckResult(state.addNotification(DeprecatedRepeatedRelVarInPatternExpression(pos, name)), Seq.empty)

    RelationshipChain.findDuplicateRelationships(astNode).foldSemanticCheck {
      duplicate => perDuplicate(duplicate.name, duplicate.position)
    }
  }

  def checkNodeProperties(ctx: SemanticContext, properties: Option[Expression]): SemanticCheck =
    checkNoParamMapsWhenMatching(properties, ctx) chain
      checkValidPropertyKeyNamesInPattern(properties) chain
      SemanticExpressionCheck.simple(properties) chain
      expectType(CTMap.covariant, properties)

  def checkValidPropertyKeyNamesInReturnItems(returnItems: ReturnItems, position: InputPosition): SemanticCheck = {
    val propertyKeys = returnItems.items.collect { case item => item.expression.findByAllClass[Property]map(prop => prop.propertyKey) }.flatten
    SemanticPatternCheck.checkValidPropertyKeyNames(propertyKeys, position)
  }

  def checkValidPropertyKeyNames(propertyKeys: Seq[PropertyKeyName], pos: InputPosition): SemanticCheck = {
    val errorMessage = propertyKeys.collectFirst { case key if checkValidTokenName(key.name).nonEmpty =>
      checkValidTokenName(key.name).get
    }
    if (errorMessage.nonEmpty) SemanticError(errorMessage.get, pos) else None
  }

  def checkValidLabels(labelNames: Seq[LabelName], pos: InputPosition): SemanticCheck = {
    val errorMessage = labelNames.collectFirst { case label if checkValidTokenName(label.name).nonEmpty =>
      checkValidTokenName(label.name).get
    }
    if (errorMessage.nonEmpty) SemanticError(errorMessage.get, pos) else None
  }

  def checkValidRelTypes(relTypeNames: Seq[RelTypeName], pos: InputPosition): SemanticCheck = {
    val errorMessage = relTypeNames.collectFirst { case relType if checkValidTokenName(relType.name).nonEmpty =>
      checkValidTokenName(relType.name).get
    }
    if (errorMessage.nonEmpty) SemanticError(errorMessage.get, pos) else None
  }

  private def checkValidTokenName(name: String): Option[String] = {
    if (name == null || name.isEmpty || name.contains("\u0000")) {
      Some(String.format("%s is not a valid token name. " + "Token names cannot be empty or contain any null-bytes.",
        if (name != null) "'" + name + "'" else "Null"))
    } else {
      None
    }
  }
}

object checkNoParamMapsWhenMatching {
  def apply(properties: Option[Expression], ctx: SemanticContext): SemanticCheck = (properties, ctx) match {
    case (Some(e: Parameter), SemanticContext.Match) =>
      SemanticError("Parameter maps cannot be used in MATCH patterns (use a literal map instead, eg. \"{id: {param}.id}\")", e.position)
    case (Some(e: Parameter), SemanticContext.Merge) =>
      SemanticError("Parameter maps cannot be used in MERGE patterns (use a literal map instead, eg. \"{id: {param}.id}\")", e.position)
    case _ =>
      None
  }
}

object checkValidPropertyKeyNamesInPattern {
  def apply(properties: Option[Expression]): SemanticCheck = properties match {
    case Some(e: MapExpression) => SemanticPatternCheck.checkValidPropertyKeyNames(e.items.map(i => i._1), e.position)
    case _ => None
  }
}
