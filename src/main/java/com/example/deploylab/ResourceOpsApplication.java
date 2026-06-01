package com.example.deploylab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example")
public class ResourceOpsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceOpsApplication.class, args);
	}

}
