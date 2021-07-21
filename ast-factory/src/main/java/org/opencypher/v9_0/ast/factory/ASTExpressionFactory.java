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

import org.opencypher.v9_0.ast.factory.ASTFactory.StringPos;

/**
 * Factory for constructing AST expressions.
 * <p>
 * This interface is generic in many dimensions, in order to support type-safe construction of ASTs
 * without depending on the concrete AST type. This architecture allows code which creates/manipulates AST
 * to live independently of the AST, and thus makes sharing and reuse of these components much easier.
 * <p>
 * The factory contains methods for creating AST representing all of Cypher 9 expressions, as defined
 * at `https://github.com/opencypher/openCypher/`, and implemented in `https://github.com/opencypher/front-end`.
 */
public interface ASTExpressionFactory<
        EXPRESSION,
        PARAMETER,
        PATTERN,
        VARIABLE extends EXPRESSION,
        PROPERTY extends EXPRESSION,
        MAP_PROJECTION_ITEM,
        POS>
{
    VARIABLE newVariable( POS p, String name );

    PARAMETER newParameter( POS p, VARIABLE v, ParameterType type );

    PARAMETER newParameter( POS p, String offset, ParameterType type );

    PARAMETER newSensitiveStringParameter( POS p, VARIABLE v );

    PARAMETER newSensitiveStringParameter( POS p, String offset );

    EXPRESSION oldParameter( POS p, VARIABLE v );

    EXPRESSION newDouble( POS p, String image );

    EXPRESSION newDecimalInteger( POS p, String image, boolean negated );

    EXPRESSION newHexInteger( POS p, String image, boolean negated );

    EXPRESSION newOctalInteger( POS p, String image, boolean negated );

    EXPRESSION newString( POS p, String image );

    EXPRESSION newTrueLiteral( POS p );

    EXPRESSION newFalseLiteral( POS p );

    EXPRESSION newNullLiteral( POS p );

    EXPRESSION listLiteral( POS p, List<EXPRESSION> values );

    EXPRESSION mapLiteral( POS p, List<StringPos<POS>> keys, List<EXPRESSION> values );

    EXPRESSION hasLabelsOrTypes( EXPRESSION subject, List<StringPos<POS>> labels );

    PROPERTY property( EXPRESSION subject, StringPos<POS> propertyKeyName );

    EXPRESSION or( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION xor( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION and( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION ands( List<EXPRESSION> exprs );

    EXPRESSION not( POS p, EXPRESSION e );

    EXPRESSION plus( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION minus( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION multiply( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION divide( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION modulo( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION pow( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION unaryPlus( EXPRESSION e );

    EXPRESSION unaryPlus( POS p, EXPRESSION e );

    EXPRESSION unaryMinus( POS p, EXPRESSION e );

    EXPRESSION eq( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION neq( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION neq2( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION lte( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION gte( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION lt( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION gt( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION regeq( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION startsWith( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION endsWith( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION contains( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION in( POS p, EXPRESSION lhs, EXPRESSION rhs );

    EXPRESSION isNull( POS p, EXPRESSION e );

    EXPRESSION listLookup( EXPRESSION list, EXPRESSION index );

    EXPRESSION listSlice( POS p, EXPRESSION list, EXPRESSION start, EXPRESSION end );

    EXPRESSION newCountStar( POS p );

    EXPRESSION functionInvocation( POS p,
                                   POS functionNamePosition,
                                   List<String> namespace,
                                   String name,
                                   boolean distinct,
                                   List<EXPRESSION> arguments );

    EXPRESSION listComprehension( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where, EXPRESSION projection );

    EXPRESSION patternComprehension( POS p, POS relationshipPatternPosition, VARIABLE v, PATTERN pattern, EXPRESSION where, EXPRESSION projection );

    EXPRESSION filterExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where );

    EXPRESSION extractExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where, EXPRESSION projection );

    EXPRESSION reduceExpression( POS p, VARIABLE acc, EXPRESSION accExpr, VARIABLE v, EXPRESSION list, EXPRESSION innerExpr );

    EXPRESSION allExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where );

    EXPRESSION anyExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where );

    EXPRESSION noneExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where );

    EXPRESSION singleExpression( POS p, VARIABLE v, EXPRESSION list, EXPRESSION where );

    EXPRESSION patternExpression( POS p, PATTERN pattern );

    EXPRESSION existsSubQuery( POS p, List<PATTERN> patterns, EXPRESSION where );

    EXPRESSION mapProjection( POS p, VARIABLE v, List<MAP_PROJECTION_ITEM> items );

    MAP_PROJECTION_ITEM mapProjectionLiteralEntry( StringPos<POS> property, EXPRESSION value );

    MAP_PROJECTION_ITEM mapProjectionProperty( StringPos<POS> property );

    MAP_PROJECTION_ITEM mapProjectionVariable( VARIABLE v );

    MAP_PROJECTION_ITEM mapProjectionAll( POS p );

    EXPRESSION caseExpression( POS p, EXPRESSION e, List<EXPRESSION> whens, List<EXPRESSION> thens, EXPRESSION elze );

    POS inputPosition( int offset, int line, int column );
}
