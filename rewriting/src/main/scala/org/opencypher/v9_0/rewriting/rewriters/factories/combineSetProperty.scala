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
package org.opencypher.v9_0.rewriting.rewriters.factories

import org.opencypher.v9_0.ast.SetClause
import org.opencypher.v9_0.ast.SetItem
import org.opencypher.v9_0.ast.SetProperty
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.SetPropertyItems
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.bottomUp
import org.opencypher.v9_0.util.symbols.CypherType

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

case object PropertiesCombined extends StepSequencer.Condition

object combineSetProperty extends Rewriter with StepSequencer.Step with ASTRewriterFactory {
  override def preConditions: Set[StepSequencer.Condition] = Set()
  override def postConditions: Set[StepSequencer.Condition] = Set(PropertiesCombined)
  override def invalidatedConditions: Set[StepSequencer.Condition] = Set()

  override def getRewriter(
    semanticState: SemanticState,
    parameterTypeMapping: Map[String, CypherType],
    cypherExceptionFactory: CypherExceptionFactory,
    anonymousVariableNameGenerator: AnonymousVariableNameGenerator
  ): Rewriter = this

  override def apply(input: AnyRef): AnyRef = instance(input)

  private def onSameEntity(setItem: SetItem, entity: Expression) = setItem match {
    case SetPropertyItem(Property(map, _), _) => map == entity
    case _                                    => false
  }

  private def combine(entity: Expression, ops: Seq[SetPropertyItem]): SetProperty =
    if (ops.size == 1) ops.head
    else SetPropertyItems(entity, ops.map(o => (o.property.propertyKey, o.expression)))(ops.head.position)

  private val instance: Rewriter = bottomUp(Rewriter.lift {
    case s @ SetClause(items) =>
      val newItems = ArrayBuffer.empty[SetItem]
      val itemsArray = items.toArray

      // find all contiguous blocks of SetPropertyItem on the same item, e.g., SET n.p1 = 1, n.p2 = 2
      // we are not allowed to change the order of the SET operations so it is only safe to combine
      // contiguous blocks.
      var i = 0
      while (i < itemsArray.length) {
        val item = itemsArray(i)
        item match {
          case s @ SetPropertyItem(Property(map, _), _) if i < itemsArray.length - 1 =>
            val itemsToCombine: mutable.ArrayBuffer[SetPropertyItem] = ArrayBuffer(s)
            while (i + 1 < itemsArray.length && onSameEntity(itemsArray(i + 1), map)) {
              itemsToCombine += itemsArray(i + 1).asInstanceOf[SetPropertyItem]
              i += 1
            }
            newItems += combine(map, itemsToCombine.toSeq)

          case item =>
            newItems += item
        }
        i += 1
      }
      s.copy(items = newItems.toSeq)(s.position)
  })
}
