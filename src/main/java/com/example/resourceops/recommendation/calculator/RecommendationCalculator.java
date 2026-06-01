package com.example.resourceops.recommendation.calculator;

import com.example.resourceops.recommendation.dto.ObservedMetricDto;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import org.springframework.stereotype.Component;

@Component
public class RecommendationCalculator {

    private static final int MIN_CPU_MILLICORES = 50;
    private static final int MIN_MEMORY_MIB = 128;
    private static final int MAX_CPU_MILLICORES = 2000;
    private static final int MAX_MEMORY_MIB = 3072;
    private static final double MAX_SCALE_UP_RATIO = 2.0;
    private static final double CPU_AVG_BUFFER = 2.0;
    private static final double CPU_P95_BUFFER = 1.2;
    private static final double MEMORY_AVG_BUFFER = 1.5;
    private static final double MEMORY_P95_BUFFER = 1.2;

    public ResourceRequestDto calculate(ResourceRequestDto currentRequest, ObservedMetricDto observedMetric) {
        validate(currentRequest, observedMetric);

        int recommendedCpu = roundUpToUnit(
                Math.max(
                        observedMetric.avgCpuUsageMillicores() * CPU_AVG_BUFFER,
                        observedMetric.p95CpuUsageMillicores() * CPU_P95_BUFFER
                ),
                10
        );

        int recommendedMemory = roundUpToUnit(
                Math.max(
                        observedMetric.avgMemoryUsageMiB() * MEMORY_AVG_BUFFER,
                        observedMetric.p95MemoryUsageMiB() * MEMORY_P95_BUFFER
                ),
                16
        );

        recommendedCpu = Math.max(recommendedCpu, MIN_CPU_MILLICORES);
        recommendedMemory = Math.max(recommendedMemory, MIN_MEMORY_MIB);

        if (observedMetric.restartCount() > 0) {
            recommendedMemory = Math.max(recommendedMemory, currentRequest.memoryMiB());
        }
        
        recommendedCpu = Math.min(recommendedCpu, (int)(currentRequest.cpuMillicores() * MAX_SCALE_UP_RATIO));
        recommendedMemory = Math.min(recommendedMemory, (int)(currentRequest.memoryMiB() * MAX_SCALE_UP_RATIO));
        recommendedCpu = Math.min(recommendedCpu, MAX_CPU_MILLICORES);
        recommendedMemory = Math.min(recommendedMemory, MAX_MEMORY_MIB);
        
        return new ResourceRequestDto(recommendedCpu, recommendedMemory);
    }

    private void validate(ResourceRequestDto currentRequest, ObservedMetricDto observedMetric) {
        if (currentRequest.cpuMillicores() <= 0 || currentRequest.memoryMiB() <= 0) {
            throw new IllegalArgumentException("Current resource request values must be greater than zero.");
        }

        if (observedMetric.avgCpuUsageMillicores() < 0
                || observedMetric.p95CpuUsageMillicores() < 0
                || observedMetric.avgMemoryUsageMiB() < 0
                || observedMetric.p95MemoryUsageMiB() < 0
                || observedMetric.restartCount() < 0) {
            throw new IllegalArgumentException("Observed metric values must be greater than or equal to zero.");
        }
    }

    private int roundUpToUnit(double value, int unit) {
        return (int) (Math.ceil(value / unit) * unit);
    }
}
