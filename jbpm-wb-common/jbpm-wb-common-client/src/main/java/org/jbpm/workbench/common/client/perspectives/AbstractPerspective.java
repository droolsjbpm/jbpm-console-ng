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

package org.jbpm.workbench.common.client.perspectives;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jbpm.workbench.common.client.events.SearchEvent;
import org.kie.workbench.common.widgets.client.search.ContextualSearch;
import org.uberfire.client.annotations.Perspective;
import org.uberfire.client.workbench.panels.impl.MultiListWorkbenchPanelPresenter;
import org.uberfire.lifecycle.OnStartup;
import org.uberfire.mvp.PlaceRequest;
import org.uberfire.workbench.model.PerspectiveDefinition;
import org.uberfire.workbench.model.impl.PartDefinitionImpl;
import org.uberfire.workbench.model.impl.PerspectiveDefinitionImpl;

public abstract class AbstractPerspective {

    @Inject
    private ContextualSearch contextualSearch;

    @Inject
    private Event<SearchEvent> searchEvents;

    private PlaceRequest placeRequest;

    @PostConstruct
    protected void init() {
        contextualSearch.setPerspectiveSearchBehavior(getPerspectiveId(),
                                                      searchFilter -> searchEvents.fire(new SearchEvent(searchFilter)));
        placeRequest = getPlaceRequest();
    }

    @Perspective
    public PerspectiveDefinition getPerspective() {
        final PerspectiveDefinition p = new PerspectiveDefinitionImpl(MultiListWorkbenchPanelPresenter.class.getName());
        p.setName(getPerspectiveId());
        p.getRoot().addPart(new PartDefinitionImpl(placeRequest));
        return p;
    }

    @OnStartup
    public void onStartup(final PlaceRequest place) {
        placeRequest.getParameters().clear();
        for (final String param : place.getParameterNames()) {
            placeRequest.addParameter(param,
                                      place.getParameter(param,
                                                         null));
        }
    }

    public abstract String getPerspectiveId();

    public abstract PlaceRequest getPlaceRequest();
}
