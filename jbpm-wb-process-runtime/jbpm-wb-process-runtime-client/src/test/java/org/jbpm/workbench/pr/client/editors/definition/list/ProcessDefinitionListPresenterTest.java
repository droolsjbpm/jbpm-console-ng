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

package org.jbpm.workbench.pr.client.editors.definition.list;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.workbench.common.client.PerspectiveIds;
import org.jbpm.workbench.common.client.list.ExtendedPagedTable;
import org.jbpm.workbench.forms.client.display.providers.StartProcessFormDisplayProviderImpl;
import org.jbpm.workbench.forms.client.display.views.PopupFormDisplayerView;
import org.jbpm.workbench.forms.display.api.ProcessDisplayerConfig;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.events.ProcessDefSelectionEvent;
import org.jbpm.workbench.pr.model.ProcessSummary;
import org.jbpm.workbench.pr.service.ProcessRuntimeDataService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.uberfire.client.mvp.PerspectiveActivity;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.ext.widgets.common.client.breadcrumbs.UberfireBreadcrumbs;
import org.uberfire.mocks.CallerMock;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.Commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
public class ProcessDefinitionListPresenterTest {

    private static final String PERSPECTIVE_ID = PerspectiveIds.PROCESS_DEFINITIONS;

    private org.jbpm.workbench.common.client.resources.i18n.Constants commonConstants;

    @Mock
    protected PlaceManager placeManager;

    @Mock
    private UberfireBreadcrumbs breadcrumbs;

    @Mock
    private PerspectiveManager perspectiveManager;

    @Mock
    private PerspectiveActivity perspectiveActivity;

    @Mock
    protected EventSourceMock<ProcessDefSelectionEvent> processDefSelectionEvent;

    @Mock
    ProcessDefinitionListPresenter.ProcessDefinitionListView view;

    @Mock
    ExtendedPagedTable extendedPagedTable;

    Caller<ProcessRuntimeDataService> processRuntimeDataServiceCaller;

    @Mock
    ProcessRuntimeDataService processRuntimeDataService;

    @Mock
    StartProcessFormDisplayProviderImpl startProcessDisplayProvider;

    @Mock
    PopupFormDisplayerView formDisplayPopUp;

    @Mock
    HasData next;

    @InjectMocks
    ProcessDefinitionListPresenter presenter;

    private static List<ProcessSummary> getMockList(int instances) {
        final List<ProcessSummary> summaries = new ArrayList<>();
        for (int i = 0; i < instances; i++) {
            summaries.add(new ProcessSummary());
        }
        return summaries;
    }

    @Before
    public void setup() {
        processRuntimeDataServiceCaller = new CallerMock<ProcessRuntimeDataService>(processRuntimeDataService);
        presenter.setProcessRuntimeDataService(processRuntimeDataServiceCaller);
        when(view.getListGrid()).thenReturn(extendedPagedTable);
        when(extendedPagedTable.getColumnSortList()).thenReturn(new ColumnSortList());
        when(perspectiveManager.getCurrentPerspective()).thenReturn(perspectiveActivity);
        when(perspectiveActivity.getIdentifier()).thenReturn(PERSPECTIVE_ID);

        when(next.getVisibleRange()).thenReturn(new Range(1,
                                                          1));
        commonConstants = org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE;
        presenter.getDataProvider().addDataDisplay(next);
    }

