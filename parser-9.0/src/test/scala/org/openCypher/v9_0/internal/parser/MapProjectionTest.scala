/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
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
package org.openCypher.v9_0.internal.parser

import org.openCypher.v9_0.internal.expressions.SignedDecimalIntegerLiteral
import org.openCypher.v9_0.internal.util.DummyPosition
import org.openCypher.v9_0.internal.{expressions => exp}


class MapProjectionTest extends ParserTest[Any, Any] with Expressions {

  val t = DummyPosition(0)

  test("testIdentifierCanContainASCII") {
    implicit val parserToTest = MapProjection

    parsing("abc{}") shouldGive exp.MapProjection(exp.Variable("abc")(t), Seq.empty)(t)

    parsing("abc{.id}") shouldGive
      exp.MapProjection(exp.Variable("abc")(t),
        Seq(exp.PropertySelector(exp.Variable("id")(t))(t)))(t)

    parsing("abc{id}") shouldGive
      exp.MapProjection(exp.Variable("abc")(t),
        Seq(exp.VariableSelector(exp.Variable("id")(t))(t)))(t)

    parsing("abc { id : 42 }") shouldGive
      exp.MapProjection(exp.Variable("abc")(t),
        Seq(exp.LiteralEntry(exp.PropertyKeyName("id")(t), SignedDecimalIntegerLiteral("42")(t))(t)))(t)

    parsing("abc { `a p a` : 42 }") shouldGive
      exp.MapProjection(exp.Variable("abc")(t),
        Seq(exp.LiteralEntry(exp.PropertyKeyName("a p a")(t), SignedDecimalIntegerLiteral("42")(t))(t)))(t)

    parsing("abc { id : 42, .foo, bar }") shouldGive
      exp.MapProjection(exp.Variable("abc")(t),
        Seq(
          exp.LiteralEntry(exp.PropertyKeyName("id")(t), SignedDecimalIntegerLiteral("42")(t))(t),
          exp.PropertySelector(exp.Variable("foo")(t))(t),
          exp.VariableSelector(exp.Variable("bar")(t))(t)
        )
      )(t)
  }

  def convert(result: Any): Any = result
}
