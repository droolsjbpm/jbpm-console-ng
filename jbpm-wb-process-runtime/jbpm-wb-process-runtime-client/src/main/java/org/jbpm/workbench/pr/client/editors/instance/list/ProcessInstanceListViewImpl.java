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
package org.jbpm.workbench.pr.client.editors.instance.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Styles;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.jbpm.workbench.common.client.list.AbstractMultiGridView;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.common.client.util.DateUtils;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.filter.FilterSettingsBuilderHelper;
import org.jbpm.workbench.pr.client.resources.ProcessRuntimeResources;
import org.jbpm.workbench.pr.client.resources.css.ProcessRuntimeCSS;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.model.ProcessInstanceSummary;
import org.kie.api.runtime.process.ProcessInstance;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.widgets.table.client.ColumnMeta;
import org.uberfire.mvp.Command;

import static org.dashbuilder.dataset.filter.FilterFactory.equalsTo;
import static org.dashbuilder.dataset.sort.SortOrder.DESCENDING;
import static org.jbpm.workbench.pr.model.ProcessInstanceDataSetConstants.*;

@Dependent
public class ProcessInstanceListViewImpl extends AbstractMultiGridView<ProcessInstanceSummary, ProcessInstanceListPresenter>
        implements ProcessInstanceListPresenter.ProcessInstanceListView {

    public static final String TAB_ACTIVE = PROCESS_INSTANCES_WITH_VARIABLES_INCLUDED_LIST_PREFIX + "_0";
    public static final String TAB_COMPLETED = PROCESS_INSTANCES_WITH_VARIABLES_INCLUDED_LIST_PREFIX + "_1";
    public static final String TAB_ABORTED = PROCESS_INSTANCES_WITH_VARIABLES_INCLUDED_LIST_PREFIX + "_2";

    private List<ProcessInstanceSummary> selectedProcessInstances = new ArrayList<ProcessInstanceSummary>();

    private final Constants constants = Constants.INSTANCE;

    private List<Column<ProcessInstanceSummary, ?>> ignoreSelectionColumns = new ArrayList<Column<ProcessInstanceSummary, ?>>();

    private AnchorListItem bulkAbortNavLink;
    private AnchorListItem bulkSignalNavLink;

    private void controlBulkOperations() {
        if ( selectedProcessInstances != null && selectedProcessInstances.size() > 0 ) {
            bulkAbortNavLink.setEnabled( true );
            bulkSignalNavLink.setEnabled( true );
        } else {
            bulkAbortNavLink.setEnabled( false );
            bulkSignalNavLink.setEnabled( false );
        }
    }

    @Override
    public void init( final ProcessInstanceListPresenter presenter ) {
        final List<String> bannedColumns = new ArrayList<String>();
        bannedColumns.add(COL_ID_SELECT);
        bannedColumns.add(COLUMN_PROCESS_INSTANCE_ID);
        bannedColumns.add(COLUMN_PROCESS_NAME);
        bannedColumns.add(COLUMN_PROCESS_INSTANCE_DESCRIPTION);
        bannedColumns.add(COL_ID_ACTIONS);
        final List<String> initColumns = new ArrayList<String>();
        initColumns.add(COL_ID_SELECT);
        initColumns.add(COLUMN_PROCESS_INSTANCE_ID);
        initColumns.add(COLUMN_PROCESS_NAME);
        initColumns.add(COLUMN_PROCESS_INSTANCE_DESCRIPTION);
        initColumns.add(COLUMN_PROCESS_VERSION);
        initColumns.add(COLUMN_LAST_MODIFICATION_DATE);
        initColumns.add(COLUMN_ERROR_COUNT);
        initColumns.add(COL_ID_ACTIONS);

        final Button button = GWT.create(Button.class);
        button.setIcon(IconType.PLUS);
        button.setSize(ButtonSize.SMALL);

        button.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
                final String key = getValidKeyForAdditionalListGrid( PROCESS_INSTANCES_WITH_VARIABLES_INCLUDED_LIST_PREFIX + "_" );

                Command addNewGrid = new Command() {
                    @Override
                    public void execute() {

                        final ExtendedPagedTable<ProcessInstanceSummary> extendedPagedTable = createGridInstance( new GridGlobalPreferences( key, initColumns, bannedColumns ), key );

                        extendedPagedTable.setDataProvider( presenter.getDataProvider() );

                        filterPagedTable.createNewTab( extendedPagedTable, key, button, new Command() {
                            @Override
                            public void execute() {
                                currentListGrid = extendedPagedTable;
                                applyFilterOnPresenter( key );
                            }
                        } );
                        applyFilterOnPresenter( key );

                    }
                };
                FilterSettings tableSettings = createTableSettingsPrototype();
                tableSettings.setKey( key );
                dataSetEditorManager.showTableSettingsEditor( filterPagedTable, constants.New_Process_InstanceList(), tableSettings, addNewGrid );

            }
        } );

        super.init( presenter, new GridGlobalPreferences( PROCESS_INSTANCES_WITH_VARIABLES_INCLUDED_LIST_PREFIX, initColumns, bannedColumns ), button );
    }

    @Override
    public void initSelectionModel() {
        final ExtendedPagedTable extendedPagedTable = getListGrid();
        extendedPagedTable.setEmptyTableCaption(constants.No_Process_Instances_Found());
        extendedPagedTable.getRightActionsToolbar().clear();
        initBulkActions(extendedPagedTable);
        NoSelectionModel<ProcessInstanceSummary> selectionModel = new NoSelectionModel<ProcessInstanceSummary>();
        selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {

                boolean close = false;
                if (selectedRow == -1 || extendedPagedTable.getKeyboardSelectedRow() != selectedRow) {
                    extendedPagedTable.setRowStyles(selectedStyles);
                    selectedRow = extendedPagedTable.getKeyboardSelectedRow();
                    extendedPagedTable.redraw();
                } else {
                    close = true;
                }

                selectedItem = selectionModel.getLastSelectedObject();

                presenter.selectProcessInstance(selectedItem, close);
            }
        });

        DefaultSelectionEventManager<ProcessInstanceSummary> noActionColumnManager = DefaultSelectionEventManager
                .createCustomManager( new DefaultSelectionEventManager.EventTranslator<ProcessInstanceSummary>() {

                    @Override
                    public boolean clearCurrentSelection( CellPreviewEvent<ProcessInstanceSummary> event ) {
                        return false;
                    }

                    @Override
                    public DefaultSelectionEventManager.SelectAction translateSelectionEvent( CellPreviewEvent<ProcessInstanceSummary> event ) {
                        DefaultSelectionEventManager.SelectAction ret = DefaultSelectionEventManager.SelectAction.DEFAULT;
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if ( BrowserEvents.CLICK.equals( nativeEvent.getType() ) ) {
                            // Ignore if the event didn't occur in the correct column.
                            if ( isSelectionIgnoreColumn( event.getColumn() ) ){
                                ret = DefaultSelectionEventManager.SelectAction.IGNORE;
                            }else{
                                //Extension for checkboxes
                                Element target = nativeEvent.getEventTarget().cast();
                                if ( "input".equals( target.getTagName().toLowerCase() ) ) {
                                    final InputElement input = target.cast();
                                    if ( "checkbox".equals( input.getType().toLowerCase() ) ) {
                                        // Synchronize the checkbox with the current selection state.
                                        if ( !selectedProcessInstances.contains( event.getValue() ) ) {
                                            selectedProcessInstances.add( event.getValue() );
                                            input.setChecked( true );
                                        } else {
                                            selectedProcessInstances.remove( event.getValue() );
                                            input.setChecked( false );
                                        }
                                        getListGrid().redraw();
                                        controlBulkOperations();
                                        ret = DefaultSelectionEventManager.SelectAction.IGNORE;
                                    }
                                }
                            }
                        }
                        return ret;
                    }

                } );

        extendedPagedTable.setSelectionModel(selectionModel, noActionColumnManager);
        extendedPagedTable.setRowStyles(selectedStyles);
    }

    @Override
    public void initColumns( ExtendedPagedTable<ProcessInstanceSummary> extendedPagedTable ) {
        final ColumnMeta checkColumnMeta = initChecksColumn();
        Column<ProcessInstanceSummary, ?> actionsColumn = initActionsColumn();
        Column<ProcessInstanceSummary, ?> errorCountColumn = initErrorCountColumn();
        
        ignoreSelectionColumns.add(actionsColumn);
        ignoreSelectionColumns.add(errorCountColumn);

        final List<ColumnMeta<ProcessInstanceSummary>> columnMetas = new ArrayList<ColumnMeta<ProcessInstanceSummary>>();

        columnMetas.add(checkColumnMeta);
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_PROCESS_INSTANCE_ID,
                                                          process -> String.valueOf(process.getProcessInstanceId())),
                                         constants.Id()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_PROCESS_NAME,
                                                          process -> process.getProcessName()),
                                         constants.Name()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_PROCESS_INSTANCE_DESCRIPTION,
                                                          process -> process.getProcessInstanceDescription()),
                                         constants.Process_Instance_Description()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_IDENTITY,
                                                          process -> process.getInitiator()),
                                         constants.Initiator()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_PROCESS_VERSION,
                                                          process -> process.getProcessVersion()),
                                         constants.Version()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_STATUS,
                                                          process -> {
                                                              switch (process.getState()) {
                                                                  case ProcessInstance.STATE_ACTIVE:
                                                                      return constants.Active();
                                                                  case ProcessInstance.STATE_ABORTED:
                                                                      return constants.Aborted();
                                                                  case ProcessInstance.STATE_COMPLETED:
                                                                      return constants.Completed();
                                                                  case ProcessInstance.STATE_PENDING:
                                                                      return constants.Pending();
                                                                  case ProcessInstance.STATE_SUSPENDED:
                                                                      return constants.Suspended();
                                                                  default:
                                                                      return constants.Unknown();
                                                              }
                                                          }),
                                         constants.State()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_START,
                                                          process -> DateUtils.getDateTimeStr(process.getStartTime())),
                                         constants.Start_Date()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_LAST_MODIFICATION_DATE,
                                                          process -> DateUtils.getDateTimeStr(process.getLastModificationDate())),
                                         constants.Last_Modification_Date()));
        columnMetas.add(new ColumnMeta<>(createTextColumn(COLUMN_CORRELATION_KEY,
                                                          process -> process.getCorrelationKey()),
                                         constants.Correlation_Key()));
        columnMetas.add(new ColumnMeta<>(errorCountColumn, constants.Errors()));
        columnMetas.add(new ColumnMeta<>(actionsColumn, constants.Actions()));

        List<GridColumnPreference> columPreferenceList = extendedPagedTable.getGridPreferencesStore().getColumnPreferences();

        for (GridColumnPreference colPref : columPreferenceList) {
            if (!isColumnAdded(columnMetas,
                               colPref.getName())) {
                Column genericColumn = initGenericColumn(colPref.getName());
                genericColumn.setSortable(false);
                columnMetas.add(new ColumnMeta<ProcessInstanceSummary>(genericColumn,
                                                                       colPref.getName(),
                                                                       true,
                                                                       true));
            }
        }
        extendedPagedTable.addColumns(columnMetas);
        extendedPagedTable.setColumnWidth(checkColumnMeta.getColumn(), 38, Style.Unit.PX);
        extendedPagedTable.setColumnWidth(errorCountColumn, 65, Style.Unit.PX);
        
        extendedPagedTable.storeColumnToPreferences();
    }
    
    private boolean isSelectionIgnoreColumn(int colIx){
        boolean ret = false;
        ExtendedPagedTable extendedPagedTable = getListGrid();
        for(Column<ProcessInstanceSummary, ?> col : ignoreSelectionColumns){
            if(extendedPagedTable.getColumnIndex(col) == colIx){
                ret = true;
                break;
            }
        }
        return ret;
    }

    private boolean isColumnAdded( List<ColumnMeta<ProcessInstanceSummary>> columnMetas,
                                   String caption ) {
        if ( caption != null ) {
            for ( ColumnMeta<ProcessInstanceSummary> colMet : columnMetas ) {
                if ( caption.equals( colMet.getColumn().getDataStoreName() ) ) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addDomainSpecifColumns( ExtendedPagedTable<ProcessInstanceSummary> extendedPagedTable,
                                        Set<String> columns ) {

        extendedPagedTable.storeColumnToPreferences();

        HashMap modifiedCaptions = new HashMap<String, String>();
        ArrayList<ColumnMeta> existingExtraColumns = new ArrayList<ColumnMeta>();
        for ( ColumnMeta<ProcessInstanceSummary> cm : extendedPagedTable.getColumnMetaList() ) {
            if ( cm.isExtraColumn() ) {
                existingExtraColumns.add( cm );
            } else if ( columns.contains( cm.getCaption() ) ) {      //exist a column with the same caption
                for ( String c : columns ) {
                    if ( c.equals( cm.getCaption() ) ) {
                        modifiedCaptions.put( c, "Var_" + c );
                    }
                }
            }
        }
        for ( ColumnMeta colMet : existingExtraColumns ) {
            if ( !columns.contains( colMet.getCaption() ) ) {
                extendedPagedTable.removeColumnMeta( colMet );
            } else {
                columns.remove( colMet.getCaption() );
            }
        }

        List<ColumnMeta<ProcessInstanceSummary>> columnMetas = new ArrayList<ColumnMeta<ProcessInstanceSummary>>();
        String caption = "";
        for ( String c : columns ) {
            caption = c;
            if ( modifiedCaptions.get( c ) != null ) {
                caption = (String) modifiedCaptions.get( c );
            }
            Column genericColumn = initGenericColumn( c );
            genericColumn.setSortable( false );

            columnMetas.add( new ColumnMeta<ProcessInstanceSummary>( genericColumn, caption, true, true ) );
        }

        extendedPagedTable.addColumns( columnMetas );

    }

    private Column initGenericColumn( final String key ) {

        Column<ProcessInstanceSummary, String> genericColumn = new Column<ProcessInstanceSummary, String>( new TextCell() ) {
            @Override
            public String getValue( ProcessInstanceSummary object ) {
                return object.getDomainDataValue( key );
            }
        };
        genericColumn.setSortable( true );
        genericColumn.setDataStoreName(key);

        return genericColumn;
    }

    private void initBulkActions( final ExtendedPagedTable<ProcessInstanceSummary> extendedPagedTable ) {
        bulkAbortNavLink = GWT.create(AnchorListItem.class);
        bulkAbortNavLink.setText(constants.Bulk_Abort());
        bulkSignalNavLink = GWT.create(AnchorListItem.class);
        bulkSignalNavLink.setText(constants.Bulk_Signal());

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
        bulkDropDown.add(bulkAbortNavLink);
        bulkDropDown.add(bulkSignalNavLink);
        bulkActions.add(bulkDropDown);

        bulkAbortNavLink.setIcon( IconType.BAN );
        bulkAbortNavLink.setIconFixedWidth( true );
        bulkAbortNavLink.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                if( Window.confirm( constants.Abort_Process_Instances() ) ) {
                    presenter.bulkAbort(selectedProcessInstances);
                    selectedProcessInstances.clear();
                    extendedPagedTable.redraw();
                }
            }
        } );

        bulkSignalNavLink.setIcon( IconType.BELL );
        bulkSignalNavLink.setIconFixedWidth( true );
        bulkSignalNavLink.addClickHandler( new ClickHandler() {
            @Override
            public void onClick( ClickEvent event ) {
                presenter.bulkSignal( selectedProcessInstances );
                selectedProcessInstances.clear();
                extendedPagedTable.redraw();
            }
        } );

        extendedPagedTable.getRightActionsToolbar().add(bulkActions);

        controlBulkOperations();
    }
    
    private Column<ProcessInstanceSummary, ProcessInstanceSummary> initErrorCountColumn() {
        
        Column<ProcessInstanceSummary, ProcessInstanceSummary> column = new Column<ProcessInstanceSummary, ProcessInstanceSummary>(
                new AbstractCell<ProcessInstanceSummary>(){

            @Override
            public void render(Context context, ProcessInstanceSummary value, SafeHtmlBuilder sb) {
                int errCount =  value != null ? value.getErrorCount() : 0;
                ProcessRuntimeCSS css = ProcessRuntimeResources.INSTANCE.css();
                String badgeHtml = null;
                if(errCount > 0){
                    //TODO: set link URL to correct error view perspective path
                    String linkToErrorView = "#Error View?processInstanceId=" + value.getProcessInstanceId();
                    String elementId = DOM.createUniqueId();
                    String popoverAttrs = "data-toggle=\"popover\" data-html=\"true\" data-placement=\"right\"" +
                            " data-content=\"" + constants.ErrorCountNumber(errCount) + " | <a href='" + linkToErrorView +
                            "' title='" + constants.ErrorCountViewLink() + "'>" + constants.ErrorCountViewLink() + "</a>\"" +
                            " data-container=\"body\" data-trigger=\"hover focus\"";
                    badgeHtml = "<a id=\"" + elementId + "\" href=\"#\" class=\"badge " + css.processInstanceErrorCount() +
                            " " + css.processInstanceErrorCountPresent() + "\" " + popoverAttrs + ">" + errCount + "</a>";
                    Scheduler.get().scheduleDeferred(() -> initPopover(elementId));
                }else{
                    badgeHtml = "<span class=\"badge " +  css.processInstanceErrorCount() + "\">" + errCount + "</span>";
                }
                sb.appendHtmlConstant(badgeHtml);
            }

        }){

            @Override
            public ProcessInstanceSummary getValue(ProcessInstanceSummary process) {
                return process;
            }

        };

        column.setSortable(true);
        column.setDataStoreName(COLUMN_ERROR_COUNT);
        return column;
    }
    
    private native void initPopover(final String uid) /*-{
        $wnd.jQuery(document).ready(function() {
            var jQueryId = "#" + uid;
            $wnd.jQuery(jQueryId).popover();
        });
    }-*/;    

    private Column<ProcessInstanceSummary, ProcessInstanceSummary> initActionsColumn() {
        List<HasCell<ProcessInstanceSummary, ?>> cells = new LinkedList<HasCell<ProcessInstanceSummary, ?>>();

        cells.add(new ProcessInstanceSummaryActionCell(constants.Signal(), new Delegate<ProcessInstanceSummary>() {
            @Override
            public void execute( final ProcessInstanceSummary processInstance ) {
                presenter.signalProcessInstance(processInstance);
            }
        } ) );

        cells.add(new ProcessInstanceSummaryActionCell(constants.Abort(), new Delegate<ProcessInstanceSummary>() {
            @Override
            public void execute( ProcessInstanceSummary processInstance ) {
                if ( Window.confirm( constants.Abort_Process_Instance() ) ) {
                    presenter.abortProcessInstance( processInstance.getDeploymentId(), processInstance.getProcessInstanceId() );
                }
            }
        } ) );

        CompositeCell<ProcessInstanceSummary> cell = new CompositeCell<ProcessInstanceSummary>( cells );
        Column<ProcessInstanceSummary, ProcessInstanceSummary> actionsColumn = new Column<ProcessInstanceSummary, ProcessInstanceSummary>( cell ) {
            @Override
            public ProcessInstanceSummary getValue( ProcessInstanceSummary object ) {
                return object;
            }
        };
        actionsColumn.setDataStoreName( COL_ID_ACTIONS );
        return actionsColumn;

    }

    private ColumnMeta<ProcessInstanceSummary> initChecksColumn() {
        CheckboxCell checkboxCell = new CheckboxCell(true,false);
        Column<ProcessInstanceSummary, Boolean> checkColumn = new Column<ProcessInstanceSummary, Boolean>(checkboxCell ) {
            @Override
            public Boolean getValue( ProcessInstanceSummary object ) {
                // Get the value from the selection model.
                return selectedProcessInstances.contains( object );
            }
        };

        Header<Boolean> selectPageHeader = new Header<Boolean>(checkboxCell) {
            @Override
            public Boolean getValue() {
                List<ProcessInstanceSummary> displayedInstances = presenter.getDisplayedProcessInstances();
                return displayedInstances.size() > 0
                        && selectedProcessInstances.size() == presenter.getDisplayedProcessInstances().size();
            }
        };
        selectPageHeader.setUpdater(new ValueUpdater<Boolean>() {
            @Override
            public void update(Boolean value) {
                selectedProcessInstances.clear();
                if (value) {
                    selectedProcessInstances.addAll(presenter.getDisplayedProcessInstances());
                }
                getListGrid().redraw();
                controlBulkOperations();
            }
        });

        checkColumn.setSortable(false);
        checkColumn.setDataStoreName( COL_ID_SELECT );
        ColumnMeta<ProcessInstanceSummary> checkColMeta = new ColumnMeta<ProcessInstanceSummary>( checkColumn, "");
        checkColMeta.setHeader(selectPageHeader);
        return checkColMeta;
    }

    @Override
    public void initDefaultFilters(final GridGlobalPreferences preferences) {
        super.initDefaultFilters(preferences);

        //Filter status Active
        initGenericTabFilter(preferences,
                             TAB_ACTIVE,
                             constants.Active(),
                             constants.FilterActive(),
                             ProcessInstance.STATE_ACTIVE);

        //Filter status completed
        initGenericTabFilter(preferences,
                             TAB_COMPLETED,
                             constants.Completed(),
                             constants.FilterCompleted(),
                             ProcessInstance.STATE_COMPLETED);

        //Filter status completed
        initGenericTabFilter(preferences,
                             TAB_ABORTED,
                             constants.Aborted(),
                             constants.FilterAborted(),
                             ProcessInstance.STATE_ABORTED);
    }

    private void initGenericTabFilter(final GridGlobalPreferences preferences,
                                      final String key,
                                      final String tabName,
                                      final String tabDesc,
                                      final Integer state) {

        FilterSettingsBuilderHelper builder = FilterSettingsBuilderHelper.init();
        builder.initBuilder();

        builder.dataset(PROCESS_INSTANCE_DATASET);

        builder.filter(equalsTo(COLUMN_STATUS,
                                state));

        builder.filterOn(true,
                         true,
                         true);
        builder.tableOrderEnabled(true);
        builder.tableOrderDefault(COLUMN_START,
                                  DESCENDING);

        FilterSettings tableSettings = builder.buildSettings();
        tableSettings.setKey(key);
        tableSettings.setTableName(tabName);
        tableSettings.setTableDescription(tabDesc);
        tableSettings.setUUID(PROCESS_INSTANCE_DATASET);

        addNewTab(key,
                  preferences,
                  tableSettings);
    }

    @Override
    public FilterSettings getVariablesTableSettings( String processName ) {
        String tableSettingsJSON = "{\n"
                + "    \"type\": \"TABLE\",\n"
                + "    \"filter\": {\n"
                + "        \"enabled\": \"true\",\n"
                + "        \"selfapply\": \"true\",\n"
                + "        \"notification\": \"true\",\n"
                + "        \"listening\": \"true\"\n"
                + "    },\n"
                + "    \"table\": {\n"
                + "        \"sort\": {\n"
                + "            \"enabled\": \"true\",\n"
                + "            \"columnId\": \"" + PROCESS_INSTANCE_ID + "\",\n"
                + "            \"order\": \"ASCENDING\"\n"
                + "        }\n"
                + "    },\n"
                + "    \"dataSetLookup\": {\n"
                + "        \"dataSetUuid\": \"jbpmProcessInstancesWithVariables\",\n"
                + "        \"rowCount\": \"-1\",\n"
                + "        \"rowOffset\": \"0\",\n";
        if ( processName != null ) {
            tableSettingsJSON += "        \"filterOps\":[{\"columnId\":\"" + PROCESS_NAME + "\", \"functionType\":\"EQUALS_TO\", \"terms\":[\"" + processName + "\"]}],";
        }
        tableSettingsJSON += "        \"groupOps\": [\n"
                + "            {\n"
                + "                \"groupFunctions\": [\n"
                + "                    {\n"
                + "                        \"sourceId\": \"" + PROCESS_INSTANCE_ID + "\",\n"
                + "                        \"columnId\": \"" + PROCESS_INSTANCE_ID + "\"\n"
                + "                    },\n"
                + "                    {\n"
                + "                        \"sourceId\": \"" + PROCESS_NAME + "\",\n"
                + "                        \"columnId\": \"" + PROCESS_NAME + "\"\n"
                + "                    },\n"
                + "                    {\n"
                + "                        \"sourceId\": \"" + VARIABLE_ID + "\",\n"
                + "                        \"columnId\": \"" + VARIABLE_ID + "\"\n"
                + "                    },\n"
                + "                    {\n"
                + "                        \"sourceId\": \"" + VARIABLE_NAME + "\",\n"
                + "                        \"columnId\": \"" + VARIABLE_NAME + "\"\n"
                + "                    },\n"
                + "                    {\n"
                + "                        \"sourceId\": \"" + VARIABLE_VALUE + "\",\n"
                + "                        \"columnId\": \"" + VARIABLE_VALUE +  "\"\n"
                + "                    }\n"
                + "                ],\n"
                + "                \"join\": \"false\"\n"
                + "            }\n"
                + "        ]\n"
                + "    },\n"
                + "    \"columns\": [\n"
                + "        {\n"
                + "            \"id\": \"" + PROCESS_INSTANCE_ID + "\",\n"
                + "            \"name\": \"processInstanceId\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \""  + PROCESS_NAME + "\",\n"
                + "            \"name\": \"processName\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"" + VARIABLE_ID + "\",\n"
                + "            \"name\": \"variableID\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"" + VARIABLE_NAME + "\",\n"
                + "            \"name\": \"variableName\"\n"
                + "        },\n"
                + "        {\n"
                + "            \"id\": \"" + VARIABLE_VALUE + "\",\n"
                + "            \"name\": \"variableValue\"\n"
                + "        }\n"
                + "    ],\n"
                + "    \"tableName\": \"Filtered\",\n"
                + "    \"tableDescription\": \"Filtered Desc\",\n"
                + "    \"tableEditEnabled\": \"false\"\n"
                + "}";

        return dataSetEditorManager.getStrToTableSettings( tableSettingsJSON );
    }

    /*-------------------------------------------------*/
    /*---              DashBuilder                   --*/
    /*-------------------------------------------------*/
    @Override
    public FilterSettings createTableSettingsPrototype() {
        FilterSettingsBuilderHelper builder = FilterSettingsBuilderHelper.init();
        builder.initBuilder();

        builder.dataset( PROCESS_INSTANCE_DATASET );
        builder.filterOn( true, true, true );
        builder.tableOrderEnabled( true );
        builder.tableOrderDefault( COLUMN_START, DESCENDING );
        builder.tableWidth( 1000 );

        final FilterSettings filterSettings = builder.buildSettings();
        filterSettings.setUUID( PROCESS_INSTANCE_DATASET );
        return filterSettings;
    }

    @Override
    public void resetDefaultFilterTitleAndDescription(){
        super.resetDefaultFilterTitleAndDescription();
        saveTabSettings(TAB_ACTIVE,
                        constants.Active(),
                        constants.FilterActive());
        saveTabSettings(TAB_COMPLETED,
                        constants.Completed(),
                        constants.FilterCompleted());
        saveTabSettings(TAB_ABORTED,
                        constants.Aborted(),
                        constants.FilterAborted());
    }

}
