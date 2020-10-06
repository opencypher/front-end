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
package org.opencypher.v9_0.rewriting.rewriters

import org.opencypher.v9_0.ast.ReturnItems
import org.opencypher.v9_0.ast.ShowCurrentUser
import org.opencypher.v9_0.ast.ShowDatabase
import org.opencypher.v9_0.ast.ShowPrivileges
import org.opencypher.v9_0.ast.ShowRoles
import org.opencypher.v9_0.ast.ShowUsers
import org.opencypher.v9_0.ast.Where
import org.opencypher.v9_0.ast.Yield
import org.opencypher.v9_0.util.Rewriter
import org.opencypher.v9_0.util.bottomUp

case object expandShowWhere extends Rewriter {

    private val instance = bottomUp(Rewriter.lift {
      case s @ ShowDatabase(_, Some(Right(where)),_) => s.copy(yieldOrWhere = Some(Left((whereToYield(where), None))))(s.position)
      case s @ ShowRoles(_, _, Some(Right(where)), _) => s.copy(yieldOrWhere = Some(Left((whereToYield(where), None))))(s.position)
      case s @ ShowPrivileges(_, Some(Right(where)),_) => s.copy(yieldOrWhere = Some(Left((whereToYield(where), None))))(s.position)
      case s @ ShowUsers(Some(Right(where)),_) => s.copy(yieldOrWhere = Some(Left((whereToYield(where), None))))(s.position)
      case s @ ShowCurrentUser(Some(Right(where)),_) => s.copy(yieldOrWhere = Some(Left((whereToYield(where), None))))(s.position)
    })

    private def whereToYield(where: Where): Yield =
      Yield(ReturnItems(includeExisting = true, Seq.empty)(where.position), None, None, None, Some(where))(where.position)

  override def apply(v: AnyRef): AnyRef =
      instance(v)
  }
