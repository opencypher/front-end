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
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer.NO_TRACING
import org.opencypher.v9_0.util.AnonymousVariableNameGenerator
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.opencypher.v9_0.util.devNullLogger
import org.scalatest.mockito.MockitoSugar

object ContextHelper extends MockitoSugar {
  def create(): BaseContext = {
    new BaseContext {
      override def tracer: CompilationPhaseTracer = NO_TRACING

      override def notificationLogger: InternalNotificationLogger = devNullLogger

      override def cypherExceptionFactory: CypherExceptionFactory = OpenCypherExceptionFactory(None)

      override def monitors: Monitors = mock[Monitors]

      override def errorHandler: Seq[SemanticErrorDef] => Unit =
        (errors: Seq[SemanticErrorDef]) => errors.foreach(e => throw cypherExceptionFactory.syntaxException(e.msg, e.position))

      override def anonymousVariableNameGenerator: AnonymousVariableNameGenerator = new AnonymousVariableNameGenerator()
    }
  }
}
