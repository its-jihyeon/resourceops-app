package com.example.resourceops.recommendation.metrics;

import com.example.resourceops.recommendation.dto.CostResponseDto;
import com.example.resourceops.recommendation.dto.ResourceCostType;
import com.example.resourceops.recommendation.dto.ResourceRequestDto;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ResourceOptimizerMetrics {

    private final MeterRegistry meterRegistry;
    private final Map<MetricKey, MetricValues> values = new ConcurrentHashMap<>();

    public ResourceOptimizerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void publish(ResourceCostType type, ResourceRequestDto resourceRequest, CostResponseDto cost) {
        MetricKey key = new MetricKey(type.name(), cost.costComponent(), cost.instanceType(), cost.pricingModel());
        MetricValues metricValues = values.computeIfAbsent(key, this::registerMetrics);
        metricValues.update(resourceRequest, cost);
    }

    public void publish(ResourceCostType type, CostResponseDto cost) {
        MetricKey key = new MetricKey(type.name(), cost.costComponent(), cost.instanceType(), cost.pricingModel());
        MetricValues metricValues = values.computeIfAbsent(key, this::registerMetrics);
        metricValues.update(cost);
    }

    @Scheduled(fixedRateString = "${resource-optimizer.cost.accumulation-interval-ms:60000}")
    public void accumulateCost() {
        Instant now = Instant.now();
        values.values().forEach(metricValues -> metricValues.accumulate(now));
    }

    private MetricValues registerMetrics(MetricKey key) {
        MetricValues metricValues = new MetricValues();

        Gauge.builder("resource_optimizer_cpu_request_millicores", metricValues.cpuRequestMillicores, AtomicReference::get)
                .description("Current or recommended Kubernetes CPU request in millicores")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        Gauge.builder("resource_optimizer_memory_request_mib", metricValues.memoryRequestMiB, AtomicReference::get)
                .description("Current or recommended Kubernetes memory request in MiB")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        Gauge.builder("resource_optimizer_allocation_ratio", metricValues.allocationRatio, AtomicReference::get)
                .description("Dominant node resource allocation ratio used for cost allocation")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        Gauge.builder("resource_optimizer_cost_hourly_usd", metricValues.hourlyCostUsd, AtomicReference::get)
                .description("Estimated hourly request-based resource cost in USD")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        Gauge.builder("resource_optimizer_cost_monthly_usd", metricValues.monthlyCostUsd, AtomicReference::get)
                .description("Estimated monthly request-based resource cost in USD")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        metricValues.accumulatedCostUsd = Counter.builder("resource_optimizer_cost_usd")
                .description("Accumulated estimated request-based resource cost in USD")
                .tag("type", key.type())
                .tag("cost_component", key.costComponent())
                .tag("instance_type", key.instanceType())
                .tag("pricing_model", key.pricingModel())
                .register(meterRegistry);

        return metricValues;
    }

    private record MetricKey(String type, String costComponent, String instanceType, String pricingModel) {
    }

    private static class MetricValues {
        private final AtomicReference<Double> cpuRequestMillicores = new AtomicReference<>(0.0);
        private final AtomicReference<Double> memoryRequestMiB = new AtomicReference<>(0.0);
        private final AtomicReference<Double> allocationRatio = new AtomicReference<>(0.0);
        private final AtomicReference<Double> hourlyCostUsd = new AtomicReference<>(0.0);
        private final AtomicReference<Double> monthlyCostUsd = new AtomicReference<>(0.0);

        private Counter accumulatedCostUsd;
        private Instant lastAccumulatedAt;

        synchronized void update(ResourceRequestDto resourceRequest, CostResponseDto cost) {
            cpuRequestMillicores.set((double) resourceRequest.cpuMillicores());
            memoryRequestMiB.set((double) resourceRequest.memoryMiB());
            allocationRatio.set(Math.max(cost.cpuAllocationRatio(), cost.memoryAllocationRatio()));
            hourlyCostUsd.set(cost.hourlyCostUsd());
            monthlyCostUsd.set(cost.monthlyCostUsd());

            if (lastAccumulatedAt == null) {
                lastAccumulatedAt = Instant.now();
            }
        }

        synchronized void update(CostResponseDto cost) {
            allocationRatio.set(Math.max(cost.cpuAllocationRatio(), cost.memoryAllocationRatio()));
            hourlyCostUsd.set(cost.hourlyCostUsd());
            monthlyCostUsd.set(cost.monthlyCostUsd());

            if (lastAccumulatedAt == null) {
                lastAccumulatedAt = Instant.now();
            }
        }

        synchronized void accumulate(Instant now) {
            if (lastAccumulatedAt == null) {
                return;
            }

            long elapsedSeconds = Duration.between(lastAccumulatedAt, now).getSeconds();
            if (elapsedSeconds <= 0) {
                return;
            }

            double increment = hourlyCostUsd.get() / 3600.0 * elapsedSeconds;
            if (increment > 0.0) {
                accumulatedCostUsd.increment(increment);
            }

            lastAccumulatedAt = now;
        }
    }
}
