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

package org.jbpm.console.ng.pr.model;

import java.util.Date;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jbpm.console.ng.ga.model.GenericSummary;

@Portable
public class ProcessInstanceSummary extends GenericSummary {
    private Long processInstanceId;
    private String processId;
    private String processName;
    private String processVersion;
    private int state;
    private Date startTime;
    private String deploymentId;
    private String initiator;

    public ProcessInstanceSummary(long processInstanceId, String processId, String deploymentId, String processName, String processVersion,
            int state, Date startTime, String initiator) {
        super();
        this.id = processInstanceId;
        this.name = processName;
        this.processInstanceId = processInstanceId;
        this.processId = processId;
        this.processName = processName;
        this.deploymentId = deploymentId;
        this.processVersion = processVersion;
        this.state = state;
        this.startTime = startTime;
        this.initiator = initiator;
    }

    public ProcessInstanceSummary() {
    }

    public Long getProcessInstanceId() {
      return processInstanceId;
    }

    public String getProcessId() {
        return processId;
    }

    public String getProcessName() {
        return processName;
    }

    public int getState() {
        return state;
    }

    public void setProcessInstanceId(Long processInstanceId) {
      this.processInstanceId = processInstanceId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getProcessVersion() {
        return processVersion;
    }

    public void setProcessVersion(String processVersion) {
        this.processVersion = processVersion;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

}
