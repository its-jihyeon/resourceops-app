package com.example.resourceops.recommendation.dto;

public record AwsTrafficUsageDto(
        double albConsumedLcuHours,
        double albProcessedGiB,
        double albRequestCount,
        double natGatewayProcessedGiB,
        double dataTransferOutGiB
) {
}
