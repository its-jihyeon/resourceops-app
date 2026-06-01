package com.example.resourceops.recommendation.cloudwatch;

import com.example.resourceops.recommendation.config.AwsCostProperties;
import com.example.resourceops.recommendation.dto.AwsTrafficUsageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Component
public class CloudWatchMetricCollector {

    private static final double BYTES_PER_GIB = 1024.0 * 1024.0 * 1024.0;

    private final AwsCostProperties awsCostProperties;
    private final CloudWatchClient cloudWatchClient;

    public CloudWatchMetricCollector(AwsCostProperties awsCostProperties) {
        this.awsCostProperties = awsCostProperties;
        this.cloudWatchClient = CloudWatchClient.builder()
                .region(Region.of(awsCostProperties.getRegion()))
                .build();
    }

    public AwsTrafficUsageDto collect() {
        int periodSeconds = awsCostProperties.getPollingMinutes() * 60;
        Instant endTime = Instant.now();
        Instant startTime = endTime.minusSeconds(periodSeconds);

        try {
            double consumedLcuHours = sumApplicationElbMetric(
                    "ConsumedLCUs",
                    StandardUnit.COUNT,
                    startTime,
                    endTime,
                    periodSeconds
            ) * periodSeconds / 3600.0;
            double albProcessedGiB = sumApplicationElbMetric(
                    "ProcessedBytes",
                    StandardUnit.BYTES,
                    startTime,
                    endTime,
                    periodSeconds
            ) / BYTES_PER_GIB;
            double albRequestCount = sumApplicationElbMetric(
                    "RequestCount",
                    StandardUnit.COUNT,
                    startTime,
                    endTime,
                    periodSeconds
            );
            double natProcessedGiB = collectNatProcessedGiB(startTime, endTime, periodSeconds);

            return new AwsTrafficUsageDto(
                    consumedLcuHours,
                    albProcessedGiB,
                    albRequestCount,
                    natProcessedGiB,
                    albProcessedGiB
            );
        } catch (RuntimeException e) {
            log.warn("Failed to collect CloudWatch traffic metrics", e);
            return new AwsTrafficUsageDto(0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private double collectNatProcessedGiB(Instant startTime, Instant endTime, int periodSeconds) {
        double bytesOutToDestination = sumNatGatewayMetric("BytesOutToDestination", startTime, endTime, periodSeconds);
        double bytesOutToSource = sumNatGatewayMetric("BytesOutToSource", startTime, endTime, periodSeconds);
        double bytesInFromDestination = sumNatGatewayMetric("BytesInFromDestination", startTime, endTime, periodSeconds);
        double bytesInFromSource = sumNatGatewayMetric("BytesInFromSource", startTime, endTime, periodSeconds);

        return (bytesOutToDestination + bytesOutToSource + bytesInFromDestination + bytesInFromSource) / BYTES_PER_GIB;
    }

    private double sumApplicationElbMetric(
            String metricName,
            StandardUnit unit,
            Instant startTime,
            Instant endTime,
            int periodSeconds
    ) {
        Dimension dimension = Dimension.builder()
                .name("LoadBalancer")
                .value(awsCostProperties.getAlbLoadBalancerDimension())
                .build();

        return latestSum("AWS/ApplicationELB", metricName, unit, List.of(dimension), startTime, endTime, periodSeconds);
    }

    private double sumNatGatewayMetric(String metricName, Instant startTime, Instant endTime, int periodSeconds) {
        Dimension dimension = Dimension.builder()
                .name("NatGatewayId")
                .value(awsCostProperties.getNatGatewayId())
                .build();

        return latestSum("AWS/NATGateway", metricName, StandardUnit.BYTES, List.of(dimension), startTime, endTime, periodSeconds);
    }

    private double latestSum(
            String namespace,
            String metricName,
            StandardUnit unit,
            List<Dimension> dimensions,
            Instant startTime,
            Instant endTime,
            int periodSeconds
    ) {
        GetMetricStatisticsRequest request = GetMetricStatisticsRequest.builder()
                .namespace(namespace)
                .metricName(metricName)
                .dimensions(dimensions)
                .startTime(startTime)
                .endTime(endTime)
                .period(periodSeconds)
                .statistics(Statistic.SUM)
                .unit(unit)
                .build();

        return cloudWatchClient.getMetricStatistics(request)
                .datapoints()
                .stream()
                .max(Comparator.comparing(Datapoint::timestamp))
                .map(Datapoint::sum)
                .orElse(0.0);
    }
}
