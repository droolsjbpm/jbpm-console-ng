/*
 * Copyright 2013 JBoss Inc
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

package org.jbpm.console.ng.he.client.listevents;

import java.util.Comparator;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.he.client.i8n.Constants;
import org.jbpm.console.ng.he.client.util.ResizableHeader;
import org.jbpm.console.ng.he.client.util.UtilEvent;
import org.jbpm.console.ng.he.model.UserInteractionSummary;
import org.uberfire.workbench.events.NotificationEvent;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.Heading;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionModel;

@Dependent
@Templated(value = "HumanEventListViewImpl.html")
public class HumanEventListViewImpl extends Composite implements HumanEventPresenter.ActionHistoryView {
    private Constants constants = GWT.create(Constants.class);

    @Inject
    @DataField
    public NavLink clearEventsNavLink;

    @Inject
    @DataField
    public NavLink exportEventsNavLink;

    @Inject
    @DataField
    public TextBox searchBox;

    @Inject
    @DataField
    public NavLink showInfoEventsNavLink;

    @DataField
    public Heading taskCalendarViewLabel = new Heading(4);

    @Inject
    @DataField
    public FlowPanel eventsViewContainer;

    @Inject
    @DataField
    public IconAnchor refreshIcon;

    @Inject
    private Event<NotificationEvent> notification;

    private HumanEventPresenter presenter;

    public DataGrid<UserInteractionSummary> myEventListGrid;

    public SimplePager pager;

    private ListHandler<UserInteractionSummary> sortHandler;

    private MultiSelectionModel<UserInteractionSummary> selectionModel;

    @Override
    public void init(HumanEventPresenter presenter) {
        this.presenter = presenter;

        refreshIcon.setTitle(constants.Refresh());
        refreshIcon.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                refreshHumanEvents();
                searchBox.setText("");
                displayNotification(constants.Events_Refreshed());
            }
        });

        // By Default we will start in Grid View
        initializeGridView();

        clearEventsNavLink.setText(constants.Clear_Events());
        clearEventsNavLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearEventsNavLink.setStyleName("active");
                showInfoEventsNavLink.setStyleName("");
                exportEventsNavLink.setStyleName("");
                clearHumanEvents();
            }
        });

        showInfoEventsNavLink.setText(constants.Info());
        showInfoEventsNavLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showInfoEventsNavLink.setStyleName("active");
                clearEventsNavLink.setStyleName("");
                exportEventsNavLink.setStyleName("");
                showInfoEvents();
            }
        });

        exportEventsNavLink.setText(constants.Export_Txt());
        exportEventsNavLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showInfoEventsNavLink.setStyleName("");
                clearEventsNavLink.setStyleName("");
                exportEventsNavLink.setStyleName("active");
                exportTxtEvents();
            }
        });

        taskCalendarViewLabel.setText(constants.List_Human_Event());
        taskCalendarViewLabel.setStyleName("");

        searchBox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (event.getNativeKeyCode() == 13 || event.getNativeKeyCode() == 32) {
                    displayNotification("Filter Event: |" + searchBox.getText() + "|");
                    filterEvents(searchBox.getText());
                }

            }
        });

        refreshHumanEvents();

    }

    public void filterEvents(String text) {
        presenter.filterEvents(text);
    }

    private void initializeGridView() {
        eventsViewContainer.clear();
        myEventListGrid = new DataGrid<UserInteractionSummary>();
        myEventListGrid.setStyleName("table table-bordered table-striped table-hover");
        pager = new SimplePager();
        pager.setStyleName("pagination pagination-right pull-right");
        pager.setDisplay(myEventListGrid);
        pager.setPageSize(30);

        eventsViewContainer.add(myEventListGrid);
        eventsViewContainer.add(pager);

        myEventListGrid.setHeight("350px");
        // Set the message to display when the table is empty.
        myEventListGrid.setEmptyTableWidget(new Label(constants.No_Human_Events()));

        // Attach a column sort handler to the ListDataProvider to sort the
        // list.
        sortHandler = new ColumnSortEvent.ListHandler<UserInteractionSummary>(presenter.getAllEventsSummaries());

        myEventListGrid.addColumnSortHandler(sortHandler);

        myEventListGrid.setSelectionModel(selectionModel,
                DefaultSelectionEventManager.<UserInteractionSummary> createCheckboxManager());

        initTableColumns(selectionModel);
        presenter.addDataDisplay(myEventListGrid);

    }

    private void initTableColumns(final SelectionModel<UserInteractionSummary> selectionModel) {
        // Timestamp.
        Column<UserInteractionSummary, String> timeColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                if (object.getTimestamp() != null) {
                    return UtilEvent.getDateTime(object.getTimestamp(), UtilEvent.patternDateTime);
                }
                return "";
            }
        };
        timeColumn.setSortable(true);

        myEventListGrid.addColumn(timeColumn, new ResizableHeader(constants.Time(), myEventListGrid, timeColumn));
        sortHandler.setComparator(timeColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                if (o1.getTimestamp() == null || o2.getTimestamp() == null) {
                    return 0;
                }
                return o1.getTimestamp().compareTo(o2.getTimestamp());
            }
        });

        // Module.
        Column<UserInteractionSummary, String> moduleNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getModule();
            }
        };
        moduleNameColumn.setSortable(true);

        myEventListGrid.addColumn(moduleNameColumn, new ResizableHeader(constants.Module(), myEventListGrid, moduleNameColumn));
        sortHandler.setComparator(moduleNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getModule().compareTo(o2.getModule());
            }
        });

        // User.
        Column<UserInteractionSummary, String> userNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getUser();
            }
        };
        userNameColumn.setSortable(true);

        myEventListGrid.addColumn(userNameColumn, new ResizableHeader(constants.User(), myEventListGrid, userNameColumn));
        sortHandler.setComparator(userNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getUser().compareTo(o2.getUser());
            }
        });

        // Component.
        Column<UserInteractionSummary, String> componentNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getComponent();
            }
        };
        componentNameColumn.setSortable(true);

        myEventListGrid.addColumn(componentNameColumn, new ResizableHeader(constants.Component(), myEventListGrid,
                componentNameColumn));
        sortHandler.setComparator(componentNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getComponent().compareTo(o2.getComponent());
            }
        });

        // Action.
        Column<UserInteractionSummary, String> actionNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getAction();
            }
        };
        actionNameColumn.setSortable(true);

        myEventListGrid
                .addColumn(actionNameColumn, new ResizableHeader(constants.Actions(), myEventListGrid, actionNameColumn));
        sortHandler.setComparator(actionNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getAction().compareTo(o2.getAction());
            }
        });

        // key
        Column<UserInteractionSummary, String> taskIdColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getKey();
            }
        };
        taskIdColumn.setSortable(true);
        myEventListGrid.setColumnWidth(taskIdColumn, "80px");

        myEventListGrid.addColumn(taskIdColumn, new ResizableHeader(constants.Id_Event(), myEventListGrid, taskIdColumn));
        sortHandler.setComparator(taskIdColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // Level.
        Column<UserInteractionSummary, String> levelNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getLevel();
            }
        };
        levelNameColumn.setSortable(true);

        myEventListGrid.addColumn(levelNameColumn, new ResizableHeader(constants.Level(), myEventListGrid, levelNameColumn));
        sortHandler.setComparator(levelNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getLevel().compareTo(o2.getLevel());
            }
        });
        myEventListGrid.setColumnWidth(levelNameColumn, "140px");

        // Status.
        Column<UserInteractionSummary, String> statusNameColumn = new Column<UserInteractionSummary, String>(new TextCell()) {
            @Override
            public String getValue(UserInteractionSummary object) {
                return object.getStatus();
            }
        };
        statusNameColumn.setSortable(true);

        myEventListGrid.addColumn(statusNameColumn, new ResizableHeader(constants.Status(), myEventListGrid, statusNameColumn));
        sortHandler.setComparator(statusNameColumn, new Comparator<UserInteractionSummary>() {
            @Override
            public int compare(UserInteractionSummary o1, UserInteractionSummary o2) {
                return o1.getStatus().compareTo(o2.getStatus());
            }
        });
        myEventListGrid.setColumnWidth(statusNameColumn, "140px");

    }

    @Override
    public void refreshHumanEvents() {
        presenter.refreshHumanEvent();
    }

    @Override
    public void clearHumanEvents() {
        presenter.clearHumanEvents();
    }

    @Override
    public void showInfoEvents() {
        presenter.showInfoEvents();
    }

    @Override
    public void exportTxtEvents() {
        presenter.exportToTxt();
    }

    @Override
    public void displayNotification(String text) {
        notification.fire(new NotificationEvent(text));
    }

    @Override
    public MultiSelectionModel<UserInteractionSummary> getSelectionModel() {
        return selectionModel;
    }

    public TextBox getSearchBox() {
        return searchBox;
    }
}
