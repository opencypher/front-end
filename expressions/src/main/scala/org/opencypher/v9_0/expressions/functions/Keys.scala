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
package org.opencypher.v9_0.expressions.functions

import org.opencypher.v9_0.expressions.TypeSignature
import org.opencypher.v9_0.util.symbols.CTList
import org.opencypher.v9_0.util.symbols.CTMap
import org.opencypher.v9_0.util.symbols.CTNode
import org.opencypher.v9_0.util.symbols.CTRelationship
import org.opencypher.v9_0.util.symbols.CTString

case object Keys extends Function {
  def name = "keys"

  override val signatures = Vector(
    TypeSignature(this, CTNode, CTList(CTString), "Returns a list containing the string representations for all the property names of a node.", Category.LIST),
    TypeSignature(this, CTRelationship, CTList(CTString), "Returns a list containing the string representations for all the property names of a relationship", Category.LIST),
    TypeSignature(this, CTMap, CTList(CTString), "Returns a list containing the string representations for all the property names of a map.", Category.LIST)
  )
}
