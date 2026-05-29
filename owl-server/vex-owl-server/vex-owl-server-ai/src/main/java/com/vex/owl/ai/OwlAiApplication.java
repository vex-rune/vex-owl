package com.vex.owl.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableRetry
@EnableAsync
public class OwlAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlAiApplication.class, args);
    }
}
