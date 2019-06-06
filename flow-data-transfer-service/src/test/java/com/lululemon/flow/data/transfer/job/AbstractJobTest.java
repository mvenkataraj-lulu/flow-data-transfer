package com.lululemon.flow.data.transfer.job;

import com.lululemon.flow.data.transfer.params.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AbstractJobTest {

    public AbstractJob sleepJob;


    @Before
    public void before() {
        sleepJob = new AbstractJob("test") {
            @Override
            public void execute(ResourceLoader resourceLoader, Parameters parameters) throws Exception {
                Thread.sleep(1000);
            }
        };
        sleepJob = Mockito.spy(sleepJob);
    }

    @Test
    public void testRun() throws Exception {
        Map<String, String> params = new HashMap<>();
        sleepJob.run(new ResourceLoader() {
            @Override
            public Resource getResource(String s) {
                return null;
            }

            @Override
            public ClassLoader getClassLoader() {
                return null;
            }
        }, new Parameters(params));
        verify(sleepJob, atLeastOnce()).execute(any(ResourceLoader.class), any(Parameters.class));
    }

}
