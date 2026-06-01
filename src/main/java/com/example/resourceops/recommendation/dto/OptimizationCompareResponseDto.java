package com.example.resourceops.recommendation.dto;

public record OptimizationCompareResponseDto(
        ResourceRequestDto currentResource,
        ObservedMetricDto observedMetric,
        ResourceRequestDto recommendedResource,
        CostResponseDto currentCost,
        CostResponseDto recommendedCost,
        double potentialSavingsPerMonthUsd,
        double potentialSavingsPercent,
        String queryRange
) {
}
