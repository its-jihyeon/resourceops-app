package com.example.resourceops.recommendation.dto;

public record ObservedMetricDto(
        double avgCpuUsageMillicores,
        double p95CpuUsageMillicores,
        double avgMemoryUsageMiB,
        double p95MemoryUsageMiB,
        int restartCount,
        double prometheusRunningHours
) {
}
