/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.workbench.es.client.editors.errorlist;

import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.Dependent;

import org.jbpm.workbench.df.client.filter.FilterSettingsManagerImpl;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.es.client.i18n.Constants;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.jbpm.workbench.es.model.ExecutionErrorDataSetConstants.*;

@Dependent
public class ExecutionErrorListFilterSettingsManager extends FilterSettingsManagerImpl {

    private static final String EXECUTION_ERROR_LIST_PREFIX = "DS_ExecutionErrorListGrid";
    protected static final String TAB_ALL = EXECUTION_ERROR_LIST_PREFIX + "_0";
    protected static final String TAB_ACK = EXECUTION_ERROR_LIST_PREFIX + "_1";
    protected static final String TAB_NEW = EXECUTION_ERROR_LIST_PREFIX + "_2";

    private Constants constants = Constants.INSTANCE;

    @Override
    public String getGridGlobalPreferencesKey() {
        return EXECUTION_ERROR_LIST_PREFIX;
    }

    @Override
    public FilterSettings createFilterSettingsPrototype() {
        return createFilterSettings(EXECUTION_ERROR_LIST_DATASET,
                                    COLUMN_ERROR_DATE,
                                    null);
    }

    @Override
    public List<FilterSettings> initDefaultFilters() {
        return Arrays.asList(
                //Filter All
                createFilterSettings(EXECUTION_ERROR_LIST_DATASET,
                                     COLUMN_ERROR_DATE,
                                     null,
                                     TAB_ALL,
                                     constants.All(),
                                     constants.FilterAll()),

                //Filter Unacknowledged
                createFilterSettings(EXECUTION_ERROR_LIST_DATASET,
                                     COLUMN_ERROR_DATE,
                                     builder -> builder.filter(equalsTo(COLUMN_ERROR_ACK,
                                                                        "0")),
                                     TAB_NEW,
                                     constants.New(),
                                     constants.UnacknowledgedErrors()),

                //Filter Acknowledged
                createFilterSettings(EXECUTION_ERROR_LIST_DATASET,
                                     COLUMN_ERROR_DATE,
                                     builder -> builder.filter(equalsTo(COLUMN_ERROR_ACK,
                                                                        "1")),
                                     TAB_ACK,
                                     constants.Acknowledged(),
                                     constants.AcknowledgedErrors())
        );
    }
}
