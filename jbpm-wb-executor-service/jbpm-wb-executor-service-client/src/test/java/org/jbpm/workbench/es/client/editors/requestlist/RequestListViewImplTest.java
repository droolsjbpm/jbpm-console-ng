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
package org.jbpm.workbench.es.client.editors.requestlist;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.client.ui.Button;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.common.client.util.ButtonActionCell;
import org.jbpm.workbench.df.client.filter.FilterSettings;
import org.jbpm.workbench.df.client.list.base.DataSetEditorManager;
import org.jbpm.workbench.es.client.editors.requestlist.RequestListViewImpl.ActionHasCell;
import org.jbpm.workbench.es.model.RequestSummary;
import org.jbpm.workbench.es.util.RequestStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.ext.services.shared.preferences.GridColumnPreference;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;
import org.uberfire.ext.services.shared.preferences.GridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.MultiGridPreferencesStore;
import org.uberfire.ext.services.shared.preferences.UserPreferencesService;
import org.uberfire.ext.widgets.common.client.tables.FilterPagedTable;
import org.uberfire.ext.widgets.table.client.ColumnMeta;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mvp.Command;

import static org.jbpm.workbench.es.model.RequestDataSetConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class RequestListViewImplTest {

    @Mock
    protected ExtendedPagedTable<RequestSummary> currentListGrid;

    @Mock
    FilterPagedTable filterPagedTableMock;

    @Mock
    MultiGridPreferencesStore multiGridPreferencesStoreMock;

    @Mock
    protected GridPreferencesStore gridPreferencesStoreMock;

    @Mock
    protected Button mockButton;

    @Mock
    protected RequestListPresenter presenter;

    @Mock
    private DataSetEditorManager dataSetEditorManager;

    @Mock
    private AsyncDataProvider dataProvider;

    @Mock
    protected UserPreferencesService userPreferencesService;

    @Spy
    private FilterSettings filterSettings;

    @InjectMocks
    private RequestListViewImpl view;
    
    @Mock
    protected Cell.Context cellContext;
    
    @Mock
    protected ActionCell.Delegate<RequestSummary> cellDelegate;

    @Before
    public void setup(){
        when(presenter.getDataProvider()).thenReturn(mock(AsyncDataProvider.class));
        when(presenter.createTableSettingsPrototype()).thenReturn(filterSettings);
        when(presenter.createAllTabSettings()).thenReturn(filterSettings);
        when(presenter.createCancelledTabSettings()).thenReturn(filterSettings);
        when(presenter.createCompletedTabSettings()).thenReturn(filterSettings);
        when(presenter.createErrorTabSettings()).thenReturn(filterSettings);
        when(presenter.createQueuedTabSettings()).thenReturn(filterSettings);
        when(presenter.createRetryingTabSettings()).thenReturn(filterSettings);
        when(presenter.createRunningTabSettings()).thenReturn(filterSettings);
        when(presenter.createSearchTabSettings()).thenReturn(filterSettings);

        final CallerMock<UserPreferencesService> caller = new CallerMock<>(userPreferencesService);
        view.setPreferencesService(caller);
    }

    @Test
    public void testDataStoreNameIsSet() {
        doAnswer( new Answer() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[ 0 ];
                for ( ColumnMeta columnMeta : columns ) {
                    assertNotNull( columnMeta.getColumn().getDataStoreName() );
                }
                return null;
            }
        } ).when( currentListGrid ).addColumns( anyList() );

        view.initColumns( currentListGrid );

        verify( currentListGrid ).addColumns( anyList() );
    }

    @Test
    public void setDefaultFilterTitleAndDescriptionTest() {
        when(filterPagedTableMock.getMultiGridPreferencesStore()).thenReturn(multiGridPreferencesStoreMock);
        view.resetDefaultFilterTitleAndDescription();

        verify(filterPagedTableMock, times(8)).getMultiGridPreferencesStore();
        verify(filterPagedTableMock, times(8)).saveTabSettings(anyString(), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.TAB_SEARCH), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_0"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_1"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_2"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_3"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_4"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_5"), any(HashMap.class));
        verify(filterPagedTableMock).saveTabSettings(eq(RequestListViewImpl.REQUEST_LIST_PREFIX + "_6"), any(HashMap.class));

    }

    @Test
    public void initColumnsTest() {
        doAnswer( new Answer() {
            @Override
            public Void answer( InvocationOnMock invocationOnMock ) throws Throwable {
                final List<ColumnMeta> columns = (List<ColumnMeta>) invocationOnMock.getArguments()[ 0 ];
                assertTrue(columns.size()==9);
                return null;
            }
        } ).when( currentListGrid ).addColumns(anyList());

        ArrayList<GridColumnPreference> columnPreferences = new ArrayList<GridColumnPreference>();
        when(currentListGrid.getGridPreferencesStore()).thenReturn(gridPreferencesStoreMock);
        when(gridPreferencesStoreMock.getColumnPreferences()).thenReturn(columnPreferences);

        view.initColumns(currentListGrid);

        verify( currentListGrid ).addColumns(anyList());
    }

    @Test
    public void initialColumnsTest() {
        view.init(presenter);

        List<GridColumnPreference> columnPreferences = view.getListGrid().getGridPreferencesStore().getColumnPreferences();
        assertEquals(COLUMN_ID,
                     columnPreferences.get(0).getName());
        assertEquals(COLUMN_BUSINESSKEY,
                     columnPreferences.get(1).getName());
        assertEquals(COLUMN_COMMANDNAME,
                     columnPreferences.get(2).getName());
        assertEquals(RequestListViewImpl.COL_ID_ACTIONS,
                     columnPreferences.get(3).getName());
    }

    @Test
    public void initDefaultFiltersOwnTaskFilter() {
        when(presenter.getDataProvider()).thenReturn(dataProvider);
        view.initDefaultFilters(new GridGlobalPreferences(), mockButton);

        verify(filterPagedTableMock, times(8)).addTab(any(ExtendedPagedTable.class), anyString(), any(Command.class), eq(false));
        verify(filterPagedTableMock).addAddTableButton(mockButton);
        verify(presenter).setAddingDefaultFilters(true);
    }
    
    @Test
    public void multiStateActionHasCellTest(){
        RequestListViewImpl.ActionHasCell multiStateCell = new RequestListViewImpl.ActionHasCell("", cellDelegate,
                RequestStatus.QUEUED,
                RequestStatus.DONE,
                RequestStatus.CANCELLED
        );
        for(RequestStatus rs: RequestStatus.values()){
            RequestSummary testRequest = new RequestSummary(1L, new Date(), rs.toString(), null,
                    "Test message", "Test key", 1, 1, "procName", 55L, "procInstDesc");
            boolean shouldRender = false;
            switch(rs){
                case QUEUED:
                case DONE:
                case CANCELLED:
                    shouldRender = true;
                    break;
            }
            runActionHasCellTest(multiStateCell, testRequest, shouldRender);
        }
    }
    
    @Test
    public void singleStateActionHasCellTest(){
        for(RequestStatus cellRs: RequestStatus.values()){
            RequestListViewImpl.ActionHasCell multiStateCell = new RequestListViewImpl.ActionHasCell("", cellDelegate, cellRs);
            for(RequestStatus valueRs: RequestStatus.values()){
                RequestSummary testRequest = new RequestSummary(1L, new Date(), valueRs.toString(), null,
                        "Test message", "Test key", 1, 1, "procName", 55L, "procInstDesc");
                boolean shouldRender = (valueRs == cellRs);
                runActionHasCellTest(multiStateCell, testRequest, shouldRender);
            }
        }
    }
    
    @Test
    public void predicateActionHasCellTest(){
        RequestSummary testRequest = new RequestSummary(1L, new Date(), "DONE", null, "Test message", "Test key", 1, 1, "procName", 55L, "procInstDesc");

        RequestListViewImpl.ActionHasCell alwaysDisplayedActionCell = new RequestListViewImpl.ActionHasCell("", (val -> true), cellDelegate);
        runActionHasCellTest(alwaysDisplayedActionCell, testRequest, true);

        RequestListViewImpl.ActionHasCell neverDisplayedActionCell = new RequestListViewImpl.ActionHasCell("", (val -> false), cellDelegate);
        runActionHasCellTest(neverDisplayedActionCell, testRequest, false);
    }
    
    private void runActionHasCellTest(RequestListViewImpl.ActionHasCell cell, RequestSummary val, boolean shouldRender){
        RequestListViewImpl.ActionHasCell cellMock = spy(cell);
        SafeHtmlBuilder cellHtmlBuilder = mock(SafeHtmlBuilder.class);
        doAnswer(invocationOnMock -> {
            invocationOnMock.callRealMethod();
            verify(cellHtmlBuilder, times(shouldRender ? 1 : 0)).append(any());
            return null;
        }).when(cellMock).render(any(), any(), eq(cellHtmlBuilder));

        cellMock.render(cellContext, val, cellHtmlBuilder);

        verify(cellMock).render(cellContext, val, cellHtmlBuilder);
    }

}
