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
package org.opencypher.v9_0.ast.factory;

import java.util.List;

public interface ASTExceptionFactory
{
    Exception syntaxException( String got, List<String> expected, Exception source, int offset, int line, int column );

    Exception syntaxException( Exception source, int offset, int line, int column );

    //Exception messages
    String invalidDropCommand = "Unsupported drop constraint command: Please delete the constraint by name instead";

    static String relationshipPattternNotAllowed( ConstraintType type )
    {
        return String.format( "'%s' does not allow relationship patterns", type.description() );
    }

    static String onlySinglePropertyAllowed( ConstraintType type )
    {
        return String.format("'%s' does not allow multiple properties", type.description());
    }

    static String invalidShowFilterType( String command, ShowCommandFilterTypes got )
    {
        return String.format( "Filter type %s is not defined for show %s command.", got.description(), command );
    }

    static String invalidCreateIndexType( CreateIndexTypes got )
    {
        return String.format( "Index type %s is not defined for create index command.", got.description() );
    }

    String periodicCommitNotSupported = "The PERIODIC COMMIT query hint is no longer supported. Please use CALL { ... } IN TRANSACTIONS instead.";
}
