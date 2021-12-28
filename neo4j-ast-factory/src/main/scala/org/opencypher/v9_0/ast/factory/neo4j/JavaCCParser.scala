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
package org.opencypher.v9_0.ast.factory.neo4j

import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.parser.javacc.Cypher
import org.opencypher.v9_0.parser.javacc.CypherCharStream
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InputPosition

case object JavaCCParser {

  // Triggers to fallback to parboiled parser
  // The EXECUTE privileges are still left to be ported.
  private val FALLBACK_TRIGGERS = Seq("EXECUTE")

  def shouldFallback(errorMsg: String): Boolean = {
    val upper = errorMsg.toUpperCase()
    FALLBACK_TRIGGERS.exists(upper.contains)
  }

  /**
   * parse() should only be used when parsing a query that is certain to not include an administration command that has not yet been ported to JavaCCParser.
   * Most likely, it should only be in tests.
   * @param queryText The query to be parsed.
   * @param cypherExceptionFactory A factory for producing error messages related to the specific implementation of the language.
   * @return
   */
  def parse(queryText: String, cypherExceptionFactory: CypherExceptionFactory, anonymousVariableNameGenerator: AnonymousVariableNameGenerator): Statement = {
    val charStream = new CypherCharStream(queryText)
    val astFactory = new Neo4jASTFactory(queryText, anonymousVariableNameGenerator)
    val astExceptionFactory = new Neo4jASTExceptionFactory(cypherExceptionFactory)

    val statements = new Cypher(astFactory, astExceptionFactory, charStream).Statements()
    if (statements.size() == 1) {
      statements.get(0)
    } else {
      throw cypherExceptionFactory.syntaxException(s"Expected exactly one statement per query but got: ${statements.size}", InputPosition.NONE)
    }
  }
}
