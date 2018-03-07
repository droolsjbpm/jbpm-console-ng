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

package org.jbpm.workbench.pr.backend.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.jbpm.workbench.ks.integration.KieServerIntegration;
import org.jbpm.workbench.pr.model.ProcessDefinitionKey;
import org.jbpm.workbench.pr.model.ProcessInstanceKey;
import org.jbpm.workbench.pr.model.ProcessSummary;
import org.jbpm.workbench.pr.model.RuntimeLogSummary;
import org.jbpm.workbench.pr.service.ProcessRuntimeDataService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static java.util.Collections.singletonList;
import static org.jbpm.workbench.pr.backend.server.ProcessSummaryMapperTest.assertProcessSummary;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoteProcessRuntimeDataServiceImplTest {

    private final String processId = "processId";
    private final String containerId = "containerId";
    private final String serverTemplateId = "serverTemplateId";

    @Mock
    private KieServerIntegration kieServerIntegration;

    @Mock
    private QueryServicesClient queryServicesClient;

    @Mock
    private ProcessServicesClient processServicesClient;

    @InjectMocks
    private RemoteProcessRuntimeDataServiceImpl service;

    @Before
    public void setup() {
        final KieServicesClient kieServicesClient = mock(KieServicesClient.class);
        when(kieServerIntegration.getServerClient(anyString())).thenReturn(kieServicesClient);
        when(kieServicesClient.getServicesClient(QueryServicesClient.class)).thenReturn(queryServicesClient);
        when(kieServicesClient.getServicesClient(ProcessServicesClient.class)).thenReturn(processServicesClient);
    }

    @Test
    public void getProcessInstanceDetailsTest() {
        final Long processInstanceId = 1L;
        final TaskSummary taskSummaryMock = mock(TaskSummary.class);
        final TaskSummaryList taskSummaryListSpy = spy(new TaskSummaryList(singletonList(taskSummaryMock)));
        final ProcessInstance processInstanceSpy = spy(ProcessInstance.builder()
                                                               .activeUserTasks(taskSummaryListSpy)
                                                               .build());
        when(processServicesClient.getProcessInstance(containerId,
                                                      processInstanceId)).thenReturn(processInstanceSpy);
        service.getProcessInstance(serverTemplateId,
                                   new ProcessInstanceKey(serverTemplateId,
                                                          containerId,
                                                          processInstanceId));
        verify(processInstanceSpy).getProcessId();
        verify(processInstanceSpy).getState();
        verify(processInstanceSpy).getContainerId();
        verify(processInstanceSpy).getProcessVersion();
        verify(processInstanceSpy).getCorrelationKey();
        verify(processInstanceSpy).getParentId();
        verify(processInstanceSpy).getSlaCompliance();
        verify(processInstanceSpy).getSlaDueDate();
        verifyActiveUserTasks(taskSummaryListSpy,
                              taskSummaryMock);
        verifyCurrentActivities(processInstanceId);
    }

    public void verifyActiveUserTasks(TaskSummaryList taskSummaryList,
                                      TaskSummary taskSummary) {
        verify(taskSummaryList).getItems();
        verify(taskSummary).getName();
        verify(taskSummary).getStatus();
        verify(taskSummary).getActualOwner();
    }

    private void verifyCurrentActivities(Long processInstanceId) {
        final NodeInstance nodeInstanceMock = mock(NodeInstance.class);
        final List<NodeInstance> nodeInstanceList = singletonList(nodeInstanceMock);
        when(processServicesClient.findActiveNodeInstances(containerId,
                                                           processInstanceId,
                                                           0,
                                                           Integer.MAX_VALUE)).thenReturn(nodeInstanceList);
        when(nodeInstanceMock.getDate()).thenReturn(new Date());
        service.getProcessInstanceActiveNodes(serverTemplateId,
                                              containerId,
                                              processInstanceId);
        verify(processServicesClient).findActiveNodeInstances(containerId,
                                                              processInstanceId,
                                                              0,
                                                              Integer.MAX_VALUE);
        verify(nodeInstanceMock).getDate();
        verify(nodeInstanceMock).getId();
        verify(nodeInstanceMock).getName();
        verify(nodeInstanceMock).getNodeType();
    }

    @Test
    public void testInvalidServerTemplate() throws Exception {
        final Method[] methods = ProcessRuntimeDataService.class.getMethods();
        for (Method method : methods) {
            final Class<?> returnType = method.getReturnType();
            final Object[] args = new Object[method.getParameterCount()];
            Object result = method.invoke(service,
                                          args);

            assertMethodResult(method,
                               returnType,
                               result);

            args[0] = "";
            result = method.invoke(service,
                                   args);
            assertMethodResult(method,
                               returnType,
                               result);
        }
    }

    private void assertMethodResult(final Method method,
                                    final Class<?> returnType,
                                    final Object result) {
        if (Collection.class.isAssignableFrom(returnType)) {
            assertNotNull(format("Returned collection for method %s should not be null",
                                 method.getName()),
                          result);
            assertTrue(format("Returned collection for method %s should be empty",
                              method.getName()),
                       ((Collection) result).isEmpty());
        } else {
            assertNull(format("Returned object for method %s should be null",
                              method.getName()),
                       result);
        }
    }

    @Test
    public void testGetProcesses() {
        final ProcessDefinition def = ProcessDefinition.builder().id(processId).build();

        when(queryServicesClient.findProcesses(0,
                                               10,
                                               "",
                                               true)).thenReturn(singletonList(def));

        final List<ProcessSummary> summaries = service.getProcesses(serverTemplateId,
                                                                    0,
                                                                    10,
                                                                    "",
                                                                    true);

        assertNotNull(summaries);
        assertEquals(1,
                     summaries.size());
        assertProcessSummary(def,
                             summaries.get(0));
    }

    @Test
    public void testGetProcessesByFilter() {
        final ProcessDefinition def = ProcessDefinition.builder().id(processId).build();

        when(queryServicesClient.findProcesses("filter",
                                               0,
                                               10,
                                               "",
                                               true)).thenReturn(singletonList(def));

        final List<ProcessSummary> summaries = service.getProcessesByFilter(serverTemplateId,
                                                                            "filter",
                                                                            0,
                                                                            10,
                                                                            "",
                                                                            true);

        assertNotNull(summaries);
        assertEquals(1,
                     summaries.size());
        assertProcessSummary(def,
                             summaries.get(0));
    }

    @Test
    public void testGetProcess() {
        final ProcessDefinition def = ProcessDefinition.builder().id(processId).build();

        when(processServicesClient.getProcessDefinition(containerId,
                                                        processId)).thenReturn(def);

        final ProcessDefinitionKey pdk = new ProcessDefinitionKey(serverTemplateId,
                                                                  containerId,
                                                                  processId);

        final ProcessSummary summary = service.getProcess(serverTemplateId,
                                                          pdk);

        assertNotNull(summary);
        assertProcessSummary(def,
                             summary);
    }

    @Test
    public void getProcessInstanceLogsTest() {

        final Long processInstanceId = 1L;
        NodeInstance nodeInstance1 = NodeInstance.builder()
                .processInstanceId(processInstanceId)
                .date(new Date())
                .id(2L)
                .name("Test_task")
                .nodeType("HumanTaskNode")
                .completed(false)
                .build();

        NodeInstance nodeInstance2 = NodeInstance.builder()
                .processInstanceId(processInstanceId)
                .id(3L)
                .date(new Date())
                .name("")
                .nodeType("StartNode")
                .completed(false)
                .build();

        when(processServicesClient.findNodeInstances(containerId,
                                                     processInstanceId,
                                                     0,
                                                     Integer.MAX_VALUE))
                .thenReturn(Arrays.asList(nodeInstance1,
                                          nodeInstance2));

        List<RuntimeLogSummary> logs = service.getProcessInstanceLogs(serverTemplateId,
                                                                      containerId,
                                                                      processInstanceId);

        assertEquals(2,
                     logs.size());
        assertRuntimeLogNodeInstance(nodeInstance1,
                                     logs.get(0));
        assertRuntimeLogNodeInstance(nodeInstance2,
                                     logs.get(1));
    }

    private void assertRuntimeLogNodeInstance(NodeInstance node,
                                              RuntimeLogSummary log) {
        assertEquals(Long.valueOf(node.getId()),
                     Long.valueOf(log.getId()));
        assertEquals(node.getNodeType(),
                     log.getNodeType());
        assertEquals(node.getName(),
                     log.getNodeName());
        assertEquals(node.getCompleted(),
                     log.isCompleted());
    }
}