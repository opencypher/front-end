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

import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.scalatest.Args
import org.scalatest.Assertions
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuiteLike
import org.scalatest.Matchers
import org.scalatest.Status
import org.scalatest.Suite
import org.scalatestplus.junit.JUnitRunner
import org.scalatestplus.mockito.MockitoSugar

import scala.reflect.Manifest

@RunWith(classOf[JUnitRunner])
abstract class CypherFunSuite
    extends Suite
    with Assertions
    with CypherTestSupport
    with MockitoSugar
    with FunSuiteLike
    with Matchers
    with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    initTest()
  }

  override protected def afterEach(): Unit = {
    stopTest()
  }

  def argCaptor[T <: AnyRef](implicit manifest: Manifest[T]): ArgumentCaptor[T] = {
    ArgumentCaptor.forClass(manifest.runtimeClass.asInstanceOf[Class[T]])
  }

  protected def normalizeNewLines(string: String) = {
    string.replace("\r\n", "\n")
  }
}

trait TestName extends Suite {
  final def testName = __testName.get

  private var __testName: Option[String] = None

  override protected def runTest(testName: String, args: Args): Status = {
    __testName = Some(testName)
    try {
      super.runTest(testName, args)
    } finally {
      __testName = None
    }
  }
}
