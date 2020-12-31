/*
 * Copyright 2020 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.mantisrx.server.worker.jobmaster.clutch.rps;

import com.netflix.control.clutch.Clutch;
import io.mantisrx.shaded.com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RpsMetricComputerTest {
    @Test
    public void testApply() {

        Map<Clutch.Metric, Double> metrics = ImmutableMap.of(
                Clutch.Metric.RPS, 1.0,
                Clutch.Metric.DROPS, 20.0,
                Clutch.Metric.LAG, 300.0,
                Clutch.Metric.SOURCEJOB_DROP, 4000.0
        );
        double result = new RpsMetricComputer().apply(null, metrics);
        assertEquals(4321.0, result, 1e-10);
    }
}
