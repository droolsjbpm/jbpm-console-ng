/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.workbench.pr.client.editors.diagram;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import org.jboss.errai.common.client.api.Caller;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.events.ProcessDefSelectionEvent;
import org.jbpm.workbench.pr.events.ProcessInstanceSelectionEvent;
import org.jbpm.workbench.pr.service.ProcessImageService;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.ext.widgets.common.client.common.HasBusyIndicator;

@Dependent
public class ProcessDiagramPresenter {

    @Inject
    private ProcessDiagramWidgetView view;

    private Caller<ProcessImageService> processImageService;

    private Constants constants = Constants.INSTANCE;

    @Inject
    public void setProcessImageService(final Caller<ProcessImageService> processImageService) {
        this.processImageService = processImageService;
    }

    public void onProcessInstanceSelectionEvent(@Observes ProcessInstanceSelectionEvent event) {
        String containerId = event.getDeploymentId();
        Long processInstanceId = event.getProcessInstanceId();
        String serverTemplateId = event.getServerTemplateId();

        if (processInstanceId != null) {
            processImageService.call((String svgContent) -> displayImage(svgContent)).getProcessInstanceDiagram(serverTemplateId,
                                                                                                                containerId,
                                                                                                                processInstanceId);
        }
    }

    public void onProcessSelectionEvent(@Observes final ProcessDefSelectionEvent event) {
        String containerId = event.getDeploymentId();
        String serverTemplateId = event.getServerTemplateId();
        String processId = event.getProcessId();
        processImageService.call((String svgContent) -> displayImage(svgContent)).getProcessDiagram(serverTemplateId,
                                                                                                    containerId,
                                                                                                    processId);
    }

    protected void displayImage(final String svgContent) {
        if (svgContent == null || svgContent.isEmpty()) {
            view.displayMessage(constants.Process_Diagram_Not_Found());
        } else {
            view.displayImage(svgContent);
        }
    }

    @WorkbenchPartTitle
    public String getName() {
        return constants.Diagram();
    }

    @WorkbenchPartView
    public IsWidget getView() {
        return view;
    }

    public interface View
            extends
            HasBusyIndicator,
            IsWidget {

    }
}
