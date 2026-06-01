package com.example.resourceops.recommendation.dto;

public record InstancePricingInfo(
        String instanceType,
        int vcpus,
        double memoryGiB,
        double onDemandHourlyUsd,
        double spotHourlyUsd,
        double reserved1YrHourlyUsd
) {
    public double hourlyPrice(PricingModel pricingModel) {
        return switch (pricingModel) {
            case ON_DEMAND -> onDemandHourlyUsd;
            case SPOT -> spotHourlyUsd;
            case RESERVED_1YR -> reserved1YrHourlyUsd;
        };
    }
}
