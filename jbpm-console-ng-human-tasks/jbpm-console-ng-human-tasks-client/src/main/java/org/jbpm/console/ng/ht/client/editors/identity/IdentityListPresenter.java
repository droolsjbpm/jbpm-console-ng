/*
 * Copyright 2013 JBoss by Red Hat.
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

package org.jbpm.console.ng.ht.client.editors.identity;

import com.github.gwtbootstrap.client.ui.TextBox;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;
import org.jbpm.console.ng.ht.model.IdentitySummary;
import org.uberfire.client.annotations.OnReveal;
import org.uberfire.client.annotations.WorkbenchPartTitle;
import org.uberfire.client.annotations.WorkbenchPartView;
import org.uberfire.client.annotations.WorkbenchScreen;
import org.uberfire.client.mvp.UberView;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import org.jbpm.console.ng.ht.service.TaskServiceEntryPoint;

@Dependent
@WorkbenchScreen(identifier = "Users and Groups")
public class IdentityListPresenter {

    public interface IdentityListView extends UberView<IdentityListPresenter> {

        void displayNotification(String text);

        TextBox getUserText();

        DataGrid<IdentitySummary> getDataGrid();

    }

    @Inject
    private IdentityListView view;
    @Inject
    Caller<TaskServiceEntryPoint> taskServices;
    private ListDataProvider<IdentitySummary> dataProvider = new ListDataProvider<IdentitySummary>();

    @WorkbenchPartTitle
    public String getTitle() {
        return "Users and Groups";
    }

    @WorkbenchPartView
    public UberView<IdentityListPresenter> getView() {
        return view;
    }

    public IdentityListPresenter() {
    }

    @PostConstruct
    public void init() {
    }

    public void addDataDisplay(HasData<IdentitySummary> display) {
        dataProvider.addDataDisplay(display);
    }

    public ListDataProvider<IdentitySummary> getDataProvider() {
        return dataProvider;
    }

    public void refreshData() {
        dataProvider.refresh();
    }

    @OnReveal
    public void onReveal() {
        refreshIdentityList();
    }

    public void refreshIdentityList() {
        taskServices.call(new RemoteCallback<List<IdentitySummary>>() {
            @Override
            public void callback(List<IdentitySummary> entities) {
                dataProvider.getList().clear();
                if (entities != null) {
                    dataProvider.getList().addAll(entities);
                    dataProvider.refresh();
                }

            }
        }).getOrganizationalEntities();
    }

    public void getEntityById(String entityId) {
        taskServices.call(new RemoteCallback<IdentitySummary>() {
            @Override
            public void callback(IdentitySummary identity) {
                dataProvider.getList().clear();
                if (identity != null) {
                    List<IdentitySummary> values = new ArrayList<IdentitySummary>();
                    values.add(identity);

                    dataProvider.getList().addAll(values);
                    dataProvider.refresh();
                }

            }
        }).getOrganizationalEntityById(entityId);
    }

}
