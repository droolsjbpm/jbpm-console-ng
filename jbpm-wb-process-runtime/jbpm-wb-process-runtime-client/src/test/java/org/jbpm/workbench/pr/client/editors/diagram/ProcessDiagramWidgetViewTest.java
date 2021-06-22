/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLDivElement;
import org.gwtbootstrap3.client.ui.Anchor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.uberfire.client.views.pfly.widgets.D3;
import org.uberfire.client.views.pfly.widgets.D3.ZoomEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@RunWith(GwtMockitoTestRunner.class)
public class ProcessDiagramWidgetViewTest {

    @InjectMocks
    private ProcessDiagramWidgetViewImpl view;

    @Mock(name = "processDiagramDiv")
    private HTMLDivElement processDiagramDiv;

    @Mock
    private HTMLDivElement alertDiv;

    @Mock
    Anchor expandAnchor;

    @Mock
    private ZoomControlView zoomControlView;

    @Mock
    private D3 d3Mock;

    @Mock
    private D3.Selection svgSelect;

    @Mock
    private D3.Zoom zoomMock;

    @Mock
    private D3.Transform transformMock;

    double svgWidth = 200;
    double svgHeight = 100;

    @Before
    public void setup() {
        view.setD3Component(d3Mock);
        when(d3Mock.select(anyString())).thenReturn(svgSelect);
        when(d3Mock.zoom()).thenReturn(zoomMock);
        when(svgSelect.attr("viewBox")).thenReturn("0 0 " + svgWidth + " " + svgHeight);

        D3.Event event = mock( D3.Event.class, withSettings().extraInterfaces(ZoomEvent.class));
        ZoomEvent zoomEvent = (ZoomEvent) event; 
        when(d3Mock.getEvent()).thenReturn(event);
        when(zoomEvent.getTransform()).thenReturn(transformMock);
    }

    private void testD3ZoomInitialization() {
        String svgContent = "svgContent";

        view.displayImage(svgContent);
        assertNotNull(view.getProcessDiagramDivId());
        double[] scaleExtent = new double[2];
        scaleExtent[0] = 0.1;
        scaleExtent[1] = 3;

        verify(zoomMock).scaleExtent(eq(scaleExtent));
    }

    @Test
    public void testInit() {
        view.init();
        assertFalse(view.isDoubleClick);
    }

    @Test
    public void testZoomCall() {
        double k = 1.2;
        testD3ZoomInitialization();
        ArgumentCaptor<D3.CallbackFunction> captor = ArgumentCaptor.forClass(D3.CallbackFunction.class);
        verify(zoomMock).on(anyString(), captor.capture());
        when(transformMock.getK()).thenReturn(k);
        captor.getValue().execute();
        verify(transformMock).setX(((svgWidth * k) - svgWidth) / 2);
        verify(transformMock).setY(((svgHeight * k) - svgHeight) / 2);
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = 0;
        translateExtent[0][1] = 0;
        translateExtent[1][0] = svgWidth * k;
        translateExtent[1][1] = svgHeight * k;
        verify(zoomMock).translateExtent(eq(translateExtent));
        verify(zoomControlView).disableMinusButton(false);
        verify(zoomControlView).disablePlusButton(false);
    }

    @Test
    public void test100ZoomCalls() {
        testD3ZoomInitialization();
        ArgumentCaptor<D3.CallbackFunction> captor = ArgumentCaptor.forClass(D3.CallbackFunction.class);
        verify(zoomMock).on(anyString(), captor.capture());
        when(transformMock.getK()).thenReturn(1.0);
        captor.getValue().execute();

        verify(transformMock).setX(0);
        verify(transformMock).setY(0);
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = 0;
        translateExtent[0][1] = 0;
        translateExtent[1][0] = 200;
        translateExtent[1][1] = 100;
        verify(zoomMock).translateExtent(eq(translateExtent));
        verify(zoomControlView).disableMinusButton(false);
        verify(zoomControlView).disablePlusButton(false);
    }

    @Test
    public void test50ZoomCalls() {
        testD3ZoomInitialization();
        ArgumentCaptor<D3.CallbackFunction> captor = ArgumentCaptor.forClass(D3.CallbackFunction.class);
        verify(zoomMock).on(anyString(), captor.capture());
        when(transformMock.getK()).thenReturn(0.5);
        captor.getValue().execute();

        verify(transformMock).setX(-50);
        verify(transformMock).setY(-25);
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = 0;
        translateExtent[0][1] = 0;
        translateExtent[1][0] = 100;
        translateExtent[1][1] = 50;
        verify(zoomMock).translateExtent(eq(translateExtent));
        verify(zoomControlView).disableMinusButton(false);
        verify(zoomControlView).disablePlusButton(false);
    }

    @Test
    public void test10ZoomCalls() {
        testD3ZoomInitialization();
        ArgumentCaptor<D3.CallbackFunction> captor = ArgumentCaptor.forClass(D3.CallbackFunction.class);
        verify(zoomMock).on(anyString(), captor.capture());
        when(transformMock.getK()).thenReturn(0.1);
        captor.getValue().execute();

        verify(transformMock).setX(-90);
        verify(transformMock).setY(-45);
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = 0;
        translateExtent[0][1] = 0;
        translateExtent[1][0] = 20;
        translateExtent[1][1] = 10;
        verify(zoomMock).translateExtent(eq(translateExtent));
        verify(zoomControlView).disableMinusButton(true);
        verify(zoomControlView).disablePlusButton(false);
    }

    @Test
    public void test300ZoomCalls() {
        testD3ZoomInitialization();
        ArgumentCaptor<D3.CallbackFunction> captor = ArgumentCaptor.forClass(D3.CallbackFunction.class);
        verify(zoomMock).on(anyString(), captor.capture());
        when(transformMock.getK()).thenReturn(3.0);
        captor.getValue().execute();

        verify(transformMock).setX(200);
        verify(transformMock).setY(100);
        double[][] translateExtent = new double[2][2];
        translateExtent[0][0] = 0;
        translateExtent[0][1] = 0;
        translateExtent[1][0] = 600;
        translateExtent[1][1] = 300;
        verify(zoomMock).translateExtent(eq(translateExtent));
        verify(zoomControlView).disableMinusButton(false);
        verify(zoomControlView).disablePlusButton(true);
    }

}
