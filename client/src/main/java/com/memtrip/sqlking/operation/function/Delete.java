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
package com.memtrip.sqlking.operation.function;

import com.memtrip.sqlking.Model;
import com.memtrip.sqlking.database.SQLProvider;
import com.memtrip.sqlking.operation.clause.Clause;

/**
 * @author Samuel Kirton <a href="mailto:sam@memtrip.com" />
 */
public class Delete {
    private Clause[] mConditions;

    public Clause[] getConditions() {
        return mConditions;
    }

    private Delete(Clause[] conditions) {
        mConditions = conditions;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Clause[] mClause;

        private Builder() { }

        public Builder where(Clause... clause) {
            mClause = clause;
            return this;
        }

        public boolean execute(Class<? extends Model> classDef, SQLProvider sqlProvider) {
            return sqlProvider.delete(
                    new Delete(mClause),
                    sqlProvider.getResolver().getSQLQuery(classDef)
            );
        }
    }
}