package io.mantisrx.server.worker.jobmaster.clutch.rps;

import com.netflix.control.clutch.Clutch;
import com.netflix.control.clutch.ClutchConfiguration;
import com.yahoo.sketches.quantiles.UpdateDoublesSketch;
import io.mantisrx.runtime.descriptor.StageScalingPolicy;
import io.mantisrx.runtime.descriptor.StageSchedulingInfo;
import io.mantisrx.shaded.com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import io.vavr.control.Option;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RpsClutchConfigurationSelectorTest {
    private static final Logger logger = LoggerFactory.getLogger(RpsClutchConfigurationSelectorTest.class);

    @Test
    public void testApply() {
        UpdateDoublesSketch rpsSketch = UpdateDoublesSketch.builder().setK(1024).build();
        rpsSketch.update(100);
        Map<Clutch.Metric, UpdateDoublesSketch> sketches = ImmutableMap.of(Clutch.Metric.RPS, rpsSketch);

        ClutchRpsPIDConfig rpsConfig = new ClutchRpsPIDConfig(0.0, Tuple.of(0.2, 0.1), 0, 0, Option.none(), Option.none(), Option.none(), Option.none());
        io.mantisrx.server.worker.jobmaster.clutch.ClutchConfiguration customConfig = new io.mantisrx.server.worker.jobmaster.clutch.ClutchConfiguration(
                1, 10, 0, Option.none(), Option.of(300L), Option.none(), Option.none(), Option.none(), Option.none(), Option.none(), Option.of(rpsConfig), Option.none());

        StageSchedulingInfo schedulingInfo = new StageSchedulingInfo(3, null, null, null, null, true);
        RpsClutchConfigurationSelector selector = new RpsClutchConfigurationSelector(1, schedulingInfo, customConfig);

        ClutchConfiguration config = selector.apply(sketches);

        assertEquals(Clutch.Metric.RPS, config.getMetric());
        assertEquals(100.0, config.getSetPoint(), 1e-10);
        assertEquals(1, config.getMinSize());
        assertEquals(10, config.getMaxSize());
        assertEquals(Tuple.of(0.2, 0.1), config.getRope());
        assertEquals(300L, config.getCooldownInterval());
    }

    @Test
    public void testScalingPolicyFallback() {
        UpdateDoublesSketch rpsSketch = UpdateDoublesSketch.builder().setK(1024).build();
        rpsSketch.update(100);
        Map<Clutch.Metric, UpdateDoublesSketch> sketches = ImmutableMap.of(Clutch.Metric.RPS, rpsSketch);

        StageScalingPolicy scalingPolicy = new StageScalingPolicy(1, 2, 9, 0, 0, 400L, null);

        StageSchedulingInfo schedulingInfo = new StageSchedulingInfo(3, null, null, null, scalingPolicy, true);
        RpsClutchConfigurationSelector selector = new RpsClutchConfigurationSelector(1, schedulingInfo, null);

        ClutchConfiguration config = selector.apply(sketches);

        assertEquals(Clutch.Metric.RPS, config.getMetric());
        assertEquals(100.0, config.getSetPoint(), 1e-10);
        assertEquals(2, config.getMinSize());
        assertEquals(9, config.getMaxSize());
        assertEquals(Tuple.of(0.3, 0.0), config.getRope());
        assertEquals(400L, config.getCooldownInterval());

    }

    @Test
    public void testSetPointQuantile() {
        UpdateDoublesSketch rpsSketch = UpdateDoublesSketch.builder().setK(1024).build();
        for (int i = 1; i <= 100; i++) {
            rpsSketch.update(i);
        }
        Map<Clutch.Metric, UpdateDoublesSketch> sketches = ImmutableMap.of(Clutch.Metric.RPS, rpsSketch);

        StageScalingPolicy scalingPolicy = new StageScalingPolicy(1, 2, 9, 0, 0, 400L, null);

        StageSchedulingInfo schedulingInfo = new StageSchedulingInfo(3, null, null, null, scalingPolicy, true);
        RpsClutchConfigurationSelector selector = new RpsClutchConfigurationSelector(1, schedulingInfo, null);

        ClutchConfiguration config = selector.apply(sketches);

        assertEquals(76.0, config.getSetPoint(), 1e-10);

    }

    @Test
    public void testSetPointDriftAdjust() {
        UpdateDoublesSketch rpsSketch = UpdateDoublesSketch.builder().setK(1024).build();
        for (int i = 1; i <= 100; i++) {
            if (i <= 76) {
                rpsSketch.update(i);
            } else {
                rpsSketch.update(1000 + i);
            }
        }
        Map<Clutch.Metric, UpdateDoublesSketch> sketches = ImmutableMap.of(Clutch.Metric.RPS, rpsSketch);

        StageScalingPolicy scalingPolicy = new StageScalingPolicy(1, 2, 9, 0, 0, 400L, null);

        StageSchedulingInfo schedulingInfo = new StageSchedulingInfo(3, null, null, null, scalingPolicy, true);
        RpsClutchConfigurationSelector selector = new RpsClutchConfigurationSelector(1, schedulingInfo, null);

        ClutchConfiguration config = selector.apply(sketches);

        assertEquals(83.6, config.getSetPoint(), 1e-10);

    }
}
