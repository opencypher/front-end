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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.Return
import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.ShowAliases
import org.opencypher.v9_0.ast.ShowCurrentUser
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.ShowPrivilegeCommands
import org.opencypher.v9_0.ast.ShowPrivileges
import org.opencypher.v9_0.ast.ShowRoles
import org.opencypher.v9_0.ast.ShowUsers
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.ast.YieldOrWhere
import org.opencypher.v9_0.rewriting.rewriters.factories.PreparatoryRewritingRewriterFactory
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.StepSequencer
import org.opencypher.v9_0.util.StepSequencer.Condition
import org.opencypher.v9_0.util.StepSequencer.Step
import org.opencypher.v9_0.util.bottomUp

case object WithBetweenShowAndWhereInserted extends Condition

// rewrites SHOW ... WHERE <e> " ==> SHOW ... YIELD * WHERE <e>
case object expandShowWhere extends Rewriter with Step with PreparatoryRewritingRewriterFactory {

  override def preConditions: Set[StepSequencer.Condition] = Set.empty

  override def postConditions: Set[StepSequencer.Condition] = Set(WithBetweenShowAndWhereInserted)

  override def invalidatedConditions: Set[StepSequencer.Condition] = Set.empty

  override def apply(v: AnyRef): AnyRef =
    instance(v)

  private val instance = bottomUp(Rewriter.lift {
    // move freestanding WHERE to YIELD * WHERE and add default columns to the YIELD
    case s @ ShowDatabase(_, Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowRoles(_, _, Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowPrivileges(_, Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowPrivilegeCommands(_, _, Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowUsers(Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowCurrentUser(Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)
    case s @ ShowAliases(Some(Right(where)), _) =>
      s.copy(yieldOrWhere = Some(Left((whereToYield(where, s.defaultColumnNames), None))))(s.position)

    // add default columns to explicit YIELD/RETURN * as well
    case s @ ShowDatabase(_, Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowRoles(_, _, Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowPrivileges(_, Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowPrivilegeCommands(_, _, Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowUsers(Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowCurrentUser(Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
    case s @ ShowAliases(Some(Left((yieldClause, returnClause))), _)
      if yieldClause.returnItems.includeExisting || returnClause.exists(_.returnItems.includeExisting) =>
      s.copy(yieldOrWhere = addDefaultColumns(yieldClause, returnClause, s.defaultColumnNames))(s.position)
  })

  private def whereToYield(where: Where, defaultColumns: List[String]): Yield =
    Yield(
      ReturnItems(includeExisting = true, Seq.empty, Some(defaultColumns))(where.position),
      None,
      None,
      None,
      Some(where)
    )(where.position)

  private def addDefaultColumns(
    yieldClause: Yield,
    maybeReturn: Option[Return],
    defaultColumns: List[String]
  ): YieldOrWhere = {
    // Update yield clause with default columns if includeExisting
    val (newYield, yieldColumns) =
      if (yieldClause.returnItems.includeExisting) {
        val yieldColumns = yieldClause.returnItems.defaultOrderOnColumns.getOrElse(defaultColumns)
        val newYield = yieldClause.withReturnItems(yieldClause.returnItems.withDefaultOrderOnColumns(yieldColumns))
        (newYield, yieldColumns)
      } else (yieldClause, yieldClause.returnItems.items.map(_.name).toList)

    // Update the return clause with default columns if includeExisting,
    // using the columns from the yield clause (either the default or the explicitly yielded ones)
    // Example: `... YIELD a, b, c ... RETURN *` will add List(a, b, c) to the default columns on the return
    val newReturn = maybeReturn.map(returnClause =>
      if (returnClause.returnItems.includeExisting) {
        val returnColumns = returnClause.returnItems.defaultOrderOnColumns.getOrElse(yieldColumns)
        returnClause.withReturnItems(returnClause.returnItems.withDefaultOrderOnColumns(returnColumns))
      } else returnClause
    )

    Some(Left(newYield -> newReturn))
  }

  override def getRewriter(
    cypherExceptionFactory: CypherExceptionFactory,
    notificationLogger: InternalNotificationLogger
  ): Rewriter = instance
}
