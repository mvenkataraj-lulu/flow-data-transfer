package com.lululemon.flow.data.transfer.listener;

import com.lululemon.flow.data.transfer.job.AbstractJob;

public interface JobExecutionListener {

    void beforeJob(AbstractJob job);
    void failedJob(AbstractJob job);
    void afterJob(AbstractJob job);
    void successJob(AbstractJob job);

}
