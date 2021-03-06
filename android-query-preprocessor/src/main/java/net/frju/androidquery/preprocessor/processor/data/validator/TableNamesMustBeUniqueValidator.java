package net.frju.androidquery.preprocessor.processor.data.validator;

import net.frju.androidquery.preprocessor.processor.Validator;
import net.frju.androidquery.preprocessor.processor.ValidatorException;
import net.frju.androidquery.preprocessor.processor.data.Data;
import net.frju.androidquery.preprocessor.processor.data.Table;

import java.util.List;

public class TableNamesMustBeUniqueValidator implements Validator {
    private final Data mData;

    public TableNamesMustBeUniqueValidator(Data data) {
        mData = data;
    }

    private Table findDuplicateTable(List<Table> tables) {
        for (Table table : tables) {
            Table duplicateTable = getDuplicateTable(table, tables);
            if (duplicateTable != null) {
                return duplicateTable;
            }
        }

        return null;
    }

    private Table getDuplicateTable(Table check, List<Table> tables) {
        int occurrences = 0;

        for (Table table : tables) {
            if (check.getName().equals(table.getName())) occurrences++;
            if (occurrences > 1) return table;
        }

        return null;
    }

    @Override
    public void validate() throws ValidatorException {
        Table table = findDuplicateTable(mData.getTables());
        if (table != null) {
            throw new ValidatorException(table.getElement(), "[The @Table: `" + table.getName() + "` is duplicated, table names must be unique]");
        }
    }
}
