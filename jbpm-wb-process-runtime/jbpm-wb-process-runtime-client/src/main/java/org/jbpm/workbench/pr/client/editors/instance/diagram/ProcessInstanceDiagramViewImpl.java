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

package org.jbpm.workbench.pr.client.editors.instance.diagram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLParagraphElement;
import org.gwtbootstrap3.client.ui.Anchor;
import org.jboss.errai.common.client.dom.Event;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jbpm.workbench.pr.client.editors.diagram.ProcessDiagramWidgetViewImpl;
import org.jbpm.workbench.pr.client.resources.i18n.Constants;
import org.jbpm.workbench.pr.model.NodeInstanceSummary;
import org.jbpm.workbench.pr.model.ProcessInstanceSummary;
import org.jbpm.workbench.pr.model.ProcessNodeSummary;
import org.jbpm.workbench.pr.model.TimerInstanceSummary;
import org.uberfire.client.callbacks.Callback;
import org.uberfire.client.views.pfly.widgets.D3;
import org.uberfire.client.views.pfly.widgets.Select;
import com.google.gwt.user.client.Command;

@Dependent
@Templated(stylesheet = "ProcessInstanceDiagram.css")
public class ProcessInstanceDiagramViewImpl extends Composite implements ProcessInstanceDiagramView {

    @Inject
    @DataField("diagram")
    private ProcessDiagramWidgetViewImpl diagram;

    @Inject
    @DataField("available-nodes")
    private Select processNodes;

    @Inject
    @DataField("diagram-details")
    private HTMLDivElement diagramDetails;

    @Inject
    @DataField("node-details-panel")
    private ProcessNodeItemView processNodeSummaryView;

    @Inject
    @DataField("node-instances")
    private NodeInstancesView nodeInstancesView;

    @Inject
    @DataField("timer-instances")
    private TimerInstancesView timerInstancesView;

    @Inject
    @DataField("node-actions-panel")
    private HTMLDivElement nodeActionsPanel;

    @Inject
    @DataField("parent-sub-process")
    private HTMLDivElement parentAndSubProcessDiv;

    @Inject
    private NodeCounterView nodeCounterView;

    @Inject
    @DataField("sub-process-instances")
    private SubProcessInstancesView subProcessInstancesView;

    @Inject
    @DataField("parent-process-name")
    private Anchor parentProcessAnchor;

    @Inject
    @DataField("none-sub-process")
    private HTMLParagraphElement noneSubProcess;

    private Callback<String> onProcessNodeSelectedCallback;

    private Command parentSelectedCommand;

    private Map<String, Long> badges = new HashMap<>();

    private Constants constants = Constants.INSTANCE;

    private Command showOrHideParentAndSubProcessPanelCommand;

    private Command showOrHideNodeActionsCommand;

    @PostConstruct
    public void init(){
        nodeCounterView.setCallback(() -> showHideBadges());
        noneSubProcess.classList.add("hidden");
    }

    @Override
    public void setOnProcessNodeSelectedCallback(Callback<String> callback) {
        this.onProcessNodeSelectedCallback = callback;
    }

    @Override
    public void setOnDiagramNodeSelectionCallback(Callback<String> callback) {
        diagram.setOnDiagramNodeSelectionCallback(callback);
    }

    @Override
    public void setOnDiagramNodeSelectionDoubleClick(Callback<String> callback) {
        diagram.setOnNodeSelectionDoubleClick(callback);
    }

    @Override
    public void setParentSelectedCommand(Command parentSelectedCommand) {
        this.parentSelectedCommand = parentSelectedCommand;
    }

    @Override
    public void setProcessNodes(final List<ProcessNodeSummary> nodes) {
        processNodes.removeAllOptions();
        nodes.forEach(node -> processNodes.addOption(node.getLabel(), node.getUniqueId()));
        processNodes.refresh();
    }

    @Override
    public ProcessNodeSummary getValue() {
        return processNodeSummaryView.getValue();
    }

    @Override
    public void setValue(final ProcessNodeSummary node) {
        processNodeSummaryView.setValue(node);
        if (node.getId() == null) {
            processNodeSummaryView.getElement().classList.add("hidden");
            processNodes.setValue("");
        } else {
            processNodeSummaryView.getElement().classList.remove("hidden");
            processNodes.setValue(node.getUniqueId());
        }
    }

    @Override
    public void setNodeInstances(final List<NodeInstanceSummary> nodes) {
        if(nodes.isEmpty()){
            nodeInstancesView.getElement().classList.add("hidden");
        } else {
            nodeInstancesView.getElement().classList.remove("hidden");
        }
        nodeInstancesView.setValue(nodes);
    }

    @Override
    public void showParentAndSubProcessPanel() {
        parentAndSubProcessDiv.classList.remove("hidden");
    }

    @Override
    public void hideParentAndSubProcessPanel() {
        parentAndSubProcessDiv.classList.add("hidden");
    }

    @Override
    public void setShowOrHideParentAndSubProcessPanelCommand(Command command) {
        this.showOrHideParentAndSubProcessPanelCommand = command;
    }

    @Override
    public void showOrHideParentAndSubProcessPanelTriggered() {
        if (showOrHideParentAndSubProcessPanelCommand != null) {
            showOrHideParentAndSubProcessPanelCommand.execute();
        }
    }

    @Override
    public void setSubProcessInstances(final List<ProcessInstanceSummary> subProcessInstances) {
        if (subProcessInstances.isEmpty()) {
            subProcessInstancesView.getElement().classList.add("hidden");
            noneSubProcess.classList.remove("hidden");
            noneSubProcess.textContent = constants.None();
        } else {
            subProcessInstancesView.getElement().classList.remove("hidden");
            subProcessInstancesView.setValue(subProcessInstances);
            noneSubProcess.classList.add("hidden");
        }
    }

