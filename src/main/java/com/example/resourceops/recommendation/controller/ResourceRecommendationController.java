package com.example.resourceops.recommendation.controller;

import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.ObservedMetricDto;
import com.example.resourceops.recommendation.dto.OptimizationCompareResponseDto;
import com.example.resourceops.recommendation.dto.PricingModel;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import com.example.resourceops.recommendation.service.ResourceRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class ResourceRecommendationController {

    private final ResourceRecommendationService recommendationService;

    @Operation(summary = "현재 request와 관측 metric 기반 비용 비교 및 optimizer metric 갱신")
    @GetMapping("/compare")
    public OptimizationCompareResponseDto compare(
            @RequestParam int cpuRequestMillicores,
            @RequestParam int memoryRequestMiB,
            @RequestParam double avgCpuUsageMillicores,
            @RequestParam(defaultValue = "0") double p95CpuUsageMillicores,
            @RequestParam double avgMemoryUsageMiB,
            @RequestParam(defaultValue = "0") double p95MemoryUsageMiB,
            @RequestParam(defaultValue = "0") int restartCount,
            @RequestParam(defaultValue = "t3.medium") String instanceType,
            @RequestParam(defaultValue = "ON_DEMAND") PricingModel pricingModel,
            @RequestParam(defaultValue = "99") double prometheusRunningHours
    ) {
        ResourceRequestDto currentRequest = new ResourceRequestDto(cpuRequestMillicores, memoryRequestMiB);
        ObservedMetricDto observedMetric = new ObservedMetricDto(
                avgCpuUsageMillicores,
                p95CpuUsageMillicores,
                avgMemoryUsageMiB,
                p95MemoryUsageMiB,
                restartCount,
                prometheusRunningHours
        );

        return recommendationService.compare(currentRequest, observedMetric, instanceType, pricingModel);
    }

    @Operation(summary = "현재 request 기준 비용 계산 및 CURRENT metric 갱신")
    @GetMapping("/cost/current")
    public CostResponseDto calculateCurrentCost(
            @RequestParam int cpuRequestMillicores,
            @RequestParam int memoryRequestMiB,
            @RequestParam(defaultValue = "t3.medium") String instanceType,
            @RequestParam(defaultValue = "ON_DEMAND") PricingModel pricingModel
    ) {
        ResourceRequestDto currentRequest = new ResourceRequestDto(cpuRequestMillicores, memoryRequestMiB);
        return recommendationService.calculateCurrentCost(currentRequest, instanceType, pricingModel);
    }

    @Operation(summary = "Prometheus 평균 계산 기준 시간 조회")
    @GetMapping("/query-range")
    public String getQueryRange(
            @RequestParam double prometheusRunningHours
    ) {
        return recommendationService.getQueryRange(prometheusRunningHours);
    }
}
