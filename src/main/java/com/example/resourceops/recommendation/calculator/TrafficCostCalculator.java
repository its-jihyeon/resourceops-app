package com.example.resourceops.recommendation.calculator;

import com.example.resourceops.recommendation.config.AwsCostProperties;
import com.example.resourceops.recommendation.config.AwsPricingProperties;
import com.example.resourceops.recommendation.config.CostSimulationProperties;
import com.example.resourceops.recommendation.dto.AwsTrafficUsageDto;
import com.example.resourceops.recommendation.dto.CostComponent;
import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.ResourceCostType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrafficCostCalculator {

    private static final double HOURS_PER_MONTH = 730.0;

    private final AwsCostProperties awsCostProperties;
    private final AwsPricingProperties awsPricingProperties;
    private final CostSimulationProperties costSimulationProperties;

    public List<CostResponseDto> calculate(ResourceCostType type, AwsTrafficUsageDto trafficUsage) {
        return List.of(
                calculateAlbCost(type, trafficUsage),
                calculateNatGatewayCost(type, trafficUsage),
                calculateDataTransferCost(type, trafficUsage)
        );
    }

    private CostResponseDto calculateAlbCost(ResourceCostType type, AwsTrafficUsageDto trafficUsage) {
        double intervalHours = awsCostProperties.getPollingMinutes() / 60.0;
        double intervalCost = awsPricingProperties.getAlbHourUsd() * intervalHours
                + awsPricingProperties.getAlbLcuHourUsd() * trafficUsage.albConsumedLcuHours();
        double hourlyCost = intervalCost / intervalHours;

        if (type == ResourceCostType.RECOMMENDED
                && costSimulationProperties.isAlbNightShutdownEnabled()
                && isDevNight()) {
            hourlyCost = 0.0;
        }

        return response(type, CostComponent.ALB, hourlyCost);
    }

    private CostResponseDto calculateNatGatewayCost(ResourceCostType type, AwsTrafficUsageDto trafficUsage) {
        double intervalHours = awsCostProperties.getPollingMinutes() / 60.0;
        double intervalCost = awsPricingProperties.getNatGatewayHourUsd() * intervalHours
                + awsPricingProperties.getNatGatewayDataGbUsd() * trafficUsage.natGatewayProcessedGiB();
        double hourlyCost = intervalCost / intervalHours;

        if (type == ResourceCostType.RECOMMENDED
                && costSimulationProperties.isNatNightShutdownEnabled()
                && isDevNight()) {
            hourlyCost = 0.0;
        }

        return response(type, CostComponent.NAT_GATEWAY, hourlyCost);
    }

    private CostResponseDto calculateDataTransferCost(ResourceCostType type, AwsTrafficUsageDto trafficUsage) {
        double intervalHours = awsCostProperties.getPollingMinutes() / 60.0;
        double intervalCost = awsPricingProperties.getDataTransferOutGbUsd() * trafficUsage.dataTransferOutGiB();
        double hourlyCost = intervalHours == 0.0 ? 0.0 : intervalCost / intervalHours;

        return response(type, CostComponent.DATA_TRANSFER, hourlyCost);
    }

    private CostResponseDto response(ResourceCostType type, CostComponent costComponent, double hourlyCost) {
        return new CostResponseDto(
                type.name(),
                costComponent.name(),
                "AWS_MANAGED",
                "ON_DEMAND",
                0.0,
                0.0,
                "TRAFFIC",
                round(hourlyCost),
                round(hourlyCost * HOURS_PER_MONTH)
        );
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private boolean isDevNight() {
        LocalTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalTime();
        LocalTime start = LocalTime.parse(costSimulationProperties.getDevNightStartTime());
        LocalTime end = LocalTime.parse(costSimulationProperties.getDevNightEndTime());

        boolean result;
        if (start.equals(end)) {
            result = false;
        } else if (start.isBefore(end)) {
            result = !now.isBefore(start) && now.isBefore(end);
        } else {
            result = !now.isBefore(start) || now.isBefore(end);
        }

        log.info("Cost simulation time check now={}, start={}, end={}, isDevNight={}", now, start, end, result);
        return result;
    }
}
