/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.ht.client.editors.taskslist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.Range;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.DataSetOp;
import org.dashbuilder.dataset.DataSetOpType;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.filter.ColumnFilter;
import org.dashbuilder.dataset.filter.CoreFunctionFilter;
import org.dashbuilder.dataset.filter.CoreFunctionType;
import org.dashbuilder.dataset.filter.DataSetFilter;
import org.dashbuilder.dataset.sort.SortOrder;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.Group;
import org.jbpm.workbench.common.client.dataset.AbstractDataSetReadyCallback;
import org.jbpm.workbench.common.client.list.AbstractMultiGridPresenter;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.common.client.list.MultiGridView;
import org.jbpm.workbench.common.client.menu.RestoreDefaultFiltersMenuBuilder;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.list.base.DataSetQueryHelper;
import org.jbpm.workbench.ht.client.resources.i18n.Constants;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.jbpm.workbench.ht.model.events.NewTaskEvent;
import org.jbpm.workbench.ht.model.events.TaskCompletedEvent;
import org.jbpm.workbench.ht.model.events.TaskRefreshedEvent;
import org.jbpm.workbench.ht.model.events.TaskSelectionEvent;
import org.jbpm.workbench.ht.service.TaskService;
import org.uberfire.client.mvp.PlaceStatus;
import org.uberfire.client.workbench.widgets.common.ErrorPopupPresenter;
import org.uberfire.ext.widgets.common.client.menu.RefreshMenuBuilder;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.menu.MenuFactory;
import org.uberfire.workbench.model.menu.Menus;

import static org.dashbuilder.dataset.filter.FilterFactory.*;
import static org.jbpm.workbench.common.client.util.TaskUtils.*;
import static org.jbpm.workbench.ht.model.TaskDataSetConstants.*;

public abstract class AbstractTaskListPresenter<V extends AbstractTaskListPresenter.TaskListView> extends AbstractMultiGridPresenter<TaskSummary, V> {

    public interface TaskListView<T extends AbstractTaskListPresenter> extends MultiGridView<TaskSummary, T> {

        void addDomainSpecifColumns(ExtendedPagedTable<TaskSummary> extendedPagedTable, Set<String> columns);

        FilterSettings getVariablesTableSettings(String processName);

        void setSelectedTask(TaskSummary selectedTask);

    }

    protected Constants constants = Constants.INSTANCE;

    private Caller<TaskService> taskService;

    private DataSetQueryHelper dataSetQueryHelperDomainSpecific;

    @Inject
    private ErrorPopupPresenter errorPopup;

    @Inject
    private Event<TaskSelectionEvent> taskSelected;


    @Override
    public void getData(final Range visibleRange) {
        try {
            if (!isAddingDefaultFilters()) {
                FilterSettings currentTableSettings = dataSetQueryHelper.getCurrentTableSettings();
                currentTableSettings.setServerTemplateId(selectedServerTemplate);
                currentTableSettings.setTablePageSize(view.getListGrid().getPageSize());
                ColumnSortList columnSortList = view.getListGrid().getColumnSortList();
                if (columnSortList != null && columnSortList.size() > 0) {
                    dataSetQueryHelper.setLastOrderedColumn(columnSortList.size() > 0 ? columnSortList.get(0).getColumn().getDataStoreName() : "");
                    dataSetQueryHelper.setLastSortOrder((columnSortList.size() > 0) && columnSortList.get(0).isAscending() ? SortOrder.ASCENDING : SortOrder.DESCENDING);
                } else {
                    dataSetQueryHelper.setLastOrderedColumn(COLUMN_CREATED_ON);
                    dataSetQueryHelper.setLastSortOrder(SortOrder.ASCENDING);
                }

                final List<ColumnFilter> filters = getColumnFilters(textSearchStr);
                if (filters.isEmpty() == false) {
                    if (currentTableSettings.getDataSetLookup().getFirstFilterOp() != null) {
                        currentTableSettings.getDataSetLookup().getFirstFilterOp().addFilterColumn(OR(filters));
                    } else {
                        final DataSetFilter filter = new DataSetFilter();
                        filter.addFilterColumn(OR(filters));
                        currentTableSettings.getDataSetLookup().addOperation(filter);
                    }
                }
                dataSetQueryHelper.setDataSetHandler(currentTableSettings);
                dataSetQueryHelper.lookupDataSet(visibleRange.getStart(), createDataSetTaskCallback(visibleRange.getStart(), currentTableSettings));
            }
        } catch (Exception e) {
            errorPopup.showMessage(constants.UnexpectedError(e.getMessage()));
        }

    }

