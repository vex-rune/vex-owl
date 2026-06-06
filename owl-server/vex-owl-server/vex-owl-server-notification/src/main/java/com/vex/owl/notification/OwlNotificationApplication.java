package com.vex.owl.notification;

import com.vex.owl.notification.api.client.NotificationClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(clients = {NotificationClient.class})
@EnableJpaAuditing
@EnableAsync
public class OwlNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlNotificationApplication.class, args);
    }
}