package com.example.resourceops.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "resource-optimizer.prometheus")
public class PrometheusProperties {

    private String url = "http://kube-prometheus-stack-prometheus.monitoring:9090";
    private long fixedRateMs = 300000; // 5분
}