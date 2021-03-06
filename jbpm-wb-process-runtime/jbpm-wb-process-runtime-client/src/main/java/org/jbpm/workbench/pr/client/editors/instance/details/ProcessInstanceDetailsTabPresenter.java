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
package org.jbpm.workbench.pr.client.editors.instance.details;

import java.util.List;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.workbench.common.client.util.SlaStatusConverter;
import org.jbpm.workbench.pr.client.editors.instance.ProcessInstanceSummaryAware;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.client.util.ProcessInstanceStatusUtils;
import org.jbpm.workbench.pr.events.ProcessInstanceSelectionEvent;
import org.jbpm.workbench.pr.model.NodeInstanceSummary;
import org.jbpm.workbench.pr.model.ProcessInstanceKey;
import org.jbpm.workbench.pr.model.ProcessInstanceSummary;
import org.jbpm.workbench.pr.model.UserTaskSummary;
import org.jbpm.workbench.pr.service.ProcessRuntimeDataService;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
public class ProcessInstanceDetailsTabPresenter implements ProcessInstanceSummaryAware {

    private ProcessInstanceDetailsTabView view;

    private Constants constants = Constants.INSTANCE;

    private Caller<ProcessRuntimeDataService> processRuntimeDataService;


    private Event<ProcessInstanceSelectionEvent> processInstanceSelectionEvent;

    @Inject
    public void setView(final ProcessInstanceDetailsTabView view) {
        this.view = view;
    }

    @Inject
    private Event<NotificationEvent> notification;

    @Inject
    public void setProcessRuntimeDataService(final Caller<ProcessRuntimeDataService> processRuntimeDataService) {
        this.processRuntimeDataService = processRuntimeDataService;
    }

    @Inject
    public void setProcessInstanceSelectionEvent(final Event<ProcessInstanceSelectionEvent> processInstanceSelectionEvent) {
        this.processInstanceSelectionEvent = processInstanceSelectionEvent;
    }

    public IsWidget getWidget() {
        return view;
    }

    @Override
    public void setProcessInstance(ProcessInstanceSummary process) {
        view.setProcessDefinitionIdText("");
        view.setProcessVersionText("");
        view.setProcessDeploymentText("");
        view.setCorrelationKeyText("");
        view.setActiveTasksListBox("");
        view.setStateText("");
        view.setCurrentActivitiesListBox("");
        view.setParentProcessInstanceIdText("", false);
        view.setProcessDefinitionIdText(process.getProcessId());
        view.setProcessVersionText(process.getProcessVersion());
        view.setProcessDeploymentText(process.getDeploymentId());
        view.setCorrelationKeyText(process.getCorrelationKey());
        if (process.getParentId() > 0) {
            setParentInstance(process);
        } else {
            view.setParentProcessInstanceIdText(constants.No_Parent_Process_Instance(), false);
        }

        view.setSlaComplianceText(new SlaStatusConverter().toWidgetValue(process.getSlaCompliance()));
        view.setStateText(ProcessInstanceStatusUtils.toWidgetValue(process.getState()));

        if (process.getActiveTasks() != null && !process.getActiveTasks().isEmpty()) {
            SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

            for (UserTaskSummary uts : process.getActiveTasks()) {
                safeHtmlBuilder.appendEscapedLines(uts.getName() + " (" + uts.getStatus() + ")  " + constants.Owner() + ": " + uts.getOwner() + " \n");
            }
            view.setActiveTasksListBox(safeHtmlBuilder.toSafeHtml().asString());
        }

        processRuntimeDataService.call(
                (final List<NodeInstanceSummary> details) -> {
                    final SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();
                    for (NodeInstanceSummary nis : details) {
                        safeHtmlBuilder.appendEscapedLines(nis.getTimestamp() + ": "
                                                                   + nis.getId() + " - " + nis.getName() + " (" + nis.getType() + ") \n");
                    }
                    view.setCurrentActivitiesListBox(safeHtmlBuilder.toSafeHtml().asString());
                }, (Object o, Throwable throwable) -> {
                    notification.fire(new NotificationEvent(Constants.INSTANCE.CanNotGetInstancesDetailsMessage(throwable.getMessage()), NotificationEvent.NotificationType.WARNING));
                    return false;
                }
        ).getProcessInstanceActiveNodes(process.getProcessInstanceKey());
    }

    protected void setParentInstance(ProcessInstanceSummary process) {
        processRuntimeDataService.call((ProcessInstanceSummary pi) -> {
            if (pi != null) {
                view.setParentProcessInstanceIdText(process.getParentId().toString(), true);
                view.setProcessInstanceDetailsCallback(() -> onProcessInstanceIdSelected(process.getServerTemplateId(), process.getDeploymentId(), process.getParentId()));
            } else {
                view.setParentProcessInstanceIdText(process.getParentId().toString(), false);
            }
        }).getProcessInstance(new ProcessInstanceKey(process.getServerTemplateId(), process.getDeploymentId(), process.getParentId()));
    }

    public void onProcessInstanceIdSelected(String serverTemplateId, String processDeploymentId, Long parentId) {
        processInstanceSelectionEvent.fire(new ProcessInstanceSelectionEvent(new ProcessInstanceKey(serverTemplateId, processDeploymentId, parentId), false));
    }

    public interface ProcessInstanceDetailsTabView extends IsWidget {

        void setCurrentActivitiesListBox(String value);

        void setActiveTasksListBox(String value);

        void setProcessDefinitionIdText(String value);

        void setStateText(String value);

        void setProcessDeploymentText(String value);

        void setProcessVersionText(String value);

        void setCorrelationKeyText(String value);

        void setParentProcessInstanceIdText(String value, boolean hasParentProcessInstanceId);

        void setSlaComplianceText(String value);

        void setProcessInstanceDetailsCallback(Command callback);
    }
}
