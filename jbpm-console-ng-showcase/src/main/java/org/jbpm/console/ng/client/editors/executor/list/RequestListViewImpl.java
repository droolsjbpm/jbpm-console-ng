///*
// * Copyright 2012 JBoss Inc
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *       http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package org.jbpm.console.ng.client.editors.executor.list;
//
//import java.util.Comparator;
//import java.util.Set;
//
//import javax.enterprise.context.Dependent;
//import javax.enterprise.event.Event;
//import javax.enterprise.event.Observes;
//import javax.inject.Inject;
//
//import org.jbpm.console.ng.client.editors.tasks.inbox.events.TaskSelectionEvent;
//import org.jbpm.console.ng.client.editors.tasks.inbox.events.UserTaskEvent;
//import org.jbpm.console.ng.client.model.TaskSummary;
//import org.uberfire.client.mvp.PlaceManager;
//import org.uberfire.client.workbench.widgets.events.NotificationEvent;
//import org.uberfire.shared.mvp.PlaceRequest;
//
//
//import com.google.gwt.cell.client.ButtonCell;
//import com.google.gwt.cell.client.CheckboxCell;
//import com.google.gwt.cell.client.EditTextCell;
//import com.google.gwt.cell.client.FieldUpdater;
//import com.google.gwt.cell.client.NumberCell;
//import com.google.gwt.cell.client.TextCell;
//import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.safehtml.shared.SafeHtmlUtils;
//import com.google.gwt.user.cellview.client.Column;
//import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
//import com.google.gwt.user.cellview.client.DataGrid;
//import com.google.gwt.user.cellview.client.SafeHtmlHeader;
//import com.google.gwt.user.cellview.client.SimplePager;
//import com.google.gwt.user.client.ui.Button;
//import com.google.gwt.user.client.ui.CheckBox;
//import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.Label;
//import com.google.gwt.view.client.DefaultSelectionEventManager;
//import com.google.gwt.view.client.MultiSelectionModel;
//import com.google.gwt.view.client.SelectionChangeEvent;
//import com.google.gwt.view.client.SelectionModel;
//import org.jboss.errai.ui.shared.api.annotations.DataField;
//import org.jboss.errai.ui.shared.api.annotations.EventHandler;
//import org.jboss.errai.ui.shared.api.annotations.Templated;
//import org.jbpm.console.ng.client.editors.tasks.inbox.events.RequestChangedEvent;
//import org.jbpm.console.ng.client.editors.tasks.inbox.events.RequestSelectionEvent;
//import org.jbpm.console.ng.client.model.RequestSummary;
//import org.jbpm.console.ng.client.util.ResizableHeader;
//
//import org.jbpm.console.ng.client.i18n.Constants;
//
//@Dependent
//@Templated(value = "RequestListViewImpl.html")
//public class RequestListViewImpl extends Composite
//    implements
//    RequestListPresenter.InboxView {
//
//   
//
//    @Inject
//    private PlaceManager                       placeManager;
//    private RequestListPresenter             presenter;
//    
//    @Inject
//    @DataField
//    public Button                              refreshRequestsButton;
//    
//    @Inject
//    @DataField
//    public DataGrid<RequestSummary>               myRequestListGrid;
//    @Inject
//    @DataField
//    public SimplePager                         pager;
//    @Inject
//    @DataField
//    public CheckBox                            showCompletedCheck;
//    
//    private Set<RequestSummary>                   selectedRequests;
//    @Inject
//    private Event<NotificationEvent>           notification;
//    @Inject
//    private Event<RequestSelectionEvent>          taskSelection;
//    
//    private ListHandler<RequestSummary>            sortHandler;
//
//    private Constants constants = GWT.create(Constants.class);
//
//    @Override
//    public void init(RequestListPresenter presenter) {
//        this.presenter = presenter;
//
//        
//        myRequestListGrid.setWidth( "100%" );
//        myRequestListGrid.setHeight("200px");
//
//        // Set the message to display when the table is empty.
//        myRequestListGrid.setEmptyTableWidget( new Label( constants.Hooray_you_don_t_have_any_pending_Task__() ) );
//
//        // Attach a column sort handler to the ListDataProvider to sort the list.
//        sortHandler =
//                new ListHandler<RequestSummary>( presenter.getDataProvider().getList() );
//        myRequestListGrid.addColumnSortHandler( sortHandler );
//        
//        // Create a Pager to control the table.
//        
//        pager.setDisplay( myRequestListGrid );
//        pager.setPageSize( 6 );
//
//        // Add a selection model so we can select cells.
//        final MultiSelectionModel<RequestSummary> selectionModel =
//                new MultiSelectionModel<RequestSummary>();
//        selectionModel.addSelectionChangeHandler( new SelectionChangeEvent.Handler() {
//            public void onSelectionChange(SelectionChangeEvent event) {
//                selectedRequests = selectionModel.getSelectedSet();
//                for ( RequestSummary r : selectedRequests ) {
//                    taskSelection.fire( new RequestSelectionEvent( r.getId() ) );
//                }
//            }
//        } );
//
//        myRequestListGrid.setSelectionModel( selectionModel,
//                                          DefaultSelectionEventManager
//                                                  .<RequestSummary> createCheckboxManager() );
//
//        initTableColumns( selectionModel );
//
//       
//
//        presenter.addDataDisplay( myRequestListGrid );
//
//    }
//
//    public void recieveStatusChanged(@Observes UserTaskEvent event) {
//        Boolean isChecked = showCompletedCheck.getValue();
//
//        presenter.refreshRequests(
//                                isChecked );
//        
//    }
//
//    @EventHandler("refreshRequestsButton")
//    public void refreshRequestsButton(ClickEvent e) {
//        Boolean isChecked = showCompletedCheck.getValue();
//        presenter.refreshRequests( isChecked );
//    }
//
//   
//   
//
//   
//
//    private void initTableColumns(final SelectionModel<RequestSummary> selectionModel) {
//        // Checkbox column. This table will uses a checkbox column for selection.
//        // Alternatively, you can call dataGrid.setSelectionEnabled(true) to enable
//        // mouse selection.
//
//        Column<RequestSummary, Boolean> checkColumn =
//                new Column<RequestSummary, Boolean>( new CheckboxCell( true,
//                                                                    false ) ) {
//                    @Override
//                    public Boolean getValue(RequestSummary object) {
//                        // Get the value from the selection model.
//                        return selectionModel.isSelected( object );
//                    }
//                };
//        myRequestListGrid.addColumn( checkColumn,
//                                  SafeHtmlUtils.fromSafeConstant( "<br/>" ) );
//       
//
//        // Id
//        Column<RequestSummary, Number> taskIdColumn =
//                new Column<RequestSummary, Number>( new NumberCell() ) {
//                    @Override
//                    public Number getValue(RequestSummary object) {
//                        return object.getId();
//                    }
//                };
//        taskIdColumn.setSortable( true );
//        sortHandler.setComparator( taskIdColumn,
//                                   new Comparator<RequestSummary>() {
//                                       public int compare(RequestSummary o1,
//                                                          RequestSummary o2) {
//                                           return Long.valueOf( o1.getId() ).compareTo( Long.valueOf( o2.getId() ) );
//                                       }
//                                   } );
//        myRequestListGrid.addColumn( taskIdColumn,
//                                  new ResizableHeader(constants.Id(),myRequestListGrid, taskIdColumn ));
//       
//   
//        // Task name.
//        Column<RequestSummary, String> taskNameColumn =
//                new Column<RequestSummary, String>( new EditTextCell() ) {
//                    @Override
//                    public String getValue(RequestSummary object) {
//                        return object.getCommandName();
//                    }
//                };
//        taskNameColumn.setSortable( true );
//        sortHandler.setComparator( taskNameColumn,
//                                   new Comparator<RequestSummary>() {
//                                       public int compare(RequestSummary o1,
//                                                          RequestSummary o2) {
//                                           return o1.getCommandName().compareTo( o2.getCommandName() );
//                                       }
//                                   } );
//        myRequestListGrid.addColumn( taskNameColumn,
//                                  new ResizableHeader(constants.Task(),myRequestListGrid, taskNameColumn ));
//        
//       
//      
//
//        // Status.
//        Column<RequestSummary, String> statusColumn = new Column<RequestSummary, String>( new TextCell() ) {
//            @Override
//            public String getValue(RequestSummary object) {
//                return object.getStatus().toString();
//            }
//        };
//        statusColumn.setSortable( true );
//        sortHandler.setComparator( statusColumn,
//                                   new Comparator<RequestSummary>() {
//                                       public int compare(RequestSummary o1,
//                                                          RequestSummary o2) {
//                                           return o1.getStatus().compareTo( o2.getStatus() );
//                                       }
//                                   } );
//
//        myRequestListGrid.addColumn( statusColumn,
//                                  new ResizableHeader(constants.Status(),myRequestListGrid, statusColumn ));
//       
//
//       
//
//        
//        
//        // Due Date.
//        Column<RequestSummary, String> dueDateColumn = new Column<RequestSummary, String>( new TextCell() ) {
//            @Override
//            public String getValue(RequestSummary object) {
//                if(object.getTime() != null){
//                    return object.getTime().toString();
//                }
//                return "";
//            }
//        };
//        dueDateColumn.setSortable( true );
//
//        myRequestListGrid.addColumn( dueDateColumn,
//                                  new ResizableHeader(constants.Due_On(),myRequestListGrid, dueDateColumn ));
//       
//
//       
//     
//
//        Column<RequestSummary, String> editColumn =
//                new Column<RequestSummary, String>( new ButtonCell() ) {
//                    @Override
//                    public String getValue(RequestSummary task) {
//                        return "Details";
//                    }
//
//                };
//
//        editColumn.setFieldUpdater( new FieldUpdater<RequestSummary, String>() {
//            @Override
//            public void update(int index,
//                               RequestSummary request,
//                               String value) {
//                placeManager.goTo( new PlaceRequest( constants.Request_Details_Perspective_Errai() ) );
//                taskSelection.fire( new RequestSelectionEvent( request.getId() ) );
//            }
//        } );
//
//        myRequestListGrid.addColumn( editColumn,
//                                  new SafeHtmlHeader( SafeHtmlUtils.fromSafeConstant( constants.Details() ) ) );
//      
//   
//
//    }
//
//    public void displayNotification(String text) {
//        notification.fire( new NotificationEvent( text ) );
//    }
//
//   
//
//    public void onRequestSelected(@Observes RequestChangedEvent taskChanged) {
//        Boolean isChecked = showCompletedCheck.getValue();
//        presenter.refreshRequests(isChecked );
//
//    }
//
//    public CheckBox getShowCompletedCheck() {
//        return showCompletedCheck;
//    }
//
//    public DataGrid<RequestSummary> getDataGrid() {
//        return myRequestListGrid;
//    }
//
//    public ListHandler<RequestSummary> getSortHandler() {
//        return sortHandler;
//    }
//    
//    
//
//}
