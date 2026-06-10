package com.vex.owl.ai;

import com.vex.owl.ai.infra.minimax.client.MiniMaxClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vex.owl.ai", "com.vex.event"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableRetry
@EnableAsync
@EnableFeignClients(clients = {MiniMaxClient.class})
public class OwlAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlAiApplication.class, args);
    }
}
