package com.vex.owl.auth;

import com.vex.owl.notification.api.client.NotificationClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.vex.owl.notification.api.client.NotificationClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = NotificationClient.class)
@EnableJpaAuditing
@EnableRedisRepositories
public class OwlAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlAuthApplication.class, args);
    }
}