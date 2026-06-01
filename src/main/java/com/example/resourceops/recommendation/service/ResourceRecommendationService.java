package com.example.resourceops.recommendation.service;

import com.example.resourceops.recommendation.calculator.CostCalculator;
import com.example.resourceops.recommendation.calculator.RecommendationCalculator;
import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.ObservedMetricDto;
import com.example.resourceops.recommendation.dto.OptimizationCompareResponseDto;
import com.example.resourceops.recommendation.dto.PricingModel;
import com.example.resourceops.recommendation.dto.ResourceCostType;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import com.example.resourceops.recommendation.metrics.ResourceOptimizerMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceRecommendationService {

    private final CostCalculator costCalculator;
    private final RecommendationCalculator recommendationCalculator;
    private final ResourceOptimizerMetrics resourceOptimizerMetrics;

    public OptimizationCompareResponseDto compare(
            ResourceRequestDto currentRequest,
            ObservedMetricDto observedMetric,
            String instanceType,
            PricingModel pricingModel
    ) {
        CostResponseDto currentCost = costCalculator.calculate(
                ResourceCostType.CURRENT,
                currentRequest,
                instanceType,
                pricingModel
        );
        ResourceRequestDto recommendedRequest = recommendationCalculator.calculate(currentRequest, observedMetric);
        CostResponseDto recommendedCost = costCalculator.calculate(
                ResourceCostType.RECOMMENDED,
                recommendedRequest,
                instanceType,
                pricingModel
        );

        resourceOptimizerMetrics.publish(ResourceCostType.CURRENT, currentRequest, currentCost);
        resourceOptimizerMetrics.publish(ResourceCostType.RECOMMENDED, recommendedRequest, recommendedCost);

        double savings = currentCost.monthlyCostUsd() - recommendedCost.monthlyCostUsd();
        double savingsPercent = currentCost.monthlyCostUsd() == 0.0
                ? 0.0
                : savings / currentCost.monthlyCostUsd() * 100.0;

        return new OptimizationCompareResponseDto(
                currentRequest,
                observedMetric,
                recommendedRequest,
                currentCost,
                recommendedCost,
                round(savings),
                round(savingsPercent),
                getQueryRange(observedMetric.prometheusRunningHours())
        );
    }

    public CostResponseDto calculateCurrentCost(
            ResourceRequestDto currentRequest,
            String instanceType,
            PricingModel pricingModel
    ) {
        CostResponseDto currentCost = costCalculator.calculate(
                ResourceCostType.CURRENT,
                currentRequest,
                instanceType,
                pricingModel
        );
        resourceOptimizerMetrics.publish(ResourceCostType.CURRENT, currentRequest, currentCost);
        return currentCost;
    }

    public String getQueryRange(double prometheusRunningHours) {
        if (prometheusRunningHours < 1) {
            return "HOLD";
        }

        if (prometheusRunningHours < 12) {
            return prometheusRunningHours + "h";
        }

        return "12h";
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }
}