    protected List<ColumnFilter> getColumnFilters(final String searchString) {
        final List<ColumnFilter> filters = new ArrayList<ColumnFilter>();
        if (searchString != null && searchString.trim().length() > 0) {
            try {
                final Long taskId = Long.valueOf(searchString.trim());
                filters.add(equalsTo(COLUMN_TASK_ID, taskId));
            } catch (NumberFormatException ex) {
                filters.add(likeTo(COLUMN_NAME, "%" + searchString.toLowerCase() + "%", false));
                filters.add(likeTo(COLUMN_DESCRIPTION, "%" + searchString.toLowerCase() + "%", false));
                filters.add(likeTo(COLUMN_PROCESS_ID, "%" + searchString.toLowerCase() + "%", false));
            }
        }
        return filters;
    }

    /**
     * Generates a dataset filter depending of the user roles and the kind of dataset.
     * <br>In case of the adminDataset (isAdminDateset=true), retrieve the tasks that are accessible for the user logged
     * roles, without restriction over the task owner.
     * <br>In other cases, retrieve the tasks available for the user logged roles AND without owner(claimed by the groups
     * members) OR the user logged owned tasks
     * @param isAdminDataset true if the filter to create is an adminDataSet
     * @return the dynamic filter to add, depeding on the user logged roles and the kind of dataset
     */
    protected ColumnFilter getUserGroupFilters(boolean isAdminDataset) {
        Set<Group> groups = identity.getGroups();
        List<ColumnFilter> condList = new ArrayList<ColumnFilter>();
        for (Group g : groups) {
            condList.add(equalsTo(COLUMN_ORGANIZATIONAL_ENTITY, g.getName()));

        }
        condList.add(equalsTo(COLUMN_ORGANIZATIONAL_ENTITY, identity.getIdentifier()));
        ColumnFilter myGroupFilter;
        if (isAdminDataset) {
            return OR(COLUMN_ORGANIZATIONAL_ENTITY, condList);
        } else {
            myGroupFilter = AND(OR(condList),
                    OR(equalsTo(COLUMN_ACTUAL_OWNER, ""), isNull(COLUMN_ACTUAL_OWNER)));
            return OR(myGroupFilter, equalsTo(COLUMN_ACTUAL_OWNER, identity.getIdentifier()));
        }
    }

    protected DataSetReadyCallback createDataSetTaskCallback(final int startRange, final FilterSettings tableSettings) {
        return new AbstractDataSetReadyCallback(errorPopup, view, tableSettings.getUUID()) {

            @Override
            public void callback(DataSet dataSet) {
                if (dataSet != null && dataSetQueryHelper.getCurrentTableSettings().getKey().equals(tableSettings.getKey())) {
                    final List<TaskSummary> myTasksFromDataSet = new ArrayList<TaskSummary>();

                    for (int i = 0; i < dataSet.getRowCount(); i++) {
                        myTasksFromDataSet.add(createTaskSummaryFromDataSet(dataSet, i));

                    }
                    List<DataSetOp> ops = tableSettings.getDataSetLookup().getOperationList();
                    String filterValue = isFilteredByTaskName(ops); //Add here the check to add the domain data columns taskName?

                    boolean lastPageExactCount = false;
                    if( dataSet.getRowCount() < view.getListGrid().getPageSize()) {
                        lastPageExactCount = true;
                    }

                    if (filterValue != null) {
                        getDomainSpecifDataForTasks(startRange, filterValue, myTasksFromDataSet, lastPageExactCount);
                    } else {
                        updateDataOnCallback(myTasksFromDataSet, startRange,startRange + myTasksFromDataSet.size(), lastPageExactCount);
                    }

                }
                view.hideBusyIndicator();
            }

        };
    }

