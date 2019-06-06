package com.lululemon.flow.data.transfer.api;

import com.google.common.base.Throwables;
import com.lululemon.flow.data.transfer.service.JobService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class JobControllerTest {

    @Mock
    private JobService jobService;

    @Spy
    @InjectMocks
    private JobController jobController;

    @Before
    public void before() {
        reset(jobController, jobService);
    }

    @Test
    public void testTransform() {

        // Setup
        final JobRequest.Transform transform = new JobRequest.Transform();
        transform.setSqlPath("sqlPath");
        final JobExecution jobExecution = mock(JobExecution.class);
        final ExitStatus exitStatus = mock(ExitStatus.class);
        when(jobExecution.getExitStatus()).thenReturn(exitStatus);
        when(exitStatus.getExitCode()).thenReturn("code");
        when(jobExecution.getStatus()).thenReturn(BatchStatus.ABANDONED);
        when(jobService.runJob(transform)).thenReturn(jobExecution);

        final JobResponse expectedResponse = JobResponse.builder()
                .jobId(jobExecution.getJobId())
                .executionId(jobExecution.getId())
                .code(jobExecution.getExitStatus().getExitCode())
                .status(jobExecution.getStatus().toString())
                .message(jobExecution.getExitStatus().getExitDescription())
                .build();

        // Execute
        final JobResponse response = jobController.transform(transform);

        // Verify
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testFailJobResponse() {

        // Setup
        final Exception exception = new Exception();
        final JobResponse expectedResponse = JobResponse.builder()
                .code("FAILED")
                .status("FAILED")
                .message(Throwables.getStackTraceAsString(exception))
                .build();

        // Execute
        final JobResponse response = jobController.buildJobResponse(exception);

        // Verify
        assertEquals(expectedResponse, response);
    }

    @Test
    public void testTransformRequest() throws IOException {

        // Setup
        JobRequest.Transform transform = new JobRequest.Transform();
        transform.setSqlPath("sqlPath");

        // Execute
        String json = JobController.requestAsJson(transform);
        JobRequest.Transform result = JobController.OBJECT_MAPPER.readValue(json, JobRequest.Transform.class);

        // Verify
        assertEquals(result, result);
    }
}
