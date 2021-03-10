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
package org.opencypher.v9_0.parser.javacc

import org.opencypher.v9_0.parser.javacc.CypherConstants.ALL
import org.opencypher.v9_0.parser.javacc.CypherConstants.ALL_SHORTEST_PATH
import org.opencypher.v9_0.parser.javacc.CypherConstants.AND
import org.opencypher.v9_0.parser.javacc.CypherConstants.ANY
import org.opencypher.v9_0.parser.javacc.CypherConstants.AS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ASC
import org.opencypher.v9_0.parser.javacc.CypherConstants.ASSERT
import org.opencypher.v9_0.parser.javacc.CypherConstants.BRIEF
import org.opencypher.v9_0.parser.javacc.CypherConstants.BTREE
import org.opencypher.v9_0.parser.javacc.CypherConstants.BY
import org.opencypher.v9_0.parser.javacc.CypherConstants.CALL
import org.opencypher.v9_0.parser.javacc.CypherConstants.CASE
import org.opencypher.v9_0.parser.javacc.CypherConstants.CATALOG
import org.opencypher.v9_0.parser.javacc.CypherConstants.COMMIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CONSTRAINT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CONTAINS
import org.opencypher.v9_0.parser.javacc.CypherConstants.COPY
import org.opencypher.v9_0.parser.javacc.CypherConstants.COUNT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CREATE
import org.opencypher.v9_0.parser.javacc.CypherConstants.CSV
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATA
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASE
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASES
import org.opencypher.v9_0.parser.javacc.CypherConstants.DBMS
import org.opencypher.v9_0.parser.javacc.CypherConstants.DEFAULT_TOKEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.DELETE
import org.opencypher.v9_0.parser.javacc.CypherConstants.DESC
import org.opencypher.v9_0.parser.javacc.CypherConstants.DESTROY
import org.opencypher.v9_0.parser.javacc.CypherConstants.DETACH
import org.opencypher.v9_0.parser.javacc.CypherConstants.DISTINCT
import org.opencypher.v9_0.parser.javacc.CypherConstants.DROP
import org.opencypher.v9_0.parser.javacc.CypherConstants.DUMP
import org.opencypher.v9_0.parser.javacc.CypherConstants.ELSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.END
import org.opencypher.v9_0.parser.javacc.CypherConstants.ENDS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ESCAPED_SYMBOLIC_NAME
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXISTS
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXTRACT
import org.opencypher.v9_0.parser.javacc.CypherConstants.FALSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.FIELDTERMINATOR
import org.opencypher.v9_0.parser.javacc.CypherConstants.FILTER
import org.opencypher.v9_0.parser.javacc.CypherConstants.FOREACH
import org.opencypher.v9_0.parser.javacc.CypherConstants.FROM
import org.opencypher.v9_0.parser.javacc.CypherConstants.GRANT
import org.opencypher.v9_0.parser.javacc.CypherConstants.GRAPH
import org.opencypher.v9_0.parser.javacc.CypherConstants.HEADERS
import org.opencypher.v9_0.parser.javacc.CypherConstants.IF
import org.opencypher.v9_0.parser.javacc.CypherConstants.IN
import org.opencypher.v9_0.parser.javacc.CypherConstants.INDEX
import org.opencypher.v9_0.parser.javacc.CypherConstants.INDEXES
import org.opencypher.v9_0.parser.javacc.CypherConstants.IS
import org.opencypher.v9_0.parser.javacc.CypherConstants.JOIN
import org.opencypher.v9_0.parser.javacc.CypherConstants.KEY
import org.opencypher.v9_0.parser.javacc.CypherConstants.LIMITROWS
import org.opencypher.v9_0.parser.javacc.CypherConstants.LOAD
import org.opencypher.v9_0.parser.javacc.CypherConstants.MATCH
import org.opencypher.v9_0.parser.javacc.CypherConstants.MERGE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NODE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NONE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NOT
import org.opencypher.v9_0.parser.javacc.CypherConstants.NOWAIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.NULL
import org.opencypher.v9_0.parser.javacc.CypherConstants.OF
import org.opencypher.v9_0.parser.javacc.CypherConstants.ON
import org.opencypher.v9_0.parser.javacc.CypherConstants.OPTIONAL
import org.opencypher.v9_0.parser.javacc.CypherConstants.OR
import org.opencypher.v9_0.parser.javacc.CypherConstants.ORDER
import org.opencypher.v9_0.parser.javacc.CypherConstants.OUTPUT
import org.opencypher.v9_0.parser.javacc.CypherConstants.PERIODIC
import org.opencypher.v9_0.parser.javacc.CypherConstants.POPULATED
import org.opencypher.v9_0.parser.javacc.CypherConstants.REDUCE
import org.opencypher.v9_0.parser.javacc.CypherConstants.REMOVE
import org.opencypher.v9_0.parser.javacc.CypherConstants.REPLACE
import org.opencypher.v9_0.parser.javacc.CypherConstants.RETURN
import org.opencypher.v9_0.parser.javacc.CypherConstants.REVOKE
import org.opencypher.v9_0.parser.javacc.CypherConstants.ROLE
import org.opencypher.v9_0.parser.javacc.CypherConstants.ROLES
import org.opencypher.v9_0.parser.javacc.CypherConstants.SCAN
import org.opencypher.v9_0.parser.javacc.CypherConstants.SEC
import org.opencypher.v9_0.parser.javacc.CypherConstants.SECOND
import org.opencypher.v9_0.parser.javacc.CypherConstants.SECONDS
import org.opencypher.v9_0.parser.javacc.CypherConstants.SEEK
import org.opencypher.v9_0.parser.javacc.CypherConstants.SET
import org.opencypher.v9_0.parser.javacc.CypherConstants.SHORTEST_PATH
import org.opencypher.v9_0.parser.javacc.CypherConstants.SHOW
import org.opencypher.v9_0.parser.javacc.CypherConstants.SINGLE
import org.opencypher.v9_0.parser.javacc.CypherConstants.SKIPROWS
import org.opencypher.v9_0.parser.javacc.CypherConstants.START
import org.opencypher.v9_0.parser.javacc.CypherConstants.STARTS
import org.opencypher.v9_0.parser.javacc.CypherConstants.STOP
import org.opencypher.v9_0.parser.javacc.CypherConstants.THEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.TO
import org.opencypher.v9_0.parser.javacc.CypherConstants.TRUE
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNION
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNIQUE
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNWIND
import org.opencypher.v9_0.parser.javacc.CypherConstants.USE
import org.opencypher.v9_0.parser.javacc.CypherConstants.USERS
import org.opencypher.v9_0.parser.javacc.CypherConstants.USING
import org.opencypher.v9_0.parser.javacc.CypherConstants.VERBOSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.WAIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.WHEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.WHERE
import org.opencypher.v9_0.parser.javacc.CypherConstants.WITH
import org.opencypher.v9_0.parser.javacc.CypherConstants.XOR
import org.opencypher.v9_0.parser.javacc.CypherConstants.YIELD

