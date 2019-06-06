package com.lululemon.flow.data.transfer.job;

import com.google.common.base.Stopwatch;
import com.lululemon.flow.data.transfer.listener.JobExecutionListener;
import com.lululemon.flow.data.transfer.params.Parameters;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.retry.support.RetryTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Setter
public abstract class AbstractJob {

    private String name;
    private String jobType;
    private long duration;
    private Parameters parameters;
    private String milestoneName;
    private String milestoneWeight;
    private String track;
    private String schedulerJobRunDate;
    private String schedulerJobName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    public static final String PARAM_MILESTONE_NAME="milestoneName";
    public static final String PARAM_MILESTONE_WEIGHT="milestoneWeight";
    public static final String PARAM_TRACK="track";
    public static final String PARAM_SCHEDULER_JOB_RUN_DATE="schedulerJobRunDate";
    public static final String PARAM_SCHEDULER_JOB_NAME="schedulerJobRunName";
    private List<JobExecutionListener> listeners = new ArrayList<>();
    @Setter
    protected RetryTemplate retryTemplate;

    public AbstractJob(String name) {
        this.name = name;
    }

    public AbstractJob(String name, String jobType) {
        this.name = name;
        this.jobType = jobType;
    }

    public void run(ResourceLoader resourceLoader, Parameters parameters) {
        this.parameters = parameters;
        listeners.forEach(l -> l.beforeJob(this));
        this.startTime = LocalDateTime.now();

        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            execute(resourceLoader, this.parameters);
            this.endTime = LocalDateTime.now();
            listeners.forEach(l -> l.successJob(this));
        } catch (Exception e) {
            duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            listeners.forEach(l -> l.failedJob(this));
            throw new RuntimeException("Can't execute job " + name, e);
        } finally {
            duration = stopwatch.elapsed(TimeUnit.MILLISECONDS);
            listeners.forEach(l -> l.afterJob(this));
        }
    }

    protected abstract void execute(ResourceLoader resourceLoader, Parameters parameters) throws Exception;

    public void addJobExecutionListener(JobExecutionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }
}
