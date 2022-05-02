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

import org.opencypher.v9_0.ast
import org.opencypher.v9_0.ast.AstConstructionTestSupport
import org.opencypher.v9_0.expressions.Expression
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PatternComprehension
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.RelationshipsPattern
import org.opencypher.v9_0.expressions.SemanticDirection.OUTGOING
import org.opencypher.v9_0.util.symbols.CTAny
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

/**
 * Label expression in Node patterns
 */
class NodeLabelExpressionsParserTest extends CypherFunSuite with JavaccParserAstTestBase[NodePattern]
    with AstConstructionTestSupport {

  implicit val parser: JavaccRule[NodePattern] = JavaccRule.NodePattern

  test("(n)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(labelAtom("A", (1, 4, 3))),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n:A $param)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(labelAtom("A", (1, 4, 3))),
        properties = Some(parameter("param", CTAny, (1, 6, 5))),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A&B)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelConjunction(
            labelAtom("A", (1, 4, 3)),
            labelAtom("B")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A&B|C)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelDisjunction(
            labelConjunction(
              labelAtom("A", (1, 4, 3)),
              labelAtom("B")
            ),
            labelAtom("C")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A|B&C)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelDisjunction(
            labelAtom("A", (1, 4, 3)),
            labelConjunction(
              labelAtom("B"),
              labelAtom("C")
            )
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:!(A))") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelNegation(
            labelAtom("A")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(:A&B)") {
    givesIncludingPositions {
      nodePat(
        labelExpression = Some(
          labelConjunction(
            labelAtom("A", (1, 3, 2)),
            labelAtom("B", (1, 5, 4))
          )
        ),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A|B)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelDisjunction(
            labelAtom("A", (1, 4, 3)),
            labelAtom("B")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:!A)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression =
          Some(
            labelNegation(
              labelAtom("A")
            )
          ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:A&B&C)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelConjunction(
            labelConjunction(
              labelAtom("A", (1, 4, 3)),
              labelAtom("B", (1, 6, 5)),
              (1, 5, 4)
            ),
            labelAtom("C", (1, 8, 7)),
            (1, 7, 6)
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:!A&B)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelConjunction(
            labelNegation(labelAtom("A")),
            labelAtom("B")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n:A&(B&C))") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelConjunction(
            labelAtom("A", (1, 4, 3)),
            labelConjunction(
              labelAtom("B", (1, 7, 6)),
              labelAtom("C", (1, 9, 8)),
              (1, 8, 7)
            ),
            (1, 5, 4)
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  test("(n:!(A&B))") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelNegation(
            labelConjunction(
              labelAtom("A"),
              labelAtom("B")
            )
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n:(A&B)|C)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelDisjunction(
            labelConjunction(
              labelAtom("A", (1, 5, 4)),
              labelAtom("B")
            ),
            labelAtom("C")
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n:%)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(labelWildcard((1, 4, 3))),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n:!%&%)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = Some(
          labelConjunction(
            labelNegation(labelWildcard((1, 5, 4)), (1, 4, 3)),
            labelWildcard((1, 7, 6)),
            (1, 6, 5)
          )
        ),
        namePos = (1, 2, 1),
        position = (1, 1, 0)
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("(n WHERE n:A&B)") {
    givesIncludingPositions {
      nodePat(
        name = Some("n"),
        labelExpression = None,
        predicates = Some(labelExpressionPredicate(
          "n",
          labelConjunction(
            labelAtom("A", (1, 12, 11)),
            labelAtom("B", (1, 14, 13))
          )
        ))
      )
    }
  }
}

