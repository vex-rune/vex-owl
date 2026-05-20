package com.vex.owl.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Vex-Owl 认证服务启动类
 * 提供用户认证、令牌管理、JWT处理等功能
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OwlAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OwlAuthApplication.class, args);
    }
}