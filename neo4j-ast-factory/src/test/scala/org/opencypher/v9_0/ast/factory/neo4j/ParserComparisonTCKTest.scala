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

import org.opencypher.tools.tck.api.CypherTCK
import org.opencypher.tools.tck.api.Execute
import org.opencypher.tools.tck.api.Scenario
import org.scalatest.FunSpecLike

/**
 * Compares the parboiled and JavaCC parsers with all TCK scenarios.
 */
class ParserComparisonTCKTest extends ParserComparisonTestBase with FunSpecLike {

  val scenariosPerFeature: Map[String, Seq[Scenario]] =
    CypherTCK.allTckScenarios.foldLeft(Map.empty[String, Seq[Scenario]]) {
      case (acc, scenario: Scenario) =>
        val soFar: Seq[Scenario] = acc.getOrElse(scenario.featureName, Seq.empty[Scenario])
        acc + (scenario.featureName -> (soFar :+ scenario))
    }
  var x = 0

  val DENYLIST = Set[String](
    // JavaCC fails invalid hex string during parsing, while parboiled defers failing until semantic checking
    """Feature "Literals3 - Hexadecimal integer": Scenario "Fail on an hexadecimal literal containing a upper case invalid alphanumeric character"""",
    """Feature "Literals3 - Hexadecimal integer": Scenario "Fail on an incomplete hexadecimal integer"""",
    """Feature "Literals3 - Hexadecimal integer": Scenario "Fail on an hexadecimal literal containing a lower case invalid alphanumeric character"""",

    // Failing with M16 TCK - require investigation
    """Feature "Literals2 - Decimal integer": Scenario "Fail on an integer containing a alphabetic character"""",
    """Feature "Literals7 - List": Scenario "Fail on a nested list with non-matching brackets"""",
    """Feature "Literals7 - List": Scenario "Fail on a list containing only a comma"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing only a comma"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing key starting with a number"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing key with dot"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing a list without key"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing a value without key"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing a map without key"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a nested map with non-matching braces"""",
    """Feature "Literals8 - Maps": Scenario "Fail on a map containing key with symbol"""",
  )

  scenariosPerFeature foreach {
    case (featureName, scenarios) =>
      describe(featureName) {
        scenarios
          .filterNot(scenarioObj => DENYLIST(denyListEntry(scenarioObj)))
          .foreach {
            scenarioObj =>
              val denyListName = denyListEntry(scenarioObj)
              describe(denyListName) {
                scenarioObj.steps foreach {
                  case Execute(query, _, _) =>
                    x = x + 1
                    it(s"[$x]\n$query") {
                      withClue(denyListName) {
                        try {
                          assertSameAST(query)
                        } catch {
                          // Allow withClue to populate the testcase name
                          case e: Exception => fail(e.getMessage, e)
                        }
                      }
                    }
                  case _ =>
                }
              }
        }
      }
    case _ =>
  }

  // Use the same denylist format as the other TCK tests
  private def denyListEntry(scenario:Scenario): String =
    s"""Feature "${scenario.featureName}": Scenario "${scenario.name}"""" + scenario.exampleIndex.map(ix => s""": Example "$ix"""").getOrElse("")

}

