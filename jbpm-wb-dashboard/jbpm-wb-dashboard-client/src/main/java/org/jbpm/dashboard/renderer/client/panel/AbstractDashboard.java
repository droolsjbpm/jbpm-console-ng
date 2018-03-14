/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.dashboard.renderer.client.panel;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.dashbuilder.dataset.DataSetLookup;
import org.dashbuilder.dataset.client.DataSetClientServices;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.client.AbstractDisplayer;
import org.dashbuilder.displayer.client.DataSetHandlerImpl;
import org.dashbuilder.displayer.client.DisplayerCoordinator;
import org.dashbuilder.displayer.client.DisplayerLocator;
import org.dashbuilder.renderer.client.metric.MetricDisplayer;
import org.dashbuilder.renderer.client.table.TableDisplayer;
import org.jbpm.workbench.common.client.PerspectiveIds;
import org.jbpm.workbench.ks.integration.ConsoleDataSetLookup;
import org.jbpm.workbench.common.client.menu.ServerTemplateSelectorMenuBuilder;
import org.jbpm.dashboard.renderer.client.panel.formatter.DurationFormatter;
import org.jbpm.dashboard.renderer.client.panel.i18n.DashboardI18n;
import org.jbpm.dashboard.renderer.client.panel.widgets.ProcessBreadCrumb;
import org.uberfire.client.mvp.PerspectiveManager;
import org.uberfire.client.mvp.PlaceManager;
import org.uberfire.client.workbench.events.ClosePlaceEvent;
import org.uberfire.ext.widgets.common.client.breadcrumbs.UberfireBreadcrumbs;
import org.uberfire.mvp.Command;
import org.uberfire.mvp.Commands;

import static org.kie.soup.commons.validation.PortablePreconditions.*;

public abstract class AbstractDashboard {

    protected DataSetClientServices dataSetClientServices;
    protected PlaceManager placeManager;
    protected DashboardI18n i18n;

    protected MetricDisplayer selectedMetric = null;
    protected String selectedProcess = null;
    protected ProcessBreadCrumb processBreadCrumb;
    protected DisplayerLocator displayerLocator;
    protected DisplayerCoordinator displayerCoordinator;

    protected ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder;

    UberfireBreadcrumbs breadcrumbs;

    private PerspectiveManager perspectiveManager;

    private String detailScreenId;

    public String getPerspectiveId() {
        return perspectiveManager.getCurrentPerspective().getIdentifier();
    }

    public AbstractDashboard() {
    }

    public AbstractDashboard(final DataSetClientServices dataSetClientServices,
                             final PlaceManager placeManager,
                             final DashboardI18n i18n,
                             final ProcessBreadCrumb processBreadCrumb,
                             final DisplayerLocator displayerLocator,
                             final DisplayerCoordinator displayerCoordinator,
                             final ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder) {
        this.dataSetClientServices = dataSetClientServices;
        this.placeManager = placeManager;
        this.i18n = i18n;
        this.processBreadCrumb = processBreadCrumb;
        this.displayerLocator = displayerLocator;
        this.displayerCoordinator = displayerCoordinator;
        this.serverTemplateSelectorMenuBuilder = serverTemplateSelectorMenuBuilder;
    }

    @PostConstruct
    public void setBreadcrumbs() {
        createListBreadcrumb();
        breadcrumbs.addToolbar(getPerspectiveId(),
                               serverTemplateSelectorMenuBuilder.getView().getElement());
    }

    @Inject
    public void setServerTemplateSelectorMenuBuilder(final ServerTemplateSelectorMenuBuilder serverTemplateSelectorMenuBuilder) {
        this.serverTemplateSelectorMenuBuilder = serverTemplateSelectorMenuBuilder;
    }

    public abstract void createListBreadcrumb();

    public abstract void tableRedraw();

    public void setupListBreadcrumb(String listLabel) {
        breadcrumbs.clearBreadcrumbs(getPerspectiveId());

        breadcrumbs.addBreadCrumb(getPerspectiveId(),
                                  i18n.Home(),
                                  () -> placeManager.goTo(PerspectiveIds.HOME));
        breadcrumbs.addBreadCrumb(getPerspectiveId(),
                                  listLabel,
                                  Commands.DO_NOTHING);
    }

    public void setupDetailBreadcrumb(String listLabel,
                                      String detailLabel,
                                      String detailScreenId) {
        breadcrumbs.clearBreadcrumbs(getPerspectiveId());
        breadcrumbs.addBreadCrumb(getPerspectiveId(),
                                  i18n.Home(),
                                  () -> placeManager.goTo(PerspectiveIds.HOME));
        breadcrumbs.addBreadCrumb(getPerspectiveId(),
                                  listLabel,
                                  () -> closeDetails(detailScreenId));
        breadcrumbs.addBreadCrumb(getPerspectiveId(),
                                  detailLabel,
                                  Commands.DO_NOTHING);
        this.detailScreenId = detailScreenId;
    }

