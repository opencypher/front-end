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
package org.opencypher.v9_0.parser.javacc;

import static org.opencypher.v9_0.parser.javacc.CypherConstants.ACCESS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ACTIVE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ADMIN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ADMINISTRATOR;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ALIAS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ALIASES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ALL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ALL_SHORTEST_PATH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ALTER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.AND;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ANY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.AS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ASC;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ASSERT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ASSIGN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.AT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.BOOSTED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.BRIEF;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.BTREE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.BUILT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.BY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CALL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CASE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CHANGE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COMMAND;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COMMANDS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COMMIT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COMPOSITE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CONSTRAINT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CONSTRAINTS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CONTAINS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COPY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.COUNT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CREATE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CSV;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.CURRENT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DATA;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DATABASES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DBMS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DEALLOCATE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DEFAULT_TOKEN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DEFINED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DELETE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DENY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DESC;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DESTROY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DETACH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DISTINCT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DRIVER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DROP;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.DUMP;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EACH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ELEMENT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ELEMENTS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ELSE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ENABLE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ENCRYPTED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.END;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ENDS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ESCAPED_SYMBOLIC_NAME;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EXECUTABLE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EXECUTE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EXIST;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EXISTENCE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.EXISTS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FALSE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FIELDTERMINATOR;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FOR;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FOREACH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FROM;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FULLTEXT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FUNCTION;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.FUNCTIONS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.GRANT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.GRAPH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.GRAPHS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.HEADERS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.HOME;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IDENTIFIER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IF;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IMMUTABLE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IMPERSONATE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.INDEX;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.INDEXES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.INF;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.INFINITY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.IS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.JOIN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.KEY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.LABEL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.LABELS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.LIMITROWS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.LOAD;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.LOOKUP;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.MANAGEMENT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.MATCH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.MERGE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NAME;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NAMES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NAN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NEW;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NODE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NODES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NONE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NOT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NOWAIT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.NULL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.OF;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ON;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ONLY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.OPTIONAL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.OPTIONS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.OR;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ORDER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.OUTPUT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PASSWORD;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PASSWORDS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PERIODIC;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PLAINTEXT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.POINT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.POPULATED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PRIVILEGE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PRIVILEGES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PROCEDURE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PROCEDURES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PROPERTIES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.PROPERTY;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.RANGE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.READ;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REDUCE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REL;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.RELATIONSHIP;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.RELATIONSHIPS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REMOVE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.RENAME;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REPLACE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REQUIRE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REQUIRED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.RETURN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.REVOKE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ROLE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ROLES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ROW;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.ROWS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SCAN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SEC;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SECOND;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SECONDS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SEEK;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SERVER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SERVERS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SET;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SHORTEST_PATH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SHOW;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SINGLE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SKIPROWS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.START;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.STARTS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.STATUS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.STOP;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.SUSPENDED;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TARGET;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TERMINATE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TEXT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.THEN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TO;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TRANSACTION;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TRANSACTIONS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TRAVERSE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TRUE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TYPE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.TYPES;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.UNION;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.UNIQUE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.UNWIND;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.USE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.USER;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.USERS;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.USING;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.VERBOSE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.WAIT;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.WHEN;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.WHERE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.WITH;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.WRITE;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.XOR;
import static org.opencypher.v9_0.parser.javacc.CypherConstants.YIELD;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IdentifierTokens {
    private static final Set<Integer> identifiers = new HashSet<>(Arrays.asList(
            ESCAPED_SYMBOLIC_NAME,
            // keywords
            ACCESS,
            ACTIVE,
            ADMIN,
            ADMINISTRATOR,
            ALIAS,
            ALIASES,
            ALL_SHORTEST_PATH,
            ALL,
            ALTER,
            AND,
            ANY,
            AS,
            ASC,
            ASSERT,
            ASSIGN,
            AT,
            BOOSTED,
            BRIEF,
            BTREE,
            BUILT,
            BY,
            CALL,
            CASE,
            CHANGE,
            COMMAND,
            COMMANDS,
            COMMIT,
            COMPOSITE,
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
            DEALLOCATE,
            DEFAULT_TOKEN,
            DEFINED,
            DELETE,
            DENY,
            DESC,
            DESTROY,
            DETACH,
            DISTINCT,
            DRIVER,
            DROP,
            DUMP,
            EACH,
            ELEMENT,
            ELEMENTS,
            ELSE,
            ENABLE,
            ENCRYPTED,
            END,
            ENDS,
            EXECUTABLE,
            EXECUTE,
            EXIST,
            EXISTENCE,
            EXISTS,
            FALSE,
            FIELDTERMINATOR,
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
            IDENTIFIER,
            IF,
            IMPERSONATE,
            IMMUTABLE,
            IN,
            INDEX,
            INDEXES,
            INF,
            INFINITY,
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
            NAN,
            NEW,
            NODE,
            NODES,
            NONE,
            NOT,
            NOWAIT,
            NULL,
            OF,
            ON,
            ONLY,
            OPTIONS,
            OPTIONAL,
            OR,
            ORDER,
            OUTPUT,
            PASSWORD,
            PASSWORDS,
            PERIODIC,
            PLAINTEXT,
            POINT,
            POPULATED,
            PRIVILEGE,
            PRIVILEGES,
            PROCEDURE,
            PROCEDURES,
            PROPERTIES,
            PROPERTY,
            RANGE,
            READ,
            REDUCE,
            REL,
            RELATIONSHIP,
            RELATIONSHIPS,
            REMOVE,
            RENAME,
            REPLACE,
            REQUIRE,
            REQUIRED,
            RETURN,
            REVOKE,
            ROLE,
            ROLES,
            ROW,
            ROWS,
            SCAN,
            SEC,
            SECOND,
            SECONDS,
            SEEK,
            SERVER,
            SERVERS,
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
            TARGET,
            TERMINATE,
            TEXT,
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
            YIELD));

    public static Set<Integer> getIdentifierTokens() {
        return identifiers;
    }
}
