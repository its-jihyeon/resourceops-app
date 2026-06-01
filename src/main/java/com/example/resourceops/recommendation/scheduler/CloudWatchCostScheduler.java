package com.example.resourceops.recommendation.scheduler;

import com.example.resourceops.recommendation.calculator.TrafficCostCalculator;
import com.example.resourceops.recommendation.cloudwatch.CloudWatchMetricCollector;
import com.example.resourceops.recommendation.dto.AwsTrafficUsageDto;
import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.ResourceCostType;
import com.example.resourceops.recommendation.metrics.ResourceOptimizerMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "resource-optimizer.cloudwatch", name = "enabled", havingValue = "true")
public class CloudWatchCostScheduler {

    private final CloudWatchMetricCollector cloudWatchMetricCollector;
    private final TrafficCostCalculator trafficCostCalculator;
    private final ResourceOptimizerMetrics resourceOptimizerMetrics;

    @Scheduled(fixedRateString = "${resource-optimizer.cloudwatch.fixed-rate-ms:600000}")
    public void collectAndPublish() {
        AwsTrafficUsageDto trafficUsage = cloudWatchMetricCollector.collect();

        publish(ResourceCostType.CURRENT, trafficUsage);
        publish(ResourceCostType.RECOMMENDED, trafficUsage);

        log.debug("Published CloudWatch traffic cost metrics: {}", trafficUsage);
    }

    private void publish(ResourceCostType type, AwsTrafficUsageDto trafficUsage) {
        for (CostResponseDto cost : trafficCostCalculator.calculate(type, trafficUsage)) {
            resourceOptimizerMetrics.publish(type, cost);
        }
    }
}
