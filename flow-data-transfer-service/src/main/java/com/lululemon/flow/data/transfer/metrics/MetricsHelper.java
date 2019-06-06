package com.lululemon.flow.data.transfer.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Contains all needed methods to report metrics
 */
@Component
public class MetricsHelper {

    private static final String BATCH_METRIC = "transform.job.%s";
    public static final String BATCH_TIME = BATCH_METRIC + ".time";
    public static final String BATCH_COUNT = BATCH_METRIC + ".count";
    public static final String BATCH_ERROR = BATCH_METRIC + ".error";

    private final MetricRegistry metricRegistry;

    public MetricsHelper(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    /**
     * Report histogram metric. It will be created if does not exist.
     *
     * @param name  name of metric
     * @param value value
     */
    public void reportHistogramMetric(String name, long value) {
        metricRegistry.histogram(name, () ->
                new Histogram(new SlidingTimeWindowReservoir(5, TimeUnit.SECONDS))).update(value);
    }

    /**
     * Report meter metric. It will be created if does not exist
     *
     * @param name name
     */
    public void reportMeterMetric(String name) {
        metricRegistry.meter(name).mark();
    }


    public void reportGauge(String name, int value) {
        metricRegistry.gauge(name, () -> () -> value);
    }


    public void reportCounter(String name, int value) {
        Counter counter = metricRegistry.counter(name);
        long count = counter.getCount();
        if (value > count) {
            counter.inc(value);
        } else if (value < count) {
            counter.dec(value);
        }
    }
}
