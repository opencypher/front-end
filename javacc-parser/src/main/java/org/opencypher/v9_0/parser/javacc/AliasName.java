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

import java.util.ArrayList;
import java.util.List;
import org.opencypher.v9_0.ast.factory.ASTExceptionFactory;
import org.opencypher.v9_0.ast.factory.SimpleEither;

public class AliasName<PARAMETER> {
    ASTExceptionFactory exceptionFactory;
    Token start;
    List<String> names = new ArrayList<>();
    PARAMETER parameter;

    public AliasName(ASTExceptionFactory exceptionFactory, Token token) {
        this.exceptionFactory = exceptionFactory;
        this.start = token;
        this.names.add(token.image);
    }

    public AliasName(ASTExceptionFactory exceptionFactory, PARAMETER parameter) {
        this.exceptionFactory = exceptionFactory;
        this.parameter = parameter;
    }

    public void add(Token token) {
        names.add(token.image);
    }

    public SimpleEither<String, PARAMETER> getRemoteAliasName() throws Exception {
        if (parameter != null) {
            return SimpleEither.right(parameter);
        } else {
            if (names.size() > 1) {
                throw exceptionFactory.syntaxException(
                        new ParseException(ASTExceptionFactory.invalidDotsInRemoteAliasName(String.join(".", names))),
                        start.beginOffset,
                        start.beginLine,
                        start.beginColumn);
            } else {
                return SimpleEither.left(names.get(0));
            }
        }
    }

    public SimpleEither<String, PARAMETER> getLocalAliasName() {
        if (parameter != null) {
            return SimpleEither.right(parameter);
        } else {
            return SimpleEither.left(String.join(".", names));
        }
    }
}
