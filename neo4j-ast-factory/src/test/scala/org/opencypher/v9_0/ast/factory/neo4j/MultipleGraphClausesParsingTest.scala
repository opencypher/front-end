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
import org.opencypher.v9_0.ast.Clause
import org.opencypher.v9_0.ast.GraphSelection
import org.opencypher.v9_0.expressions
import org.opencypher.v9_0.util.symbols

class MultipleGraphClausesParsingTest extends JavaccParserAstTestBase[Clause] {

  implicit private val parser: JavaccRule[Clause] = JavaccRule.Clause

  val keywords: Seq[(String, expressions.Expression => ast.GraphSelection)] = Seq(
    "USE" -> use,
    "USE GRAPH" -> use
  )

  val graphSelection: Seq[(String, expressions.Expression)] = Seq(
    "foo.bar" ->
      prop(varFor("foo"), "bar"),
    "(foo.bar)" ->
      prop(varFor("foo"), "bar"),
    "foo()" ->
      function("foo")(),
    "foo   (    )" ->
      function("foo")(),
    "graph.foo" ->
      prop(varFor("graph"), "foo"),
    "graph.foo()" ->
      function("graph", "foo")(),
    "foo.bar(baz(grok))" ->
      function("foo", "bar")(function("baz")(varFor("grok"))),
    "foo. bar   (baz  (grok   )  )" ->
      function("foo", "bar")(function("baz")(varFor("grok"))),
    "foo.bar(baz(grok), another.name)" ->
      function("foo", "bar")(function("baz")(varFor("grok")), prop(varFor("another"), "name")),
    "foo.bar(1, $par)" ->
      function("foo", "bar")(
        literalInt(1),
        parameter("par", symbols.CTAny)
      ),
    "a + b" ->
      add(varFor("a"), varFor("b")),
    "`graph`" ->
      varFor("graph"),
    "graph1" ->
      varFor("graph1"),
    "`foo.bar.baz.baz`" ->
      varFor("foo.bar.baz.baz"),
    "`foo.bar`.baz" ->
      prop(varFor("foo.bar"), "baz"),
    "foo.`bar.baz`" ->
      prop(varFor("foo"), "bar.baz"),
    "`foo.bar`.`baz.baz`" ->
      prop(varFor("foo.bar"), "baz.baz")
  )

  val fullGraphSelections: Seq[(String, ast.GraphSelection)] = Seq(
    "USE GRAPH graph()" -> use(function("graph")()),
    // Interpreted as GRAPH keyword, followed by parenthesized expression
    "USE graph(x)" -> use(varFor("x"))
  )

  val combinations: Seq[(String, GraphSelection)] = for {
    (keyword, clause) <- keywords
    (input, expectedExpression) <- graphSelection
  } yield s"$keyword $input" -> clause(expectedExpression)

  for {
    (input, expected) <- combinations ++ fullGraphSelections
  } {
    test(input) {
      gives(expected)
    }
  }

  private def function(nameParts: String*)(args: expressions.Expression*) =
    expressions.FunctionInvocation(
      expressions.Namespace(nameParts.init.toList)(pos),
      expressions.FunctionName(nameParts.last)(pos),
      distinct = false,
      args.toIndexedSeq
    )(pos)

}
