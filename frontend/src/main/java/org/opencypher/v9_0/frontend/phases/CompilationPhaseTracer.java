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
package org.opencypher.v9_0.frontend.phases;

public interface CompilationPhaseTracer {
    enum CompilationPhase {
        PARSING,
        DEPRECATION_WARNINGS,
        ADDITION_ERRORS,
        SEMANTIC_CHECK,
        SEMANTIC_TYPE_CHECK,
        AST_REWRITE,
        LOGICAL_PLANNING,
        CODE_GENERATION,
        PIPE_BUILDING,
        METADATA_COLLECTION,
    }

    CompilationPhaseEvent beginPhase(CompilationPhase phase);

    interface CompilationPhaseEvent extends AutoCloseable {
        @Override
        void close();
    }

    CompilationPhaseEvent NONE_PHASE = () -> {};

    CompilationPhaseTracer NO_TRACING = phase -> NONE_PHASE;
}
