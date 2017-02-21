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

package org.jbpm.console.ng.ht.forms.backend.server;


import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jbpm.console.ng.ht.forms.display.ht.api.TaskFormPermissionDeniedException;
import org.jbpm.kie.services.api.FormProviderService;
import org.jboss.errai.bus.server.annotations.Service;
import org.jbpm.console.ng.ga.forms.service.FormServiceEntryPoint;
import org.jbpm.services.task.exception.PermissionDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@ApplicationScoped
public class FormServiceEntryPointImpl implements FormServiceEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(FormServiceEntryPointImpl.class);


    @Inject
    private FormProviderService displayService;


    @PostConstruct
    public void init() {


    }

    @Override
    public String getFormDisplayTask(long taskId) {
        try {
            return displayService.getFormDisplayTask(taskId);
        } catch (PermissionDeniedException ex) {
            throw new TaskFormPermissionDeniedException();
        }
    }

    @Override
    public String getFormDisplayProcess(String domainId, String processId) {
        return displayService.getFormDisplayProcess(domainId, processId);
    }

}
