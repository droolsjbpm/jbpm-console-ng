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
package org.jbpm.workbench.common.client.list;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.cell.client.NumberCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.Button;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.shared.api.identity.User;
import org.jbpm.workbench.common.client.resources.CommonResources;
import org.jbpm.workbench.common.client.resources.css.CommonCSS;
import org.jbpm.workbench.common.client.resources.i18n.Constants;
import org.jbpm.workbench.common.model.GenericSummary;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.list.base.DataSetEditorManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.services.shared.preferences.GridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.MultiGridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.UserPreferencesService;
import org.uberfire.ext.services.shared.preferences.UserPreferencesType;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;
import org.uberfire.ext.widgets.common.client.common.popups.YesNoCancelPopup;
import org.uberfire.ext.widgets.common.client.tables.FilterPagedTable;
import org.uberfire.ext.widgets.common.client.tables.popup.NewTabFilterPopup;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.workbench.events.NotificationEvent;

public abstract class AbstractMultiGridView<T extends GenericSummary, V extends AbstractMultiGridPresenter>
        extends Composite implements MultiGridView<T, V> {

    public static final String TAB_SEARCH = "base";
    public static final String FILTER_TABLE_SETTINGS = "tableSettings";
    public static final String USER_DEFINED = "ud_";

    interface Binder extends UiBinder<Widget, AbstractMultiGridView> {
    }

    private static Binder uiBinder = GWT.create( Binder.class );

    private final CommonCSS commonCSS = CommonResources.INSTANCE.css();

    private final Constants constants = Constants.INSTANCE;

    @Inject
    public User identity;

    @Inject
    protected Event<NotificationEvent> notification;

    protected DataSetEditorManager dataSetEditorManager;

    @Inject
    protected PlaceManager placeManager;

    private Caller<UserPreferencesService> preferencesService;

    @UiField
    org.gwtbootstrap3.client.ui.Column column;

    protected V presenter;

    protected FilterPagedTable<T> filterPagedTable;

    protected ExtendedPagedTable<T> currentListGrid;

    protected RowStyles<T> selectedStyles = new RowStyles<T>() {

        @Override
        public String getStyleNames( T row,
                int rowIndex ) {
            if ( rowIndex == selectedRow ) {
                return commonCSS.selected();
            }
            return null;
        }
    };


    protected T selectedItem;

    protected int selectedRow = -1;

    protected Column actionsColumn;

    public GridGlobalPreferences currentGlobalPreferences;

    public Button createTabButton;

    protected AdvancedSearchTable<T> advancedSearchTable;

    public AbstractMultiGridView() {
        initWidget( uiBinder.createAndBindUi( this ) );
    }

    public void init( final V presenter,
            final GridGlobalPreferences preferences,
            final Button createNewGridButton ) {
        this.presenter = presenter;
        this.currentGlobalPreferences = preferences;
        this.createTabButton = createNewGridButton;

        filterPagedTable = GWT.create(FilterPagedTable.class);
        column.add( filterPagedTable.makeWidget() );

        filterPagedTable.setPreferencesService( preferencesService );
        preferencesService.call( new RemoteCallback<MultiGridPreferencesStore>() {

            @Override
            public void callback( MultiGridPreferencesStore multiGridPreferencesStore ) {
                if ( multiGridPreferencesStore == null ) {
                    multiGridPreferencesStore = new MultiGridPreferencesStore( preferences.getKey() );
                }
                String selectedGridId = multiGridPreferencesStore.getSelectedGrid();
                filterPagedTable.setMultiGridPreferencesStore( multiGridPreferencesStore );
                presenter.onGridPreferencesStoreLoaded();
                ArrayList<String> existingGrids = multiGridPreferencesStore.getGridsId();

                if ( existingGrids != null && existingGrids.size() > 0 ) {
                    resetDefaultFilterTitleAndDescription();
                    presenter.setAddingDefaultFilters( true );
                    for ( int i = 0; i < existingGrids.size(); i++ ) {
                        final String key = existingGrids.get( i );
                        final ExtendedPagedTable<T> extendedPagedTable = loadGridInstance( preferences, key );
                        currentListGrid = extendedPagedTable;
                        extendedPagedTable.setDataProvider( presenter.getDataProvider());
                        filterPagedTable.addTab( extendedPagedTable, key, new Command() {
                            @Override
                            public void execute() {
                                currentListGrid = extendedPagedTable;
                                applyFilterOnPresenter( key );
                            }
                        } );
                        if ( currentListGrid != null && key.equals( selectedGridId ) ) {
                            currentListGrid = extendedPagedTable;
                        }
                    }

                    filterPagedTable.addAddTableButton( createNewGridButton );
                    presenter.setAddingDefaultFilters( false );
                    if ( selectedGridId != null ) {
                        multiGridPreferencesStore.setSelectedGrid( selectedGridId );
                        preferencesService.call().saveUserPreferences( multiGridPreferencesStore );
                        filterPagedTable.setSelectedTab();
                    }

                } else {
                    createFilters(preferences,
                                  createNewGridButton);
                }
                initSelectionModel();
            }

        } ).loadUserPreferences( preferences.getKey(), UserPreferencesType.MULTIGRIDPREFERENCES );
    }

    public void displayNotification( String text ) {
        notification.fire(new NotificationEvent(text));
    }

    public void showRestoreDefaultFilterConfirmationPopup() {
        YesNoCancelPopup yesNoCancelPopup = YesNoCancelPopup.newYesNoCancelPopup(constants.RestoreDefaultFilters(),
                                                                                 constants.AreYouSureRestoreDefaultFilters(),
                new Command() {
                    @Override public void execute() {
                        showBusyIndicator(constants.Loading());
                        restoreTabs();
                    }
                },
                null,
                new Command() {
                    @Override public void execute() {
                    }
                });
        yesNoCancelPopup.show();
    }

    public void restoreTabs(){
        ArrayList<String> existingGrids = getMultiGridPreferencesStore().getGridsId();
        ArrayList<String> allTabs = new ArrayList<String>( existingGrids.size() );

        presenter.setAddingDefaultFilters( true );
        if ( existingGrids != null && existingGrids.size() > 0 ) {

            for ( int i = 0; i < existingGrids.size(); i++ ) {
                allTabs.add( existingGrids.get( i ) );
            }

            for ( int i = 0; i < allTabs.size(); i++ ) {
                filterPagedTable.removeTab( allTabs.get( i ) );
            }

        }
        filterPagedTable.removeTab( 0 );
        createFilters(currentGlobalPreferences,
                      createTabButton);
    }

    protected void createFilters(GridGlobalPreferences currentGlobalPreferences,
                                 Button createTabButton) {
        initDefaultFilters(currentGlobalPreferences);
        filterPagedTable.addAddTableButton(createTabButton);
        selectFirstTabAndEnableQueries();
    }

    public void initGenericToolBar( ExtendedPagedTable<T> extendedPagedTable ) {
    }

    public String getValidKeyForAdditionalListGrid( String baseName ) {
        return filterPagedTable.getValidKeyForAdditionalListGrid( baseName + USER_DEFINED  );
    }

    public ExtendedPagedTable<T> createGridInstance(final GridGlobalPreferences preferences, final String key) {
        return createGridInstance(createExtendedPagedTable(preferences, key), preferences, null);
    }

    public ExtendedPagedTable<T> createGridInstance(final ExtendedPagedTable<T> newListGrid, final GridGlobalPreferences preferences, final GridPreferencesStore preferencesStore) {
        if (preferencesStore == null) {
            newListGrid.setGridPreferencesStore(new GridPreferencesStore(preferences));
        } else {
            newListGrid.setGridPreferencesStore(preferencesStore);
        }
        initColumns(newListGrid);
        initGenericToolBar(newListGrid);
        newListGrid.loadPageSizePreferences();
        return newListGrid;
    }

    protected ExtendedPagedTable<T> createExtendedPagedTable(final GridGlobalPreferences preferences, final String key){
        return TAB_SEARCH.equals(key) ? createAdvancedSearchTable(preferences) : new ExtendedPagedTable<T>(preferences);
    }

    protected AdvancedSearchTable<T> createAdvancedSearchTable(final GridGlobalPreferences preferences){
        advancedSearchTable = new AdvancedSearchTable<T>(preferences);
        presenter.setupAdvanceSearchView();
        return advancedSearchTable;
    }

    public ExtendedPagedTable<T> loadGridInstance(final GridGlobalPreferences preferences,
                                                  final String key) {
        final ExtendedPagedTable<T> newListGrid = createExtendedPagedTable(preferences,
                                                                           key);

        preferencesService.call(new RemoteCallback<GridPreferencesStore>() {

            @Override
            public void callback(GridPreferencesStore preferencesStore) {
                createGridInstance(newListGrid,
                                   preferences,
                                   preferencesStore);
            }
        }).loadUserPreferences(key,
                               UserPreferencesType.GRIDPREFERENCES);

        return newListGrid;
    }

    public void showBusyIndicator( final String message ) {
        BusyPopup.showMessage(message);
    }

    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    public ExtendedPagedTable<T> getListGrid() {
        return currentListGrid;
    }

    /*
     * For each specific implementation define the
     *  DataGrid columns and how they must be initialized
     */
    public abstract void initColumns( ExtendedPagedTable<T> extendedPagedTable );

    public abstract void initSelectionModel();

    public MultiGridPreferencesStore getMultiGridPreferencesStore() {
        if ( filterPagedTable != null ) {
            return filterPagedTable.getMultiGridPreferencesStore();
        }
        return null;
    }

    public void initDefaultFilters(final GridGlobalPreferences preferences) {
        presenter.setAddingDefaultFilters(true);

        //Search Tab
        final FilterSettings settings = createTableSettingsPrototype();
        settings.setTableName(constants.Search());
        settings.setTableDescription(constants.SearchResults());
        settings.setKey(TAB_SEARCH);
        addNewTab(TAB_SEARCH,
                  preferences,
                  settings);
    }

    public void addNewTab(final String key,
                          final GridGlobalPreferences preferences,
                          final FilterSettings tableSettings) {
        final HashMap<String, Object> tabSettingsValues = new HashMap<>();

        tabSettingsValues.put(FILTER_TABLE_SETTINGS,
                              dataSetEditorManager.getTableSettingsToStr(tableSettings));
        tabSettingsValues.put(NewTabFilterPopup.FILTER_TAB_NAME_PARAM,
                              tableSettings.getTableName());
        tabSettingsValues.put(NewTabFilterPopup.FILTER_TAB_DESC_PARAM,
                              tableSettings.getTableDescription());

        filterPagedTable.saveNewTabSettings(key,
                                            tabSettingsValues);

        final ExtendedPagedTable<T> extendedPagedTable = createGridInstance(preferences,
                                                                            key);
        currentListGrid = extendedPagedTable;
        extendedPagedTable.setDataProvider(presenter.getDataProvider());

        filterPagedTable.addTab(extendedPagedTable,
                                key,
                                () -> {
                                    currentListGrid = extendedPagedTable;
                                    applyFilterOnPresenter(key);
                                }
        );
    }

    public abstract FilterSettings createTableSettingsPrototype();

    public void selectFirstTabAndEnableQueries() {
        presenter.setAddingDefaultFilters(false);
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                getMultiGridPreferencesStore().setSelectedGrid(TAB_SEARCH);
                filterPagedTable.setSelectedTab();
            }
        });
    }

    public void applyFilterOnPresenter(final String key) {
        applyFilterOnPresenter(getTableFilterSettings(key));
    }

    public void applyFilterOnPresenter(final FilterSettings filterSettings) {
        initSelectionModel();
        presenter.filterGrid(filterSettings);
    }

    protected FilterSettings getTableFilterSettings(final String key) {
        final HashMap<String, Object> params = getGridSettings(key);
        final String tableSettingsJSON = (String) params.get(FILTER_TABLE_SETTINGS);
        return dataSetEditorManager.getStrToTableSettings(tableSettingsJSON);
    }

    @Override
    public FilterSettings getAdvancedSearchFilterSettings() {
        return getTableFilterSettings(TAB_SEARCH);
    }

    @Override
    public void saveAdvancedSearchFilterSettings(final FilterSettings settings) {
        final HashMap<String, Object> gridSettings = getGridSettings(TAB_SEARCH);
        gridSettings.put(FILTER_TABLE_SETTINGS,
                         dataSetEditorManager.getTableSettingsToStr(settings));
        filterPagedTable.saveTabSettings(TAB_SEARCH,
                                         gridSettings);
    }

    protected HashMap<String, Object> getGridSettings(final String key) {
        return filterPagedTable.getMultiGridPreferencesStore().getGridSettings(key);
    }

    public void setIdentity(User identity){
        this.identity = identity;
    }

    @Inject
    public void setPreferencesService(final Caller<UserPreferencesService> preferencesService){
        this.preferencesService = preferencesService;
    }

    @Inject
    public void setDataSetEditorManager(final DataSetEditorManager dataSetEditorManager) {
        this.dataSetEditorManager = dataSetEditorManager;
    }

    public void resetDefaultFilterTitleAndDescription(){
        saveTabSettings(TAB_SEARCH,
                        constants.Search(),
                        constants.SearchResults());
    }

    protected void saveTabSettings(final String key, final String name, final String description){
        final HashMap<String, Object> tabSettingsValues = getGridSettings(key);
        if (tabSettingsValues != null) {
            tabSettingsValues.put(NewTabFilterPopup.FILTER_TAB_NAME_PARAM,
                                  name);
            tabSettingsValues.put(NewTabFilterPopup.FILTER_TAB_DESC_PARAM,
                                  description);
            filterPagedTable.saveTabSettings(key,
                                             tabSettingsValues);
        }
    }

    public FilterPagedTable<T> getFilterPagedTable() {
        return filterPagedTable;
    }

    public void setFilterPagedTable(FilterPagedTable<T> filterPagedTable) {
        this.filterPagedTable = filterPagedTable;
    }

    public Column<T, String> createTextColumn(final String columnId, final Function<T, String> valueFunction) {
        Column<T, String> column = new Column<T, String>( new TextCell() ) {
            @Override
            public String getValue( T domain ) {
                return valueFunction.apply( domain );
            }
        };
        column.setSortable( true );
        column.setDataStoreName( columnId );
        return column;
    }

    public Column<T, Number> createNumberColumn(final String columnId, final Function<T, Number> valueFunction) {
        Column<T, Number> column = new Column<T, Number>( new NumberCell() ) {
            @Override
            public Number getValue( T domain ) {
                return valueFunction.apply( domain );
            }
        };
        column.setSortable( true );
        column.setDataStoreName( columnId );
        return column;
    }

    public int getRefreshValue() {
        return getMultiGridPreferencesStore().getRefreshInterval();
    }

    public void saveRefreshValue( int newValue ) {
        filterPagedTable.saveNewRefreshInterval( newValue );
    }

    @Override
    public void addTextFilter(String label,
                              String placeholder,
                              ParameterizedCommand<String> addCallback,
                              ParameterizedCommand<String> removeCallback) {
        if(advancedSearchTable != null){
            advancedSearchTable.getAdvancedSearchFiltersView().addTextFilter(label, placeholder, addCallback, removeCallback);
        }
    }

    @Override
    public void addNumericFilter(String label,
                                 String placeholder,
                                 ParameterizedCommand<String> addCallback,
                                 ParameterizedCommand<String> removeCallback) {
        if(advancedSearchTable != null){
            advancedSearchTable.getAdvancedSearchFiltersView().addNumericFilter(label, placeholder, addCallback, removeCallback);
        }
    }

    @Override
    public void addSelectFilter(String label,
                                Map<String, String> options,
                                Boolean liveSearch,
                                ParameterizedCommand<String> addCallback,
                                ParameterizedCommand<String> removeCallback) {
        if(advancedSearchTable != null){
            advancedSearchTable.getAdvancedSearchFiltersView().addSelectFilter(label, options, liveSearch, addCallback, removeCallback);
        }
    }

    @Override
    public void addActiveFilter(String labelKey,
                                String labelValue,
                                String value,
                                ParameterizedCommand<String> removeCallback) {
        if(advancedSearchTable != null){
            advancedSearchTable.getAdvancedSearchFiltersView().addActiveFilter(labelKey, labelValue, value, removeCallback);
        }
    }

}