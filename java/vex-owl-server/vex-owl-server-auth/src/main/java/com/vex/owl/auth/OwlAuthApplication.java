package com.vex.owl.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Vex-Owl 认证服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
public class OwlAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlAuthApplication.class, args);
    }
}