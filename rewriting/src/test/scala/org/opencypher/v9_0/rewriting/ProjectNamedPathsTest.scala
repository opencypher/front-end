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
package org.opencypher.v9_0.rewriting

import org.opencypher.v9_0.ast.AliasedReturnItem
import org.opencypher.v9_0.ast.AscSortItem
import org.opencypher.v9_0.ast.Match
import org.opencypher.v9_0.ast.OrderBy
import org.opencypher.v9_0.ast.Query
import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.SubQuery
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.With
import org.opencypher.v9_0.ast.semantics.SemanticState
import org.opencypher.v9_0.expressions.CountStar
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.MultiRelationshipPathStep
import org.opencypher.v9_0.expressions.NilPathStep
import org.opencypher.v9_0.expressions.NodePathStep
import org.opencypher.v9_0.expressions.NodePattern
import org.opencypher.v9_0.expressions.PathExpression
import org.opencypher.v9_0.expressions.Pattern
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.RelationshipPattern
import org.opencypher.v9_0.expressions.SemanticDirection
import org.opencypher.v9_0.expressions.SingleRelationshipPathStep
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.rewriting.rewriters.expandStar
import org.opencypher.v9_0.rewriting.rewriters.normalizeWithAndReturnClauses
import org.opencypher.v9_0.rewriting.rewriters.projectNamedPaths
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.devNullLogger
import org.opencypher.v9_0.util.inSequence
import org.opencypher.v9_0.util.test_helpers.CypherFunSuite
import org.opencypher.v9_0.util.test_helpers.TestName

class ProjectNamedPathsTest extends CypherFunSuite with AstRewritingTestSupport with TestName {

  private def projectionInlinedAst(queryText: String) = ast(queryText).endoRewrite(projectNamedPaths)

  private def ast(queryText: String) = {
    val parsed = parser.parse(queryText, OpenCypherExceptionFactory(None))
    val exceptionFactory = OpenCypherExceptionFactory(Some(pos))
    val normalized = parsed.endoRewrite(inSequence(normalizeWithAndReturnClauses(exceptionFactory, devNullLogger)))
    val checkResult = normalized.semanticCheck(SemanticState.clean)
    normalized.endoRewrite(inSequence(expandStar(checkResult.state)))
  }

  private def parseReturnedExpr(queryText: String) = {
    val query = projectionInlinedAst(queryText).asInstanceOf[Query]
    query.part.asInstanceOf[SingleQuery].clauses.last.asInstanceOf[Return].returnItems.items.collectFirst {
      case AliasedReturnItem(expr, Variable("p")) => expr
    }.get
  }

  test("MATCH p = (a) RETURN p" ) {
    val returns = parseReturnedExpr("MATCH p = (a) RETURN p")

    val expected = PathExpression(
      NodePathStep(varFor("a"), NilPathStep)
    )_

    returns should equal(expected: PathExpression)
  }

  test("MATCH p = (a) CALL {RETURN 1} RETURN p" ) {
      val returns = parseReturnedExpr(testName)

      val expected = PathExpression(
        NodePathStep(varFor("a"), NilPathStep)
      )_

      returns should equal(expected: PathExpression)
  }

