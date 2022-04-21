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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.AllPropertiesSelector
import org.opencypher.v9_0.expressions.DesugaredMapProjection
import org.opencypher.v9_0.expressions.LiteralEntry
import org.opencypher.v9_0.expressions.MapProjection
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.PropertyKeyName
import org.opencypher.v9_0.expressions.PropertySelector
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.expressions.VariableSelector
import org.opencypher.v9_0.rewriting.conditions.PatternExpressionsHaveSemanticInfo
import org.opencypher.v9_0.rewriting.rewriters.factories.ASTRewriterFactory
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.symbols.CypherType
import org.opencypher.v9_0.util.topDown

case object OnlyDesugaredMapProjections extends StepSequencer.Condition

/*
Handles rewriting map projection elements to literal entries when possible. If the user
has used an all properties selector ( n{ .* } ), we need to do the work in runtime.
In these situations, the rewriter turns as much as possible into literal entries,
so the runtime only has two cases to handle - literal entries and the special all-props selector.

We can't rewrite all the way to literal maps, since map projections yield a null map when the map_variable is null,
and the same behaviour can't be mimicked with literal maps.
 */
case class desugarMapProjection(state: SemanticState) extends Rewriter {
  override def apply(that: AnyRef): AnyRef = topDown(instance).apply(that)

  private val instance: Rewriter = Rewriter.lift {
    case e @ MapProjection(id, items) =>
      def propertySelect(propertyPosition: InputPosition, name: String): LiteralEntry = {
        val key = PropertyKeyName(name)(propertyPosition)
        val value = Property(id.copyId, key)(propertyPosition)
        LiteralEntry(key, value)(propertyPosition)
      }

      def identifierSelect(id: Variable): LiteralEntry =
        LiteralEntry(PropertyKeyName(id.name)(id.position), id)(id.position)

      var includeAllProps = false

      val mapExpressionItems = items.flatMap {
        case x: LiteralEntry                        => Some(x)
        case _: AllPropertiesSelector               => includeAllProps = true; None
        case PropertySelector(property: Variable)   => Some(propertySelect(property.position, property.name))
        case VariableSelector(identifier: Variable) => Some(identifierSelect(identifier))
      }

      DesugaredMapProjection(id, mapExpressionItems, includeAllProps)(e.position)
  }
}

object desugarMapProjection extends StepSequencer.Step with ASTRewriterFactory {
  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(OnlyDesugaredMapProjections)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set(
    ProjectionClausesHaveSemanticInfo, // It can invalidate this condition by rewriting things inside WITH/RETURN.
    PatternExpressionsHaveSemanticInfo // It can invalidate this condition by rewriting things inside PatternExpressions.
  )

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = desugarMapProjection(semanticState)
}
