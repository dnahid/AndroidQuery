package net.frju.androidquery.preprocessor.processor.freemarker.method;

import net.frju.androidquery.preprocessor.processor.data.Column;
import net.frju.androidquery.preprocessor.processor.data.Data;
import net.frju.androidquery.preprocessor.processor.data.ForeignKey;
import net.frju.androidquery.preprocessor.processor.data.Table;
import net.frju.androidquery.preprocessor.processor.utils.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class AssembleCreateTableMethod implements TemplateMethodModelEx {

    private static final String ASSEMBLE_CREATE_TABLE = "assembleCreateTable";

    private final Data mData;

    public static Map<String, Object> getMethodMap(Data data) {
        Map<String, Object> map = new HashMap<>();
        map.put(ASSEMBLE_CREATE_TABLE, new AssembleCreateTableMethod(data));
        return map;
    }

    private AssembleCreateTableMethod(Data data) {
        mData = data;
    }

    /**
     * Build a create table statement based on the provided tableName and members
     * @param	table	The table that the statement will create
     * @return	A SQL statement that will create a table
     */
    private String buildCreateTableStatement(Table table, List<Table> tables) {
        StringBuilder statementBuilder = new StringBuilder();

        statementBuilder.append("CREATE TABLE ");
        statementBuilder.append(table.getRealName());
        statementBuilder.append(" (");

        for (int i = 0; i < table.getColumns().size(); i++) {
            Column column = table.getColumns().get(i);

            String columnSql = StringUtils.columnToSql(mData, tables, column);
            if (columnSql != null) {
                statementBuilder.append(columnSql);
                statementBuilder.append(",");
            }
        }

        for (ForeignKey foreignKey : table.getForeignKeys()) {
            statementBuilder.append("FOREIGN KEY(")
                    .append(foreignKey.getThisColumn()).append(") REFERENCES ")
                    .append(foreignKey.getTable())
                    .append("(")
                    .append(foreignKey.getForeignColumn())
                    .append("),");
        }

        if (statementBuilder.charAt(statementBuilder.length() - 1) == ',') {
            statementBuilder.deleteCharAt(statementBuilder.length() - 1);
        }

        statementBuilder.append(");");

        return "\"" + statementBuilder.toString() + "\";";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        Object tableNameValue = arguments.get(0);
        Object tablesValue = arguments.get(1);

        Table table;
        if (tableNameValue instanceof StringModel) {
            StringModel stringModel = (StringModel)tableNameValue;
            table = (Table)stringModel.getAdaptedObject(Table.class);
        } else {
            throw new IllegalStateException("The assembleCreateTable argument must be type of " +
                    "net.frju.androidquery.preprocessor.processor.data.Table");
        }

        List<Table> tables = Util.getTables(tablesValue);

        return buildCreateTableStatement(table, tables);
    }
}