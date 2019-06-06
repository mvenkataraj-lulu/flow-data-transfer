package com.lululemon.flow.data.transfer.listener.impl;

import com.lululemon.flow.data.transfer.job.AbstractJob;
import com.lululemon.flow.data.transfer.listener.JobExecutionListener;
import com.lululemon.flow.data.transfer.metrics.MetricsHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MetricsListener implements JobExecutionListener {

    @Autowired
    private MetricsHelper metricsHelper;

    @Override
    public void beforeJob(AbstractJob job) {
        String jobName = job.getName();
        log.info("Start {} job", jobName);
        String metricCount = String.format(MetricsHelper.BATCH_COUNT, jobName);
        metricsHelper.reportMeterMetric(metricCount);
    }

    @Override
    public void failedJob(AbstractJob job) {
        String jobName = job.getName();
        log.info("Failed {} job, execution time: {} seconds",
                jobName, TimeUnit.MILLISECONDS.toSeconds(job.getDuration()));
        String metricError = String.format(MetricsHelper.BATCH_ERROR, jobName);
        metricsHelper.reportMeterMetric(metricError);
    }

    @Override
    public void afterJob(AbstractJob job) {
        String jobName = job.getName();
        log.info("Finish {} job, execution time: {} seconds",
                jobName, TimeUnit.MILLISECONDS.toSeconds(job.getDuration()));
        String metricTime = String.format(MetricsHelper.BATCH_TIME, jobName);
        String totalTime = String.format(MetricsHelper.BATCH_TIME, "total");
        metricsHelper.reportHistogramMetric(metricTime, job.getDuration());
        metricsHelper.reportHistogramMetric(totalTime, job.getDuration());
    }

    @Override
    public void successJob(AbstractJob job) {
    }
}
