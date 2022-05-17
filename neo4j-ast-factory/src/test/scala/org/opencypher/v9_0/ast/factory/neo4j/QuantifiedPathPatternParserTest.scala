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
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.factory.neo4j.JavaccRule.Variable
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.FixedQuantifier
import org.opencypher.v9_0.expressions.GraphPatternQuantifier
import org.opencypher.v9_0.expressions.IntervalQuantifier
import org.opencypher.v9_0.expressions.NamedPatternPart
import org.opencypher.v9_0.expressions.ParenthesizedPath
import org.opencypher.v9_0.expressions.PathConcatenation
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.PatternAtom
import org.opencypher.v9_0.expressions.PatternPart
import org.opencypher.v9_0.expressions.PlusQuantifier
import org.opencypher.v9_0.expressions.QuantifiedPath
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.SemanticDirection.BOTH
import org.opencypher.v9_0.expressions.StarQuantifier
import org.opencypher.v9_0.expressions.UnsignedDecimalIntegerLiteral
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite

class QuantifiedPathPatternParserTest extends CypherFunSuite with JavaccParserAstTestBase[PatternPart]
    with AstConstructionTestSupport {

  implicit val parser: JavaccRule[PatternPart] = JavaccRule.PatternPart

  test("(n)") {
    givesIncludingPositions {
      EveryPath(
        nodePat(name = Some("n"), position = (1, 1, 0))
      )
    }
  }

  test("(((n)))") {
    gives {
      EveryPath(ParenthesizedPath(EveryPath(ParenthesizedPath(EveryPath(
        nodePat(Some("n"))
      ))(pos)))(pos))
    }
  }

  test("((n)-[r]->(m))*") {
    givesIncludingPositions {
      EveryPath(QuantifiedPath(
        EveryPath(
          RelationshipChain(
            nodePat(Some("n"), position = (1, 2, 1)),
            relPat(Some("r"), direction = SemanticDirection.OUTGOING, position = (1, 5, 4)),
            nodePat(Some("m"), position = (1, 11, 10))
          )((1, 2, 1))
        ),
        StarQuantifier()((1, 15, 14))
      )((1, 1, 0)))
    }
  }

  test("(p = (n)-[r]->(m))*") {
    gives {
      EveryPath(QuantifiedPath(
        NamedPatternPart(
          Variable("p"),
          EveryPath(
            RelationshipChain(
              nodePat(Some("n")),
              relPat(Some("r"), direction = SemanticDirection.OUTGOING),
              nodePat(Some("m"))
            )(pos)
          )
        )(pos),
        StarQuantifier()(pos)
      )(pos))
    }
  }

  test("(a) ((n)-[r]->(m))*") {
    givesIncludingPositions {
      EveryPath(
        PathConcatenation(Seq(
          nodePat(name = Some("a"), position = (1, 1, 0)),
          QuantifiedPath(
            EveryPath(
              RelationshipChain(
                nodePat(Some("n"), position = (1, 6, 5)),
                relPat(Some("r"), direction = SemanticDirection.OUTGOING, position = (1, 9, 8)),
                nodePat(Some("m"), position = (1, 15, 14))
              )((1, 6, 5))
            ),
            StarQuantifier()((1, 19, 18))
          )((1, 5, 4))
        ))((1, 1, 0))
      )
    }
  }

  test("((n)-[r]->(m))* (b)") {
    givesIncludingPositions {
      EveryPath(
        PathConcatenation(Seq(
          QuantifiedPath(
            EveryPath(
              RelationshipChain(
                nodePat(Some("n"), position = (1, 2, 1)),
                relPat(Some("r"), direction = SemanticDirection.OUTGOING, position = (1, 5, 4)),
                nodePat(Some("m"), position = (1, 11, 10))
              )((1, 2, 1))
            ),
            StarQuantifier()((1, 15, 14))
          )((1, 1, 0)),
          nodePat(name = Some("b"), position = (1, 17, 16))
        ))((1, 1, 0))
      )
    }
  }

  test(
    """(a) (p = (n)-[r]->(m)){1,3} (b)""".stripMargin
  ) {
    gives {
      EveryPath(
        PathConcatenation(Seq(
          nodePat(name = Some("a")),
          QuantifiedPath(
            NamedPatternPart(
              Variable("p"),
              EveryPath(
                RelationshipChain(
                  nodePat(Some("n")),
                  relPat(Some("r"), direction = SemanticDirection.OUTGOING),
                  nodePat(Some("m"))
                )(pos)
              )
            )(pos),
            IntervalQuantifier(
              Some(UnsignedDecimalIntegerLiteral("1")(pos)),
              Some(UnsignedDecimalIntegerLiteral("3")(pos))
            )(
              pos
            )
          )(pos),
          nodePat(name = Some("b"))
        ))(pos)
      )
    }
  }

  // we allow arbitrary juxtaposition in the parser and only disallow it in semantic analysis
  test("(a) ((n)-[r]->(m))* (b) (c) ((p)-[q]->(s))+") {
    givesIncludingPositions {
      EveryPath(
        PathConcatenation(Seq(
          nodePat(name = Some("a"), position = (1, 1, 0)),
          QuantifiedPath(
            EveryPath(
              RelationshipChain(
                nodePat(Some("n"), position = (1, 6, 5)),
                relPat(Some("r"), direction = SemanticDirection.OUTGOING, position = (1, 9, 8)),
                nodePat(Some("m"), position = (1, 15, 14))
              )((1, 6, 5))
            ),
            StarQuantifier()((1, 19, 18))
          )((1, 5, 4)),
          nodePat(name = Some("b"), position = (1, 21, 20)),
          nodePat(name = Some("c"), position = (1, 25, 24)),
          QuantifiedPath(
            EveryPath(
              RelationshipChain(
                nodePat(Some("p"), position = (1, 30, 29)),
                relPat(Some("q"), direction = SemanticDirection.OUTGOING, position = (1, 33, 32)),
                nodePat(Some("s"), position = (1, 39, 38))
              )((1, 30, 29))
            ),
            PlusQuantifier()((1, 43, 42))
          )((1, 29, 28))
        ))((1, 1, 0))
      )
    }
  }

  test("p= ( (a)-->(b) )") {
    gives {
      NamedPatternPart(
        varFor("p"),
        EveryPath(ParenthesizedPath(EveryPath(RelationshipChain(
          nodePat(Some("a")),
          relPat(),
          nodePat(Some("b"))
        )(pos)))(pos))
      )(pos)
    }
  }

  // We parse this and fail later in semantic checking
  test("(p = (q = (n)-[r]->(m))*)*") {
    gives {
      EveryPath(
        QuantifiedPath(
          NamedPatternPart(
            varFor("p"),
            EveryPath(QuantifiedPath(
              NamedPatternPart(
                varFor("q"),
                EveryPath(RelationshipChain(
                  nodePat(Some("n")),
                  relPat(Some("r")),
                  nodePat(Some("m"))
                )(pos))
              )(pos),
              StarQuantifier()(pos)
            )(pos))
          )(pos),
          StarQuantifier()(pos)
        )(pos)
      )
    }
  }

  // We parse this and fail later in semantic checking
  test("p = (n) (q = (a)-[]->(b))") {
    gives {
      NamedPatternPart(
        varFor("p"),
        EveryPath(PathConcatenation(Seq(
          nodePat(Some("n")),
          ParenthesizedPath(NamedPatternPart(
            varFor("q"),
            EveryPath(RelationshipChain(nodePat(Some("a")), relPat(), nodePat(Some("b")))(pos))
          )(pos))(pos)
        ))(pos))
      )(pos)
    }
  }

  test("((a)-->(b)) ((x)-->(y))*") {
    gives {
      EveryPath(PathConcatenation(Seq(
        ParenthesizedPath(EveryPath(RelationshipChain(nodePat(Some("a")), relPat(), nodePat(Some("b")))(pos)))(pos),
        QuantifiedPath(
          EveryPath(RelationshipChain(nodePat(Some("x")), relPat(), nodePat(Some("y")))(pos)),
          StarQuantifier()(pos)
        )(pos)
      ))(pos))
    }
  }
}

