/*
 * Copyright © 2002-2019 Neo4j Sweden AB (http://neo4j.com)
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
package org.opencypher.v9_0.expressions

import org.opencypher.v9_0.util.InputPosition

sealed trait CachedType

case object CACHED_NODE extends CachedType

case object CACHED_RELATIONSHIP extends CachedType

/**
  * Common super class of [[CachedProperty]]
  * and its slotted specializations.
  */
trait ASTCachedProperty extends LogicalProperty {
  def cachedType: CachedType
  def variableName: String
  override val map: Variable = Variable(variableName)(this.position)
}

/**
  * A property value that is cached in the execution context. Such a value can be
  * retrieved very fast, but care has to be taken to it doesn't out-dated by writes to
  * the graph/transaction state.
  *
  * @param variableName the variable
  * @param propertyKey  the property key
  */
case class CachedProperty(variableName: String,
                          propertyKey: PropertyKeyName,
                          override val cachedType: CachedType
                         )(val position: InputPosition) extends ASTCachedProperty {

  def cacheKey: String = s"$variableName.${propertyKey.name}"

  override def asCanonicalStringVal: String = s"cache[$cacheKey]"
}
