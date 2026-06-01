package com.example.resourceops.recommendation.pricing;

import com.example.resourceops.recommendation.dto.InstancePricingInfo;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class InstancePricingCatalog {

    private final Map<String, InstancePricingInfo> instances = Map.of(
            "t3.nano", new InstancePricingInfo("t3.nano", 2, 0.5, 0.007, 0.001, 0.004),
            "t3.micro", new InstancePricingInfo("t3.micro", 2, 1.0, 0.013, 0.004, 0.008),
            "t3.small", new InstancePricingInfo("t3.small", 2, 2.0, 0.026, 0.008, 0.016),
            "t3.medium", new InstancePricingInfo("t3.medium", 2, 4.0, 0.052, 0.018, 0.031),
            "t3.large", new InstancePricingInfo("t3.large", 2, 8.0, 0.104, 0.030, 0.063)
    );

    public InstancePricingInfo get(String instanceType) {
        InstancePricingInfo pricingInfo = instances.get(instanceType);
        if (pricingInfo == null) {
            throw new IllegalArgumentException("Unsupported instance type: " + instanceType);
        }

        return pricingInfo;
    }
}
