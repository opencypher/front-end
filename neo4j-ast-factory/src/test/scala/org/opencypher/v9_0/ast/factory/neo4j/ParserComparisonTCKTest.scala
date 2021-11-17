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

  val DENYLIST: Set[String] = Set[String](
    // defers to semantic checking
    """Feature "Literals3 - Hexadecimal integer": Scenario "Fail on an incomplete hexadecimal integer"""",

    // Failing with M16 TCK - require investigation
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

    // TCK M17, fails in parboiled parser
    """Feature "ExistentialSubquery2 - Full existential subquery": Scenario "Full existential subquery with aggregation"""",

    // TCK M17, new syntax not implemented in parboiled
    """Feature "Call5 - Results projection": Scenario "Allow standalone call to procedure with YIELD *"""",
    """Feature "Call5 - Results projection": Scenario "Fail on in-query call to procedure with YIELD *"""",
  )

  val positionAcceptanceList = Seq(
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling string ranges 1"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling string ranges 4"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling numerical ranges 1"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling numerical ranges 3"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling string ranges 3"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling string ranges 3"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling string ranges 2"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling numerical ranges 4"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling empty range"""",
    """Feature "Comparison3 - Full-Bound Range": Scenario "Handling numerical ranges 2"""",
    """Feature "Comparison4 - Combination of Comparisons": Scenario "Handling long chains of operators"""",

    // TCK M17, seems like below parses with wrong position in parboiled
    """Feature "Call5 - Results projection": Scenario "Fail on renaming all outputs to the same variable name"""",
    """Feature "Call5 - Results projection": Scenario "Fail on renaming to an already bound variable name"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "0"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "1"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "2"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "3"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "4"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "5"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "6"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "7"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "8"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "9"""",
    """Feature "Call5 - Results projection": Scenario "Rename outputs to unbound variable names": Example "10"""",

    // TCK M17, seems like below parses with wrong position in JavaCC
    // TODO: remove below once this card has been played: https://trello.com/c/JhZlBMQa/3821-existential-subqueries-parsed-with-wrong-position-in-javacc-parser
    """Feature "ExistentialSubquery1 - Simple existential subquery": Scenario "Simple subquery without WHERE clause"""",
    """Feature "ExistentialSubquery1 - Simple existential subquery": Scenario "Simple subquery with WHERE clause"""",
    """Feature "ExistentialSubquery1 - Simple existential subquery": Scenario "Simple subquery without WHERE clause, not existing pattern"""",
    """Feature "ExistentialSubquery1 - Simple existential subquery": Scenario "Simple subquery with WHERE clause, not existing pattern"""",
  )

  scenariosPerFeature foreach {
    case (featureName, scenarios) =>
      describe(featureName) {
        scenarios
          .filterNot(scenarioObj => DENYLIST(denyListEntry(scenarioObj)))
          .foreach { scenarioObj =>
              val testName = denyListEntry(scenarioObj)
              describe(testName) {
                scenarioObj.steps foreach {
                  case Execute(query, _, _) =>
                    x = x + 1
                    it(s"[$x]\n$query") {
                      withClue(testName) {
                        try {
                          assertSameAST(query, !positionAcceptanceList.contains(testName))
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

