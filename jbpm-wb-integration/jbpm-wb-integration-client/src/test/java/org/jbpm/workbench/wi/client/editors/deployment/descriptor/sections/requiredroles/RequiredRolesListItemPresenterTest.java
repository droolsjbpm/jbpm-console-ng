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

package org.jbpm.workbench.wi.client.editors.deployment.descriptor.sections.requiredroles;

import org.jbpm.workbench.wi.client.editors.deployment.descriptor.sections.requiredroles.DeploymentsRequiredRolesPresenter.RemoteableClassListPresenter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.screens.library.client.settings.util.modal.single.AddSingleValueModal;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class RequiredRolesListItemPresenterTest {

    @Mock
    private RequiredRolesListItemPresenter.View view;
    
    @Mock
    private AddSingleValueModal singleValueModal;

    private RequiredRolesListItemPresenter requiredRolesListItemPresenter;

    @Before
    public void before() {
        requiredRolesListItemPresenter = spy(new RequiredRolesListItemPresenter(view,
                                                                                singleValueModal));
    }

    @Test
    public void testSetup() {
        requiredRolesListItemPresenter.setup("Role", mock(DeploymentsRequiredRolesPresenter.class));
        verify(view).init(eq(requiredRolesListItemPresenter));
        verify(view).setRole(eq("Role"));
    }

    @Test
    public void testRemove() {
        final DeploymentsRequiredRolesPresenter parentPresenter = mock(DeploymentsRequiredRolesPresenter.class);
        final RemoteableClassListPresenter listPresenter = mock(RemoteableClassListPresenter.class);

        requiredRolesListItemPresenter.parentPresenter = parentPresenter;
        requiredRolesListItemPresenter.setListPresenter(listPresenter);

        requiredRolesListItemPresenter.remove();

        verify(listPresenter).remove(eq(requiredRolesListItemPresenter));
        verify(parentPresenter).fireChangeEvent();
    }

    @Test
    public void testOpenRequiredRoleModal() {
        final DeploymentsRequiredRolesPresenter parentPresenter = mock(DeploymentsRequiredRolesPresenter.class);
        final RemoteableClassListPresenter listPresenter = mock(RemoteableClassListPresenter.class);

        requiredRolesListItemPresenter.parentPresenter = parentPresenter;
        requiredRolesListItemPresenter.setListPresenter(listPresenter);

        requiredRolesListItemPresenter.openRequiredRoleModal();

        ArgumentCaptor<Consumer> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(singleValueModal).show(captor.capture(), any());
        captor.getValue().accept("Value");

        verify(listPresenter).add("Value");
        verify(parentPresenter).fireChangeEvent();
        verify(singleValueModal).show(any(), any());
    }
}