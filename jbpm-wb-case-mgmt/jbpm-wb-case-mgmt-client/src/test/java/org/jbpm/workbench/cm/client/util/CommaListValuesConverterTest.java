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

package org.jbpm.workbench.cm.client.util;

import java.util.List;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.*;

public class CommaListValuesConverterTest {

    @Test
    public void testParsingStringToList() {
        assertToModelValue(" ",
                           0);
        assertToModelValue("test",
                           1,
                           "test");
        assertToModelValue("test1, test2",
                           2,
                           "test1",
                           "test2");
        assertToModelValue(" test1, test2, ",
                           2,
                           "test1",
                           "test2");
    }

    @Test
    public void testParsingListToString() {
        assertToWidgetValue(emptyList(),
                            "");
        assertToWidgetValue(asList("test1",
                                   "test2"),
                            "test1, test2");
    }

    private void assertToModelValue(String value,
                                    int size,
                                    String... values) {
        final List modelValue = new CommaListValuesConverter().toModelValue(value);
        assertEquals(size,
                     modelValue.size());
        for (String expectedValue : values) {
            assertTrue(modelValue.contains(expectedValue));
        }
    }

    private void assertToWidgetValue(List<String> values,
                                     String value) {
        final String widgetValue = new CommaListValuesConverter().toWidgetValue(values);
        assertEquals(value,
                     widgetValue);
    }
}