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

import org.opencypher.v9_0.parser.javacc.CypherConstants.ACCESS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ACTIVE
import org.opencypher.v9_0.parser.javacc.CypherConstants.ALL
import org.opencypher.v9_0.parser.javacc.CypherConstants.ALL_SHORTEST_PATH
import org.opencypher.v9_0.parser.javacc.CypherConstants.ALTER
import org.opencypher.v9_0.parser.javacc.CypherConstants.AND
import org.opencypher.v9_0.parser.javacc.CypherConstants.ANY
import org.opencypher.v9_0.parser.javacc.CypherConstants.AS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ASC
import org.opencypher.v9_0.parser.javacc.CypherConstants.ASSERT
import org.opencypher.v9_0.parser.javacc.CypherConstants.ASSIGN
import org.opencypher.v9_0.parser.javacc.CypherConstants.BRIEF
import org.opencypher.v9_0.parser.javacc.CypherConstants.BTREE
import org.opencypher.v9_0.parser.javacc.CypherConstants.BUILT
import org.opencypher.v9_0.parser.javacc.CypherConstants.BY
import org.opencypher.v9_0.parser.javacc.CypherConstants.CALL
import org.opencypher.v9_0.parser.javacc.CypherConstants.CASE
import org.opencypher.v9_0.parser.javacc.CypherConstants.CATALOG
import org.opencypher.v9_0.parser.javacc.CypherConstants.CHANGE
import org.opencypher.v9_0.parser.javacc.CypherConstants.COMMIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CONSTRAINT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CONSTRAINTS
import org.opencypher.v9_0.parser.javacc.CypherConstants.CONTAINS
import org.opencypher.v9_0.parser.javacc.CypherConstants.COPY
import org.opencypher.v9_0.parser.javacc.CypherConstants.COUNT
import org.opencypher.v9_0.parser.javacc.CypherConstants.CREATE
import org.opencypher.v9_0.parser.javacc.CypherConstants.CSV
import org.opencypher.v9_0.parser.javacc.CypherConstants.CURRENT
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATA
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASE
import org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASES
import org.opencypher.v9_0.parser.javacc.CypherConstants.DBMS
import org.opencypher.v9_0.parser.javacc.CypherConstants.DEFAULT_TOKEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.DEFINED
import org.opencypher.v9_0.parser.javacc.CypherConstants.DELETE
import org.opencypher.v9_0.parser.javacc.CypherConstants.DENY
import org.opencypher.v9_0.parser.javacc.CypherConstants.DESC
import org.opencypher.v9_0.parser.javacc.CypherConstants.DESTROY
import org.opencypher.v9_0.parser.javacc.CypherConstants.DETACH
import org.opencypher.v9_0.parser.javacc.CypherConstants.DISTINCT
import org.opencypher.v9_0.parser.javacc.CypherConstants.DROP
import org.opencypher.v9_0.parser.javacc.CypherConstants.DUMP
import org.opencypher.v9_0.parser.javacc.CypherConstants.EACH
import org.opencypher.v9_0.parser.javacc.CypherConstants.ELEMENT
import org.opencypher.v9_0.parser.javacc.CypherConstants.ELEMENTS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ELSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.ENCRYPTED
import org.opencypher.v9_0.parser.javacc.CypherConstants.END
import org.opencypher.v9_0.parser.javacc.CypherConstants.ENDS
import org.opencypher.v9_0.parser.javacc.CypherConstants.ESCAPED_SYMBOLIC_NAME
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXECUTABLE
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXIST
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXISTENCE
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXISTS
import org.opencypher.v9_0.parser.javacc.CypherConstants.EXTRACT
import org.opencypher.v9_0.parser.javacc.CypherConstants.FALSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.FIELDTERMINATOR
import org.opencypher.v9_0.parser.javacc.CypherConstants.FILTER
import org.opencypher.v9_0.parser.javacc.CypherConstants.FOR
import org.opencypher.v9_0.parser.javacc.CypherConstants.FOREACH
import org.opencypher.v9_0.parser.javacc.CypherConstants.FROM
import org.opencypher.v9_0.parser.javacc.CypherConstants.FULLTEXT
import org.opencypher.v9_0.parser.javacc.CypherConstants.FUNCTION
import org.opencypher.v9_0.parser.javacc.CypherConstants.FUNCTIONS
import org.opencypher.v9_0.parser.javacc.CypherConstants.GRANT
import org.opencypher.v9_0.parser.javacc.CypherConstants.GRAPH
import org.opencypher.v9_0.parser.javacc.CypherConstants.GRAPHS
import org.opencypher.v9_0.parser.javacc.CypherConstants.HEADERS
import org.opencypher.v9_0.parser.javacc.CypherConstants.HOME
import org.opencypher.v9_0.parser.javacc.CypherConstants.IF
import org.opencypher.v9_0.parser.javacc.CypherConstants.IN
import org.opencypher.v9_0.parser.javacc.CypherConstants.INDEX
import org.opencypher.v9_0.parser.javacc.CypherConstants.INDEXES
import org.opencypher.v9_0.parser.javacc.CypherConstants.IS
import org.opencypher.v9_0.parser.javacc.CypherConstants.JOIN
import org.opencypher.v9_0.parser.javacc.CypherConstants.KEY
import org.opencypher.v9_0.parser.javacc.CypherConstants.LABEL
import org.opencypher.v9_0.parser.javacc.CypherConstants.LABELS
import org.opencypher.v9_0.parser.javacc.CypherConstants.LIMITROWS
import org.opencypher.v9_0.parser.javacc.CypherConstants.LOAD
import org.opencypher.v9_0.parser.javacc.CypherConstants.LOOKUP
import org.opencypher.v9_0.parser.javacc.CypherConstants.MANAGEMENT
import org.opencypher.v9_0.parser.javacc.CypherConstants.MATCH
import org.opencypher.v9_0.parser.javacc.CypherConstants.MERGE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NAME
import org.opencypher.v9_0.parser.javacc.CypherConstants.NAMES
import org.opencypher.v9_0.parser.javacc.CypherConstants.NEW
import org.opencypher.v9_0.parser.javacc.CypherConstants.NODE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NODES
import org.opencypher.v9_0.parser.javacc.CypherConstants.NONE
import org.opencypher.v9_0.parser.javacc.CypherConstants.NOT
import org.opencypher.v9_0.parser.javacc.CypherConstants.NOWAIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.NULL
import org.opencypher.v9_0.parser.javacc.CypherConstants.OF
import org.opencypher.v9_0.parser.javacc.CypherConstants.ON
import org.opencypher.v9_0.parser.javacc.CypherConstants.OPTIONAL
import org.opencypher.v9_0.parser.javacc.CypherConstants.OPTIONS
import org.opencypher.v9_0.parser.javacc.CypherConstants.OR
import org.opencypher.v9_0.parser.javacc.CypherConstants.ORDER
import org.opencypher.v9_0.parser.javacc.CypherConstants.OUTPUT
import org.opencypher.v9_0.parser.javacc.CypherConstants.PASSWORD
import org.opencypher.v9_0.parser.javacc.CypherConstants.PASSWORDS
import org.opencypher.v9_0.parser.javacc.CypherConstants.PERIODIC
import org.opencypher.v9_0.parser.javacc.CypherConstants.PLAINTEXT
import org.opencypher.v9_0.parser.javacc.CypherConstants.POPULATED
import org.opencypher.v9_0.parser.javacc.CypherConstants.PRIVILEGE
import org.opencypher.v9_0.parser.javacc.CypherConstants.PRIVILEGES
import org.opencypher.v9_0.parser.javacc.CypherConstants.PROCEDURE
import org.opencypher.v9_0.parser.javacc.CypherConstants.PROCEDURES
import org.opencypher.v9_0.parser.javacc.CypherConstants.PROPERTY
import org.opencypher.v9_0.parser.javacc.CypherConstants.READ
import org.opencypher.v9_0.parser.javacc.CypherConstants.REDUCE
import org.opencypher.v9_0.parser.javacc.CypherConstants.REL
import org.opencypher.v9_0.parser.javacc.CypherConstants.RELATIONSHIP
import org.opencypher.v9_0.parser.javacc.CypherConstants.RELATIONSHIPS
import org.opencypher.v9_0.parser.javacc.CypherConstants.REMOVE
import org.opencypher.v9_0.parser.javacc.CypherConstants.RENAME
import org.opencypher.v9_0.parser.javacc.CypherConstants.REPLACE
import org.opencypher.v9_0.parser.javacc.CypherConstants.REQUIRED
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
import org.opencypher.v9_0.parser.javacc.CypherConstants.STATUS
import org.opencypher.v9_0.parser.javacc.CypherConstants.STOP
import org.opencypher.v9_0.parser.javacc.CypherConstants.SUSPENDED
import org.opencypher.v9_0.parser.javacc.CypherConstants.TERMINATE
import org.opencypher.v9_0.parser.javacc.CypherConstants.THEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.TO
import org.opencypher.v9_0.parser.javacc.CypherConstants.TRANSACTION
import org.opencypher.v9_0.parser.javacc.CypherConstants.TRANSACTIONS
import org.opencypher.v9_0.parser.javacc.CypherConstants.TRAVERSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.TRUE
import org.opencypher.v9_0.parser.javacc.CypherConstants.TYPE
import org.opencypher.v9_0.parser.javacc.CypherConstants.TYPES
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNION
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNIQUE
import org.opencypher.v9_0.parser.javacc.CypherConstants.UNWIND
import org.opencypher.v9_0.parser.javacc.CypherConstants.USE
import org.opencypher.v9_0.parser.javacc.CypherConstants.USER
import org.opencypher.v9_0.parser.javacc.CypherConstants.USERS
import org.opencypher.v9_0.parser.javacc.CypherConstants.USING
import org.opencypher.v9_0.parser.javacc.CypherConstants.VERBOSE
import org.opencypher.v9_0.parser.javacc.CypherConstants.WAIT
import org.opencypher.v9_0.parser.javacc.CypherConstants.WHEN
import org.opencypher.v9_0.parser.javacc.CypherConstants.WHERE
import org.opencypher.v9_0.parser.javacc.CypherConstants.WITH
import org.opencypher.v9_0.parser.javacc.CypherConstants.WRITE
import org.opencypher.v9_0.parser.javacc.CypherConstants.XOR
import org.opencypher.v9_0.parser.javacc.CypherConstants.YIELD