    @Test
    public void testProcessDefNameDefinitionPropagation() {
        final ProcessSummary processSummary = new ProcessSummary();
        processSummary.setProcessDefId("testProcessDefId");
        processSummary.setDeploymentId("testDeploymentId");
        processSummary.setProcessDefName("testProcessDefName");
        processSummary.setDynamic(false);

        presenter.selectProcessDefinition(processSummary);

        verify(processDefSelectionEvent).fire(any(ProcessDefSelectionEvent.class));
        ArgumentCaptor<ProcessDefSelectionEvent> argument = ArgumentCaptor.forClass(ProcessDefSelectionEvent.class);
        verify(processDefSelectionEvent).fire(argument.capture());
        final ProcessDefSelectionEvent event = argument.getValue();
        assertEquals(processSummary.getProcessDefName(),
                     event.getProcessDefName());
        assertEquals(processSummary.getDeploymentId(),
                     event.getDeploymentId());
        assertEquals(processSummary.getProcessDefId(),
                     event.getProcessId());
        assertEquals(processSummary.isDynamic(),
                     event.isDynamic());

        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(Constants.INSTANCE.ProcessDefinitionBreadcrumb((processSummary.getName()))),
                                          eq(Commands.DO_NOTHING));

    }

    @Test
    public void testProcessDefNameDefinitionOpenGenericForm() {
        String processDefName = "testProcessDefName";

        presenter.openGenericForm("processDefId",
                                  "deploymentId",
                                  processDefName);

        ArgumentCaptor<ProcessDisplayerConfig> argument = ArgumentCaptor.forClass(ProcessDisplayerConfig.class);
        verify(startProcessDisplayProvider).setup(argument.capture(),
                                                  any());
        assertEquals(processDefName,
                     argument.getValue().getProcessName());
    }

    @Test
    public void testGetData() {
        when(processRuntimeDataService.getProcesses(anyString(),
                                                    anyInt(),
                                                    anyInt(),
                                                    anyString(),
                                                    anyBoolean()))
                .thenReturn(getMockList(10))
                .thenReturn(getMockList(1));

        Range range = new Range(0,
                                10);
        final ProcessDefinitionListPresenter presenter = spy(this.presenter);
        presenter.getData(range);

        verify(presenter).updateDataOnCallback(anyList(),
                                               eq(0),
                                               eq(10),
                                               eq(false));

        range = new Range(10,
                          10);
        presenter.getData(range);

        verify(presenter).updateDataOnCallback(anyList(),
                                               eq(10),
                                               eq(11),
                                               eq(true));
    }

    @Test
    public void testOnRuntimeDataServiceError() {
        final ProcessDefinitionListPresenter presenter = spy(this.presenter);
        final String errorMessage = Constants.INSTANCE.ResourceCouldNotBeLoaded(commonConstants.Process_Definitions());

        doNothing().when(presenter).showErrorPopup(any());

        assertFalse(presenter.onRuntimeDataServiceError());
        verify(presenter).showErrorPopup(errorMessage);
        verify(view,
               times(2)).hideBusyIndicator();
    }

    @Test
    public void testListBreadcrumbCreation() {
        presenter.createListBreadcrumb();
        ArgumentCaptor<Command> captureCommand = ArgumentCaptor.forClass(Command.class);
        verify(breadcrumbs).clearBreadcrumbs(PERSPECTIVE_ID);
        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(commonConstants.Home()),
                                          captureCommand.capture());

        captureCommand.getValue().execute();
        verify(placeManager).goTo(PerspectiveIds.HOME);

        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(commonConstants.Manage_Process_Definitions()),
                                          eq(Commands.DO_NOTHING));

        verifyNoMoreInteractions(breadcrumbs);
    }

    @Test
    public void testSetupDetailBreadcrumb() {
        String detailLabel = "detailLabel";
        String detailScreenId = "screenId";

        PlaceManager placeManagerMock = mock(PlaceManager.class);
        presenter.setPlaceManager(placeManagerMock);
        presenter.setupDetailBreadcrumb(placeManagerMock,
                                        commonConstants.Manage_Process_Definitions(),
                                        detailLabel,
                                        detailScreenId);

        ArgumentCaptor<Command> captureCommand = ArgumentCaptor.forClass(Command.class);

        verify(breadcrumbs).clearBreadcrumbs(PERSPECTIVE_ID);
        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(commonConstants.Home()),
                                          captureCommand.capture());
        captureCommand.getValue().execute();
        verify(placeManagerMock).goTo(PerspectiveIds.HOME);

        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(commonConstants.Manage_Process_Definitions()),
                                          captureCommand.capture());

        captureCommand.getValue().execute();
        verify(placeManagerMock).closePlace(detailScreenId);

        verify(breadcrumbs).addBreadCrumb(eq(PERSPECTIVE_ID),
                                          eq(detailLabel),
                                          eq(Commands.DO_NOTHING));
    }
}
