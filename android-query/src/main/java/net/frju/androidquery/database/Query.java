package net.frju.androidquery.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import net.frju.androidquery.operation.condition.Condition;
import net.frju.androidquery.operation.condition.In;
import net.frju.androidquery.operation.condition.Where;
import net.frju.androidquery.operation.function.Count;
import net.frju.androidquery.operation.function.CursorResult;
import net.frju.androidquery.operation.function.Delete;
import net.frju.androidquery.operation.function.Insert;
import net.frju.androidquery.operation.function.Save;
import net.frju.androidquery.operation.function.Select;
import net.frju.androidquery.operation.function.Update;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public abstract class Query {

    protected static int save(Save save, Class<?> classDef, DatabaseProvider databaseProvider) {
        int nb = 0;

        TableDescription table = databaseProvider.getResolver().getTableDescription(classDef);
        boolean isPrimaryKeyAutoIncrement = table.isPrimaryKeyAutoIncrement();

        ArrayList<Object> modelsToInsert = new ArrayList<>();
        for (Object model : save.getModels()) {
            long id = 1; // first valid autoincrement id is always >= 1
            if (isPrimaryKeyAutoIncrement) {
                // Try to guess if we for sure need to insert thanks to primary key
                Object primaryKeyValue = table.getPrimaryKeyValue(model);
                id = Long.parseLong(primaryKeyValue.toString()); // should be a short, int or long
            }

            //noinspection unchecked
            if (id <= 0 || Update.getBuilder(classDef, databaseProvider).model(model).query() <= 0) {
                modelsToInsert.add(model);
            } else {
                nb++;
            }
        }

        nb += Insert.getBuilder(databaseProvider, modelsToInsert.toArray()).query();

        return nb;
    }

    protected static int insert(Insert insert, Class<?> classDef, DatabaseProvider databaseProvider) {
        if (insert.getModels() != null && insert.getModels().length > 0) {
            Object[] models = insert.getModels();
            ContentValues[] valuesArray = new ContentValues[models.length];
            TableDescription tableDescription = getTableDescription(classDef, databaseProvider);
            for (int i = 0; i < models.length; i++) {
                if (models[i] instanceof ModelListener) {
                    ((ModelListener) models[i]).onPreInsert();
                }
                valuesArray[i] = tableDescription.getContentValues(models[i]);
            }

            if (models.length == 1) {
                long newId = databaseProvider.insert(tableDescription.getTableRealName(), valuesArray[0]);
                if (newId != -1) {
                    tableDescription.setIdToModel(models[0], newId);
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return databaseProvider.bulkInsert(tableDescription.getTableRealName(), valuesArray);
            }
        }

        return 0;
    }

    protected static Cursor selectCursor(Select select, Class<?> classDef, DatabaseProvider databaseProvider) {

        TableDescription tableDescription = getTableDescription(classDef, databaseProvider);

        return databaseProvider.query(
                tableDescription.getTableRealName(),
                select.getJoin() != null ? tableDescription.getColumnNamesWithTablePrefix() : tableDescription.getColumnNames(),
                select.getClause(),
                select.getJoin(),
                null,
                null,
                select.getOrderBy(),
                select.getLimit()
        );
    }

    protected static <T> CursorResult<T> select(Select select, Class<T> classDef, DatabaseProvider databaseProvider) {
        Cursor cursor = selectCursor(select, classDef, databaseProvider);
        return new CursorResult<>(classDef, databaseProvider.getResolver(), cursor);
    }

    protected static <T> T selectSingle(Select select, Class<T> classDef, DatabaseProvider databaseProvider) {
        Cursor cursor = selectCursor(select, classDef, databaseProvider);

        T[] results = getTableDescription(classDef, databaseProvider).getArrayResult(cursor);

        if (results != null && results.length > 0) {
            return results[0];
        } else {
            return null;
        }
    }

    protected static int update(Update update, Class<?> classDef, DatabaseProvider databaseProvider) {
        Object[] models = update.getModels();
        if (models != null) {
            TableDescription tableDesc = getTableDescription(classDef, databaseProvider);
            String primaryKeyName = tableDesc.getPrimaryKeyRealName();
            ContentValues[] valuesArray = new ContentValues[models.length];
            Condition[][] conditionsArray = new Condition[models.length][];
            for (int i = 0; i < models.length; i++) {
                Object model = models[i];

                conditionsArray[i] = update.getConditions();
                if (conditionsArray[i] == null) {
                    if (TextUtils.isEmpty(primaryKeyName)) {
                        throw new IllegalStateException("update with model() method require a primary key");
                    }
                    conditionsArray[i] = new Condition[1];
                    conditionsArray[i][0] = Where.where(primaryKeyName, Where.Op.IS, tableDesc.getPrimaryKeyValue(model));
                }

                if (model instanceof ModelListener) {
                    ((ModelListener) model).onPreUpdate();
                }

                valuesArray[i] = tableDesc.getContentValues(model);
            }
            return databaseProvider.bulkUpdate(
                    tableDesc.getTableRealName(),
                    valuesArray,
                    conditionsArray
            );
        } else {
            return databaseProvider.bulkUpdate(
                    getTableDescription(classDef, databaseProvider).getTableRealName(),
                    new ContentValues[]{update.getContentValues()},
                    new Condition[][]{update.getConditions()}
            );
        }
    }

    protected static long count(Count count, Class<?> classDef, DatabaseProvider databaseProvider) {
        return databaseProvider.count(
                getTableDescription(classDef, databaseProvider).getTableRealName(),
                count.getClause()
        );
    }

    protected static int delete(Delete delete, Class<?> classDef, DatabaseProvider databaseProvider) {
        Object[] models = delete.getModels();

        if (models != null) {
            TableDescription tableDesc = getTableDescription(classDef, databaseProvider);
            String primaryKeyName = tableDesc.getPrimaryKeyRealName();
            if (TextUtils.isEmpty(primaryKeyName)) {
                throw new IllegalStateException("delete with model() method require a primary key");
            }

            Object[] keys = new String[delete.getModels().length];
            for (int i = 0; i < models.length; i++) {
                keys[i] = tableDesc.getPrimaryKeyValue(models[i]);

                if (models[i] instanceof ModelListener) {
                    ((ModelListener) models[i]).onPreDelete();
                }
            }

            Condition condition = new In(primaryKeyName, keys);

            return databaseProvider.delete(
                    tableDesc.getTableRealName(),
                    new Condition[]{condition}
            );
        } else {
            return databaseProvider.delete(
                    getTableDescription(classDef, databaseProvider).getTableRealName(),
                    delete.getConditions()
            );
        }
    }

    protected static Cursor rawQuery(String query, DatabaseProvider databaseProvider) {
        return databaseProvider.rawQuery(query);
    }

    protected static <T> rx.Observable<T> wrapRx(final Callable<T> func) {
        return rx.Observable.create(
                new rx.Observable.OnSubscribe<T>() {
                    @Override
                    public void call(rx.Subscriber<? super T> subscriber) {
                        try {
                            subscriber.onNext(func.call());
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                }
        );
    }

    protected static <T> Observable<T> wrapRx2(final Callable<T> func) {
        return Observable.create(
                new ObservableOnSubscribe<T>() {
                    @Override
                    public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                        try {
                            emitter.onNext(func.call());
                        } catch (Exception e) {
                            emitter.onError(e);
                        }
                    }
                }
        );
    }

    private static TableDescription getTableDescription(Class<?> classDef, DatabaseProvider databaseProvider) {
        return databaseProvider.getResolver().getTableDescription(classDef);
    }
}
