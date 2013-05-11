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

package org.jbpm.console.ng.bd.model;

import java.io.Serializable;
import java.util.List;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class OrganizationSummary implements Serializable {
    private long id;
    private String name;
    private List<DomainSummary> domains;

    public OrganizationSummary() {
    }

    public OrganizationSummary(long id, String name, List<DomainSummary> domains) {
        this.id = id;
        this.name = name;
        this.domains = domains;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DomainSummary> getDomains() {
        return domains;
    }

    public void setDomains(List<DomainSummary> domains) {
        this.domains = domains;
    }

}
