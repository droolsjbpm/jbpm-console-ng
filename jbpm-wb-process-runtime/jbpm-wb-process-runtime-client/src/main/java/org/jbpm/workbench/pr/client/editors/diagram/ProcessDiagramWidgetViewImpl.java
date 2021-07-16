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

package org.jbpm.workbench.pr.client.editors.diagram;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.pr.events.ProcessDiagramExpandEvent;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.views.pfly.widgets.D3;
import org.uberfire.ext.widgets.common.client.common.BusyPopup;

@Dependent
@Templated
public class ProcessDiagramWidgetViewImpl extends Composite implements ProcessDiagramWidgetView,
                                                                       RequiresResize {

    @Inject
    @DataField("processDiagramDiv")
    private HTMLDivElement processDiagramDiv;

    @Inject
    @DataField("diagramContainerDiv")
    private HTMLDivElement diagramContainerDiv;

    @Inject
    @DataField("message")
    @Named("span")
    private HTMLElement heading;

    @Inject
    @DataField
    private HTMLDivElement alert;

    @Inject
    @DataField("expand-diagram")
    private Anchor expandAnchor;

    @Inject
    private ZoomControlView zoomControlView;

    @Inject
    private Event<ProcessDiagramExpandEvent> processDiagramExpandEvent;

    private Callback<String> nodeSelectionCallback;

    private Callback<String> nodeSelectionDoubleClick;

    private D3 d3;

    private boolean isExpand = false;

    protected boolean isDoubleClick;

    public void setD3Component(D3 d3) {
        this.d3 = d3;
    }

    @PostConstruct
    public void init() {
        d3 = D3.Builder.get();
        expandAnchor.setIcon(IconType.EXPAND);
        this.isDoubleClick = false;

        processDiagramDiv.id = DOM.createUniqueId();
    }

    @Override
    public void setOnDiagramNodeSelectionCallback(final Callback<String> callback) {
        this.nodeSelectionCallback = callback;
    }

    @Override
    public void setOnNodeSelectionDoubleClick(final Callback<String> callback) {
        this.nodeSelectionDoubleClick = callback;
    }

    @Override
    public void displayImage(final String svgContent) {
        processDiagramDiv.innerHTML = svgContent;

        final D3 svg = d3.select("#" + processDiagramDiv.id + " svg");

        String[] viewBoxValues = svg.attr("viewBox").toString().split(" ");

        double svgWidth = Double.parseDouble(viewBoxValues[2]);
        double svgHeight = Double.parseDouble(viewBoxValues[3]);
        svg.attr("width", svgWidth);
        svg.attr("height", svgHeight);
        final D3.Zoom zoom = d3.zoom();

        double[] scaleExtent = new double[2];
        scaleExtent[0] = 0.1;
        scaleExtent[1] = 3;
        zoom.scaleExtent(scaleExtent);

        D3.CallbackFunction callback = () -> {
            D3.ZoomEvent event = d3.getEvent();
            double k = event.getTransform().getK();
            event.getTransform().setX(((svgWidth * k) - svgWidth) / 2);
            event.getTransform().setY(((svgHeight * k) - svgHeight) / 2);
            refreshExtent(zoom, 0, 0, svgWidth * k, svgHeight * k);
            svg.attr("transform",
                     event.getTransform());
            zoomControlView.disablePlusButton(k >= 3);
            zoomControlView.disableMinusButton(k <= 0.1);

            double zoomTxt = Math.round(100 + (event.getTransform().getK() - 1) * 100);
            zoomControlView.setZoomText(zoomTxt + "%");
        };

        svg.call(zoom.on("zoom",
                         callback));

        zoomControlView.setScaleTo100Command(() -> zoom.transform(svg.transition().duration(500), d3.getZoomIdentity()));

        zoomControlView.setScaleTo300Command(() -> zoom.scaleTo(svg.transition().duration(500), 3.0));

        zoomControlView.setScaleTo150Command(() -> zoom.scaleTo(svg.transition().duration(500), 1.5));

        zoomControlView.setScaleTo50Command(() -> zoom.scaleTo(svg.transition().duration(500), 0.5));

        zoomControlView.setScaleMinusCommand(() -> zoom.scaleBy(svg.transition().duration(200), 0.95));

        zoomControlView.setScalePlusCommand(() -> zoom.scaleBy(svg.transition().duration(200), 1.05));

        processDiagramDiv.appendChild(zoomControlView.getElement());

        if (nodeSelectionCallback == null) {
            return;
        }

        final D3.Selection selectAll = select();
        d3.select("svg").on("dblclick.zoom", () -> {
        });
        selectAll.on("mouseenter", () -> {
            Object target = D3.Builder.get().getEvent().getCurrentTarget();
            D3 node = d3.select(target);
            node.style("cursor", "pointer");
            node.attr("opacity", 0.7);
        });
        selectAll.on("mouseleave", () -> {
            Object target = D3.Builder.get().getEvent().getCurrentTarget();
            D3 node = d3.select(target);
            node.style("cursor", "default");
            node.attr("opacity", 1);
        });
        DoubleClickTimer timer = new DoubleClickTimer();
        selectAll.on("click", () -> {
            isDoubleClick = false;
            Object target = D3.Builder.get().getEvent().getCurrentTarget();
            D3 node = d3.select(target);
            timer.setTarget(node);
            timer.schedule(50);
        });
        selectAll.on("dblclick", () -> {
            isDoubleClick = true;
            timer.cancel();
            Object target = D3.Builder.get().getEvent().getCurrentTarget();
            D3 node = d3.select(target);
            nodeSelectionDoubleClick.callback((String) node.attr("bpmn2nodeid"));
        });
    }

    protected native D3.Selection select() /*-{
        return $wnd.d3.selectAll("[bpmn2nodeid]");
    }-*/;

    private void refreshExtent(D3.Zoom zoom, double minX, double minY, double maxX, double maxY) {
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = minX;
        translateExtent[0][1] = minY;
        translateExtent[1][0] = maxX;
        translateExtent[1][1] = maxY;

        zoom.translateExtent(translateExtent);
    }
    @Override
    public void displayMessage(final String message) {
        alert.classList.remove("hidden");
        heading.textContent = message;
    }

    @Override
    public void onResize() {
        int height = getParent().getOffsetHeight();
        int width = getParent().getOffsetWidth();

        setPixelSize(width,
                     height);
    }

    @Override
    public void showBusyIndicator(final String message) {
        BusyPopup.showMessage(message);
    }

    @Override
    public void hideBusyIndicator() {
        BusyPopup.close();
    }

    @Override
    public void expandDiagramContainer() {
        diagramContainerDiv.classList.remove("col-md-10");
        diagramContainerDiv.classList.add("col-md-12");
        isExpand = true;
    }

    @EventHandler("expand-diagram")
    protected void onClickExpandDiagram(final ClickEvent event) {
        if (isExpand) {
            diagramContainerDiv.classList.add("col-md-10");
            diagramContainerDiv.classList.remove("col-md-12");
            isExpand = false;
            expandAnchor.setIcon(IconType.EXPAND);
        } else {
            expandDiagramContainer();
            isExpand = true;
            expandAnchor.setIcon(IconType.COMPRESS);
        }
        processDiagramExpandEvent.fire(new ProcessDiagramExpandEvent(isExpand));
    }

    @Override
    public void disableExpandAnchor() {
        expandAnchor.setEnabled(false);
    }

    private class DoubleClickTimer extends Timer {

        public D3 node;

        public void setTarget(D3 node) {
            this.node = node;
        }

        @Override
        public void run() {
            if(!isDoubleClick) {
                nodeSelectionCallback.callback((String) node.attr("bpmn2nodeid"));
            }
        }
    }

    public String getProcessDiagramDivId() {
        return processDiagramDiv.id;
    }
}
