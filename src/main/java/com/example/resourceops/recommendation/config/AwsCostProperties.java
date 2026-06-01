package com.example.resourceops.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "resource-optimizer.aws")
public class AwsCostProperties {

    private String region = "ap-northeast-2";
    private String albLoadBalancerDimension = "app/k8s-resource-resource-c5d4133575/0549ffba1540a2a9";
    private String natGatewayId = "nat-0854b9c44fe1d4867";
    private int pollingMinutes = 10;
}
