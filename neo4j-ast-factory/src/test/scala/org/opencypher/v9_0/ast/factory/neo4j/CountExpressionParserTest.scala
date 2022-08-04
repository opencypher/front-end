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

import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.UnaliasedReturnItem
import org.opencypher.v9_0.expressions.CaseExpression
import org.opencypher.v9_0.expressions.CountExpression
import org.opencypher.v9_0.expressions.LabelExpression
import org.opencypher.v9_0.expressions.LabelName
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SemanticDirection.OUTGOING
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.InputPosition

class CountExpressionParserTest extends JavaccParserAstTestBase[Statement] {

  implicit private val parser: JavaccRule[Statement] = JavaccRule.Statement

  test(
    """MATCH (m)
      |WHERE COUNT { (m)-[r]->(p) } > 1
      |RETURN m""".stripMargin
  ) {

    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(Some(Variable("r")(InputPosition(29, 2, 20))), None, None, None, None, OUTGOING)(
          InputPosition(27, 2, 18)
        ),
        nodePat(Some("p"))
      )(InputPosition(27, 2, 18)),
      None
    )(InputPosition(16, 2, 7), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m")), Some(where(gt(countExpression, literal(1))))),
        return_(variableReturnItem("m"))
      )
    }
  }

  test(
    """MATCH (m)
      |WHERE COUNT { (m)-[]->() } > 1
      |RETURN m""".stripMargin
  ) {
    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(None, None, None, None, None, OUTGOING)(InputPosition(27, 2, 18)),
        nodePat(None)
      )(InputPosition(27, 2, 18)),
      None
    )(InputPosition(16, 2, 7), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m")), Some(where(gt(countExpression, literal(1))))),
        return_(variableReturnItem("m"))
      )
    }
  }

  test(
    """MATCH (m)
      |WHERE COUNT { (m) } > 1
      |RETURN m""".stripMargin
  ) {
    val countExpression: CountExpression = CountExpression(nodePat(Some("m")), None)(InputPosition(16, 2, 7), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m")), Some(where(gt(countExpression, literal(1))))),
        return_(variableReturnItem("m"))
      )
    }
  }

  // This would parse but would not pass the semantic check
  test(
    """MATCH (m)
      |WHERE COUNT { (m) WHERE m.prop = 3 } > 1
      |RETURN m""".stripMargin
  ) {
    val countExpression: CountExpression =
      CountExpression(nodePat(Some("m")), Some(propEquality("m", "prop", 3)))(InputPosition(16, 2, 7), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m")), Some(where(gt(countExpression, literal(1))))),
        return_(variableReturnItem("m"))
      )
    }
  }

  // COUNT in a RETURN statement
  test(
    """MATCH (m)
      |RETURN COUNT { (m)-[]->() }""".stripMargin
  ) {
    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(None, None, None, None, None, OUTGOING)(InputPosition(28, 2, 19)),
        nodePat(None)
      )(InputPosition(28, 2, 19)),
      None
    )(InputPosition(17, 2, 8), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m"))),
        return_(returnItem(countExpression, "COUNT { (m)-[]->() }", InputPosition(17, 2, 8)))
      )
    }
  }

  // COUNT in a SET statement
  test(
    """MATCH (m)
      |SET m.howMany = COUNT { (m)-[]->() }
    """.stripMargin
  ) {
    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(None, None, None, None, None, OUTGOING)(InputPosition(37, 2, 28)),
        nodePat(None)
      )(InputPosition(37, 2, 28)),
      None
    )(InputPosition(26, 2, 17), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m"))),
        set_(Seq(setPropertyItem("m", "howMany", countExpression)))
      )
    }
  }

  // COUNT in a WHEN statement
  test(
    """MATCH (m)
      |RETURN CASE WHEN COUNT { (m)-[]->() } > 0 THEN "hasProperty" ELSE "other" END
    """.stripMargin
  ) {
    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(None, None, None, None, None, OUTGOING)(InputPosition(38, 2, 29)),
        nodePat(None)
      )(InputPosition(38, 2, 29)),
      None
    )(InputPosition(27, 2, 18), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("m"))),
        return_(UnaliasedReturnItem(
          CaseExpression(None, List((gt(countExpression, literal(0)), literal("hasProperty"))), Some(literal("other")))(
            pos
          ),
          "CASE WHEN COUNT { (m)-[]->() } > 0 THEN \"hasProperty\" ELSE \"other\" END"
        )(pos))
      )
    }
  }

  // COUNT in a WITH statement
  test("WITH COUNT { (m)-[]->() } AS result RETURN result") {
    val countExpression: CountExpression = CountExpression(
      RelationshipChain(
        nodePat(Some("m")),
        RelationshipPattern(None, None, None, None, None, OUTGOING)(InputPosition(16, 1, 17)),
        nodePat(None)
      )(InputPosition(16, 1, 17)),
      None
    )(InputPosition(5, 1, 6), Set.empty)

    givesIncludingPositions {
      query(
        with_(AliasedReturnItem(countExpression, Variable("result")(pos))(pos, isAutoAliased = false)),
        return_(UnaliasedReturnItem(Variable("result")(pos), "result")(pos))
      )
    }
  }

  test("MATCH (a) WHERE COUNT{(a: Label)} > 1 RETURN a") {
    val countExpression: CountExpression = CountExpression(
      nodePat(Some("a"), Some(LabelExpression.Leaf(LabelName("Label")(InputPosition(26, 1, 27))))),
      None
    )(InputPosition(16, 1, 17), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("a")), Some(where(gt(countExpression, literal(1))))),
        return_(variableReturnItem("a"))
      )
    }
  }

  test("MATCH (a) RETURN COUNT{ MATCH (a) }") {
    val countExpression: CountExpression = CountExpression(
      nodePat(Some("a"), None),
      None
    )(InputPosition(17, 1, 18), Set.empty)

    givesIncludingPositions {
      query(
        match_(nodePat(name = Some("a")), None),
        return_(returnItem(countExpression, "COUNT{ MATCH (a) }"))
      )
    }
  }

  test(
    """MATCH (a), (b)
      |WHERE COUNT{(a)-[:FOLLOWS]->(b), (a)<-[:FOLLOWS]-(b)} > 0
      |RETURN a, b
      |""".stripMargin
  ) {
    failsToParse
  }

  test(
    """MATCH (a) WHERE COUNT{pt = (a)-[]->(b)} > 1 RETURN a""".stripMargin
  ) {
    failsToParse
  }
}
