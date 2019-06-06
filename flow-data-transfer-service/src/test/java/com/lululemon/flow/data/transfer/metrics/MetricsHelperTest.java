package com.lululemon.flow.data.transfer.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MetricsHelper}
 *
 * @author Oleksii Usatov
 */
@RunWith(MockitoJUnitRunner.class)
public class MetricsHelperTest {

    @Mock
    private MetricRegistry metricRegistry;

    @Test
    public void before() {
        reset(metricRegistry);
    }

    @Test
    public void testReportCounterInc() {

        // Setup
        final String name = "name";
        MetricsHelper metricsHelper = new MetricsHelper(metricRegistry);
        Counter counter = mock(Counter.class);
        when(metricRegistry.counter(name)).thenReturn(counter);
        when(counter.getCount()).thenReturn(1L);

        // Execute
        metricsHelper.reportCounter(name, 2);

        // Verify
        verify(counter).inc(2);
    }

    @Test
    public void testReportCounterDec() {

        // Setup
        final String name = "name";
        MetricsHelper metricsHelper = new MetricsHelper(metricRegistry);
        Counter counter = mock(Counter.class);
        when(metricRegistry.counter(name)).thenReturn(counter);
        when(counter.getCount()).thenReturn(10L);

        // Execute
        metricsHelper.reportCounter(name, 2);

        // Verify
        verify(counter).dec(2);
    }

    @Test
    public void testReportGauge() {

        // Setup
        final String name = "name";
        MetricsHelper metricsHelper = new MetricsHelper(metricRegistry);

        // Execute
        metricsHelper.reportGauge(name, 10);

        // Verify
        verify(metricRegistry).gauge(eq(name), any());
    }
}
