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

import scala.collection.JavaConverters

object ParseExceptions extends RuntimeException {

  def expected(expectedTokenSequences: Array[Array[Int]], tokenImage: Array[String]): java.util.List[String] = {
    JavaConverters.seqAsJavaList(processExpectedList(expectedTokenSequences.toSeq.flatten, tokenImage))
  }

  def processExpectedList(expected: Seq[Int], tokenImage: Array[String]): Seq[String] = {
    if (expected.contains(CypherConstants.IDENTIFIER)) {
      if (expected.contains(CypherConstants.PLUS)) {
        filterExpression(expected)
          .map(tokenImage(_))
          .filter(!_.equals("\"$\"")) :+ "an expression"
      } else {
        filterIdentifierTokens(expected)
          .map(token =>
            if (token.equals(CypherConstants.IDENTIFIER)) {
              "an identifier"
            } else {
              val image = tokenImage(token)
              if (image.equals("\"$\"")) {
                "a parameter"
              } else {
                image
              }
            })
      }
    } else {
      expected.map(tokenImage(_)).distinct
    }
  }

  def filterExpression(expected: Seq[Int]): Seq[Int] = {
    filterIdentifierTokens(expected).groupBy(identity).mapValues(_.size)
      .map({ case (token, count) =>
        if (ExpressionTokens.tokens.contains(token)) {
          (token, count - 1)
        } else {
          (token, count)
        }
      }).filter({ case (_, count) => count > 0 })
      .keySet.toSeq
  }

  def filterIdentifierTokens(expected: Seq[Int]): Seq[Int] = {
    expected.groupBy(identity).mapValues(_.size).map({ case (token, count) =>
      if (IdentifierTokens.tokens.contains(token)) {
        (token, count - 1)
      } else {
        (token, count)
      }
    }).filter({ case (_, count) => count > 0 })
      .keySet.toSeq
  }
}
