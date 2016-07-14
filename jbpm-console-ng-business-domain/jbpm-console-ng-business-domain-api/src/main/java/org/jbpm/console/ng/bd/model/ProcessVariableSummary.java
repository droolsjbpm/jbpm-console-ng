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

package org.jbpm.console.ng.bd.model;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jbpm.console.ng.ga.model.GenericSummary;

@Portable
public class ProcessVariableSummary extends GenericSummary {

    private String variableId;
    private String variableInstanceId;
    private long processInstanceId;
    private String oldValue;
    private String newValue;
    private long timestamp;
    private String type;
    private String deploymentId;
    private String serverTemplateId;

    public ProcessVariableSummary(String variableId, String variableInstanceId, long processInstanceId, String oldValue,
            String newValue, long timestamp, String type) {
        this.id = variableId;
        this.name = variableId;
        this.variableId = variableId;
        this.variableInstanceId = variableInstanceId;
        this.processInstanceId = processInstanceId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
        this.type = type;
    }

    public ProcessVariableSummary(String variableId, String variableInstanceId, long processInstanceId, String oldValue,
            String newValue, long timestamp, String type, String deploymentId, String serverTemplateId) {
        this.id = variableId;
        this.name = variableId;
        this.variableId = variableId;
        this.variableInstanceId = variableInstanceId;
        this.processInstanceId = processInstanceId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.timestamp = timestamp;
        this.type = type;
        this.deploymentId = deploymentId;
        this.serverTemplateId = serverTemplateId;
    }

    public ProcessVariableSummary() {
    }

    public String getVariableId() {
        return variableId;
    }

    public void setVariableId(String variableId) {
        this.variableId = variableId;
    }

    public String getVariableInstanceId() {
        return variableInstanceId;
    }

    public void setVariableInstanceId(String variableInstanceId) {
        this.variableInstanceId = variableInstanceId;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
