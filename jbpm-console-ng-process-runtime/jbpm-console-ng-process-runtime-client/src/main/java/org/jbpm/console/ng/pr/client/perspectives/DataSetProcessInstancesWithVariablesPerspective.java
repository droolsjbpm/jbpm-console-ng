/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.console.ng.pr.client.perspectives;

import javax.enterprise.context.ApplicationScoped;

import org.jbpm.console.ng.gc.client.perspectives.AbstractPerspective;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.annotations.WorkbenchPerspective;
import org.uberfire.client.workbench.panels.impl.ClosableSimpleWorkbenchPanelPresenter;
import org.uberfire.mvp.impl.DefaultPlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

@ApplicationScoped
@WorkbenchPerspective( identifier = DataSetProcessInstancesWithVariablesPerspective.PERSPECTIVE_ID )
public class DataSetProcessInstancesWithVariablesPerspective extends AbstractPerspective {

    public static final String PERSPECTIVE_ID = "DataSet Process Instances With Variables";

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl( ClosableSimpleWorkbenchPanelPresenter.class.getName() );
        p.setName( PERSPECTIVE_ID );

        DefaultPlaceRequest defaultPlaceRequest = new DefaultPlaceRequest( "DataSet Process Instance List With Variables" );

        p.getRoot().addPart( new PartDefinitionImpl( defaultPlaceRequest ) );
        return p;
    }

    @Override
    public String getPerspectiveId() {
        return PERSPECTIVE_ID;
    }
}
