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

public enum ShowCommandFilterTypes
{
    // index specific
    BTREE( "BTREE" ),
    FULLTEXT( "FULLTEXT" ),
    LOOKUP( "LOOKUP" ),
    // constraint specific
    UNIQUE( "UNIQUE" ),
    NODE_KEY( "NODE KEY" ),
    OLD_EXISTS( "EXISTS" ),
    OLD_EXIST( "EXIST" ),
    EXIST( "[PROPERTY] EXIST[ENCE]" ),
    NODE_OLD_EXISTS( "NODE EXISTS" ),
    NODE_OLD_EXIST( "NODE EXIST" ),
    NODE_EXIST( "NODE [PROPERTY] EXIST[ENCE]" ),
    RELATIONSHIP_OLD_EXISTS( "RELATIONSHIP EXISTS" ),
    RELATIONSHIP_OLD_EXIST( "RELATIONSHIP EXIST" ),
    RELATIONSHIP_EXIST( "REL[ATIONSHIP] [PROPERTY] EXIST[ENCE]" ),
    // function specific
    BUILT_IN( "BUILT IN" ),
    USER_DEFINED( "USER DEFINED" ),
    // general
    ALL( "ALL" ),
    INVALID( "INVALID" );

    private final String description;

    ShowCommandFilterTypes( String description )
    {
        this.description = description;
    }

    public String description()
    {
        return description;
    }
}
