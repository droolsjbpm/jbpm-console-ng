/*
 * Copyright 2012 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.console.ng.pr.model.events;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ProcessDefSelectionEvent {
    private String processId;
    private String deploymentId;

    private String serverTemplateId;

    public ProcessDefSelectionEvent() {
    }

    public ProcessDefSelectionEvent(String processId) {
        this.processId = processId;
    }

    public ProcessDefSelectionEvent(String processId, String deploymentId) {
        this.processId = processId;
        this.deploymentId = deploymentId;
    }

    public ProcessDefSelectionEvent(String processId, String deploymentId, String serverTemplateId) {
        this.processId = processId;
        this.deploymentId = deploymentId;
        this.serverTemplateId = serverTemplateId;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getServerTemplateId() {
        return serverTemplateId;
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }


}
