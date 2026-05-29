package com.vex.owl.user.user.auth;

import com.vex.owl.notification.api.client.NotificationClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {NotificationClient.class})
@EnableJpaAuditing
@EnableRedisRepositories
@EnableRetry
@EnableAsync
public class OwlUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlUserApplication.class, args);
    }
}