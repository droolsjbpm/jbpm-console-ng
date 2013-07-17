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

package org.jbpm.console.ng.ht.client.editors.taskcomments;

import com.github.gwtbootstrap.client.ui.Button;
import java.util.Comparator;
import java.util.Date;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.console.ng.ht.model.CommentSummary;

import com.github.gwtbootstrap.client.ui.DataGrid;
import com.github.gwtbootstrap.client.ui.Label;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import java.text.SimpleDateFormat;
import org.jbpm.console.ng.ht.client.i18n.Constants;

@Dependent
@Templated(value = "TaskCommentsPopupViewImpl.html")
public class TaskCommentsPopupViewImpl extends Composite implements TaskCommentsPopupPresenter.TaskCommentsPopupView {
    private Constants constants = GWT.create(Constants.class);

    private TaskCommentsPopupPresenter presenter;

    @Inject
    @DataField
    public Label taskIdText;

    @Inject
    @DataField
    public Label taskNameText;

    @Inject
    @DataField
    public TextArea newTaskCommentTextArea;
    
    @Inject
    @DataField
    public Label newTaskCommentLabel;

    @Inject
    @DataField
    public Button addCommentButton;

    @Inject
    @DataField
    public UnorderedList navBarUL;

    @Inject
    @DataField
    public DataGrid<CommentSummary> commentsListGrid;

    @Inject
    @DataField
    public SimplePager pager;

    @Inject
    @DataField
    public FlowPanel listContainer;

    private ListHandler<CommentSummary> sortHandler;



    @Override
    public Label getTaskIdText() {
        return taskIdText;
    }

    @Override
    public Label getTaskNameText() {
        return taskNameText;
    }

    @Override
    public UnorderedList getNavBarUL() {
        return navBarUL;
    }

    @Override
    public TextArea getNewTaskCommentTextArea() {
        return newTaskCommentTextArea;
    }

    @Override
    public Button addCommentButton() {
        return addCommentButton;
    }

    @Override
    public DataGrid<CommentSummary> getDataGrid() {
        return commentsListGrid;
    }

    @Override
    public SimplePager getPager() {
        return pager;
    }

    @Override
    public void init(TaskCommentsPopupPresenter presenter) {
        this.presenter = presenter;
        listContainer.add(commentsListGrid);
        listContainer.add(pager);
        commentsListGrid.setHeight("100px");
        Label emtpyTable = new Label(constants.No_Comments_For_This_Task());
        commentsListGrid.setEmptyTableWidget(emtpyTable);
        // Attach a column sort handler to the ListDataProvider to sort the list.
        sortHandler = new ListHandler<CommentSummary>(presenter.getDataProvider().getList());
        commentsListGrid.addColumnSortHandler(sortHandler);
        initTableColumns();
        presenter.addDataDisplay(commentsListGrid);
        // Create a Pager to control the table.
        pager.setVisible(false);
        pager.setDisplay(commentsListGrid);
        pager.setPageSize(6);
        newTaskCommentTextArea.setWidth("300px");
        addCommentButton.setText(constants.Add_Comment());
        newTaskCommentLabel.setText(constants.Comment());
    }

    @EventHandler("addCommentButton")
    public void addCommentButton(ClickEvent e) {
        presenter.addTaskComment(Long.parseLong(taskIdText.getText()), newTaskCommentTextArea.getText(), new Date());
        //Fix for BZ 983377
        newTaskCommentTextArea.setText("");
    }

    private void initTableColumns() {
        // addedBy
        Column<CommentSummary, String> addedByColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary c) {
                // for some reason the username comes in format [User:'<name>'], so parse just the <name>
                int first = c.getAddedBy().indexOf('\'');
                int last = c.getAddedBy().lastIndexOf('\'');
                return c.getAddedBy().substring(first + 1, last);
            }
        };
        addedByColumn.setSortable(false);
        commentsListGrid.addColumn(addedByColumn, constants.Added_By());
        commentsListGrid.setColumnWidth(addedByColumn, "100px");

        // date
        Column<CommentSummary, String> addedAtColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary c) {
                DateTimeFormat format = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
                return format.format(c.getAddedAt());
            }
        };
        addedAtColumn.setSortable(true);
        addedAtColumn.setDefaultSortAscending(true);
        commentsListGrid.addColumn(addedAtColumn, constants.At());
        sortHandler.setComparator(addedAtColumn, new Comparator<CommentSummary>() {
            @Override
            public int compare(CommentSummary o1, CommentSummary o2) {
                return o1.getAddedAt().compareTo(o2.getAddedAt());
            }
        });

        // comment text
        Column<CommentSummary, String> commentTextColumn = new Column<CommentSummary, String>(new TextCell()) {
            @Override
            public String getValue(CommentSummary object) {
                return object.getText();
            }
        };
        addedByColumn.setSortable(false);
        commentsListGrid.addColumn(commentTextColumn, constants.Comment());
    }
}
