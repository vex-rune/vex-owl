package com.vex.owl.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Vex-Owl AI 服务启动类
 * <p>基于 Spring Boot 3.x + Spring AI Alibaba，
 * 提供多模型路由的 AI 对话服务。</p>
 */
@SpringBootApplication(scanBasePackages = "com.vex.owl")
public class OwlAiApplication {

    /**
     * 服务入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(OwlAiApplication.class, args);
    }
}
