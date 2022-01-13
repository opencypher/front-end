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
package org.opencypher.v9_0.frontend.phases

import org.opencypher.v9_0.ast.semantics.SemanticErrorDef
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.ErrorMessageProvider
import org.opencypher.v9_0.util.InternalNotificationLogger

trait BaseContext {
  def tracer: CompilationPhaseTracer
  def notificationLogger: InternalNotificationLogger
  def cypherExceptionFactory: CypherExceptionFactory
  def monitors: Monitors
  def errorHandler: Seq[SemanticErrorDef] => Unit

  def errorMessageProvider: ErrorMessageProvider
}
