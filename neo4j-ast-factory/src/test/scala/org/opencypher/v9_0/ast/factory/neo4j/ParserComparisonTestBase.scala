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

import org.junit.runner.RunWith
import org.opencypher.v9_0.ast.LoadCSV
import org.opencypher.v9_0.ast.PeriodicCommitHint
import org.opencypher.v9_0.ast.ReadAdministrationCommand
import org.opencypher.v9_0.ast.RemovePropertyItem
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.SetExactPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetIncludingPropertiesFromMapItem
import org.opencypher.v9_0.ast.SetPropertyItem
import org.opencypher.v9_0.ast.SingleQuery
import org.opencypher.v9_0.ast.Statement
import org.opencypher.v9_0.ast.UseGraph
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.expressions.ContainerIndex
import org.opencypher.v9_0.expressions.EveryPath
import org.opencypher.v9_0.expressions.HasLabelsOrTypes
import org.opencypher.v9_0.expressions.ListSlice
import org.opencypher.v9_0.expressions.Property
import org.opencypher.v9_0.expressions.RelationshipChain
import org.opencypher.v9_0.expressions.Variable
import org.opencypher.v9_0.util.ASTNode
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.Foldable.SkipChildren
import org.opencypher.v9_0.util.Foldable.TraverseChildren
import org.opencypher.v9_0.util.InputPosition
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.OpenCypherExceptionFactory.SyntaxException
import org.scalatest.Assertion
import org.scalatest.Assertions
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

import scala.util.Failure
import scala.util.Success
import scala.util.Try

@RunWith(classOf[JUnitRunner])
abstract class ParserComparisonTestBase() extends Assertions with Matchers {
  private val exceptionFactory = new OpenCypherExceptionFactory(None)

  private def fixLineSeparator(message: String): String = {
    // This is needed because current version of scala seems to produce \n from multi line strings
    // This produces a problem with windows line endings \r\n
    if(message.contains(System.lineSeparator()))
      message
    else
      message.replaceAll("\n", System.lineSeparator())
  }

  /**
   * Tests that JavaCC parser produces SyntaxException.
   */
  protected def assertJavaCCException(query: String, expectedMessage: String): Assertion = {
    val exception = the[OpenCypherExceptionFactory.SyntaxException] thrownBy {
      JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    }
    exception.getMessage shouldBe fixLineSeparator(expectedMessage)
  }

  /**
   * Tests that JavaCC parser produces given exception.
   */
  protected def assertJavaCCException(query: String, expected:Exception): Assertion = {
    val exception = the[expected.type] thrownBy {
      JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    }
    exception.getMessage shouldBe fixLineSeparator(expected.getMessage)
  }

  /**
   * Tests that JavaCC parser produces SyntaxException.
   */
  protected def assertJavaCCExceptionStart(query: String, expectedMessage: String): Assertion = {
    val exception = the[OpenCypherExceptionFactory.SyntaxException] thrownBy {
      JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    }
    exception.getMessage should startWith(fixLineSeparator(expectedMessage))
  }

  /**
   * Tests that the JavaCC parser produce correct AST.
   */
  protected def assertJavaCCAST(query: String, expected: Statement, comparePosition: Boolean = true ) : Unit = {
    val ast = JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator())
    ast shouldBe expected
    if (comparePosition) {
      //change flag to true to get basic print methods for position of words
      printQueryPositions(query, printFlag = false)
      verifyPositions(ast, expected)
    }
  }

  private def printQueryPositions(query: String, printFlag: Boolean): Unit = {
    if (printFlag) {
      println(query)
      query.split("[ ,:.()\\[\\]]+").foreach(split =>
        println(s"$split: ${query.indexOf(split)+1}, ${query.indexOf(split)}"))
      println("---")
    }
  }

  /**
   * Tests that the parboiled and JavaCC parsers produce the same AST and error positions.
   */
  protected def assertSameAST(query: String, comparePosition: Boolean = true): Unit = assertSameASTForQueries(query, query, comparePosition)

  protected def assertSameASTForQueries(query: String, parBoiledQuery: String, comparePosition: Boolean = true): Unit = {
    withClue(query+System.lineSeparator()) {
      val parboiledParser = new org.opencypher.v9_0.parser.CypherParser()
      val parboiledAST = Try(parboiledParser.parse(parBoiledQuery, exceptionFactory, None))

      val javaccAST = Try(JavaCCParser.parse(query, exceptionFactory, new AnonymousVariableNameGenerator()))

      (javaccAST, parboiledAST) match {
        case (Failure(javaccEx: SyntaxException), Failure(parboiledEx: SyntaxException)) =>
          withClue(Seq(javaccEx, parboiledEx).mkString("", "\n\n", "\n\n")) {
            javaccEx.pos shouldBe parboiledEx.pos
          }
        case (Failure(javaccEx), Success(_)) =>
          throw javaccEx
        case (Success(_), Failure(parboiledEx)) =>
          throw parboiledEx
        case (Success(javaCCStatement), Success(parboiledStatement)) =>
          javaCCStatement shouldBe parboiledStatement
          if (comparePosition) {
            verifyPositions(javaCCStatement, parboiledStatement)
          }
        case (Success(_), Failure(parboiledEx)) =>
          throw parboiledEx
        case _ =>
      }
    }
  }

  def verifyPositions(javaCCAstNode: ASTNode, parboiledASTNode: ASTNode): Unit = {

    def astWithPosition(astNode: ASTNode) = {
      {
        lazy val containsReadAdministratorCommand = astNode.treeExists {
          case _: ReadAdministrationCommand => true
        }

        astNode.treeFold(Seq.empty[(ASTNode, InputPosition)]) {
          case _: Property |
               _: SetPropertyItem |
               _: RemovePropertyItem |
               _: LoadCSV |
               _: UseGraph |
               _: EveryPath |
               _: RelationshipChain |
               _: Yield |
               _: ContainerIndex |
               _: ListSlice |
               _: HasLabelsOrTypes |
               _: SingleQuery |
               _: PeriodicCommitHint |
               _: ReadAdministrationCommand |
               _: SetIncludingPropertiesFromMapItem |
               _: SetExactPropertiesFromMapItem => acc => TraverseChildren(acc)
          case returnItems: ReturnItems if returnItems.items.isEmpty => acc => SkipChildren(acc)
          case _: Variable if containsReadAdministratorCommand => acc => TraverseChildren(acc)
          case astNode: ASTNode => acc => TraverseChildren(acc :+ (astNode -> astNode.position))
          case _ => acc => TraverseChildren(acc)
        }
      }
    }

    astWithPosition(javaCCAstNode).zip(astWithPosition(parboiledASTNode))
      .foreach {
        case ((astChildNode1, pos1), (_, pos2)) => withClue(
          s"AST node $astChildNode1 was parsed with different positions (javaCC: $pos1, parboid: $pos2):")(pos1 shouldBe pos2)
        case _ => // Do nothing
      }
  }

  implicit protected def lift(pos: (Int, Int, Int)): InputPosition = InputPosition(pos._3, pos._1, pos._2)
}
