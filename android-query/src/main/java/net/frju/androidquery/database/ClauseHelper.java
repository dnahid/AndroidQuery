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
package net.frju.androidquery.database;

import net.frju.androidquery.operation.clause.And;
import net.frju.androidquery.operation.clause.Clause;
import net.frju.androidquery.operation.clause.In;
import net.frju.androidquery.operation.clause.On;
import net.frju.androidquery.operation.clause.Or;
import net.frju.androidquery.operation.clause.Where;
import net.frju.androidquery.operation.join.CrossInnerJoin;
import net.frju.androidquery.operation.join.InnerJoin;
import net.frju.androidquery.operation.join.Join;
import net.frju.androidquery.operation.join.LeftOuterJoin;
import net.frju.androidquery.operation.join.NaturalInnerJoin;
import net.frju.androidquery.operation.join.NaturalLeftOuterJoin;
import net.frju.androidquery.operation.keyword.Limit;
import net.frju.androidquery.operation.keyword.OrderBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author Samuel Kirton [sam@memtrip.com]
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

    public String getClause(Clause[] clause) {
        StringBuilder clauseBuilder = new StringBuilder();

        if (clause != null) {
            for (Clause item : clause) {
                clauseBuilder.append(getClause(item));
            }
        }

        return clauseBuilder.toString();
    }

    private String getClause(Clause clause) {
        StringBuilder clauseBuilder = new StringBuilder();

        if (clause instanceof In) {
            clauseBuilder.append(buildInCondition((In) clause));
        } else if (clause instanceof Where) {
            clauseBuilder.append(buildWhereCondition((Where) clause));
        }else if (clause instanceof On) {
            clauseBuilder.append(buildOnCondition((On) clause));
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

    private String buildWhereCondition(Where where) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(where.getColumn());
        stringBuilder.append(SPACE);
        stringBuilder.append(where.getOperator().toString());
        stringBuilder.append(SPACE);
        stringBuilder.append(VALUE);

        return stringBuilder.toString();
    }

    private String buildInCondition(In in) {
        String row = in.getColumn();
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

    private String buildOnCondition(On on) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("ON");
        stringBuilder.append(SPACE);
        stringBuilder.append(on.getColumn1());
        stringBuilder.append(SPACE);
        stringBuilder.append("=");
        stringBuilder.append(SPACE);
        stringBuilder.append(on.getColumn2());

        return stringBuilder.toString();
    }

    public String[] getClauseArgs(Clause[] clause) {
        List<String> args = new ArrayList<>();

        if (clause != null) {
            for (Clause item : clause) {
                args.addAll(getClauseArgs(item));
            }
        }

        return args.toArray(new String[args.size()]);
    }

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

    private String buildWhereArgs(Where where) {
        String value = String.valueOf(where.getValue());

        if (value != null && value.equals("true")) {
            return "1";
        } else if (value != null && value.equals("false")) {
            return "0";
        }

        return value;
    }

    private List<String> buildInArgs(In in) {
        List<String> args = new ArrayList<>();

        for (int i = 0; i < in.getValues().length; i++) {
            args.add(String.valueOf(in.getValues()[i]));
        }

        return args;
    }

    public String getOrderBy(OrderBy[] orderByArray) {
        StringBuilder stringBuilder = new StringBuilder();

        if (orderByArray != null && orderByArray.length > 0) {
            for (OrderBy orderBy : orderByArray) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(COMMA);
                }

                if (orderBy.getOrder() == OrderBy.Order.RANDOM) {
                    stringBuilder.append(orderBy.getOrder().toString() + "()");
                } else {
                    stringBuilder.append(orderBy.getField());
                    stringBuilder.append(SPACE);
                    stringBuilder.append(orderBy.getOrder().toString());
                }
            }
        }

        return stringBuilder.toString();
    }

    public String getLimit(Limit limit) {
        StringBuilder stringBuilder = new StringBuilder();

        if (limit != null) {
            stringBuilder.append(limit.getStart());
            stringBuilder.append(COMMA);
            stringBuilder.append(limit.getEnd());
        }

        return stringBuilder.toString();
    }

    public String getJoinStatement(Join[] joins, Resolver resolver) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Join join : joins) {
            TableDescription tableDescription = resolver.getTableDescription(join.getTable());
            String tableRealName = tableDescription.getTableRealName();

            stringBuilder
            		.append(" ")
            		.append(getJoinType(join))
                    .append(" ")
                    .append(tableRealName)
                    .append(" ")
                    .append(getClause(join.getClauses()));

            if (join.getJoin() != null) {
                stringBuilder.append(" ")
                        .append(getJoinStatement(new Join[] { join.getJoin() }, resolver));
            }
        }

        return stringBuilder.toString();
    }

    public String buildJoinQuery(String[] tableColumns, Join[] joins, String tableName, Clause[] clause,
                                 OrderBy[] orderBy, Limit limit, Resolver resolver) {

        String[] joinColumns = getJoinColumns(joins, resolver);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");

        for (String column : tableColumns) {
            stringBuilder.append(column).append(", ");
        }

        for (String column : joinColumns) {
            String columnAlias = column.replace(".","_");
            stringBuilder.append(column)
                    .append(" as ")
                    .append(columnAlias)
                    .append(", ");
        }

        // remove the trailing comma
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());

        String clauseString = getClause(clause);
        if (clauseString != null && clauseString.length() > 0) {
            clauseString = "WHERE " + clauseString;
        }

        String orderByString = getOrderBy(orderBy);
        if (orderByString != null && orderByString.length() > 0) {
            orderByString = "ORDER BY " + orderByString;
        }

        String limitString = getLimit(limit);
        if (limitString != null && limitString.length() > 0) {
            limitString = "LIMIT " + limitString;
        }

        stringBuilder.append(" FROM ")
                .append(tableName)
                .append(" ")
                .append(getJoinStatement(joins, resolver))
                .append(" ")
                .append(clauseString)
                .append(" ")
                .append(orderByString)
                .append(" ")
                .append(limitString);

        return stringBuilder.toString();
    }

    private String[] getJoinColumns(Join[] joins, Resolver resolver) {
        List<String> joinColumns = new ArrayList<>();

        for (Join join : joins) {
            TableDescription tableDescription = resolver.getTableDescription(join.getTable());
            String[] columnNames = tableDescription.getColumnNamesWithTablePrefix();

            Collections.addAll(joinColumns, columnNames);

            if (join.getJoin() != null) {
                String[] innerJoinColumns = getJoinColumns(new Join[] { join.getJoin() }, resolver);
                Collections.addAll(joinColumns, innerJoinColumns);
            }
        }

        String[] columns = new String[joinColumns.size()];
        return joinColumns.toArray(columns);
    }

    private String getJoinType(Join join) {
        if (join instanceof InnerJoin) {
            return "INNER JOIN";
        } else if (join instanceof CrossInnerJoin) {
            return "CROSS INNER JOIN";
        } else if (join instanceof LeftOuterJoin) {
            return "LEFT OUTER JOIN";
        } else if (join instanceof NaturalInnerJoin) {
            return "NATURAL INNER JOIN";
        } else if (join instanceof NaturalLeftOuterJoin) {
            return "NATURAL LEFT OUTER JOIN";
        }

        return "INNER JOIN";
    }
}
