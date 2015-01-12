/*
 * Copyright 2012 JBoss Inc
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
package org.jbpm.console.ng.ht.admin.client.editors.settings;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.ht.admin.client.i18n.ProcessAdminConstants;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.workbench.events.NotificationEvent;

@Dependent
@Templated(value = "ProcessAdminSettingsViewImpl.html")
public class ProcessAdminSettingsViewImpl extends Composite implements ProcessAdminSettingsPresenter.ProcessAdminSettingsView {

    @Inject
    private PlaceManager placeManager;

    private ProcessAdminSettingsPresenter presenter;

    @Inject
    @DataField
    public Button generateMockInstancesButton;

    @Inject
    @DataField
    public Label deploymentIdLabel;
    
    @Inject
    @DataField
    public TextBox deploymentIdText;

    @Inject
    @DataField
    public Label processIdLabel;

    @Inject
    @DataField
    public TextBox processIdText;

    @Inject
    @DataField
    public Label amountOfTasksLabel;
    
    @Inject
    @DataField
    public TextBox amountOfTasksText;

    @Inject
    private Event<NotificationEvent> notification;

    private ProcessAdminConstants processAdminConstants = GWT.create(ProcessAdminConstants.class);

    @Override
    public void init(ProcessAdminSettingsPresenter presenter) {
        this.presenter = presenter;
        
        amountOfTasksLabel.setText( processAdminConstants.Amount_Of_Tasks());
        deploymentIdLabel.setText( processAdminConstants.DeploymentId());
        processIdLabel.setText( processAdminConstants.ProcessId());
        generateMockInstancesButton.setText( processAdminConstants.Generate_Mock_Instances());
    }

    

    @EventHandler("generateMockTasksButton")
    public void generateMockTasksButton(ClickEvent e) {
        
            presenter.generateMockInstances( deploymentIdText.getText(), processIdText.getText(), Integer.parseInt( amountOfTasksText.getText() ) );
       
    }

   

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    @Override
    public TextBox getDeploymentIdText() {
        return deploymentIdText;
    }

    @Override
    public TextBox getProcessIdText() {
        return processIdText;
    }


    @Override
    public Button getGenerateMockInstancesButton() {
        return generateMockInstancesButton;
    }

}
