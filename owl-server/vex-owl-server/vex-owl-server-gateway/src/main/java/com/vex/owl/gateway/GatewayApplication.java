package com.vex.owl.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Vex-Owl API 网关服务启动类
 * 统一请求入口，提供路由转发、JWT鉴权、跨域处理、限流等功能
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
