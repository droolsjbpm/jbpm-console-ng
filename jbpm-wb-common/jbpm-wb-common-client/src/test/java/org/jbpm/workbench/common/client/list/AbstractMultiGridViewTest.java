/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Consumer;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.view.client.AsyncDataProvider;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jbpm.workbench.common.client.util.ConditionalKebabActionCell;
import org.jbpm.workbench.common.model.GenericSummary;
import org.jbpm.workbench.common.preferences.ManagePreferences;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.services.shared.preferences.GridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.GridSortedColumnPreference;
import org.uberfire.ext.services.shared.preferences.UserPreferencesService;
import org.uberfire.ext.services.shared.preferences.UserPreferencesType;
import org.uberfire.ext.widgets.table.client.ColumnMeta;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.ParameterizedCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public abstract class AbstractMultiGridViewTest<T extends GenericSummary> {

    @Mock
    protected AsyncDataProvider dataProviderMock;

    protected CallerMock<UserPreferencesService> userPreferencesService;

    @Mock
    protected UserPreferencesService userPreferencesServiceMock;

    @Mock
    protected ListTable extendedPagedTable;

    @Mock
    protected HasWidgets rightActionsToolbar;

    @Mock
    protected Button mockButton;

    @Spy
    protected GridPreferencesStore gridPreferencesStore;

    @Mock
    protected ManagePreferences preferences;

    @Mock
    protected ManagedInstance<ConditionalKebabActionCell> conditionalKebabActionCell;

    protected abstract AbstractMultiGridView getView();

    protected abstract AbstractMultiGridPresenter getPresenter();

    public abstract List<String> getExpectedInitialColumns();

    public abstract List<String> getExpectedBannedColumns();

    public abstract Integer getExpectedNumberOfColumns();

    @Before
    public void setupMocks() {
        userPreferencesService = new CallerMock<>(userPreferencesServiceMock);
        getView().setUserPreferencesService(userPreferencesService);
        when(getPresenter().getDataProvider()).thenReturn(dataProviderMock);
        when(userPreferencesServiceMock.loadUserPreferences(any(), eq(UserPreferencesType.GRIDPREFERENCES))).thenReturn(new GridPreferencesStore(new GridGlobalPreferences()));
        when(conditionalKebabActionCell.get()).thenReturn(mock(ConditionalKebabActionCell.class));
        doNothing().when(getView()).addNewTableToColumn(any());
        when(extendedPagedTable.getRightActionsToolbar()).thenReturn(rightActionsToolbar);
        List<Button> a = Collections.singletonList(mockButton);
        when(rightActionsToolbar.spliterator()).thenReturn(Spliterators.spliteratorUnknownSize(a.iterator(), 0));
    }

    @Test
    public void selectionIgnoreColumnTest() {
        ExtendedPagedTable<GenericSummary> extPagedTable = new ExtendedPagedTable<GenericSummary>(new GridGlobalPreferences());
        Column testCol = getView().createTextColumn("testCol", (val -> val));

        extPagedTable.addSelectionIgnoreColumn(testCol);
        assertFalse(extPagedTable.isSelectionIgnoreColumn(extPagedTable.getColumnIndex(testCol)));
        assertTrue(extPagedTable.removeSelectionIgnoreColumn(testCol));

        extPagedTable.addColumn(testCol, "");
        assertFalse(extPagedTable.isSelectionIgnoreColumn(extPagedTable.getColumnIndex(testCol)));
        extPagedTable.addSelectionIgnoreColumn(testCol);
        assertTrue(extPagedTable.isSelectionIgnoreColumn(extPagedTable.getColumnIndex(testCol)));
    }

    @Test
    public void testInitialColumns() {
        final List<String> expectedInitColumns = getExpectedInitialColumns();

        assertEquals(expectedInitColumns.size(), getView().getInitColumns().size());
        for (int i = 0; i < expectedInitColumns.size(); i++) {
            assertEquals(expectedInitColumns.get(i), getView().getInitColumns().get(i));
        }
    }

    @Test
    public void testBannedColumns() {
        List<String> bannedColumns = getView().getBannedColumns();

        assertEquals(getExpectedBannedColumns().size(), bannedColumns.size());
        for (int i = 0; i < bannedColumns.size(); i++) {
            assertEquals(getExpectedBannedColumns().get(i), bannedColumns.get(i));
        }
    }

    @Test
    public void testDataStoreNameIsSet() {
        final ListTable<T> currentListGrid = spy(new ListTable<T>(new GridGlobalPreferences()));
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[0];
                for (ColumnMeta columnMeta : columns) {
                    assertNotNull(columnMeta.getColumn().getDataStoreName());
                }
                return null;
            }
        }).when(currentListGrid).addColumns(anyList());

        getView().initColumns(currentListGrid);

        verify(currentListGrid).addColumns(anyList());
    }

    @Test
    public void testInitColumns() {
        final ListTable<T> currentListGrid = spy(new ListTable<T>(new GridGlobalPreferences()));
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[0];
                assertTrue(columns.size() == getExpectedNumberOfColumns());
                return null;
            }
        }).when(currentListGrid).addColumns(anyList());

        ArrayList<GridColumnPreference> columnPreferences = new ArrayList<GridColumnPreference>();
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStore);
        when(gridPreferencesStore.getColumnPreferences()).thenReturn(columnPreferences);

        getView().initColumns(currentListGrid);

        InOrder inOrder = inOrder(currentListGrid);
        inOrder.verify(currentListGrid).addColumns(anyList());
        inOrder.verify(currentListGrid).setColumnWidth(any(), anyDouble(), any());
    }

    @Test
    public void testInitActionsColumn() {
        ColumnMeta columnMeta = getView().initActionsColumn();
        assertEquals(false, columnMeta.isVisibleIndex());
    }

    @Test
    public void testGlobalPreferences() {
        doAnswer((InvocationOnMock inv) -> {
            ((ParameterizedCommand<ManagePreferences>) inv.getArguments()[0]).execute(new ManagePreferences().defaultValue(new ManagePreferences()));
            return null;
        }).when(preferences).load(any(ParameterizedCommand.class), any(ParameterizedCommand.class));
        Consumer<ListTable> consumer = table -> assertEquals(ManagePreferences.DEFAULT_PAGINATION_OPTION.intValue(),
                                                             table.getGridPreferencesStore().getPageSizePreferences());
        getView().loadListTable("key", consumer);
    }

    @Test
    public void testControlBulkOperationsEnabled() {
        when(extendedPagedTable.hasSelectedItems()).thenReturn(true);
        getView().controlBulkOperations(extendedPagedTable);
        verify(mockButton).setEnabled(true);
    }

    @Test
    public void testControlBulkOperationsDisabled() {
        when(extendedPagedTable.hasSelectedItems()).thenReturn(false);
        getView().controlBulkOperations(extendedPagedTable);
        verify(mockButton).setEnabled(false);
    }

    @Test
    public void testEnableDataGridMinWidth() {
        doAnswer((InvocationOnMock inv) -> {
            ((ParameterizedCommand<ManagePreferences>) inv.getArguments()[0]).execute(new ManagePreferences().defaultValue(new ManagePreferences()));
            return null;
        }).when(preferences).load(any(ParameterizedCommand.class), any(ParameterizedCommand.class));
        Consumer<ListTable> consumer = table -> assertTrue(table.isDataGridMinWidthEnabled());
        getView().loadListTable("key", consumer);
    }

    public static Column newColumnMock(final String dataStoreName){
        Column column = mock(Column.class);
        when(column.getCell()).thenReturn(mock(Cell.class));
        when(column.getDataStoreName()).thenReturn(dataStoreName);
        return column;
    }

    @Test
    public void testReloadColumnSortListWhenInitListTable() {
        ListTable<T> listTable = mock(ListTable.class);
        when(getView().getListGrid()).thenReturn(listTable);
        GridPreferencesStore gridPreferencesStore = mock(GridPreferencesStore.class);
        when(listTable.getGridPreferencesStore()).thenReturn(gridPreferencesStore);
        GridSortedColumnPreference gridSortedColumnPreference = new GridSortedColumnPreference("test", true);
        when(gridPreferencesStore.getGridSortedColumnPreference()).thenReturn(gridSortedColumnPreference);
        ColumnSortList columnSortList = getColumnSortList("startDate", null);
        when(listTable.getColumnSortList()).thenReturn(columnSortList);

        when(listTable.getColumnMetaList()).thenReturn(Arrays.asList(new ColumnMeta(getColumn("test"), "test"),
                                                                     new ColumnMeta(getColumn("startDate"), "")));

        assertEquals(1, columnSortList.size());

        getView().reloadColumnSortList();

        assertEquals(1, columnSortList.size());
        assertEquals("test", columnSortList.get(0).getColumn().getDataStoreName());
    }

    private ColumnSortList getColumnSortList(String dataStoreName,
                                             ColumnSortList columnSortList) {
        if (columnSortList == null) {
            columnSortList = new ColumnSortList();
        }
        columnSortList.push(new ColumnSortList.ColumnSortInfo(getColumn(dataStoreName),
                                                              true));
        return columnSortList;
    }

    private Column<T, String> getColumn(String name) {
        Column<T, String> column = new Column<T, String>(new TextCell()) {
            @Override
            public String getValue(T domain) {
                return null;
            }
        };
        column.setDataStoreName(name);
        return column;
    }

    @Test
    public void testAddColumnSortHandler() {
        ListTable<T> listTable = mock(ListTable.class);
        when(listTable.getGridPreferencesStore()).thenReturn(gridPreferencesStore);
        when(listTable.getColumnSortList()).thenReturn(new ColumnSortList());

        doAnswer(invocation -> {
            final ColumnSortEvent event = mock(ColumnSortEvent.class);
            final ColumnSortList columnSortList = new ColumnSortList();
            columnSortList.push(getColumn("test"));
            when(event.getColumnSortList()).thenReturn(columnSortList);
            ((ColumnSortEvent.Handler) invocation.getArguments()[0]).onColumnSort(event);
            return null;
        }).when(listTable).addColumnSortHandler(any());

        getView().addColumnSortHandler(listTable);

        ArgumentCaptor<GridSortedColumnPreference> captor = ArgumentCaptor.forClass(GridSortedColumnPreference.class);
        verify(gridPreferencesStore).setGridSortedColumnPreference(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals("test", captor.getValue().getDataStoreName());
        assertEquals(true, captor.getValue().isAscending());
    }

    @Test
    public void testGetSortColumn() {
        ListTable<T> listTable = mock(ListTable.class);
        when(getView().getListGrid()).thenReturn(listTable);
        final ColumnSortList columnSortList = new ColumnSortList();
        columnSortList.push(getColumn("test"));
        when(listTable.getColumnSortList()).thenReturn(null, null, new ColumnSortList(), new ColumnSortList(), columnSortList, columnSortList);

        //sort list null
        assertNull(getView().getSortColumn());
        assertNull(getView().isSortAscending());

        //sort list empty
        assertNull(getView().getSortColumn());
        assertNull(getView().isSortAscending());

        //sort list with column
        assertEquals("test", getView().getSortColumn());
        assertEquals(true, getView().isSortAscending());
    }
}
