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
package org.opencypher.v9_0.frontend.helpers

import org.opencypher.v9_0.frontend.phases.BaseContext
import org.opencypher.v9_0.frontend.phases.CompilationPhaseTracer
import org.opencypher.v9_0.frontend.phases.Monitors
import org.opencypher.v9_0.util.AllNameGenerators
import org.opencypher.v9_0.util.CypherExceptionFactory
import org.opencypher.v9_0.util.InternalNotificationLogger
import org.opencypher.v9_0.util.OpenCypherExceptionFactory
import org.scalatest.mockito.MockitoSugar.mock

//noinspection TypeAnnotation
case class TestContext(override val notificationLogger: InternalNotificationLogger = mock[InternalNotificationLogger]) extends BaseContext {
  override def tracer = CompilationPhaseTracer.NO_TRACING

  override def cypherExceptionFactory: CypherExceptionFactory = OpenCypherExceptionFactory(None)

  override def monitors = mock[Monitors]

  override def errorHandler = _ => ()

  override def allNameGenerators: AllNameGenerators = new AllNameGenerators()
}
