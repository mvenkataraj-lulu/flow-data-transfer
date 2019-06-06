package com.lululemon.flow.data.transfer.listener.impl;

import com.lululemon.flow.data.transfer.job.AbstractJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.reset;

/**
 * Unit tests for {@link DefaultJobExecutionListener}
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultJobExecutionListenerTest {

    @Mock
    private AbstractJob abstractJob;

    @Spy
    @InjectMocks
    private DefaultJobExecutionListener listener;

    @Before
    public void before() {
        reset(abstractJob, listener);
    }

    @Test
    public void testBeforeJob() {

        listener.beforeJob(abstractJob);
    }

    @Test
    public void testAfterJob() {

        listener.afterJob(abstractJob);
    }

    @Test
    public void testFailedJob() {

        listener.afterJob(abstractJob);
    }
}