    protected String isFilteredByTaskName(List<DataSetOp> ops) {
        for (DataSetOp dataSetOp : ops) {
            if (dataSetOp.getType().equals(DataSetOpType.FILTER)) {
                List<ColumnFilter> filters = ((DataSetFilter) dataSetOp).getColumnFilterList();

                for (ColumnFilter filter : filters) {

                    if (filter instanceof CoreFunctionFilter) {
                        CoreFunctionFilter coreFilter = ((CoreFunctionFilter) filter);
                        if (filter.getColumnId().toUpperCase().equals(COLUMN_NAME.toUpperCase()) &&
                                ((CoreFunctionFilter) filter).getType() == CoreFunctionType.EQUALS_TO) {

                            List parameters = coreFilter.getParameters();
                            if (parameters.size() > 0) {
                                return parameters.get(0).toString();
                            }
                        }
                    }
                }
            }
        }

        return null;

    }

    public void getDomainSpecifDataForTasks(final int startRange, String filterValue, final List<TaskSummary> myTasksFromDataSet, boolean lastPageExactCount) {

        FilterSettings variablesTableSettings = view.getVariablesTableSettings(filterValue);
        variablesTableSettings.setTablePageSize(-1);
        variablesTableSettings.setServerTemplateId(selectedServerTemplate);

        dataSetQueryHelperDomainSpecific.setDataSetHandler(variablesTableSettings);
        dataSetQueryHelperDomainSpecific.setCurrentTableSettings(variablesTableSettings);
        dataSetQueryHelperDomainSpecific.setLastOrderedColumn(COLUMN_TASK_ID);
        dataSetQueryHelperDomainSpecific.setLastSortOrder(SortOrder.ASCENDING);

        List<Comparable> tasksIds = new ArrayList<Comparable>();
        for (TaskSummary task : myTasksFromDataSet) {
            tasksIds.add(task.getTaskId());
        }
        DataSetFilter filter = new DataSetFilter();
        ColumnFilter filter1 = equalsTo(COLUMN_TASK_VARIABLE_TASK_ID, tasksIds);
        filter.addFilterColumn(filter1);
        variablesTableSettings.getDataSetLookup().addOperation(filter);

        dataSetQueryHelperDomainSpecific.lookupDataSet(0, createDataSetDomainSpecificCallback(startRange, myTasksFromDataSet, variablesTableSettings, lastPageExactCount));

    }

    protected DataSetReadyCallback createDataSetDomainSpecificCallback(final int startRange, final List<TaskSummary> instances, final FilterSettings tableSettings, boolean lastPageExactCount) {
        return new AbstractDataSetReadyCallback(errorPopup, view, tableSettings.getUUID()) {
            @Override
            public void callback(DataSet dataSet) {
                if (dataSet.getRowCount() > 0) {
                    Set<String> columns = new HashSet<String>();
                    for (int i = 0; i < dataSet.getRowCount(); i++) {
                        Long taskId = dataSetQueryHelperDomainSpecific.getColumnLongValue(dataSet, COLUMN_TASK_ID, i);
                        String variableName = dataSetQueryHelperDomainSpecific.getColumnStringValue(dataSet, COLUMN_TASK_VARIABLE_NAME, i);
                        String variableValue = dataSetQueryHelperDomainSpecific.getColumnStringValue(dataSet, COLUMN_TASK_VARIABLE_VALUE, i);

                        for (TaskSummary task : instances) {
                            if (task.getTaskId().equals(taskId)) {
                                task.addDomainData(variableName, variableValue);
                                columns.add(variableName);
                            }
                        }
                    }
                    view.addDomainSpecifColumns(view.getListGrid(), columns);
                }
                updateDataOnCallback(instances, startRange, startRange + instances.size(), lastPageExactCount);
            }

        };
    }

