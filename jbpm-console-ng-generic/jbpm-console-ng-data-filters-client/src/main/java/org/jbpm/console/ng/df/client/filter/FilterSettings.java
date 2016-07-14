/*
 * Copyright 2015 JBoss by Red Hat.
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
package org.jbpm.console.ng.df.client.filter;

import org.dashbuilder.dataset.DataSet;
import org.dashbuilder.displayer.ColumnSettings;
import org.dashbuilder.displayer.DisplayerSettings;
import org.dashbuilder.displayer.DisplayerType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Custom settings class holding the configuration of any jBPM table displayer
 */
public class FilterSettings extends DisplayerSettings {

    protected String key;

    protected String tableName;
    protected String tableDescription;
    protected boolean editable;

    protected String serverTemplateId;

    public FilterSettings() {
        super( DisplayerType.TABLE );
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName( String tableName ) {
        this.tableName = tableName;
    }

    public String getTableDescription() {
        return tableDescription;
    }

    public void setTableDescription( String tableDescription ) {
        this.tableDescription = tableDescription;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable( boolean editable ) {
        this.editable = editable;
    }

    public String getKey() {
        return key;
    }

    public void setKey( String key ) {
        this.key = key;
    }

    public String getServerTemplateId() {
        return serverTemplateId;
    }

    public void setServerTemplateId(String serverTemplateId) {
        this.serverTemplateId = serverTemplateId;
    }

    public boolean equals( Object obj ) {
        try {
            FilterSettings other = ( FilterSettings ) obj;
            if ( tableName == null || other.tableName == null ) return false;
            if ( !tableName.equals( other.tableName ) ) return false;
            return true;
        } catch ( ClassCastException e ) {
            return false;
        }
    }


    public static FilterSettings cloneFrom( DisplayerSettings settings ) {
        FilterSettings tableSettings = new FilterSettings();
        tableSettings.setType( DisplayerType.TABLE );
        tableSettings.setUUID( settings.getUUID() );
        tableSettings.setDataSet( settings.getDataSet() );
        tableSettings.setDataSetLookup( settings.getDataSetLookup() );
        tableSettings.setColumnSettingsList( settings.getColumnSettingsList() );
        tableSettings.getSettingsFlatMap().putAll( settings.getSettingsFlatMap() );
        return tableSettings;
    }

    public FilterSettings cloneInstance() {
        FilterSettings clone = new FilterSettings();
        clone.UUID = UUID;
        clone.tableName = tableName;
        clone.tableDescription = tableDescription;
        clone.settings = new HashMap<String, String>( settings );
        clone.columnSettingsList = new ArrayList<ColumnSettings>();

        for ( ColumnSettings columnSettings : columnSettingsList ) {
            clone.columnSettingsList.add( columnSettings.cloneInstance() );
        }
        if ( dataSet != null ) clone.dataSet = dataSet.cloneInstance();
        if ( dataSetLookup != null ) clone.dataSetLookup = dataSetLookup.cloneInstance();

        return clone;
    }

    public DataSet getDataSet() {
        return super.getDataSet();
    }
}
