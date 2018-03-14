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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.view.client.CellPreviewEvent;
import org.jbpm.workbench.common.client.list.AbstractMultiGridView;
import org.jbpm.workbench.common.client.list.ListTable;
import org.jbpm.workbench.common.client.util.ConditionalAction;
import org.jbpm.workbench.common.client.util.DateUtils;
import org.jbpm.workbench.ht.client.resources.HumanTaskResources;
import org.jbpm.workbench.ht.client.resources.i18n.Constants;
import org.jbpm.workbench.ht.model.TaskSummary;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.widgets.table.client.ColumnMeta;

import static org.jbpm.workbench.common.client.util.TaskUtils.TASK_STATUS_COMPLETED;
import static org.jbpm.workbench.ht.model.TaskDataSetConstants.*;

public abstract class AbstractTaskListView<P extends AbstractTaskListPresenter> extends AbstractMultiGridView<TaskSummary, P>
        implements AbstractTaskListPresenter.TaskListView<P> {

    protected final Constants constants = Constants.INSTANCE;

    @Override
    public List<String> getBannedColumns() {
        return Arrays.asList(COLUMN_NAME,
                             COL_ID_ACTIONS);
    }

    @Override
    public void initSelectionModel(final ListTable<TaskSummary> extendedPagedTable) {
        final RowStyles selectedStyles = new RowStyles<TaskSummary>() {

            @Override
            public String getStyleNames(TaskSummary row,
                                        int rowIndex) {
                if (row.getStatus().equals(TASK_STATUS_COMPLETED)) {
                    return HumanTaskResources.INSTANCE.css().taskCompleted();
                }
                return null;
            }
        };

        extendedPagedTable.setEmptyTableCaption(constants.No_Tasks_Found());
        extendedPagedTable.setSelectionCallback((task) -> presenter.selectTask(task));
        extendedPagedTable.setRowStyles(selectedStyles);
    }

    @Override
    public void initColumns(ListTable<TaskSummary> extendedPagedTable) {
        initCellPreview(extendedPagedTable);

        ColumnMeta<TaskSummary> actionsColumnMeta = initActionsColumn();
        extendedPagedTable.addSelectionIgnoreColumn(actionsColumnMeta.getColumn());

        List<ColumnMeta<TaskSummary>> columnMetas = new ArrayList<ColumnMeta<TaskSummary>>();
        columnMetas.add(new ColumnMeta<>(
                createNumberColumn(COLUMN_TASK_ID,
                                   task -> task.getId()),
                constants.Id()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_NAME,
                                 task -> task.getName()),
                constants.Task()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_DESCRIPTION,
                                 task -> task.getDescription()),
                constants.Description()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_PROCESS_ID,
                                 task -> task.getProcessId()),
                constants.Process_Definition_Id()
        ));
        columnMetas.add(new ColumnMeta<>(
                createNumberColumn(COLUMN_PROCESS_INSTANCE_ID,
                                   task -> task.getProcessInstanceId()),
                constants.Process_Instance_Id()
        ));
        columnMetas.add(new ColumnMeta<>(
                createNumberColumn(COLUMN_PRIORITY,
                                   task -> task.getPriority()),
                constants.Priority()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_STATUS,
                                 task -> task.getStatus()),
                constants.Status()
        ));
        final Column<TaskSummary, String> createdOnColumn = createTextColumn(COLUMN_CREATED_ON,
                                                                             task -> DateUtils.getDateTimeStr(task.getCreatedOn()));
        createdOnColumn.setDefaultSortAscending(false);
        columnMetas.add(new ColumnMeta<>(
                createdOnColumn,
                constants.Created_On()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_DUE_DATE,
                                 task -> DateUtils.getDateTimeStr(task.getExpirationTime())),
                constants.Due_On()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_ACTUAL_OWNER,
                                 task -> task.getActualOwner()),
                constants.Actual_Owner()));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_PROCESS_INSTANCE_CORRELATION_KEY,
                                 task -> task.getProcessInstanceCorrelationKey()),
                constants.Process_Instance_Correlation_Key()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_PROCESS_INSTANCE_DESCRIPTION,
                                 task -> task.getProcessInstanceDescription()),
                constants.Process_Instance_Description()
        ));
        columnMetas.add(new ColumnMeta<>(
                createTextColumn(COLUMN_LAST_MODIFICATION_DATE,
                                 task -> DateUtils.getDateTimeStr(task.getLastModificationDate())),
                constants.Last_Modification_Date()
        ));
        columnMetas.add(new ColumnMeta<>(
                createNumberColumn(COLUMN_PROCESS_SESSION_ID,
                                   task -> task.getProcessSessionId()),
                constants.ProcessSessionId()
        ));
        addNewColumn(extendedPagedTable,
                     columnMetas);
        columnMetas.add(actionsColumnMeta);

        List<GridColumnPreference> columPreferenceList = extendedPagedTable.getGridPreferencesStore().getColumnPreferences();
        for (GridColumnPreference colPref : columPreferenceList) {
            if (!isColumnAdded(columnMetas,
                               colPref.getName())) {
                Column<TaskSummary, ?> genericColumn = initGenericColumn(colPref.getName());
                genericColumn.setSortable(false);
                columnMetas.add(new ColumnMeta<TaskSummary>(genericColumn,
                                                            colPref.getName(),
                                                            true,
                                                            true));
            }
        }

        extendedPagedTable.setColumnWidth(actionsColumnMeta.getColumn(),
                                          ACTIONS_COLUMN_WIDTH,
                                          Style.Unit.PX);
        extendedPagedTable.addColumns(columnMetas);
        extendedPagedTable.getColumnSortList().push(createdOnColumn);
    }

    protected void addNewColumn(ListTable<TaskSummary> extendedPagedTable,
                                List<ColumnMeta<TaskSummary>> columnMetas) {
    }

    protected void initCellPreview(final ListTable<TaskSummary> extendedPagedTable) {
        extendedPagedTable.addCellPreviewHandler(new CellPreviewEvent.Handler<TaskSummary>() {

            @Override
            public void onCellPreview(final CellPreviewEvent<TaskSummary> event) {

                if (BrowserEvents.MOUSEOVER.equalsIgnoreCase(event.getNativeEvent().getType())) {
                    onMouseOverGrid(extendedPagedTable,
                                    event);
                }
            }
        });
    }

    protected void onMouseOverGrid(final ListTable<TaskSummary> extendedPagedTable,
                                   final CellPreviewEvent<TaskSummary> event) {
        TaskSummary task = event.getValue();

        if (task.getDescription() != null) {
            extendedPagedTable.setTooltip(extendedPagedTable.getKeyboardSelectedRow(),
                                          event.getColumn(),
                                          task.getDescription());
        }
    }

    @Override
    protected List<ConditionalAction<TaskSummary>> getConditionalActions() {
        return Arrays.asList(

                new ConditionalAction<TaskSummary>(constants.Claim(),
                                                   task -> presenter.claimTask(task),
                                                   presenter.getClaimActionCondition(),
                                                   false),

                new ConditionalAction<TaskSummary>(constants.Release(),
                                                   task -> presenter.releaseTask(task),
                                                   presenter.getReleaseActionCondition(),
                                                   false),

                new ConditionalAction<TaskSummary>(constants.Suspend(),
                                                   task -> presenter.suspendTask(task),
                                                   presenter.getSuspendActionCondition(),
                                                   false),

                new ConditionalAction<TaskSummary>(constants.Resume(),
                                                   task -> presenter.resumeTask(task),
                                                   presenter.getResumeActionCondition(),
                                                   false),

                new ConditionalAction<TaskSummary>(constants.ViewProcess(),
                                                   task -> presenter.openProcessInstanceView(task.getProcessInstanceId().toString()),
                                                   presenter.getProcessInstanceCondition(),
                                                   true)
        );
    }

    protected boolean isColumnAdded(List<ColumnMeta<TaskSummary>> columnMetas,
                                    String caption) {
        if (caption != null) {
            for (ColumnMeta<TaskSummary> colMet : columnMetas) {
                if (caption.equals(colMet.getColumn().getDataStoreName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addDomainSpecifColumns(ListTable<TaskSummary> extendedPagedTable,
                                       Set<String> columns) {

        extendedPagedTable.storeColumnToPreferences();

        HashMap<String, String> modifiedCaptions = new HashMap<String, String>();
        ArrayList<ColumnMeta<TaskSummary>> existingExtraColumns = new ArrayList<ColumnMeta<TaskSummary>>();
        for (ColumnMeta<TaskSummary> cm : extendedPagedTable.getColumnMetaList()) {
            if (cm.isExtraColumn()) {
                existingExtraColumns.add(cm);
            } else if (columns.contains(cm.getCaption())) {      //exist a column with the same caption
                for (String c : columns) {
                    if (c.equals(cm.getCaption())) {
                        modifiedCaptions.put(c,
                                             "Var_" + c);
                    }
                }
            }
        }
        for (ColumnMeta<TaskSummary> colMet : existingExtraColumns) {
            if (!columns.contains(colMet.getCaption())) {
                extendedPagedTable.removeColumnMeta(colMet);
            } else {
                columns.remove(colMet.getCaption());
            }
        }

        List<ColumnMeta<TaskSummary>> columnMetas = new ArrayList<ColumnMeta<TaskSummary>>();
        String caption = "";
        for (String c : columns) {
            caption = c;
            if (modifiedCaptions.get(c) != null) {
                caption = (String) modifiedCaptions.get(c);
            }
            Column<TaskSummary, ?> genericColumn = initGenericColumn(c);
            genericColumn.setSortable(false);

            columnMetas.add(new ColumnMeta<TaskSummary>(genericColumn,
                                                        caption,
                                                        true,
                                                        true));
        }
        extendedPagedTable.addColumns(columnMetas);
    }

    protected Column<TaskSummary, ?> initGenericColumn(final String key) {
        return createTextColumn(key,
                                task -> task.getDomainDataValue(key));
    }
}