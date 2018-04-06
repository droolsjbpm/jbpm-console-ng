/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.dashboard.dataset.editor.impl;

import javax.enterprise.context.Dependent;

import org.dashbuilder.client.widgets.common.DataSetEditorPlugin;
import org.dashbuilder.dataprovider.DataSetProviderType;
import org.dashbuilder.dataset.client.resources.bundles.DataSetClientResources;
import org.jbpm.dashboard.dataset.editor.workflow.RemoteDataSetBasicAttributesWorkflow;
import org.jbpm.dashboard.dataset.editor.workflow.RemoteDataSetEditWorkflow;
import org.jbpm.dashboard.renderer.client.panel.i18n.DashboardConstants;
import org.jbpm.workbench.ks.integration.KieServerDataSetProviderType;

import com.google.gwt.safehtml.shared.SafeUri;

@Dependent
public class RemoteDataSetEditorPlugin implements DataSetEditorPlugin {

    private static DataSetProviderType TYPE = new KieServerDataSetProviderType();
    
    @Override
    public String getTypeSelectorTitle() {
        return DashboardConstants.INSTANCE.remote_data_set_editor();
    }

    @Override
    public String getTypeSelectorText() {
        return DashboardConstants.INSTANCE.remote_data_set_editor_description();
    }

    @Override
    public SafeUri getTypeSelectorImageUri() {
        return DataSetClientResources.INSTANCE.images().sqlIcon160().getSafeUri();
    }

    @Override
    public DataSetProviderType getProviderType() {
        return TYPE;
    }

    @Override
    public Class<?> getBasicAttributesWorkflowClass() {
        return RemoteDataSetBasicAttributesWorkflow.class;
    }

    @Override
    public Class<?> getWorkflowClass() {
        return RemoteDataSetEditWorkflow.class;
    }

}