    protected TaskSummary createTaskSummaryFromDataSet(final DataSet dataSet, int i) {
        return new TaskSummary(
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_TASK_ID, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_NAME, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_DESCRIPTION, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_STATUS, i),
                dataSetQueryHelper.getColumnIntValue(dataSet, COLUMN_PRIORITY, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_ACTUAL_OWNER, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_CREATED_BY, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_CREATED_ON, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_ACTIVATION_TIME, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_DUE_DATE, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_PROCESS_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PROCESS_SESSION_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PROCESS_INSTANCE_ID, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_DEPLOYMENT_ID, i),
                dataSetQueryHelper.getColumnLongValue(dataSet, COLUMN_PARENT_ID, i),
                dataSetQueryHelper.getColumnDateValue(dataSet, COLUMN_LAST_MODIFICATION_DATE, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_PROCESS_INSTANCE_CORRELATION_KEY, i),
                dataSetQueryHelper.getColumnStringValue(dataSet, COLUMN_PROCESS_INSTANCE_DESCRIPTION, i),
                HUMAN_TASKS_WITH_ADMIN_DATASET.equals(dataSet.getUUID()));
    }

    public void releaseTask(final TaskSummary task) {
        taskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskReleased(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                }).releaseTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(), task.getTaskId(), task.getTaskName() ) );
    }

    public void claimTask(final TaskSummary task) {
        taskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskClaimed(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                }
        ).claimTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(), task.getTaskId(), task.getTaskName() ) );
    }
    
    public void resumeTask(final TaskSummary task) {
        taskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskResumed(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                }
        ).resumeTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(), task.getTaskId(), task.getTaskName() ) );
    }
    
    public void suspendTask(final TaskSummary task) {
        taskService.call(
                new RemoteCallback<Void>() {
                    @Override
                    public void callback(Void nothing) {
                        view.displayNotification(constants.TaskSuspended(String.valueOf(task.getTaskId())));
                        refreshGrid();
                    }
                }
        ).suspendTask(selectedServerTemplate, task.getDeploymentId(), task.getTaskId());
        taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, task.getDeploymentId(), task.getTaskId(), task.getTaskName() ) );
    }
    
    public Menus getMenus(){ //To be used by subclass methods annotated with @WorkbenchMenu
        return MenuFactory
            .newTopLevelCustomMenu(serverTemplateSelectorMenuBuilder).endMenu()
            .newTopLevelCustomMenu(new RefreshMenuBuilder(this)).endMenu()
            .newTopLevelCustomMenu(refreshSelectorMenuBuilder).endMenu()
            .newTopLevelCustomMenu(new RestoreDefaultFiltersMenuBuilder(this)).endMenu()
            .build();
    }

    public void selectTask(final TaskSummary summary, final Boolean close) {
        final DefaultPlaceRequest defaultPlaceRequest = new DefaultPlaceRequest( "Task Details Multi" );
        final PlaceStatus status = placeManager.getStatus( defaultPlaceRequest );
        boolean logOnly = false;
        if ( summary.getStatus().equals( TASK_STATUS_COMPLETED ) && summary.isLogOnly() ) {
            logOnly = true;
        }
        if ( status == PlaceStatus.CLOSE ) {
            placeManager.goTo( defaultPlaceRequest );
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, summary.getDeploymentId(), summary.getTaskId(), summary.getTaskName(), summary.isForAdmin(), logOnly ) );
        } else if ( status == PlaceStatus.OPEN && !close ) {
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, summary.getDeploymentId(),summary.getTaskId(), summary.getTaskName(), summary.isForAdmin(), logOnly ) );
        } else if ( status == PlaceStatus.OPEN && close ) {
            placeManager.closePlace( "Task Details Multi" );
        }
    }

    public void refreshNewTask( @Observes NewTaskEvent newTask ) {
        refreshGrid();
        PlaceStatus status = placeManager.getStatus( new DefaultPlaceRequest( "Task Details Multi" ) );
        if ( status == PlaceStatus.OPEN ) {
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, null, newTask.getNewTaskId(), newTask.getNewTaskName() ) );
        } else {
            placeManager.goTo( "Task Details Multi" );
            taskSelected.fire( new TaskSelectionEvent( selectedServerTemplate, null, newTask.getNewTaskId(), newTask.getNewTaskName() ) );
        }

        view.setSelectedTask(new TaskSummary( newTask.getNewTaskId(), newTask.getNewTaskName() ));
    }

    public void onTaskRefreshedEvent( @Observes TaskRefreshedEvent event ) {
        refreshGrid();
    }

    public void onTaskCompletedEvent( @Observes TaskCompletedEvent event ) {
        refreshGrid();
    }

    @Inject
    public void setDataSetQueryHelperDomainSpecific(final DataSetQueryHelper dataSetQueryHelperDomainSpecific) {
        this.dataSetQueryHelperDomainSpecific = dataSetQueryHelperDomainSpecific;
    }

    @Inject
    public void setTaskService(final Caller<TaskService> taskService) {
        this.taskService = taskService;
    }

    @Override
    public void setupAdvanceSearchView() {
        view.addNumericFilter(constants.Id(),
                              constants.FilterByTaskId(),
                              v -> addAdvancedSearchFilter(equalsTo(COLUMN_TASK_ID,
                                                                    v)),
                              v -> removeAdvancedSearchFilter(equalsTo(COLUMN_TASK_ID,
                                                                       v))
        );

        view.addTextFilter(constants.Task(),
                           constants.FilterByTaskName(),
                           v -> addAdvancedSearchFilter(equalsTo(COLUMN_NAME,
                                                                 v)),
                           v -> removeAdvancedSearchFilter(equalsTo(COLUMN_NAME,
                                                                    v))
        );

        final Map<String, String> status = getStatusByType(TaskType.ALL).stream().sorted().collect(Collectors.toMap(Function.identity(),
                                                                                                                    Function.identity()));
        view.addSelectFilter(constants.Status(),
                             status,
                             false,
                             v -> addAdvancedSearchFilter(equalsTo(COLUMN_STATUS,
                                                                   v)),
                             v -> removeAdvancedSearchFilter(equalsTo(COLUMN_STATUS,
                                                                      v))
        );

        view.addTextFilter(constants.Process_Instance_Correlation_Key(),
                           constants.FilterByCorrelationKey(),
                           v -> addAdvancedSearchFilter(equalsTo(COLUMN_PROCESS_INSTANCE_CORRELATION_KEY,
                                                                 v)),
                           v -> removeAdvancedSearchFilter(equalsTo(COLUMN_PROCESS_INSTANCE_CORRELATION_KEY,
                                                                    v))
        );

        view.addTextFilter(constants.Actual_Owner(),
                           constants.FilterByActualOwner(),
                           v -> addAdvancedSearchFilter(equalsTo(COLUMN_ACTUAL_OWNER,
                                                                 v)),
                           v -> removeAdvancedSearchFilter(equalsTo(COLUMN_ACTUAL_OWNER,
                                                                    v))
        );

        view.addTextFilter(constants.Process_Instance_Description(),
                           constants.FilterByProcessInstanceDescription(),
                           v -> addAdvancedSearchFilter(equalsTo(COLUMN_PROCESS_INSTANCE_DESCRIPTION,
                                                                 v)),
                           v -> removeAdvancedSearchFilter(equalsTo(COLUMN_PROCESS_INSTANCE_DESCRIPTION,
                                                                    v))
        );

        //TODO missing creation date

        final FilterSettings settings = view.getAdvancedSearchFilterSettings();
        final DataSetFilter filter = new DataSetFilter();
        filter.addFilterColumn(equalsTo(COLUMN_STATUS,
                                        TASK_STATUS_READY));
        settings.getDataSetLookup().addOperation(filter);
        view.saveAdvancedSearchFilterSettings(settings);

        view.addActiveFilter(constants.Status(),
                             TASK_STATUS_READY,
                             TASK_STATUS_READY,
                             v -> removeAdvancedSearchFilter(equalsTo(COLUMN_STATUS,
                                                                      v))
        );

    }
}
