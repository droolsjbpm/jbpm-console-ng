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

package org.jbpm.workbench.pr.client.editors.instance.list;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.DataSetLookupFactory;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jbpm.workbench.common.client.filters.basic.BasicFiltersPresenter;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.kie.api.runtime.process.ProcessInstance;
import org.uberfire.client.annotations.WorkbenchScreen;

import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.jbpm.workbench.common.client.PerspectiveIds.PROCESS_INSTANCE_LIST_BASIC_FILTERS_SCREEN;
import static org.jbpm.workbench.pr.model.ProcessInstanceDataSetConstants.*;

@Dependent
@WorkbenchScreen(identifier = PROCESS_INSTANCE_LIST_BASIC_FILTERS_SCREEN)
public class ProcessInstanceListBasicFiltersPresenter extends BasicFiltersPresenter {

    private Constants constants = Constants.INSTANCE;

    @Override
    protected String getAdvancedFilterPopupTitle() {
        return constants.New_Process_InstanceList();
    }

    @Inject
    public void setFilterSettingsManager(final ProcessInstanceListFilterSettingsManager filterSettingsManager) {
        super.setFilterSettingsManager(filterSettingsManager);
    }

    @Override
    public void loadFilters() {
        view.addNumericFilter(constants.Id(),
                              constants.FilterByProcessInstanceId(),
                              f -> addSearchFilter(f,
                                                   equalsTo(COLUMN_PROCESS_INSTANCE_ID,
                                                            f.getValue()))
        );

        view.addTextFilter(constants.Initiator(),
                           constants.FilterByInitiator(),
                           f -> addSearchFilter(f,
                                                likeTo(COLUMN_IDENTITY,
                                                       f.getValue(),
                                                       false))
        );

        view.addTextFilter(constants.Correlation_Key(),
                           constants.FilterByCorrelationKey(),
                           f -> addSearchFilter(f,
                                                likeTo(COLUMN_CORRELATION_KEY,
                                                       f.getValue(),
                                                       false))
        );

        view.addTextFilter(constants.Process_Instance_Description(),
                           constants.FilterByDescription(),
                           f -> addSearchFilter(f,
                                                likeTo(COLUMN_PROCESS_INSTANCE_DESCRIPTION,
                                                       f.getValue(),
                                                       false))
        );

        final Map<String, String> states = new HashMap<>();
        states.put(String.valueOf(ProcessInstance.STATE_ACTIVE),
                   constants.Active());
        states.put(String.valueOf(ProcessInstance.STATE_ABORTED),
                   constants.Aborted());
        states.put(String.valueOf(ProcessInstance.STATE_COMPLETED),
                   constants.Completed());
        states.put(String.valueOf(ProcessInstance.STATE_PENDING),
                   constants.Pending());
        states.put(String.valueOf(ProcessInstance.STATE_SUSPENDED),
                   constants.Suspended());
        view.addSelectFilter(constants.State(),
                             states,
                             false,
                             f -> addSearchFilter(f,
                                                  equalsTo(COLUMN_STATUS,
                                                           f.getValue()))
        );

        final Map<String, String> errorOptions = new HashMap<>();
        errorOptions.put(String.valueOf(true),
                         constants.HasAtLeastOneError());
        errorOptions.put(String.valueOf(false),
                         constants.HasNoErrors());
        final Function<String, ColumnFilter> errorFilterGenerator = (String hasErrors) ->
                (Boolean.valueOf(hasErrors) ? greaterThan(COLUMN_ERROR_COUNT,
                                                          0) : lowerOrEqualsTo(COLUMN_ERROR_COUNT,
                                                                               0));
        view.addSelectFilter(constants.Errors(),
                             errorOptions,
                             false,
                             f -> addSearchFilter(f,
                                                  errorFilterGenerator.apply(f.getValue())));

        final DataSetLookup dataSetLookup = DataSetLookupFactory.newDataSetLookupBuilder()
                .dataset(PROCESS_INSTANCE_DATASET)
                .group(COLUMN_PROCESS_NAME)
                .column(COLUMN_PROCESS_NAME)
                .sort(COLUMN_PROCESS_NAME,
                      SortOrder.ASCENDING)
                .buildLookup();
        view.addDataSetSelectFilter(constants.Name(),
                                    dataSetLookup,
                                    COLUMN_PROCESS_NAME,
                                    COLUMN_PROCESS_NAME,
                                    f -> addSearchFilter(f,
                                                         equalsTo(COLUMN_PROCESS_NAME,
                                                                  f.getValue())));

        view.addDateRangeFilter(constants.Start_Date(),
                                constants.Start_Date_Placeholder(),
                                true,
                                f -> addSearchFilter(f,
                                                     between(COLUMN_START,
                                                             f.getValue().getStartDate(),
                                                             f.getValue().getEndDate()))
        );

        view.addDateRangeFilter(constants.Last_Modification_Date(),
                                constants.Last_Modification_Date_Placeholder(),
                                true,
                                f -> addSearchFilter(f,
                                                     between(COLUMN_LAST_MODIFICATION_DATE,
                                                             f.getValue().getStartDate(),
                                                             f.getValue().getEndDate()))
        );
    }
}
