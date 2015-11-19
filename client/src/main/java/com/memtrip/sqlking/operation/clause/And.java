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
package com.memtrip.sqlking.operation.clause;

/**
 * @author Samuel Kirton <a href="mailto:sam@memtrip.com" />
 */
public class And implements Clause {
    private Clause[] mClause;

    public Clause[] getClause() {
        return mClause;
    }

    public And(Clause... clause) {
        mClause = clause;
    }
}