class QuantifiedPathPatternInMatchParserTest extends CypherFunSuite with JavaccParserAstTestBase[ast.Clause]
    with AstConstructionTestSupport {

  implicit val parser: JavaccRule[ast.Clause] = JavaccRule.Clause

  test("MATCH p= ( (a)-->(b) ) WHERE a.prop") {
    gives {
      Match(
        optional = false,
        Pattern(Seq(NamedPatternPart(
          varFor("p"),
          EveryPath(ParenthesizedPath(EveryPath(RelationshipChain(
            nodePat(Some("a")),
            relPat(),
            nodePat(Some("b"))
          )(pos)))(pos))
        )(pos)))(pos),
        hints = Seq.empty,
        where = Some(where(prop("a", "prop")))
      )(pos)
    }
  }

  test("MATCH (a), (b)--(c) ((d)--(e))* (f)") {
    gives {
      Match(
        optional = false,
        Pattern(Seq(
          EveryPath(nodePat(Some("a"))),
          EveryPath(PathConcatenation(Seq(
            RelationshipChain(
              nodePat(Some("b")),
              relPat(direction = BOTH),
              nodePat(Some("c"))
            )(pos),
            QuantifiedPath(
              EveryPath(RelationshipChain(
                nodePat(Some("d")),
                relPat(direction = BOTH),
                nodePat(Some("e"))
              )(pos)),
              StarQuantifier()(pos)
            )(pos),
            nodePat(Some("f"))
          ))(pos))
        ))(pos),
        hints = Seq.empty,
        where = None
      )(pos)
    }
  }

  // quantified relationships are not implemented, yet
  test("MATCH (a)-->+(c)") {
    failsToParse
  }

  // quantified relationships are not implemented, yet
  ignore("MATCH (a)-->+(b)") {
    gives {
      Match(
        optional = false,
        Pattern(Seq(
          EveryPath(nodePat(Some("a"))),
          EveryPath(PathConcatenation(Seq(
            RelationshipChain(
              nodePat(Some("b")),
              relPat(direction = BOTH),
              nodePat(Some("c"))
            )(pos),
            QuantifiedPath(
              EveryPath(RelationshipChain(
                nodePat(Some("d")),
                relPat(direction = BOTH),
                nodePat(Some("e"))
              )(pos)),
              StarQuantifier()(pos)
            )(pos),
            nodePat(Some("f"))
          ))(pos))
        ))(pos),
        hints = Seq.empty,
        where = None
      )(pos)
    }
  }

  // pattern expressions are not implemented, yet
  test("MATCH (n) WITH [ p = (n)--(m) ((a)-->(b))+ | p ] as paths") {
    failsToParse
  }

  // pattern expression are not implemented, yet
  test("MATCH (n), (m) WHERE (n) ((a)-->(b))+ (m)") {
    failsToParse
  }

  // node abbreviations are not implemented, yet
  test("MATCH (n)--((a)-->(b))+") {
    failsToParse
  }
}

