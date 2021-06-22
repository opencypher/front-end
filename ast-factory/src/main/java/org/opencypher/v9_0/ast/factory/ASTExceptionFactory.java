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

import java.util.Arrays;
import java.util.List;

import static org.opencypher.v9_0.ast.factory.ConstraintType.NODE_EXISTS;
import static org.opencypher.v9_0.ast.factory.ConstraintType.NODE_IS_NOT_NULL;
import static org.opencypher.v9_0.ast.factory.ConstraintType.NODE_KEY;
import static org.opencypher.v9_0.ast.factory.ConstraintType.UNIQUE;

public interface ASTExceptionFactory
{
    Exception syntaxException( String got, List<String> expected, Exception source, int offset, int line, int column );

    Exception syntaxException( Exception source, int offset, int line, int column );

    //Exception messages

    String undefinedConstraintType = String.format( "No constraint type %s is defined",
                                                    Arrays.asList( NODE_EXISTS.description(), UNIQUE.description(), NODE_IS_NOT_NULL.description(),
                                                                   NODE_KEY.description() ) );
    String invalidDropCommand = "Unsupported drop constraint command: Please delete the constraint by name instead";
    String invalidCatalogStatement = "CATALOG is not allowed for this statement";

    static String relationshipPattternNotAllowed( ConstraintType type )
    {
        return String.format( "'%s' does not allow relationship patterns", type.description() );
    }

    static String onlySinglePropertyAllowed( ConstraintType type )
    {
        return String.format("'%s' does not allow multiple properties", type.description());
    }

    static String constraintTypeNotAllowed( ConstraintType newType, ConstraintType oldType )
    {
        return String.format( "Invalid input '%s': conflicting with '%s'", newType.description(), oldType.description() );
    }
}
