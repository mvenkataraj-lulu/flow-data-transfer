package com.lululemon.flow.data.transfer.service;

import com.lululemon.flow.data.transfer.api.JobRequest;
import com.lululemon.flow.data.transfer.job.AbstractJob;
import com.lululemon.flow.data.transfer.job.impl.SqlJob;
import com.lululemon.flow.data.transfer.params.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.context.ApplicationContext;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Spy
    @InjectMocks
    private JobService jobService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private JobBuilderFactory jobBuilderFactory;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StepBuilderFactory stepBuilderFactory;

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private ApplicationContext ctx;


    @Before
    public void before() {
        reset(jobService, jobExplorer, jobLauncher, jobBuilderFactory, stepBuilderFactory);
    }

    @Test
    public void getExecutionTest() {
        JobExecution expected = new JobExecution(1L);
        expected.setExitStatus(ExitStatus.COMPLETED);
        expected.setStatus(BatchStatus.COMPLETED);
        expected.setEndTime(new Date());
        when(jobExplorer.getJobExecution(any(Long.class))).thenReturn(expected);

        assertEquals(expected, jobService.getJobExecution(1L));
        verify(jobExplorer, Mockito.atLeastOnce()).getJobExecution(any(Long.class));
    }

    @Test
    public void transformTest() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        JobExecution expected = new JobExecution(1L);
        expected.setExitStatus(ExitStatus.COMPLETED);
        expected.setStatus(BatchStatus.COMPLETED);
        expected.setEndTime(new Date());
        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(expected);

        when(ctx.getBean(any(Class.class), any(JobRequest.class))).thenReturn(new SqlJob("transform"));

        JobRequest.Transform request = new JobRequest.Transform();

        JobExecution actual = jobService.runJob(request);
        assertEquals(expected, actual);
        verify(jobLauncher, atLeastOnce()).run(any(Job.class), any(JobParameters.class));
        verify(stepBuilderFactory, times(1)).get(eq("step"));
        verify(jobBuilderFactory, times(1)).get(eq("transform"));

    }

    @Test
    public void testRunStep() {

        // Setup
        final String jobName = "jobName";
        final AbstractJob job = mock(AbstractJob.class);
        final JobExecution jobExecution = new JobExecution(11L);
        final StepExecution stepExecution = new StepExecution("stepName", jobExecution);
        final StepContext stepContext = new StepContext(stepExecution);
        final ChunkContext chunkContext = new ChunkContext(stepContext);
        final Parameters parameters = mock(Parameters.class);

        // Execute
        jobService.runStep(job, chunkContext, parameters);
    }
}