    private void closeDetails(String detailScreenId) {
        placeManager.closePlace(detailScreenId);
        createListBreadcrumb();
        tableRedraw();
    }

    public void onDetailScreenClosed(@Observes ClosePlaceEvent closed) {
        if (closed.getPlace() != null
                && detailScreenId != null
                && detailScreenId.equals(closed.getPlace().getIdentifier())) {
            createListBreadcrumb();
            tableRedraw();
        }
    }

    public MetricDisplayer createMetricDisplayer(DisplayerSettings settings) {
        checkNotNull("displayerSettings",
                     settings);
        MetricDisplayer metricDisplayer = (MetricDisplayer) displayerLocator.lookupDisplayer(settings);
        metricDisplayer.setDisplayerSettings(settings);
        metricDisplayer.setDataSetHandler(new DataSetHandlerImpl(dataSetClientServices,
                                                                 getDataSetLookup(settings)));
        return metricDisplayer;
    }

    private DataSetLookup getDataSetLookup(final DisplayerSettings settings) {
        return ConsoleDataSetLookup.fromInstance(settings.getDataSetLookup(),
                                                 serverTemplateSelectorMenuBuilder.getSelectedServerTemplate());
    }

    public TableDisplayer createTableDisplayer(DisplayerSettings settings,
                                               final String columnId,
                                               final DurationFormatter durationFormatter) {
        checkNotNull("displayerSettings",
                     settings);
        final TableDisplayer tableDisplayer = (TableDisplayer) displayerLocator.lookupDisplayer(settings);
        tableDisplayer.setDisplayerSettings(settings);
        tableDisplayer.setDataSetHandler(new DataSetHandlerImpl(dataSetClientServices,
                                                                getDataSetLookup(settings)));
        tableDisplayer.addFormatter(columnId,
                                    durationFormatter);
        tableDisplayer.addOnCellSelectedCommand(new Command() {
            public void execute() {
                tableCellSelected(tableDisplayer.getSelectedCellColumn(),
                                  tableDisplayer.getSelectedCellRow());
            }
        });
        return tableDisplayer;
    }

    public MetricDisplayer getSelectedMetric() {
        return selectedMetric;
    }

    public abstract View getView();

    public void resetCurrentMetric() {
        selectedMetric = null;
        updateHeaderText();
    }

    public abstract void tableCellSelected(String columnId,
                                           int rowIndex);

    public void changeCurrentMetric(MetricDisplayer metric) {
        if (metric.isFilterOn()) {

            // Reset existing metric selected since only a single metric can be filtered at the same time
            if (selectedMetric != null && selectedMetric != metric) {
                MetricDisplayer metricToReset = selectedMetric;
                metricToReset.filterReset();
                metricToReset.redraw();
            }
            // Set the selected metric as active
            selectedMetric = metric;

            // Update the header text
            updateHeaderText();
        } else {
            selectedMetric = null;
            updateHeaderText();
        }
    }

    public abstract void updateHeaderText();

    public Widget asWidget() {
        return getView().asWidget();
    }

    public abstract void resetProcessBreadcrumb();

    public ProcessBreadCrumb getProcessBreadCrumb() {
        return processBreadCrumb;
    }

    public String getSelectedProcess() {
        return selectedProcess;
    }

    public AbstractDisplayer createDisplayer(DisplayerSettings settings) {
        checkNotNull("displayerSettings",
                     settings);
        AbstractDisplayer displayer = (AbstractDisplayer) displayerLocator.lookupDisplayer(settings);
        displayer.setDataSetHandler(new DataSetHandlerImpl(dataSetClientServices,
                                                           getDataSetLookup(settings)));
        return displayer;
    }

    public void changeCurrentProcess(String name) {
        selectedProcess = name;
        updateHeaderText();
        getView().showBreadCrumb(name);
    }

    public void resetCurrentProcess() {
        selectedProcess = null;
        updateHeaderText();
        getView().hideBreadCrumb();
    }

    @Inject
    public void setPerspectiveManager(PerspectiveManager perspectiveManager) {
        this.perspectiveManager = perspectiveManager;
    }

    @Inject
    public void setUberfireBreadcrumbs(UberfireBreadcrumbs uberfireBreadcrumbs) {
        this.breadcrumbs = uberfireBreadcrumbs;
    }

    public interface View extends IsWidget {

        void showBreadCrumb(String processName);

        void hideBreadCrumb();

        void setHeaderText(String text);

        void showLoading();

        void hideLoading();

        void showDashboard();

        void showInstances();

        DashboardI18n getI18nService();

        boolean isDashboardPanelVisible();
    }
}
