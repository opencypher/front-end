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
import org.opencypher.v9_0.parser.CypherParser
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.OpenCypherExceptionFactory

object OpenCypherJavaCCParserWithFallback {

  private val oldParser = new CypherParser()

  /**
   * This method should be used when parsing a query that might include administration commands which have not yet been ported to JavaCCParser.
   * It will try and parse with the JavaCCParser, and if that fails, we'll verify against shouldFallback that the query includes syntax which only the
   * old parser can parse. If that is the case we try to parse with the old parser.
   *
   * @param queryText                      The query to be parsed.
   * @param exceptionFactory               A factory for producing error messages related to the specific implementation of the language.
   * @param anonymousVariableNameGenerator Used to generate variable names during parsing.
   * @param offset                         An optional offset, only used in the old parser.
   * @return
   */
  def parse(queryText: String,
            exceptionFactory: OpenCypherExceptionFactory,
            anonymousVariableNameGenerator: AnonymousVariableNameGenerator,
            offset: Option[InputPosition] = None): Statement = {
    try {
      JavaCCParser.parse(queryText, exceptionFactory, anonymousVariableNameGenerator)
    } catch {
      // OpenCypherExceptionFactory error messages does not include the original query, so we need to verify if we should fallback using the original query.
      case _: OpenCypherExceptionFactory.SyntaxException if JavaCCParser.shouldFallback(queryText) =>
        oldParser.parse(queryText, exceptionFactory, offset)
    }
  }
}
