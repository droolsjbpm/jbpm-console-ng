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

package org.jbpm.workbench.pr.client.editors.instance.list;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import java.util.function.Predicate;

import org.jbpm.workbench.common.client.util.ButtonActionCell;
import org.jbpm.workbench.pr.model.ProcessInstanceSummary;
import org.kie.api.runtime.process.ProcessInstance;

public class ProcessInstanceSummaryActionCell extends ButtonActionCell<ProcessInstanceSummary> {
    
    //Predefined helper predicates
    public static final Predicate<ProcessInstanceSummary> PREDICATE_STATE_ACTIVE =
            value -> (value.getState() == ProcessInstance.STATE_ACTIVE);

    public static final Predicate<ProcessInstanceSummary> PREDICATE_ERRORS_PRESENT =
            value -> (value.getErrorCount() > 0);
            
    public static final Predicate<ProcessInstanceSummary> PREDICATE_TRUE =
            value -> true;
    
    private final Predicate<ProcessInstanceSummary> showCondition;

    public ProcessInstanceSummaryActionCell(
            final String text,
            final Predicate<ProcessInstanceSummary> showConditionPredicate,
            final ActionCell.Delegate<ProcessInstanceSummary> delegate
    ) {
        super(text, delegate);
        this.showCondition = showConditionPredicate;
    }

    @Override
    public void render(Cell.Context context, ProcessInstanceSummary value, SafeHtmlBuilder sb) {
        if (showCondition.test(value)) {
            super.render(context, value, sb);
        }
    }

}
