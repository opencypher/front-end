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
import org.opencypher.v9_0.ast.CreateDatabase
import org.opencypher.v9_0.ast.NamespacedName
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetProperty
import org.opencypher.v9_0.ast.semantics.SemanticTable
import org.opencypher.v9_0.expressions.FunctionInvocation
import org.opencypher.v9_0.expressions.FunctionName
import org.opencypher.v9_0.expressions.LabelExpression.ColonDisjunction
import org.opencypher.v9_0.expressions.Namespace
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.ShortestPaths
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.DeprecatedDatabaseNameNotification
import org.opencypher.v9_0.util.DeprecatedNodesOrRelationshipsInSetClauseNotification
import org.opencypher.v9_0.util.DeprecatedRelTypeSeparatorNotification
import org.opencypher.v9_0.util.FixedLengthRelationshipInShortestPath
import org.opencypher.v9_0.util.InternalNotification
import org.opencypher.v9_0.util.Ref

object Deprecations {

  case object syntacticallyDeprecatedFeatures extends SyntacticDeprecations {

    override val find: PartialFunction[Any, Deprecation] = {

      // legacy type separator -[:A|:B]->
      case rel @ RelationshipPattern(variable, Some(labelExpression), None, None, None, _)
        // this restriction is necessary because in all other cases, this is an error
        if variable.forall(variable => !AnonymousVariableNameGenerator.isNamed(variable.name)) &&
          !labelExpression.containsGpmSpecificRelTypeExpression &&
          labelExpression.folder.findAllByClass[ColonDisjunction].nonEmpty =>
        Deprecation(
          Some(Ref(rel) -> rel.copy(labelExpression = Some(labelExpression.replaceColonSyntax))(rel.position)),
          Some(DeprecatedRelTypeSeparatorNotification(
            labelExpression.folder.findAllByClass[ColonDisjunction].head.position
          ))
        )
      case s @ SetExactPropertiesFromMapItem(_, e: Variable) =>
        Deprecation(
          Some(Ref(s) -> s.copy(expression =
            functionInvocationForSetProperties(s, e))(s.position)),
          Some(DeprecatedNodesOrRelationshipsInSetClauseNotification(e.position))
        )
      case s @ SetIncludingPropertiesFromMapItem(_, e: Variable) =>
        Deprecation(
          Some(Ref(s) -> s.copy(expression =
            functionInvocationForSetProperties(s, e))(s.position)),
          Some(DeprecatedNodesOrRelationshipsInSetClauseNotification(e.position))
        )

      case ShortestPaths(RelationshipChain(_: NodePattern, relPat @ RelationshipPattern(_, _, None, _, _, _), _), _) =>
        Deprecation(
          None,
          Some(FixedLengthRelationshipInShortestPath(relPat.position))
        )

      case c @ CreateDatabase(nn @ NamespacedName(_, Some(_)), _, _, _) =>
        Deprecation(
          None,
          Some(DeprecatedDatabaseNameNotification(nn.toString, Some(c.position)))
        )

    }
  }

  private def functionInvocationForSetProperties(s: SetProperty, e: Variable): FunctionInvocation = {
    FunctionInvocation(
      namespace = Namespace(List())(e.position),
      functionName = FunctionName("properties")(e.position),
      distinct = false,
      args = Vector(e)
    )(s.position)
  }

  // add new semantically deprecated features here
  case object semanticallyDeprecatedFeatures extends SemanticDeprecations {

    override def find(semanticTable: SemanticTable): PartialFunction[Any, Deprecation] = Map.empty
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
