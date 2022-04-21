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
package org.opencypher.v9_0.util

import org.opencypher.v9_0.util.AnonymousVariableNameGenerator.prefix
import org.opencypher.v9_0.util.helpers.NameDeduplicator.nameGeneratorRegex

class AnonymousVariableNameGenerator() {
  private var counter = 0

  def nextName: String = {
    val result = s"$prefix$counter"
    counter += 1
    result
  }
}

object AnonymousVariableNameGenerator {
  val generatorName = "UNNAMED"
  private val prefix = s"  $generatorName"

  def isNamed(x: String): Boolean = !notNamed(x)
  def notNamed(x: String): Boolean = x.startsWith(prefix)

  def unapply(v: Any): Option[String] = v match {
    case str: String =>
      val regex = nameGeneratorRegex(generatorName)
      regex.findPrefixMatchOf(str).map(_ group 2)
    case _ => None
  }
}
