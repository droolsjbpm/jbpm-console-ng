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

package org.jbpm.workbench.common.client.list;

import com.google.gwt.dom.client.Style;
import org.jboss.errai.ioc.client.container.IOC;
import org.jbpm.workbench.common.model.GenericSummary;
import org.uberfire.ext.services.shared.preferences.GridGlobalPreferences;

public class AdvancedSearchTable<T extends GenericSummary> extends ExtendedPagedTable<T> {

    private AdvancedSearchFiltersViewImpl advancedSearchFiltersView;

    public AdvancedSearchTable(final GridGlobalPreferences gridPreferences) {
        super(gridPreferences);
        advancedSearchFiltersView = IOC.getBeanManager().lookupBean(AdvancedSearchFiltersViewImpl.class).getInstance();
        topToolbar.add(advancedSearchFiltersView);
        this.getElement().getStyle().setPaddingTop(0,
                                                   Style.Unit.PX);
    }

    public AdvancedSearchFiltersViewImpl getAdvancedSearchFiltersView() {
        return advancedSearchFiltersView;
    }
}
