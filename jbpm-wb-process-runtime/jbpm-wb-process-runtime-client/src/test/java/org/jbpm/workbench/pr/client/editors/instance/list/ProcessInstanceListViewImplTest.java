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
import java.util.List;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.jbpm.workbench.common.client.list.AdvancedSearchTable;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.df.client.list.base.DataSetEditorManager;
import org.jbpm.workbench.pr.model.ProcessInstanceSummary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.services.shared.preferences.GridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.MultiGridPreferencesStore;
import org.uberfire.ext.widgets.common.client.tables.FilterPagedTable;
import org.uberfire.ext.widgets.table.client.ColumnMeta;
import org.uberfire.mvp.Command;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub(AdvancedSearchTable.class)
public class ProcessInstanceListViewImplTest {

    @Mock
    protected ExtendedPagedTable<ProcessInstanceSummary> currentListGrid;

    @Mock
    protected GridPreferencesStore gridPreferencesStore;

    @Mock
    protected DataSetEditorManager dataSetEditorManager;

    @Mock
    protected MultiGridPreferencesStore multiGridPreferencesStore;

    @Mock
    protected FilterPagedTable filterPagedTable;

    @Mock
    protected ProcessInstanceListPresenter presenter;

    @InjectMocks
    private ProcessInstanceListViewImpl view;

    @Before
    public void setupMocks() {
        when(presenter.getDataProvider()).thenReturn(mock(AsyncDataProvider.class));
        when(filterPagedTable.getMultiGridPreferencesStore()).thenReturn(multiGridPreferencesStore);
    }

    @Test
    public void testDataStoreNameIsSet() {
        when(gridPreferencesStore.getColumnPreferences()).thenReturn(new ArrayList<GridColumnPreference>());
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStore);
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


        view.initColumns(currentListGrid);

        verify(currentListGrid).addColumns(anyList());
    }

    @Test
    public void testInitDefaultFilters() {
        view.initDefaultFilters(new GridGlobalPreferences("testGrid", new ArrayList<String>(), new ArrayList<String>()));

        verify(filterPagedTable, times(4)).addTab(any(ExtendedPagedTable.class), anyString(), any(Command.class));
        verify(filterPagedTable, times(4)).saveNewTabSettings(anyString(), (HashMap<String, Object>) any());
        verify(presenter).setAddingDefaultFilters(true);
        verify(presenter).setupAdvanceSearchView();
    }

    @Test
    public void setDefaultFilterTitleAndDescriptionTest() {
        view.resetDefaultFilterTitleAndDescription();

        verify(filterPagedTable, times(4)).getMultiGridPreferencesStore();
        verify(filterPagedTable).saveTabSettings(eq(ProcessInstanceListViewImpl.TAB_SEARCH), any(HashMap.class));
        verify(filterPagedTable).saveTabSettings(eq(ProcessInstanceListViewImpl.TAB_ACTIVE), any(HashMap.class));
        verify(filterPagedTable).saveTabSettings(eq(ProcessInstanceListViewImpl.TAB_COMPLETED), any(HashMap.class));
        verify(filterPagedTable).saveTabSettings(eq(ProcessInstanceListViewImpl.TAB_ABORTED), any(HashMap.class));
    }

    @Test
    public void testSelectColumnAddition() {
        when(gridPreferencesStore.getColumnPreferences()).thenReturn(new ArrayList<GridColumnPreference>());
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStore);

        List<ProcessInstanceSummary> displayedInstances = new ArrayList<>();
        displayedInstances.add(new ProcessInstanceSummary());
        displayedInstances.add(new ProcessInstanceSummary());

        when(presenter.getDisplayedProcessInstances()).thenReturn(displayedInstances);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[0];
                ColumnMeta checkColumnMeta = columns.get(0);

                assertTrue(checkColumnMeta.getColumn().getCell() instanceof CheckboxCell);
                assertTrue(checkColumnMeta.getHeader().getValue() instanceof Boolean);

                return null;
            }
        }).when(currentListGrid).addColumns(anyList());


        view.initColumns(currentListGrid);

        verify(currentListGrid).addColumns(anyList());
    }
    
    @Test
    public void testColumnNumber() {
        when(gridPreferencesStore.getColumnPreferences()).thenReturn(new ArrayList<GridColumnPreference>());
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStore);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[0];
                assertEquals(12, columns.size());
                return null;
            }
        }).when(currentListGrid).addColumns(anyList());

        view.initColumns(currentListGrid);

        verify(currentListGrid).addColumns(anyList());
    }

}
