/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.console.ng.gc.forms.client.display.views;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jbpm.console.ng.ga.forms.display.view.FormContentResizeListener;
import org.jbpm.console.ng.gc.forms.client.display.GenericFormDisplayer;
import org.jbpm.console.ng.gc.forms.client.resources.AppResources;
import org.uberfire.mvp.Command;

@Dependent
public class EmbeddedFormDisplayView extends Composite implements FormDisplayerView {

    private final AppResources appResources = AppResources.INSTANCE;

    private FlowPanel formContainer = GWT.create(FlowPanel.class);
    private FlowPanel formPanel = GWT.create(FlowPanel.class);
    private GenericFormDisplayer currentDisplayer;

    private Command onCloseCommand;

    private FormContentResizeListener resizeListener;

    @PostConstruct
    public void init() {
        initWidget(formContainer);
        formContainer.addStyleName(appResources.style().taskFormContainer());
    }

    @Override
    public void display(final GenericFormDisplayer displayer) {
        currentDisplayer = displayer;

        formPanel.clear();
        formPanel.addStyleName(appResources.style().taskFormPanel());
        formPanel.add(displayer.getContainer());

        formContainer.clear();
        formContainer.add(formPanel);

        if (displayer.getOpener() == null) {
            final FlowPanel buttonsPanel = GWT.create(FlowPanel.class);
            buttonsPanel.addStyleName(appResources.style().taskButtons());
            buttonsPanel.add(displayer.getFooter());
            formContainer.add(buttonsPanel);
        }
    }

    public Widget getView() {
        return this;
    }

    @Override
    public Command getOnCloseCommand() {
        return onCloseCommand;
    }

    @Override
    public void setOnCloseCommand(Command onCloseCommand) {
        this.onCloseCommand = onCloseCommand;
    }

    @Override
    public FormContentResizeListener getResizeListener() {
        return resizeListener;
    }

    @Override
    public void setResizeListener(FormContentResizeListener resizeListener) {
        this.resizeListener = resizeListener;
    }

    @Override
    public GenericFormDisplayer getCurrentDisplayer() {
        return currentDisplayer;
    }

}