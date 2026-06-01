package com.example.resourceops.recommendation.prometheus;

import com.example.resourceops.recommendation.config.PrometheusProperties;
import com.example.resourceops.recommendation.dto.ObservedMetricDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusMetricCollector {

    private final PrometheusProperties prometheusProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String NAMESPACE = "resource-ops-dev";
    private static final String QUERY_PATH = "/api/v1/query";

    public ObservedMetricDto collect() {
        try {
            double avgCpu = queryScalar(
                    "avg(rate(container_cpu_usage_seconds_total{namespace=\"" + NAMESPACE + "\",container!=\"\"}[5m])) * 1000"
            );
            double p95Cpu = queryScalar(
                    "quantile(0.95, rate(container_cpu_usage_seconds_total{namespace=\"" + NAMESPACE + "\",container!=\"\"}[5m])) * 1000"
            );
            double avgMemory = queryScalar(
                    "avg(container_memory_working_set_bytes{namespace=\"" + NAMESPACE + "\",container!=\"\"}) / 1024 / 1024"
            );
            double p95Memory = queryScalar(
                    "quantile(0.95, container_memory_working_set_bytes{namespace=\"" + NAMESPACE + "\",container!=\"\"}) / 1024 / 1024"
            );

            log.debug("Collected metrics - avgCpu={}m, p95Cpu={}m, avgMemory={}MiB, p95Memory={}MiB",
                    avgCpu, p95Cpu, avgMemory, p95Memory);

            return new ObservedMetricDto(avgCpu, p95Cpu, avgMemory, p95Memory, 0, 0.0);

        } catch (Exception e) {
            log.warn("Failed to collect Prometheus metrics, using defaults", e);
            return new ObservedMetricDto(50.0, 80.0, 128.0, 200.0, 0, 0.0);
        }
    }

    @SuppressWarnings("unchecked")
    private double queryScalar(String query) {
        String url = UriComponentsBuilder
                .fromUri(URI.create(prometheusProperties.getUrl() + QUERY_PATH))
                .queryParam("query", query)
                .build()
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null) return 0.0;

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) return 0.0;

        List<Object> result = (List<Object>) data.get("result");
        if (result == null || result.isEmpty()) return 0.0;

        Map<String, Object> first = (Map<String, Object>) result.get(0);
        List<Object> value = (List<Object>) first.get("value");
        if (value == null || value.size() < 2) return 0.0;

        try {
            return Double.parseDouble(value.get(1).toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}