object IdentifierTokens {

  val tokens = Set(
    ESCAPED_SYMBOLIC_NAME,
    //keywords
    ALL_SHORTEST_PATH,
    ALL,
    AND,
    ANY,
    AS,
    ASC,
    ASSERT,
    BRIEF,
    BTREE,
    BY,
    CALL,
    CASE,
    CATALOG,
    COMMIT,
    CONSTRAINT,
    CONTAINS,
    COPY,
    COUNT,
    CREATE,
    CSV,
    DATA,
    DATABASE,
    DATABASES,
    DBMS,
    DEFAULT_TOKEN,
    DELETE,
    DESC,
    DESTROY,
    DETACH,
    DISTINCT,
    DROP,
    DUMP,
    ELSE,
    END,
    ENDS,
    EXISTS,
    EXTRACT,
    FALSE,
    FIELDTERMINATOR,
    FILTER,
    FOREACH,
    FROM,
    GRANT,
    GRAPH,
    HEADERS,
    IF,
    IN,
    INDEX,
    INDEXES,
    IS,
    JOIN,
    KEY,
    LIMITROWS,
    LOAD,
    MATCH,
    MERGE,
    NODE,
    NONE,
    NOT,
    NOWAIT,
    NULL,
    OF,
    ON,
    OPTIONAL,
    OR,
    ORDER,
    OUTPUT,
    PERIODIC,
    POPULATED,
    REDUCE,
    REMOVE,
    REPLACE,
    RETURN,
    REVOKE,
    ROLE,
    ROLES,
    SCAN,
    SEC,
    SECOND,
    SECONDS,
    SEEK,
    SET,
    SHORTEST_PATH,
    SHOW,
    SINGLE,
    SKIPROWS,
    START,
    STARTS,
    STOP,
    THEN,
    TO,
    TRUE,
    UNION,
    UNIQUE,
    UNWIND,
    USE,
    USERS,
    USING,
    VERBOSE,
    WAIT,
    WHEN,
    WHERE,
    WITH,
    XOR,
    YIELD
  )
}
