package com.example.resourceops.recommendation.scheduler;

import com.example.resourceops.recommendation.dto.ObservedMetricDto;
import com.example.resourceops.recommendation.dto.PricingModel;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import com.example.resourceops.recommendation.prometheus.PrometheusMetricCollector;
import com.example.resourceops.recommendation.service.ResourceRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "resource-optimizer.prometheus", name = "enabled", havingValue = "true")
public class WorkloadCostScheduler {

    private final PrometheusMetricCollector prometheusMetricCollector;
    private final ResourceRecommendationService recommendationService;

    // 현재 Pod request 설정값 (application.yml에서 관리 가능)
    private static final int CPU_REQUEST_MILLICORES = 250;
    private static final int MEMORY_REQUEST_MIB = 256;
    private static final String INSTANCE_TYPE = "t3.medium";
    private static final PricingModel PRICING_MODEL = PricingModel.ON_DEMAND;

    @Scheduled(fixedRateString = "${resource-optimizer.prometheus.fixed-rate-ms:300000}")
    public void collectAndPublish() {
        try {
            ObservedMetricDto observedMetric = prometheusMetricCollector.collect();

            ResourceRequestDto currentRequest = new ResourceRequestDto(
                    CPU_REQUEST_MILLICORES,
                    MEMORY_REQUEST_MIB
            );

            recommendationService.compare(
                    currentRequest,
                    observedMetric,
                    INSTANCE_TYPE,
                    PRICING_MODEL
            );

            log.debug("Workload cost metrics updated - avgCpu={}m, avgMemory={}MiB",
                    observedMetric.avgCpuUsageMillicores(),
                    observedMetric.avgMemoryUsageMiB());

        } catch (Exception e) {
            log.warn("Failed to update workload cost metrics", e);
        }
    }
}