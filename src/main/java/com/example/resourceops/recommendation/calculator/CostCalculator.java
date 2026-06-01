package com.example.resourceops.recommendation.calculator;

import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.CostComponent;
import com.example.resourceops.recommendation.dto.InstancePricingInfo;
import com.example.resourceops.recommendation.dto.PricingModel;
import com.example.resourceops.recommendation.dto.ResourceCostType;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import com.example.resourceops.recommendation.pricing.InstancePricingCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CostCalculator {

    private static final double HOURS_PER_MONTH = 730.0;

    private final InstancePricingCatalog instancePricingCatalog;

    public CostResponseDto calculate(
            ResourceCostType type,
            ResourceRequestDto resourceRequest,
            String instanceType,
            PricingModel pricingModel
    ) {
        validate(resourceRequest);

        InstancePricingInfo pricingInfo = instancePricingCatalog.get(instanceType);
        double cpuAllocationRatio = resourceRequest.cpuMillicores() / (pricingInfo.vcpus() * 1000.0);
        double memoryAllocationRatio = resourceRequest.memoryMiB() / (pricingInfo.memoryGiB() * 1024.0);
        double allocationRatio = Math.max(cpuAllocationRatio, memoryAllocationRatio);

        double hourlyCost = pricingInfo.hourlyPrice(pricingModel) * allocationRatio;
        double monthlyCost = hourlyCost * HOURS_PER_MONTH;
        String dominantResource = cpuAllocationRatio >= memoryAllocationRatio ? "CPU" : "MEMORY";

        return new CostResponseDto(
                type.name(),
                CostComponent.WORKLOAD_REQUEST.name(),
                pricingInfo.instanceType(),
                pricingModel.name(),
                round(cpuAllocationRatio),
                round(memoryAllocationRatio),
                dominantResource,
                round(hourlyCost),
                round(monthlyCost)
        );
    }

    private void validate(ResourceRequestDto resourceRequest) {
        if (resourceRequest.cpuMillicores() < 0 || resourceRequest.memoryMiB() < 0) {
            throw new IllegalArgumentException("Resource request values must be greater than or equal to zero.");
        }
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