    @Override
    public void setParentProcessInstance(final ProcessInstanceSummary parentProcessInstance) {
        if (parentProcessInstance == null) {
            parentProcessAnchor.setText(constants.None());
            parentProcessAnchor.setEnabled(false);
        } else {
            parentProcessAnchor.setText(constants.ProcessInstanceIdAndName(parentProcessInstance.getProcessInstanceId().toString(), parentProcessInstance.getProcessName()));
            parentProcessAnchor.setEnabled(true);
        }
    }

    @Override
    public void setTimerInstances(final List<TimerInstanceSummary> timers) {
        if(timers.isEmpty()){
            timerInstancesView.getElement().classList.add("hidden");
        } else {
            timerInstancesView.getElement().classList.remove("hidden");
        }
        timerInstancesView.setValue(timers);
    }

    @EventHandler("available-nodes")
    public void onProcessNodeChange(@ForEvent("change") Event e) {
        processNodes.toggle();
        if (onProcessNodeSelectedCallback != null) {
            final String node = processNodes.getValue();
            onProcessNodeSelectedCallback.callback(node == null || node.trim().isEmpty() ? null : node);
        }
    }

    @Override
    public void showNodeActions() {
        nodeActionsPanel.classList.remove("hidden");
    }

    @Override
    public void hideNodeActions() {
        nodeActionsPanel.classList.add("hidden");
    }

    @Override
    public void setShowOrHideNodeActionsCommand(final Command command) {
        this.showOrHideNodeActionsCommand = command;
    }

    @Override
    public void showOrHideNodeActionsTriggered() {
        if (showOrHideNodeActionsCommand != null) {
            showOrHideNodeActionsCommand.execute();
        }
    }

    @Override
    public void expandDiagram() {
        diagramDetails.classList.add("hidden");
        diagram.expandDiagramContainer();
    }

    @Override
    public void displayImage(final String svgContent) {
        diagram.displayImage(svgContent);
        diagram.getElement().appendChild(nodeCounterView.getElement());
    }

    @Override
    public void setNodeBadges(final Map<String, Long> badges) {
        this.badges = badges;
    }

    @Override
    public void onShow() {
        renderBadges();
    }

    protected void showHideBadges(){
        final D3 nodes = getSelectionOfNodeBadge();
        nodes.attr("visibility", nodeCounterView.showBadges() ? "visible" : "hidden");
    }

    private boolean isRenderBadges() {
        final D3.Selection nodes = getSelectionOfNodeBadge();
        if (nodes.empty()) {
            return true;
        }
        return false;
    }

    private D3.Selection getSelectionOfNodeBadge() {
        final D3 d3 = D3.Builder.get();
        final String processDiagramDivId = diagram.getProcessDiagramDivId();
        return d3.selectAll("#" + processDiagramDivId + " svg [jbpm-node-badge]");
    }

    protected void renderBadges() {
        if(!isRenderBadges()){
            return;
        }

        final D3 d3 = D3.Builder.get();
        final String processDiagramDivId = diagram.getProcessDiagramDivId();
        final D3.Selection svg = d3.select("#" + processDiagramDivId + " svg");

        if(svg.empty()){
            //Couldn't find SVG element
            return;
        }
        
        final D3.DOMRect svgRect = svg.node().getBoundingClientRect();
        if(svgRect.getWidth() == 0 && svgRect.getHeight() == 0){
            //SVG not visible
            return;
        }

        final boolean isOryx = svg.attr(":xmlns:oryx") != null;
        badges.forEach((nodeId, count) -> {
            final String path = "#" + processDiagramDivId + " svg [bpmn2nodeid='" + nodeId + "'] " + (isOryx ? ".stencils" : "");
            final D3 node = d3.select(path);
            if (((D3.Selection) node).empty()){
                return;
            }
            
            D3.DOMRect bb = node.node().getBoundingClientRect();
            final D3 group = node.append("g")
                    .attr("transform", "translate( " + (bb.getWidth() / 2 - 12.5) + ", " + (bb.getHeight() + 2) + ")")
                    .attr("jbpm-node-badge", nodeId);

            group.append("rect")
                    .attr("x", "0")
                    .attr("y", "0")
                    .attr("width", "25")
                    .attr("height", "20")
                    .attr("rx", "5")
                    .attr("ry", "5")
                    .attr("fill", "grey")
                    .attr("opacity", "0.5");
            group.append("text")
                    .attr("font-size", "10pt")
                    .attr("font-weight", "normal")
                    .attr("font-family", "Open Sans")
                    .attr("font-style", "normal")
                    .attr("text-anchor", "middle")
                    .attr("fill", "white")
                    .attr("x", "12")
                    .attr("y", "15")
                    .text(String.valueOf(count));
        });

        showHideBadges();
    }

    @Override
    public void displayMessage(final String message) {
        diagram.displayMessage(message);
    }

    @Override
    public void showBusyIndicator(final String message) {
        diagram.showBusyIndicator(message);
    }

    @Override
    public void hideBusyIndicator() {
        diagram.hideBusyIndicator();
    }

    @EventHandler("parent-process-name")
    protected void onClickParentInstanceName(final ClickEvent event) {
        if (parentSelectedCommand != null) {
            parentSelectedCommand.execute();
        }
    }

    public void disableExpandAnchor() {
        diagram.disableExpandAnchor();
    }
}
