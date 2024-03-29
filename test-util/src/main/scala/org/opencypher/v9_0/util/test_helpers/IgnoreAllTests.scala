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
package org.opencypher.v9_0.util.test_helpers

import org.scalactic.source.Position
import org.scalatest.Tag

trait IgnoreAllTests extends CypherFunSuite {

  def ignoranceRationale = ""

  override protected def test(testName: String, testTags: Tag*)(testFun: => Any)(implicit pos: Position): Unit = {
    val ignoredTestName =
      if (ignoranceRationale.isEmpty) testName else s"testName [$ignoranceRationale]"
    ignore(ignoredTestName, testTags: _*)(testFun)
  }

  protected def testIgnored(testName: String, testTags: Tag*)(testFun: => Unit): Unit = {
    super.test(testName, testTags: _*)(testFun)
  }

}
