package com.example.resourceops.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "resource-optimizer.simulation")
public class CostSimulationProperties {

  private String devNightStartTime = "09:20";
  private String devNightEndTime = "09:40";
  private boolean albNightShutdownEnabled = true;
  private boolean natNightShutdownEnabled = true;
}
