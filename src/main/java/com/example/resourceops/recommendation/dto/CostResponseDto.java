package com.example.resourceops.recommendation.dto;

public record CostResponseDto(
        String type,
        String costComponent,
        String instanceType,
        String pricingModel,
        double cpuAllocationRatio,
        double memoryAllocationRatio,
        String dominantResource,
        double hourlyCostUsd,
        double monthlyCostUsd
) {
}
