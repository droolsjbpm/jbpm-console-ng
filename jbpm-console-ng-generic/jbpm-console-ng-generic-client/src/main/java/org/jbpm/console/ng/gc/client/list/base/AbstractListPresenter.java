/*
 * Copyright 2014 JBoss by Red Hat.
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
package org.jbpm.console.ng.gc.client.list.base;

import java.util.HashMap;
import java.util.Map;
import javax.enterprise.event.Observes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.Range;
import org.dashbuilder.common.client.StringUtils;
import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.dataset.client.DataSetClientServiceError;
import org.dashbuilder.dataset.client.DataSetReadyCallback;
import org.dashbuilder.dataset.sort.SortOrder;
import org.dashbuilder.displayer.client.DataSetHandler;
import org.dashbuilder.renderer.client.resources.i18n.CommonConstants;
import org.jbpm.console.ng.ga.model.QueryFilter;
import org.jbpm.console.ng.gc.client.displayer.TableSettings;
import org.jbpm.console.ng.gc.client.i18n.Constants;
import org.jbpm.console.ng.gc.client.list.base.events.SearchEvent;
import org.uberfire.paging.AbstractPageRow;
import org.uberfire.paging.PageResponse;


/**
 * @param <T> data type for the AsyncDataProvider
 * @author salaboy
 */
public abstract class AbstractListPresenter<T> {

    protected AsyncDataProvider<T> dataProvider;

    protected QueryFilter currentFilter;

    protected TableSettings currentTableSetting;

    protected int numberOfRows = 0;

    protected String lastOrderedColumn = null;

    protected SortOrder lastSortOrder = null;

    protected DataSet dataSet;

    protected DataSetHandler dataSetHandler;

    private Constants constants = GWT.create(Constants.class);

    protected com.google.gwt.user.client.Timer refreshTimer = null;
    protected boolean autoRefreshEnabled = true;
    protected int autoRefreshSeconds = 10; // This should be loaded from the grid settings (probably the filters)

    protected abstract AbstractListView.ListView getListView();

    public AbstractListPresenter() {
        initDataProvider();
    }

    protected void updateRefreshTimer() {
        if (autoRefreshEnabled && autoRefreshSeconds > 0) {
            if (refreshTimer == null) {
                refreshTimer = new Timer() {
                    public void run() {
                        getData(dataProvider.getDataDisplays().iterator().next().getVisibleRange()); // arghhhh
                    }
                };
            }
            refreshTimer.schedule(autoRefreshSeconds * 1000);
        }
        else if (refreshTimer != null) {
            refreshTimer.cancel();
        }
    }

    public abstract void getData(Range visibleRange);

    protected void initDataProvider(){
        dataProvider = new AsyncDataProvider<T>() {
            @Override
            protected void onRangeChanged(HasData<T> display) {
                getListView().showBusyIndicator(constants.Loading());
                final Range visibleRange = display.getVisibleRange();
                getData(visibleRange);
            }
        } ;
    }

    public void updateDataOnCallback(PageResponse response){
        getListView().hideBusyIndicator();
        dataProvider.updateRowCount(response.getTotalRowSize(),
                response.isTotalRowSizeExact());
        dataProvider.updateRowData(response.getStartRowIndex(),
                response.getPageRowList());
        updateRefreshTimer();
    }

    public void addDataDisplay( final HasData<T> display ) {
        dataProvider.addDataDisplay( display );
    }

    public AsyncDataProvider<T> getDataProvider(){
        return dataProvider;
    }

    public void refreshGrid() {
        if(dataProvider.getDataDisplays().size()>0) {
            HasData<T> next = dataProvider.getDataDisplays().iterator().next();
            next.setVisibleRangeAndClearData( next.getVisibleRange(), true );
        }
    }

    protected void onSearchEvent( @Observes SearchEvent searchEvent ) {
        String filterString = searchEvent.getFilter();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put( "textSearch", filterString.toLowerCase() );
        if ( currentFilter != null ) {
            currentFilter.setParams( params );
        }
        HasData<T> next = dataProvider.getDataDisplays().iterator().next();
        if ( filterString.equals( "" ) ) {
            next.setVisibleRangeAndClearData( next.getVisibleRange(), true );
        } else {
            next.setVisibleRangeAndClearData( new Range( 0, next.getVisibleRange().getLength() ), true );
        }

    }

    public TableSettings getCurrentTableSettings(){
      return currentTableSetting;
    }

    public void lookupDataSet(Integer offset, final DataSetReadyCallback callback) {
        try {
            GWT.log("AbstractListPresenter.lookupDataset 1 "+ currentTableSetting);
            // Get the sort settings
            if (lastOrderedColumn == null) {
                String defaultSortColumn = currentTableSetting.getTableDefaultSortColumnId();
                if (!StringUtils.isBlank( defaultSortColumn )) {
                    lastOrderedColumn = defaultSortColumn;
                    lastSortOrder = currentTableSetting.getTableDefaultSortOrder();
                }
            }
            // Apply the sort order specified (if any)
            if (lastOrderedColumn != null) {
                dataSetHandler.sort( lastOrderedColumn, lastSortOrder );
            }
            // Lookup only the target rows
            dataSetHandler.limitDataSetRows(offset, currentTableSetting.getTablePageSize());

            // Do the lookup
            GWT.log("AbstractListPresenter.lookupDataset 2 do the lookup ");

            dataSetHandler.lookupDataSet(
                    new DataSetReadyCallback() {

                        public void callback( DataSet dataSet ) {
                            AbstractListPresenter.this.dataSet = dataSet;
                            numberOfRows = dataSet.getRowCountNonTrimmed();
                            GWT.log("AbstractListPresenter.lookupDataset 3.1 number Of rows "+ numberOfRows);
                            callback.callback( dataSet );
                        }
                        public void notFound() {
                            GWT.log("AbstractListPresenter.lookupDataset 3 NOTFOUND ");

                            callback.notFound();
                        }

                        @Override
                        public boolean onError(DataSetClientServiceError error) {
                            GWT.log("AbstractListPresenter.lookupDataset 4 error "+error.getMessage());
                            callback.onError(error);
                            return false;
                        }
                    }
            );
        } catch ( Exception e ) {
            GWT.log("AbstractListPresenter: lookuDataserError"+e.getMessage());

        }
    }

}