class MatchNodeLabelExpressionsParserTest extends CypherFunSuite with JavaccParserAstTestBase[ast.Clause]
    with AstConstructionTestSupport {

  implicit val parser: JavaccRule[ast.Clause] = JavaccRule.Clause

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("MATCH (n) WHERE n:A&B") {
    givesIncludingPositions {
      match_(
        nodePat(name = Some("n")),
        Some(where(
          labelExpressionPredicate(
            "n",
            labelConjunction(
              labelAtom("A", (1, 19, 18)),
              labelAtom("B", (1, 21, 20))
            )
          )
        ))
      )
    }
  }

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("MATCH (n:A|B) WHERE n:A&C") {
    givesIncludingPositions {
      match_(
        nodePat(
          name = Some("n"),
          labelExpression = Some(
            labelDisjunction(
              labelAtom("A", (1, 10, 9)),
              labelAtom("B", (1, 12, 11))
            )
          ),
          namePos = (1, 8, 7)
        ),
        Some(where(
          labelExpressionPredicate(
            "n",
            labelConjunction(
              labelAtom("A", (1, 23, 22)),
              labelAtom("C", (1, 25, 24)),
              (1, 24, 23)
            )
          )
        ))
      )
    }
  }

  test("MATCH (n) WHERE n:A") {
    givesIncludingPositions {
      match_(
        nodePat(name = Some("n")),
        Some(
          where(
            labelExpressionPredicate("n", labelAtom("A"))
          )
        )
      )
    }
  }

}

class ExpressionLabelExpressionsParserTest extends CypherFunSuite with JavaccParserAstTestBase[Expression]
    with AstConstructionTestSupport {
  implicit val parser: JavaccRule[Expression] = JavaccRule.Expression

  //              000000000111111111122222222223333333333
  //              123456789012345678901234567890123456789
  test("[(a)-->(b:A|B) | b.prop]") {
    givesIncludingPositions {
      PatternComprehension(
        namedPath = None,
        pattern = RelationshipsPattern(
          RelationshipChain(
            nodePat(Some("a")),
            RelationshipPattern(None, List(), None, None, None, OUTGOING)((1, 5, 4)),
            nodePat(
              Some("b"),
              Some(labelDisjunction(
                labelAtom("A", (1, 11, 10)),
                labelAtom("B", (1, 13, 12)),
                (1, 12, 11)
              ))
            )
          )((1, 2, 1))
        )((1, 2, 1)),
        predicate = None,
        projection = prop("b", "prop")
      )((1, 1, 0), Set.empty, "  UNNAMED0", "  UNNAMED1")
    }
  }

  test("[x IN [1,2,3] WHERE n:A | x]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(labelExpressionPredicate(varFor("n", position = (1, 21, 20)), labelAtom("A", (1, 23, 22)))),
        Some(varFor("x"))
      )
    }
  }

  test("[x IN [1,2,3] WHERE n:(A | x)]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(
          labelExpressionPredicate(
            varFor("n", position = (1, 21, 20)),
            labelDisjunction(
              labelAtom("A", (1, 24, 23)),
              labelAtom("x", (1, 28, 27)),
              (1, 26, 25)
            )
          )
        ),
        None
      )
    }
  }

  test("[x IN [1,2,3] WHERE n:A&x]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(
          labelExpressionPredicate(
            varFor("n", position = (1, 21, 20)),
            labelConjunction(
              labelAtom("A", (1, 23, 22)),
              labelAtom("x", (1, 25, 24))
            )
          )
        ),
        None
      )
    }
  }

  test("[x IN [1,2,3] WHERE n:A & (b | x)]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(labelExpressionPredicate(
          varFor("n", position = (1, 21, 20)),
          labelConjunction(
            labelAtom("A", (1, 23, 22)),
            labelDisjunction(
              labelAtom("b", (1, 28, 27)),
              labelAtom("x", (1, 32, 31)),
              (1, 30, 29)
            )
          )
        )),
        None
      )
    }
  }

  test("[x IN [1,2,3] WHERE n:A | (b | x)]") {
    failsToParse
  }

  test("[x IN [1,2,3] WHERE n:(A | x) | x]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(
          labelExpressionPredicate(
            varFor("n", position = (1, 21, 20)),
            labelDisjunction(
              labelAtom("A", (1, 24, 23)),
              labelAtom("x", (1, 28, 27)),
              (1, 26, 25)
            )
          )
        ),
        Some(varFor("x"))
      )
    }
  }

  test("[x IN [1,2,3] WHERE n:A | x | x]") {
    givesIncludingPositions {
      listComprehension(
        varFor("x"),
        listOfInt(1, 2, 3),
        Some(labelExpressionPredicate(
          varFor("n", position = (1, 21, 20)),
          labelDisjunction(
            labelAtom("A", (1, 23, 22)),
            labelAtom("x", (1, 27, 26)),
            (1, 25, 24)
          )
        )),
        Some(varFor("x"))
      )
    }
  }
}
