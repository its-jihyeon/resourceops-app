package com.example.resourceops.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "resource-optimizer.aws-pricing")
public class AwsPricingProperties {

    private double albHourUsd = 0.0225;
    private double albLcuHourUsd = 0.008;
    private double natGatewayHourUsd = 0.045;
    private double natGatewayDataGbUsd = 0.045;
    private double dataTransferOutGbUsd = 0.09;
}
