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
package org.jbpm.workbench.es.client.editors.quicknewjob;

import com.google.gwtmockito.GwtMock;
import com.google.gwtmockito.GwtMockitoTestRunner;
import com.google.gwtmockito.WithClassesToStub;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.client.ui.html.Text;
import org.jbpm.workbench.es.client.i18n.Constants;
import org.jbpm.workbench.es.model.RequestDataSetConstants;
import org.jbpm.workbench.es.model.RequestParameterSummary;
import org.jbpm.workbench.es.service.ExecutorService;
import org.jbpm.workbench.common.client.util.UTCDateBox;
import org.jbpm.workbench.common.client.util.UTCTimeBox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.uberfire.mocks.CallerMock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(GwtMockitoTestRunner.class)
@WithClassesToStub({Text.class})
public class QuickNewJobPopupTest {

    private String JOB_NAME = "JOB_NAME_1";
    private String JOB_TYPE = "JOB_TYPE_1";
    private int JOB_RETRIES = 5;

    @Mock
    private CallerMock<ExecutorService> executorServices;

    @Mock
    private ExecutorService executorServicesMock;

    @Mock
    private UTCTimeBox jobDueDateTime;

    @Mock
    private TextBox jobNameText;

    @Mock
    private FormGroup jobNameControlGroup;

    @Mock
    private HelpBlock jobNameHelpInline;

    @Mock
    @SuppressWarnings("unused")
    private IntegerBox jobRetriesNumber;

    @Mock
    private TextBox jobTypeText;

    @Mock
    private FormGroup jobTypeControlGroup;

    @GwtMock
    private HelpBlock jobTypeHelpInline;

    @Mock
    @SuppressWarnings("unused")
    private HelpBlock jobDueDateHelpBlock;

    @InjectMocks
    private QuickNewJobPopup quickNewJobPopup;

    @Before
    public void setupMocks() {
        executorServices = new CallerMock<ExecutorService>(executorServicesMock);
        quickNewJobPopup.setExecutorService(executorServices);
    }

    @Test
    public void jobNameIsPassedAsBusinessKeyTest() {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final HashMap<String, String> ctxValues = (HashMap) invocationOnMock.getArgument(2);
                assertTrue(ctxValues.get(RequestDataSetConstants.COLUMN_BUSINESSKEY).equals(JOB_NAME));
                assertTrue(ctxValues.get(RequestDataSetConstants.COLUMN_RETRIES).equals(String.valueOf(JOB_RETRIES)));
                assertTrue(invocationOnMock.getArguments()[2].equals(JOB_TYPE));
                return null;
            }
        }).when(executorServicesMock).scheduleRequest(nullable(String.class),
                                                      nullable(String.class),
                                                      nullable(Date.class),
                                                      nullable(Map.class));

        quickNewJobPopup.createJob(JOB_NAME,
                                   new Date(),
                                   JOB_TYPE,
                                   JOB_RETRIES,
                                   new ArrayList<RequestParameterSummary>());

        verify(executorServicesMock).scheduleRequest(nullable(String.class),
                                                     nullable(String.class),
                                                     nullable(Date.class),
                                                     nullable(HashMap.class));
    }

    @Test
    public void dueTimeSetToFutureTimeTest() {
        final Long nextHalfHour = UTCDateBox.date2utc(new Date(System.currentTimeMillis() + 1800 * 1000));
        final Long nextHour = UTCDateBox.date2utc(new Date(System.currentTimeMillis() + 3600 * 1000));
        // now is current time + 30'
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                final Long setTime = (Long) invocationOnMock.getArguments()[0];
                //assert that the jobDueDateTime is set later than 30' in the future and less than 1h
                assertTrue(setTime > nextHalfHour);
                assertTrue(setTime < nextHour);
                return null;
            }
        }).when(jobDueDateTime).setValue(anyLong());

        quickNewJobPopup.cleanForm();

        verify(jobDueDateTime).setValue(anyLong());
    }

    @Test
    public void emptyJobName_shouldCauseValidationError() {
        when(jobNameText.getText()).thenReturn(""); // Return empty string

        boolean isValid = quickNewJobPopup.validateForm();
        assertFalse("Form with an empty business key should be rejected",
                           isValid);

        verify(jobNameControlGroup).setValidationState(ValidationState.ERROR);
        verify(jobNameHelpInline).setText(Constants.INSTANCE.The_Job_Must_Have_A_BusinessKey());
    }

    @Test
    public void emptyType_shouldCauseValidationError() {
        when(jobTypeText.getText()).thenReturn("  "); //Return string with spaces

        boolean isValid = quickNewJobPopup.validateForm();
        assertFalse("Form with an empty type should be rejected",
                           isValid);

        verify(jobTypeControlGroup).setValidationState(ValidationState.ERROR);
        verify(jobTypeHelpInline).setText(Constants.INSTANCE.The_Job_Must_Have_A_Type());
    }

    @Test
    public void InvalidClass_shouldCauseValidationError() {
        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                throw (new Exception("Invalid command type"));
            }
        }).when(executorServicesMock).scheduleRequest(nullable(String.class),
                                                      nullable(String.class),
                                                      nullable(Date.class),
                                                      nullable(Map.class));

        quickNewJobPopup.createJob(JOB_NAME,
                                   new Date(),
                                   JOB_TYPE,
                                   JOB_RETRIES,
                                   new ArrayList());

        verify(quickNewJobPopup.jobTypeControlGroup).setValidationState(ValidationState.ERROR);
        verify(quickNewJobPopup.jobTypeHelpInline).setText(Constants.INSTANCE.The_Job_Must_Have_A_Valid_Type());
    }
}
