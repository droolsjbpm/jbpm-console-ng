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
package org.jbpm.workbench.ht.client.perspectives;

import javax.enterprise.context.ApplicationScoped;

import org.jbpm.workbench.common.client.perspectives.AbstractPerspective;
import org.jbpm.workbench.ht.client.editors.taskslist.TaskAdminListPresenter;
import org.kie.workbench.common.workbench.client.PerspectiveIds;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.ClosableSimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

/**
 * Administration view of the task list
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = PerspectiveIds.TASKS_ADMIN)
public class TaskAdminListPerspective extends AbstractPerspective {

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl(ClosableSimpleWorkbenchPanelPresenter.class.getName());
        p.setName(PerspectiveIds.TASKS_ADMIN);
        p.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest(TaskAdminListPresenter.SCREEN_ID)));
        return p;
    }

    @Override
    public String getPerspectiveId() {
        return PerspectiveIds.TASKS_ADMIN;
    }
}