class QuantifiedPathParserTest extends CypherFunSuite
    with JavaccParserAstTestBase[PatternAtom]
    with AstConstructionTestSupport {
  implicit val parser: JavaccRule[PatternAtom] = JavaccRule.ParenthesizedPath

  test("((n)-[r]->(m))*") {
    gives {
      QuantifiedPath(
        EveryPath(
          RelationshipChain(
            nodePat(Some("n")),
            relPat(Some("r"), direction = SemanticDirection.OUTGOING),
            nodePat(Some("m"))
          )(pos)
        ),
        StarQuantifier()(pos)
      )(pos)
    }
  }

  test("(p = (n)-[r]->(m))*") {
    gives {
      QuantifiedPath(
        NamedPatternPart(
          Variable("p"),
          EveryPath(
            RelationshipChain(
              nodePat(Some("n")),
              relPat(Some("r"), direction = SemanticDirection.OUTGOING),
              nodePat(Some("m"))
            )(pos)
          )
        )(pos),
        StarQuantifier()(pos)
      )(pos)
    }
  }

  ignore("((n)-[r]->(m) WHERE n.prop = m.prop)*") {
    gives {
      QuantifiedPath(
        NamedPatternPart(
          Variable("p"),
          EveryPath(
            RelationshipChain(
              nodePat(Some("n")),
              relPat(Some("r"), direction = SemanticDirection.OUTGOING),
              nodePat(Some("m"))
            )(pos)
          )
        )(pos),
        StarQuantifier()(pos)
      )(pos)
    }
  }

  // combining all previous GPM features
  test("((n:A|B)-[r]->(m:% WHERE m.prop IS NOT NULL))*") {
    gives {
      QuantifiedPath(
        EveryPath(
          RelationshipChain(
            nodePat(
              name = Some("n"),
              labelExpression = Some(labelDisjunction(labelLeaf("A"), labelLeaf("B")))
            ),
            relPat(Some("r")),
            nodePat(
              name = Some("m"),
              labelExpression = Some(labelWildcard()),
              predicates = Some(isNotNull(prop("m", "prop")))
            )
          )(pos)
        ),
        StarQuantifier()(pos)
      )(pos)
    }
  }

  // we currently do not support path pattern predicates
  test("((a)-->(b) WHERE a.prop > b.prop)+") {
    failsToParse
  }

  // we currently do not support path pattern predicates
  test("((a)-->(b) WHERE a.prop > b.prop)") {
    failsToParse
  }
}

class QuantifiedPathPatternsQuantifierParserTest extends CypherFunSuite
    with JavaccParserAstTestBase[GraphPatternQuantifier]
    with AstConstructionTestSupport {
  implicit val parser: JavaccRule[GraphPatternQuantifier] = JavaccRule.Quantifier

  test("+") {
    givesIncludingPositions {
      PlusQuantifier()((1, 1, 0))
    }
  }

  test("*") {
    givesIncludingPositions {
      StarQuantifier()((1, 1, 0))
    }
  }

  test("{0,3}") {
    givesIncludingPositions {
      IntervalQuantifier(Some(literalUnsignedInt(0)), Some(literalUnsignedInt(3)))((1, 1, 0))
    }
  }

  test("{1,}") {
    givesIncludingPositions {
      IntervalQuantifier(Some(literalUnsignedInt(1)), None)((1, 1, 0))
    }
  }

  test("{,3}") {
    givesIncludingPositions {
      IntervalQuantifier(None, Some(literalUnsignedInt(3)))((1, 1, 0))
    }
  }

  test("{,}") {
    givesIncludingPositions {
      IntervalQuantifier(None, None)((1, 1, 0))
    }
  }

  test("{2}") {
    givesIncludingPositions {
      FixedQuantifier(literalUnsignedInt(2))((1, 1, 0))
    }
  }

  test("{1_000, 1_000_000}") {
    givesIncludingPositions {
      IntervalQuantifier(
        Some(UnsignedDecimalIntegerLiteral("1_000")((1, 2, 1))),
        Some(UnsignedDecimalIntegerLiteral("1_000_000")((1, 9, 8)))
      )((1, 1, 0))
    }
  }
}
