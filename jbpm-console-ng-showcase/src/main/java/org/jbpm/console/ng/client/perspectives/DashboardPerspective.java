/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jbpm.console.ng.client.perspectives;

import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchMenu;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;
import org.uberfire.mvp.impl.DefaultPlaceRequest;


import javax.enterprise.context.ApplicationScoped;


/**
 * A Perspective to show Dashboard
 */
@ApplicationScoped
@WorkbenchPerspective(identifier = "DashboardPerspective")
public class DashboardPerspective {


    @Perspective
    public PerspectiveDefinition buildPerspective() {
        /*
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl();
        p.setName("Dashboard builder");

        final PanelDefinition aPanel = new PanelDefinitionImpl();
        aPanel.addPart(new PartDefinitionImpl(new DefaultPlaceRequest("DashboardPanel")));
        p.getRoot().insertChild(Position.NORTH, aPanel);
        p.setTransient(true);
        return p;   */
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl();
        p.setName("Dashboard builder");
        p.getRoot().addPart(new PartDefinitionImpl(new DefaultPlaceRequest("DashboardPanel")));
        p.setTransient(true);
        return p;
    }

}
