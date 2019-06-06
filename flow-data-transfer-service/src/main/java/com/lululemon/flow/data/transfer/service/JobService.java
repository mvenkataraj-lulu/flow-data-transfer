package com.lululemon.flow.data.transfer.service;

import com.google.common.annotations.VisibleForTesting;
import com.lululemon.flow.data.transfer.Utils;
import com.lululemon.flow.data.transfer.api.JobRequest;
import com.lululemon.flow.data.transfer.job.AbstractJob;
import com.lululemon.flow.data.transfer.listener.impl.MetricsListener;
import com.lululemon.flow.data.transfer.params.Parameters;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Service
@Slf4j
public class JobService {

    private final String CID = "cid";
    private final String EXECUTION_ID = "jobExecutionId";

    private ApplicationContext ctx;
    private ResourceLoader resourceLoader;

    @VisibleForTesting
    Properties properties = new Properties();


    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;

    @Autowired
    public DataSource batchDataSource;


    @Autowired
    public JobService(ApplicationContext ctx, ResourceLoader resourceLoader,
                      JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory,
                      JobExplorer jobExplorer, JobLauncher jobLauncher) {
        this.ctx = ctx;
        this.resourceLoader = resourceLoader;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobExplorer = jobExplorer;
        this.jobLauncher = jobLauncher;
    }

    public JobExecution getJobExecution(Long id) {
        Utils.clearMdc();
        JobExecution jobExecution = jobExplorer.getJobExecution(id);
        log.info("Return execution status {} for id = {}", jobExecution.getStatus(), id);
        return jobExecution;
    }


    public JobExecution runJob(JobRequest.Transform request) {
        System.out.print("here");
        AbstractJob job = ctx.getBean(AbstractJob.class, request);
        job.addJobExecutionListener(ctx.getBean(MetricsListener.class));
        Parameters parameters = new Parameters(job.getName(), loadJobParameters(request));
        System.out.print("here1");
        return  execute(job.getName(), (stepContribution, chunkContext) -> {
            runStep(job,chunkContext, parameters);
            return null;
        });
    }

    @VisibleForTesting
    void runStep(AbstractJob job, ChunkContext chunkContext, Parameters parameters) {
        Utils.logMdc(CID,parameters.get(CID));
        Utils.logMdc(EXECUTION_ID, EXECUTION_ID + ":" + chunkContext.getStepContext().getStepExecution().getJobExecutionId());
        job.run(resourceLoader, parameters);
        Utils.clearMdc();
    }

    private JobExecution execute(String jobName, Tasklet tasklet) {
        Step step = stepBuilderFactory.get("step").tasklet(tasklet).build();
        Job job = jobBuilderFactory.get(jobName).start(step).build();
        try {
            JobParametersBuilder parametersBuilder = new JobParametersBuilder();
            parametersBuilder.addDate("date", new Date());
            return jobLauncher.run(job, parametersBuilder.toJobParameters());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private Map<String, String> loadJobParameters(JobRequest jobRequest) {
        Map<String, String> params = new HashMap(properties);
        if (jobRequest instanceof JobRequest.Transform) {
            JobRequest.Transform request = (JobRequest.Transform) jobRequest;
            params.put(Parameters.PARAM_SQL, request.getSqlPath());
        }
        params.put(CID, MDC.get(CID));
        return params;
    }

}
