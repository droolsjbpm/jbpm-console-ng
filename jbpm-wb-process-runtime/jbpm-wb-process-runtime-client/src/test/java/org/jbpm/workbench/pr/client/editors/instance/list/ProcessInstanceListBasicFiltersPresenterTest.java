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

package org.jbpm.workbench.pr.client.editors.instance.list;

import java.util.Map;

import java.util.Arrays;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.jbpm.workbench.common.client.filters.active.ActiveFilterItem;
import org.jbpm.workbench.common.client.filters.basic.AbstractBasicFiltersPresenterTest;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;

import static com.google.common.collect.Maps.immutableEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.workbench.pr.model.ProcessInstanceDataSetConstants.COLUMN_PARENT_PROCESS_INSTANCE_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class ProcessInstanceListBasicFiltersPresenterTest extends AbstractBasicFiltersPresenterTest {

    @InjectMocks
    ProcessInstanceListBasicFiltersPresenter presenter;

    @Override
    public ProcessInstanceListBasicFiltersPresenter getPresenter() {
        return presenter;
    }

    @Override
    @Test
    public void testLoadFilters() {
        presenter.loadFilters();

        final InOrder inOrder = inOrder(getView());

        inOrder.verify(getView()).addNumericFilter(eq(Constants.INSTANCE.Id()), any(), any());
        inOrder.verify(getView()).addTextFilter(eq(Constants.INSTANCE.Initiator()), any(), any());
        inOrder.verify(getView()).addTextFilter(eq(Constants.INSTANCE.Correlation_Key()), any(), any());
        inOrder.verify(getView()).addTextFilter(eq(Constants.INSTANCE.Process_Instance_Description()), any(), any());
        inOrder.verify(getView()).addMultiSelectFilter(eq(Constants.INSTANCE.Errors()), any(), any());
        inOrder.verify(getView()).addMultiSelectFilter(eq(Constants.INSTANCE.State()), any(), any());
        inOrder.verify(getView()).addDataSetSelectFilter(eq(Constants.INSTANCE.Name()), any(), any(), any(), any());
        inOrder.verify(getView()).addDataSetSelectFilter(eq(Constants.INSTANCE.Process_Definition_Id()), any(), any(), any(), any());
        inOrder.verify(getView()).addDataSetSelectFilter(eq(Constants.INSTANCE.DeploymentId()), any(), any(), any(), any());
        inOrder.verify(getView()).addDataSetSelectFilter(eq(Constants.INSTANCE.Parent_Process_Instance_Id()), any(), any(), any(), any());
        inOrder.verify(getView()).addSelectFilter(eq(Constants.INSTANCE.SlaCompliance()), any(), any());
        inOrder.verify(getView()).addDateRangeFilter(eq(Constants.INSTANCE.Start_Date()), any(), any(), any());
        inOrder.verify(getView()).addDateRangeFilter(eq(Constants.INSTANCE.Last_Modification_Date()), any(), any(), any());
    }

    @Test
    public void testSlaComplianceFilters() {
        presenter.addSLAComplianceFilter();
        org.jbpm.workbench.common.client.resources.i18n.Constants commonConstants = org.jbpm.workbench.common.client.resources.i18n.Constants.INSTANCE;

        ArgumentCaptor<Map> slaDescriptionsCaptors = ArgumentCaptor.forClass(Map.class);
        verify(getView()).addSelectFilter(anyString(), slaDescriptionsCaptors.capture(), any());
        final Map<String, String> slaDescriptions = slaDescriptionsCaptors.getValue();
        assertThat(slaDescriptions).containsOnly(
                immutableEntry(String.valueOf(ProcessInstance.SLA_NA), commonConstants.INSTANCE.SlaNA()),
                immutableEntry(String.valueOf(ProcessInstance.SLA_PENDING), commonConstants.SlaPending()),
                immutableEntry(String.valueOf(ProcessInstance.SLA_MET), commonConstants.SlaMet()),
                immutableEntry(String.valueOf(ProcessInstance.SLA_VIOLATED), commonConstants.SlaViolated()),
                immutableEntry(String.valueOf(ProcessInstance.SLA_ABORTED), commonConstants.SlaAborted()));
    }

    @Test
    public void testParentProcessIdFilter() {
        presenter.addParentProcessIdFilter();
        verify(getView()).addDataSetSelectFilter(eq(Constants.INSTANCE.Parent_Process_Instance_Id()),
                                                 any(),
                                                 eq(COLUMN_PARENT_PROCESS_INSTANCE_ID),
                                                 eq(COLUMN_PARENT_PROCESS_INSTANCE_ID),
                                                 any());
    }

    @Test
    public void onActiveFilterAddedTest() {
        ActiveFilterItem activeFilterItemMock = mock(ActiveFilterItem.class);
        when(activeFilterItemMock.getKey()).thenReturn(Constants.INSTANCE.Name());
        presenter.onActiveFilterAdded(activeFilterItemMock);
        verify(getView(), never()).checkSelectFilter(anyString(), anyString());
        verify(activeFilterItemMock, never()).getValue();

        when(activeFilterItemMock.getKey()).thenReturn(Constants.INSTANCE.State());
        when(activeFilterItemMock.getValue()).thenReturn(Arrays.asList(String.valueOf(ProcessInstance.STATE_ACTIVE),
                                                                       String.valueOf(ProcessInstance.STATE_COMPLETED)));
        presenter.onActiveFilterAdded(activeFilterItemMock);
        verify(getView()).checkSelectFilter(Constants.INSTANCE.State(), String.valueOf(ProcessInstance.STATE_ACTIVE));
        verify(getView()).checkSelectFilter(Constants.INSTANCE.State(), String.valueOf(ProcessInstance.STATE_COMPLETED));
    }

    @Test
    public void testOnActiveFilterAdded() {
        ActiveFilterItem testActiveFilterItem = new ActiveFilterItem(Constants.INSTANCE.State(), "test label", null, Arrays.asList("Active"), null);
        presenter.onActiveFilterAdded(testActiveFilterItem);
        verify(getView()).checkSelectFilter(Constants.INSTANCE.State(), "Active");

        ActiveFilterItem activeWithErrorsFilterItem = new ActiveFilterItem("errorCount", "test label", null, Arrays.asList("0"), null);
        presenter.onActiveFilterAdded(activeWithErrorsFilterItem);
        verify(getView()).checkSelectFilter(Constants.INSTANCE.Errors(), "0");

        ActiveFilterItem activeWithoutErrorsFilterItem = new ActiveFilterItem("errorCount", "test label", null, Arrays.asList("1"), null);
        presenter.onActiveFilterAdded(activeWithoutErrorsFilterItem);
        verify(getView()).checkSelectFilter(Constants.INSTANCE.Errors(), "1");
    }
}