  test("CALL {MATCH p = (a) RETURN p} RETURN p" ) {
    val rewritten = projectionInlinedAst(testName)

    val a = varFor("a")
    val p = varFor("p")
    val CALL = {
      val MATCH = Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(a), List(), None)(pos))
        ))(pos), List(), None)(pos)

      val RETURN =
        Return(distinct = false,
          ReturnItems(includeExisting = false, Seq(
            AliasedReturnItem(PathExpression(NodePathStep(a, NilPathStep))(pos), p)(pos)
          ))(pos), None, None, None)(pos)

      SubQuery(SingleQuery(List(MATCH, RETURN))(pos))(pos)
    }

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(p, p)(pos)
        ))(pos), None, None, None)(pos)

    val expected: Query = Query(None, SingleQuery(List(CALL, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("MATCH p = (a) WITH p RETURN p" ) {
    val rewritten = projectionInlinedAst("MATCH p = (a) WITH p RETURN p")
    val a = varFor("a")
    val p = varFor("p")
    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(a), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val WITH =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(PathExpression(NodePathStep(a, NilPathStep))(pos), p)(pos)
        ))(pos), None, None, None, None)(pos)

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(p, p)(pos)
        ))(pos), None, None, None)(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, WITH, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  //don't project what is already projected
  test("MATCH p = (a) WITH p, a RETURN p" ) {
    val rewritten = projectionInlinedAst("MATCH p = (a) WITH p, a RETURN p")
    val a = varFor("a")
    val p = varFor("p")
    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(a), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val WITH =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(PathExpression(NodePathStep(a, NilPathStep))(pos), p)(pos),
          AliasedReturnItem(a, a)(pos)
        ))(pos), None, None, None, None)(pos)

    val RETURN=
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(p, p)(pos)
        ))(pos), None, None, None)(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, WITH, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("MATCH p = (a) WITH p MATCH q = (b) RETURN p, q" ) {
    val rewritten = projectionInlinedAst("MATCH p = (a) WITH p MATCH q = (b) WITH p, q RETURN p, q")
    val a = varFor("a")
    val b = varFor("b")
    val p = varFor("p")
    val q = varFor("q")

    val MATCH1 =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(a), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val WITH1 =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(PathExpression(NodePathStep(a, NilPathStep))(pos), p)(pos)
        ))(pos), None, None, None, None)(pos)

    val MATCH2 =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(b), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val WITH2 =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(p, p)(pos),
          AliasedReturnItem(PathExpression(NodePathStep(b, NilPathStep))(pos), q)(pos)
        ))(pos), None, None, None, None)(pos)

    val RETURN=
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(p, p)(pos),
          AliasedReturnItem(q, q)(pos)
        ))(pos), None, None, None)(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH1, WITH1, MATCH2, WITH2, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("MATCH p = (a)-[r]->(b) RETURN p" ) {
    val returns = parseReturnedExpr("MATCH p = (a)-[r]->(b) RETURN p")

    val expected = PathExpression(
      NodePathStep(varFor("a"), SingleRelationshipPathStep(varFor("r"), SemanticDirection.OUTGOING, Some(varFor("b")), NilPathStep))
    )_

    returns should equal(expected: PathExpression)
  }

  test("MATCH p = (b)<-[r]->(a) RETURN p" ) {
    val returns = parseReturnedExpr("MATCH p = (b)<-[r]-(a) RETURN p")

    val expected = PathExpression(
      NodePathStep(varFor("b"), SingleRelationshipPathStep(varFor("r"), SemanticDirection.INCOMING, Some(varFor("a")), NilPathStep))
    )_

    returns should equal(expected: PathExpression)
  }

  test("MATCH p = (a)-[r*]->(b) RETURN p" ) {
    val returns = parseReturnedExpr("MATCH p = (a)-[r*]->(b) RETURN p")

    val expected = PathExpression(
      NodePathStep(varFor("a"), MultiRelationshipPathStep(varFor("r"), SemanticDirection.OUTGOING, Some(varFor("b")), NilPathStep))
    )_

    returns should equal(expected: PathExpression)
  }

  test("MATCH p = (b)<-[r*]-(a) RETURN p AS p" ) {
    val returns = parseReturnedExpr("MATCH p = (b)<-[r*]-(a) RETURN p AS p")

    val expected = PathExpression(
      NodePathStep(varFor("b"), MultiRelationshipPathStep(varFor("r"), SemanticDirection.INCOMING, Some(varFor("a")), NilPathStep))
    )_

    returns should equal(expected: PathExpression)
  }

  test("MATCH p = (a)-[r]->(b) RETURN p, 42 as order ORDER BY order") {
    val rewritten = projectionInlinedAst("MATCH p = (a)-[r]->(b) RETURN p, 42 as order ORDER BY order")

    val aId = varFor("a")
    val orderId = varFor("order")
    val rId = varFor("r")
    val pId = varFor("p")
    val bId = varFor("b")

    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            RelationshipChain(
              NodePattern(Some(aId), List(), None)(pos),
              RelationshipPattern(Some(rId), List(), None, None, SemanticDirection.OUTGOING)(pos), NodePattern(Some(bId), List(), None)(pos)
            )(pos))
        ))(pos), List(), None)(pos)

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(PathExpression(NodePathStep(aId, SingleRelationshipPathStep(rId, SemanticDirection.OUTGOING, Some(varFor("b")), NilPathStep)))(pos), pId)(pos),
          AliasedReturnItem(literalInt(42), orderId)(pos)
        ))(pos),
        Some(OrderBy(List(AscSortItem(orderId)(pos)))(pos)),
        None, None
      )(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("MATCH p = (a)-[r]->(b) WHERE length(p) > 10 RETURN 1") {
    val rewritten = projectionInlinedAst("MATCH p = (a)-[r]->(b) WHERE length(p) > 10 RETURN 1 as x")

    val aId = varFor("a")
    val rId = varFor("r")
    val bId = varFor("b")

    val WHERE =
      Where(
        greaterThan(
          function("length", PathExpression(NodePathStep(aId, SingleRelationshipPathStep(rId, SemanticDirection.OUTGOING, Some(varFor("b")), NilPathStep)))(pos)),
          literalInt(10)
        )
      )(pos)

    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            RelationshipChain(
              NodePattern(Some(aId), List(), None)(pos),
              RelationshipPattern(Some(rId), List(), None, None, SemanticDirection.OUTGOING)(pos), NodePattern(Some(bId), List(), None)(pos)
            )(pos))
        ))(pos), List(), Some(WHERE))(pos)

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, List(
          AliasedReturnItem(literalInt(1), varFor("x"))(pos)
        ))(pos),
        None, None, None
      )(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("Aggregating WITH downstreams" ) {
    val rewritten = projectionInlinedAst("MATCH p = (a) WITH length(p) as l, count(*) as x WITH l, x RETURN l + x")
    val a = varFor("a")
    val l = varFor("l")
    val x = varFor("x")
    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(a), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val pathExpression = PathExpression(NodePathStep(a, NilPathStep))(pos)
    val WITH1 =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(function("length", pathExpression), l)(pos),
          AliasedReturnItem(CountStar()(pos), x)(pos)
        ))(pos), None, None, None, None)(pos)

    val WITH2 =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(l, l)(pos),
          AliasedReturnItem(x, x)(pos)
        ))(pos), None, None, None, None)(pos)

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(add(l, x), varFor("l + x"))(pos)
        ))(pos), None, None, None)(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, WITH1, WITH2, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }

  test("WHERE and ORDER BY on WITH clauses should be rewritten" ) {
    val rewritten = projectionInlinedAst("MATCH p = (a) WITH a ORDER BY p WHERE length(p) = 1 RETURN a")

    val aId = varFor("a")

    val MATCH =
      Match(optional = false,
        Pattern(List(
          EveryPath(
            NodePattern(Some(aId), List(), None)(pos))
        ))(pos), List(), None)(pos)

    val pathExpression = PathExpression(NodePathStep(aId, NilPathStep))(pos)

    val WHERE =
      Where(
        equals(
          function("length", pathExpression),
          literalInt(1)
        )
      )(pos)

    val WITH =
      With(distinct = false,
        ReturnItems(includeExisting = false, Seq(
          AliasedReturnItem(aId, aId)(pos)
        ))(pos),
        Some(OrderBy(List(AscSortItem(pathExpression)(pos)))(pos)),
        None, None,
        Some(WHERE)
      )(pos)

    val RETURN =
      Return(distinct = false,
        ReturnItems(includeExisting = false, List(
          AliasedReturnItem(aId, aId)(pos)
        ))(pos),
        None, None, None
      )(pos)

    val expected: Query = Query(None, SingleQuery(List(MATCH, WITH, RETURN))(pos))(pos)

    rewritten should equal(expected)
  }
}