object IdentifierTokens {

  val tokens = Set(
    ESCAPED_SYMBOLIC_NAME,
    //keywords
    ACCESS,
    ACTIVE,
    ALL_SHORTEST_PATH,
    ALL,
    ALTER,
    AND,
    ANY,
    AS,
    ASC,
    ASSERT,
    ASSIGN,
    BRIEF,
    BTREE,
    BUILT,
    BY,
    CALL,
    CASE,
    CATALOG,
    CHANGE,
    COMMIT,
    CONSTRAINT,
    CONSTRAINTS,
    CONTAINS,
    COPY,
    COUNT,
    CREATE,
    CSV,
    CURRENT,
    DATA,
    DATABASE,
    DATABASES,
    DBMS,
    DEFAULT_TOKEN,
    DEFINED,
    DELETE,
    DENY,
    DESC,
    DESTROY,
    DETACH,
    DISTINCT,
    DROP,
    DUMP,
    EACH,
    ELEMENT,
    ELEMENTS,
    ELSE,
    ENCRYPTED,
    END,
    ENDS,
    EXECUTABLE,
    EXIST,
    EXISTENCE,
    EXISTS,
    EXTRACT,
    FALSE,
    FIELDTERMINATOR,
    FILTER,
    FOR,
    FOREACH,
    FROM,
    FULLTEXT,
    FUNCTION,
    FUNCTIONS,
    GRANT,
    GRAPH,
    GRAPHS,
    HEADERS,
    HOME,
    IF,
    IN,
    INDEX,
    INDEXES,
    IS,
    JOIN,
    KEY,
    LABEL,
    LABELS,
    LIMITROWS,
    LOAD,
    LOOKUP,
    MANAGEMENT,
    MATCH,
    MERGE,
    NAME,
    NAMES,
    NEW,
    NODE,
    NODES,
    NONE,
    NOT,
    NOWAIT,
    NULL,
    OF,
    ON,
    OPTIONS,
    OPTIONAL,
    OR,
    ORDER,
    OUTPUT,
    PASSWORD,
    PASSWORDS,
    PERIODIC,
    PLAINTEXT,
    POPULATED,
    PRIVILEGE,
    PRIVILEGES,
    PROCEDURE,
    PROCEDURES,
    PROPERTY,
    READ,
    REDUCE,
    REL,
    RELATIONSHIP,
    RELATIONSHIPS,
    REMOVE,
    RENAME,
    REPLACE,
    REQUIRED,
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
    STATUS,
    STOP,
    SUSPENDED,
    TERMINATE,
    THEN,
    TO,
    TRANSACTION,
    TRANSACTIONS,
    TRAVERSE,
    TRUE,
    TYPE,
    TYPES,
    UNION,
    UNIQUE,
    UNWIND,
    USE,
    USER,
    USERS,
    USING,
    VERBOSE,
    WAIT,
    WHEN,
    WHERE,
    WITH,
    WRITE,
    XOR,
    YIELD
  )
}
