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
package org.opencypher.v9_0.parser.javacc

import org.opencypher.v9_0.parser.javacc.CypherConstants.DECIMAL_DOUBLE
import org.opencypher.v9_0.parser.javacc.CypherConstants.IDENTIFIER
import org.opencypher.v9_0.parser.javacc.CypherConstants.LBRACKET
import org.opencypher.v9_0.parser.javacc.CypherConstants.LCURLY
import org.opencypher.v9_0.parser.javacc.CypherConstants.LPAREN
import org.opencypher.v9_0.parser.javacc.CypherConstants.MINUS
import org.opencypher.v9_0.parser.javacc.CypherConstants.PLUS
import org.opencypher.v9_0.parser.javacc.CypherConstants.STRING_LITERAL1
import org.opencypher.v9_0.parser.javacc.CypherConstants.STRING_LITERAL2
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNSIGNED_DECIMAL_INTEGER
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNSIGNED_HEX_INTEGER
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNSIGNED_OCTAL_INTEGER

object ExpressionTokens {

  val tokens = Set(
    DECIMAL_DOUBLE,
    IDENTIFIER,
    LBRACKET,
    LCURLY,
    LPAREN,
    MINUS,
    PLUS,
    STRING_LITERAL1,
    STRING_LITERAL2,
    UNSIGNED_DECIMAL_INTEGER,
    UNSIGNED_HEX_INTEGER,
    UNSIGNED_OCTAL_INTEGER,
  )
}
