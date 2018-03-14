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

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.workbench.ks.integration.AbstractKieServerService;
import org.jbpm.workbench.pr.service.ProcessImageService;
import org.kie.server.client.UIServicesClient;

@Service
@ApplicationScoped
public class RemoteProcessImageServiceImpl extends AbstractKieServerService implements ProcessImageService {

    @Override
    public String getProcessInstanceDiagram(String serverTemplateId,
                                            String containerId,
                                            Long processInstanceId) {
        UIServicesClient uiServicesClient = getClient(serverTemplateId,
                                                      containerId,
                                                      UIServicesClient.class);

        return removeActionsFromSVG(uiServicesClient.getProcessInstanceImage(containerId,
                                                                             processInstanceId));
    }

    @Override
    public String getProcessDiagram(String serverTemplateId,
                                    String containerId,
                                    String processId) {
        UIServicesClient uiServicesClient = getClient(serverTemplateId,
                                                      containerId,
                                                      UIServicesClient.class);

        return removeActionsFromSVG(uiServicesClient.getProcessImage(containerId,
                                                                     processId));
    }

    protected String removeActionsFromSVG(final String originalHTML) {
        if (originalHTML == null) {
            return null;
        }

        return originalHTML.replaceAll("onclick=\".*\"|onmouseover=\".*\"",
                                       "");
    }
}