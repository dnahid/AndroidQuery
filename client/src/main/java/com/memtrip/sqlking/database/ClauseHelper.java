/**
 * Copyright 2013-present memtrip LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.memtrip.sqlking.database;

import com.memtrip.sqlking.operation.clause.And;
import com.memtrip.sqlking.operation.clause.Clause;
import com.memtrip.sqlking.operation.clause.In;
import com.memtrip.sqlking.operation.clause.Or;
import com.memtrip.sqlking.operation.clause.Where;
import com.memtrip.sqlking.operation.keyword.Limit;
import com.memtrip.sqlking.operation.keyword.OrderBy;

import java.util.ArrayList;
import java.util.List;

/**
* @author Samuel Kirton <a href="mailto:sam@memtrip.com" />
*/
public class ClauseHelper {
    private static final String SPACE = " ";
    private static final String VALUE = "?";
    private static final String BRACKET_START = "(";
    private static final String BRACKET_END = ")";
    private static final String COMMA = ",";
    private static final String IN = "IN";
    private static final String AND = "AND";
    private static final String OR = "OR";

    protected ClauseHelper() { }

    /**
     * Build the conditional clause
     * @param clause
     * @return
     */
    public String getClause(Clause[] clause) {
        StringBuilder clauseBuilder = new StringBuilder();

        if (clause != null) {
            for (Clause item : clause) {
                clauseBuilder.append(getClause(item));
            }
        }

        return clauseBuilder.toString();
    }

    /**
     * Loop through the supported Clause implementations and build the clause accordingly
     * @param clause
     * @return
     */
    private String getClause(Clause clause) {
        StringBuilder clauseBuilder = new StringBuilder();

        if (clause instanceof In) {
            clauseBuilder.append(buildInCondition((In) clause));
        } else if (clause instanceof Where) {
            clauseBuilder.append(buildWhereCondition((Where) clause));
        } else if (clause instanceof And) {
            clauseBuilder.append(BRACKET_START);
            And and = (And)clause;
            for (Clause item : and.getClause()) {
                clauseBuilder.append(getClause(item));
                clauseBuilder.append(SPACE);
                clauseBuilder.append(AND);
                clauseBuilder.append(SPACE);
            }

            // remove the excess AND with its 2 spaces
            clauseBuilder.delete(clauseBuilder.length() - 5, clauseBuilder.length());
            clauseBuilder.append(BRACKET_END);
        } else if (clause instanceof Or) {
            clauseBuilder.append(BRACKET_START);
            Or or = (Or)clause;
            for (Clause item : or.getClause()) {
                clauseBuilder.append(getClause(item));
                clauseBuilder.append(SPACE);
                clauseBuilder.append(OR);
                clauseBuilder.append(SPACE);
            }

            // remove the excess OR with its 2 spaces
            clauseBuilder.delete(clauseBuilder.length() - 4, clauseBuilder.length());
            clauseBuilder.append(BRACKET_END);
        }

        return clauseBuilder.toString();
    }

    /**
     * Build the WHERE conditional string
     * @param condition
     * @return
     */
    private String buildWhereCondition(Where condition) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(condition.getRow());
        stringBuilder.append(SPACE);
        stringBuilder.append(condition.getExpression().toString());
        stringBuilder.append(SPACE);
        stringBuilder.append(VALUE);

        return stringBuilder.toString();
    }

    /**
     * Build the IN condition string
     * @param in
     * @return
     */
    private String buildInCondition(In in) {
        String row = in.getRow();
        int length = in.getValues().length;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(row);
        stringBuilder.append(SPACE);
        stringBuilder.append(IN);
        stringBuilder.append(SPACE);

        stringBuilder.append(BRACKET_START);

        if (length > 0) {
            for (int i = 0; i < length; i++) {
                stringBuilder.append(VALUE);
                stringBuilder.append(COMMA);
            }

            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }

        stringBuilder.append(BRACKET_END);

        return stringBuilder.toString();
    }

    /**
     * Build the clause arguments
     * @param clause
     * @return
     */
    public String[] getClauseArgs(Clause[] clause) {
        List<String> args = new ArrayList<>();

        if (clause != null) {
            for (Clause item : clause) {
                args.addAll(getClauseArgs(item));
            }
        }

        return args.toArray(new String[args.size()]);
    }

    /**
     * Loop through the supported Clause implementations and build the args accordingly
     * @param clause
     * @return
     */
    private List<String> getClauseArgs(Clause clause) {
        List<String> args = new ArrayList<>();

        if (clause instanceof In) {
            args.addAll(buildInArgs((In) clause));
        } else if (clause instanceof Where) {
            args.add(buildWhereArgs((Where) clause));
        } else if (clause instanceof And)  {
            And and = (And)clause;
            for (Clause item : and.getClause()) {
                args.addAll(getClauseArgs(item));
            }
        } else if (clause instanceof Or) {
            Or or = (Or)clause;
            for (Clause item : or.getClause()) {
                args.addAll(getClauseArgs(item));
            }
        }

        return args;
    }

    /**
     * Build the WHERE arguments
     * @param where
     * @return
     */
    private String buildWhereArgs(Where where) {
        String value = String.valueOf(where.getValue());

        if (value != null && value.equals("true")) {
            return "1";
        } else if (value != null && value.equals("false")) {
            return "0";
        }

        return value;
    }

    /**
     * Build the IN arguments
     * @param in
     * @return
     */
    private List<String> buildInArgs(In in) {
        List<String> args = new ArrayList<>();

        for (int i = 0; i < in.getValues().length; i++) {
            args.add(String.valueOf(in.getValues()[i]));
        }

        return args;
    }

    /**
     * Build the order by keyword part of the SQL statement
     * @param orderBy
     * @return
     */
    public String getOrderBy(OrderBy orderBy) {
        StringBuilder stringBuilder = new StringBuilder();

        if (orderBy != null) {
            stringBuilder.append(orderBy.getField());
            stringBuilder.append(SPACE);
            stringBuilder.append(orderBy.getOrder().toString());
        }

        return stringBuilder.toString();
    }

    /**
     * Build the limit keyword part of the SQL statement
     * @param limit
     * @return
     */
    public String getLimit(Limit limit) {
        StringBuilder stringBuilder = new StringBuilder();

        if (limit != null) {
            stringBuilder.append(limit.getStart());
            stringBuilder.append(COMMA);
            stringBuilder.append(limit.getEnd());
        }

        return stringBuilder.toString();
    }
}