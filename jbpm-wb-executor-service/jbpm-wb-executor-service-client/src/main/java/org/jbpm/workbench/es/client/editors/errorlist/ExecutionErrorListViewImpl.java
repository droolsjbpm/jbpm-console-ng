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

package org.jbpm.workbench.es.client.editors.errorlist;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.jbpm.workbench.common.client.list.AbstractMultiGridView;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.common.client.util.BooleanConverter;
import org.jbpm.workbench.common.client.util.ConditionalButtonActionCell;
import org.jbpm.workbench.common.client.util.DateTimeConverter;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.es.client.i18n.Constants;
import org.jbpm.workbench.es.client.util.ExecutionErrorTypeConverter;
import org.jbpm.workbench.es.model.ExecutionErrorSummary;
import org.uberfire.client.views.pfly.widgets.ConfirmPopup;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.widgets.table.client.ColumnMeta;
import org.uberfire.mvp.Command;

import static org.jbpm.workbench.es.model.ExecutionErrorDataSetConstants.*;

@Dependent
public class ExecutionErrorListViewImpl extends AbstractMultiGridView<ExecutionErrorSummary, ExecutionErrorListPresenter>
        implements ExecutionErrorListPresenter.ExecutionErrorListView {

    private static final String TAB_ALL = EXECUTION_ERROR_LIST_PREFIX + "_0";
    private static final String TAB_ACK = EXECUTION_ERROR_LIST_PREFIX + "_1";
    private static final String TAB_NEW = EXECUTION_ERROR_LIST_PREFIX + "_2";

    private final Constants constants = Constants.INSTANCE;

    @Inject
    ConfirmPopup confirmPopup;

    @Inject
    ExecutionErrorTypeConverter executionErrorTypeConverter;

    @Inject
    DateTimeConverter dateTimeConverter;

    @Inject
    BooleanConverter booleanConverter;

    @Override
    public void init(final ExecutionErrorListPresenter presenter) {
        final List<String> bannedColumns = new ArrayList<String>();
        bannedColumns.add(COL_ID_SELECT);
        bannedColumns.add(COLUMN_ERROR_ID);
        bannedColumns.add(COLUMN_ERROR_TYPE);
        bannedColumns.add(COLUMN_PROCESS_INST_ID);
        bannedColumns.add(COLUMN_ERROR_DATE);
        bannedColumns.add(COL_ID_ACTIONS);
        final List<String> initColumns = new ArrayList<String>();
        initColumns.add(COL_ID_SELECT);
        initColumns.add(COLUMN_ERROR_ID);
        initColumns.add(COLUMN_ERROR_TYPE);
        initColumns.add(COLUMN_PROCESS_INST_ID);
        initColumns.add(COLUMN_ERROR_DATE);
        initColumns.add(COLUMN_DEPLOYMENT_ID);
        initColumns.add(COL_ID_ACTIONS);
        createTabButton.addClickHandler((ClickEvent event) -> {
            final String key = getValidKeyForAdditionalListGrid(EXECUTION_ERROR_LIST_PREFIX + "_");

            Command addNewGrid = () -> {

                final ExtendedPagedTable<ExecutionErrorSummary> extendedPagedTable =
                        createGridInstance(new GridGlobalPreferences(key,
                                                                     initColumns,
                                                                     bannedColumns),
                                           key);

                extendedPagedTable.setDataProvider(presenter.getDataProvider());

                filterPagedTable.createNewTab(extendedPagedTable,
                                              key,
                                              createTabButton,
                                              (() -> {
                                                  currentListGrid = extendedPagedTable;
                                                  applyFilterOnPresenter(key);
                                              }));
                applyFilterOnPresenter(key);
            };
            FilterSettings tableSettings = presenter.createTableSettingsPrototype();
            tableSettings.setKey(key);
            dataSetEditorManager.showTableSettingsEditor(filterPagedTable,
                                                         constants.New_ErrorList(),
                                                         tableSettings,
                                                         addNewGrid);
        });

        super.init(presenter,
                   new GridGlobalPreferences(EXECUTION_ERROR_LIST_PREFIX,
                                             initColumns,
                                             bannedColumns));
    }

    @Override
    public void initSelectionModel(final ExtendedPagedTable<ExecutionErrorSummary> extendedPagedTable) {
        extendedPagedTable.setEmptyTableCaption(constants.No_Execution_Errors_Found());
        extendedPagedTable.setSelectionCallback((error, close) -> presenter.selectExecutionError(error,
                                                                                              close));
        initBulkActions(extendedPagedTable);
    }

    private void initBulkActions(final ExtendedPagedTable<ExecutionErrorSummary> extendedPagedTable) {
        extendedPagedTable.getRightActionsToolbar().clear();

        final AnchorListItem bulkAckNavLink = GWT.create(AnchorListItem.class);
        bulkAckNavLink.setText(constants.Bulk_Ack());

        final ButtonGroup bulkActions = GWT.create(ButtonGroup.class);
        final Button bulkButton = GWT.create(Button.class);
        bulkButton.setText(constants.Bulk_Actions());
        bulkButton.setDataToggle(Toggle.DROPDOWN);
        bulkButton.getElement().getStyle().setMarginRight(5,
                                                         Style.Unit.PX);
        bulkActions.add(bulkButton);
        final DropDownMenu bulkDropDown = GWT.create(DropDownMenu.class);
        bulkDropDown.addStyleName(Styles.DROPDOWN_MENU + "-right");
        bulkDropDown.getElement().getStyle().setMarginRight(5,
                                                            Style.Unit.PX);
        bulkDropDown.add(bulkAckNavLink);
        bulkActions.add(bulkDropDown);
        bulkAckNavLink.setIcon(IconType.BAN);
        bulkAckNavLink.setIconFixedWidth(true);
        bulkAckNavLink.addClickHandler((ClickEvent event) -> {
            confirmPopup.show(constants.Bulk_Ack(),
                              constants.Acknowledge(),
                              constants.Bulk_Ack_confirm(),
                              () -> {
                                  presenter.bulkAcknowledge(extendedPagedTable.getSelectedItems());
                                  extendedPagedTable.deselectAllItems();
                                  extendedPagedTable.redraw();
                              });
        });

        extendedPagedTable.getRightActionsToolbar().add(bulkActions);
    }

    @Override
    public void initColumns(final ExtendedPagedTable<ExecutionErrorSummary> extendedPagedTable) {
        final ColumnMeta<ExecutionErrorSummary> checkColumnMeta = initChecksColumn(extendedPagedTable);

        final List<ColumnMeta<ExecutionErrorSummary>> columnMetas = new ArrayList<ColumnMeta<ExecutionErrorSummary>>();

        columnMetas.add(checkColumnMeta);
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_ID,
                                                          errorSummary -> errorSummary.getErrorId()),
                                         constants.Id()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_TYPE,
                                                          errorSummary -> executionErrorTypeConverter.toWidgetValue(errorSummary.getType())),
                                         constants.Type()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_ACK,
                                                          errorSummary -> booleanConverter.toWidgetValue(errorSummary.isAcknowledged())),
                                         constants.Ack()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_ACK_AT,
                                                          errorSummary ->
                                                                  dateTimeConverter.toWidgetValue(errorSummary.getAcknowledgedAt())),
                                         constants.AckAt()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_ACK_BY,
                                                          errorSummary -> errorSummary.getAcknowledgedBy()),
                                         constants.AckBy()));
        columnMetas.add(new ColumnMeta<>(createNumberColumn(COLUMN_JOB_ID,
                                                            errorSummary -> errorSummary.getJobId()),
                                         constants.JobId()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_PROCESS_ID,
                                                          errorSummary -> errorSummary.getProcessId()),
                                         constants.ProcessId()));
        columnMetas.add(new ColumnMeta<>(createNumberColumn(COLUMN_PROCESS_INST_ID,
                                                            errorSummary -> errorSummary.getProcessInstanceId()),
                                         constants.Process_Instance_Id()));

        columnMetas.add(new ColumnMeta<>(createNumberColumn(COLUMN_ACTIVITY_ID,
                                                            errorSummary -> errorSummary.getActivityId()),
                                         constants.ActivityId()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ACTIVITY_NAME,
                                                          errorSummary -> errorSummary.getActivityName()),
                                         constants.ActivityName()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_DEPLOYMENT_ID,
                                                          errorSummary -> errorSummary.getDeploymentId()),
                                         constants.DeploymentId()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_DATE,
                                                          errorSummary ->
                                                                  dateTimeConverter.toWidgetValue(errorSummary.getErrorDate())),
                                         constants.ErrorDate()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_ERROR_MSG,
                                                          errorSummary -> errorSummary.getErrorMessage()),
                                         constants.Message()));

        Column<ExecutionErrorSummary, ExecutionErrorSummary> actionsColumn = initActionsColumn();

        extendedPagedTable.addSelectionIgnoreColumn(actionsColumn);

        ColumnMeta actionsColumnMeta= new ColumnMeta<>(actionsColumn,
                                                         constants.Actions());
        columnMetas.add(actionsColumnMeta);

        extendedPagedTable.setColumnWidth(checkColumnMeta.getColumn(),
                                          38,
                                          Style.Unit.PX);
        extendedPagedTable.setColumnWidth(actionsColumnMeta.getColumn(),
                                          260,
                                          Style.Unit.PX);
        extendedPagedTable.addColumns(columnMetas);
    }

    private Column<ExecutionErrorSummary, ExecutionErrorSummary> initActionsColumn() {
        List<HasCell<ExecutionErrorSummary, ?>> cells = new LinkedList<HasCell<ExecutionErrorSummary, ?>>();

        cells.add(new ConditionalButtonActionCell<ExecutionErrorSummary>(
                constants.Acknowledge(),
                errorSummary -> presenter.acknowledgeExecutionError(errorSummary.getErrorId(),
                                                                       errorSummary.getDeploymentId()),
                presenter.getAcknowledgeActionCondition()));

        cells.add(new ConditionalButtonActionCell<ExecutionErrorSummary>(
                constants.ViewJob(),
                errorSummary -> presenter.goToJob(errorSummary),
                presenter.getViewJobActionCondition()));

        cells.add(new ConditionalButtonActionCell<ExecutionErrorSummary>(
                constants.ViewProcessInstance(),
                errorSummary -> presenter.goToProcessInstance(errorSummary),
                presenter.getViewProcessInstanceActionCondition()));


        CompositeCell<ExecutionErrorSummary> cell = new CompositeCell<ExecutionErrorSummary>(cells);
        Column<ExecutionErrorSummary, ExecutionErrorSummary> actionsColumn = new Column<ExecutionErrorSummary, ExecutionErrorSummary>(cell) {
            @Override
            public ExecutionErrorSummary getValue(ExecutionErrorSummary object) {
                return object;
            }
        };
        actionsColumn.setDataStoreName(COL_ID_ACTIONS);
        return actionsColumn;
    }

    public void initDefaultFilters(GridGlobalPreferences preferences,
                                   Button createTabButton) {

        super.initDefaultFilters(preferences,
                                 createTabButton);

        initGenericTabFilter(presenter.createAllTabSettings(),
                             TAB_ALL,
                             constants.All(),
                             constants.FilterAll(),
                             preferences);

        initGenericTabFilter(presenter.createNewTabSettings(),
                             TAB_NEW,
                             constants.New(),
                             constants.UnacknowledgedErrors(),
                             preferences);

        initGenericTabFilter(presenter.createAcknowledgedTabSettings(),
                             TAB_ACK,
                             constants.Acknowledged(),
                             constants.AcknowledgedErrors(),
                             preferences);

        filterPagedTable.addAddTableButton(createTabButton);
    }

    private void initGenericTabFilter(FilterSettings tableSettings,
                                      final String key,
                                      String tabName,
                                      String tabDesc,
                                      GridGlobalPreferences preferences) {

        tableSettings.setKey(key);
        tableSettings.setTableName(tabName);
        tableSettings.setTableDescription(tabDesc);
        tableSettings.setUUID(EXECUTION_ERROR_LIST_DATASET);

        addNewTab(preferences,
                  tableSettings);
    }

    @Override
    public void resetDefaultFilterTitleAndDescription() {
        super.resetDefaultFilterTitleAndDescription();
        saveTabSettings(TAB_NEW,
                        constants.New(),
                        constants.UnacknowledgedErrors());
        saveTabSettings(TAB_ACK,
                        constants.Acknowledge(),
                        constants.AcknowledgedErrors());
        saveTabSettings(TAB_ALL,
                        constants.All(),
                        constants.FilterAll());
    }

}