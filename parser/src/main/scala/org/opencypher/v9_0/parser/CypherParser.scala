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
package org.opencypher.v9_0.parser

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.util.CypherException
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition
import org.parboiled.scala.EOI
import org.parboiled.scala.Parser
import org.parboiled.scala.Rule1

/**
  * Parser for Cypher queries.
  */
class CypherParser extends Parser
  with Statement
  with Expressions {

  @throws(classOf[CypherException])
  def parse(queryText: String, cypherExceptionFactory: CypherExceptionFactory, offset: Option[InputPosition] = None): ast.Statement =
    parseOrThrow(queryText, cypherExceptionFactory, offset, CypherParser.Statements)
}

object CypherParser extends Parser with Statement with Expressions {
  val Statements: Rule1[Seq[ast.Statement]] = rule {
    oneOrMore(WS ~ Statement ~ WS, separator = ch(';')) ~~ optional(ch(';')) ~~ EOI.label("end of input")
  }
}
