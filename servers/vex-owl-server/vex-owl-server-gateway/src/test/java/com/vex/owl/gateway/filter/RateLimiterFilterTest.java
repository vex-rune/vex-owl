package com.vex.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 限流过滤器单元测试
 * 直接测试限流逻辑是否正常工作，不需要启动下游服务
 */
@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("限流过滤器测试")
public class RateLimiterFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private RateLimiterFilter rateLimiterFilter;

    @BeforeEach
    void setUp() {
        // 每个测试前清空限流器缓存，避免互相影响
        ConcurrentHashMap<String, ?> limiters = (ConcurrentHashMap<String, ?>) ReflectionTestUtils.getField(rateLimiterFilter, "ipLimiters");
        if (limiters != null) {
            limiters.clear();
        }
    }

    @Test
    @DisplayName("测试超过限流阈值时返回429")
    void testRateLimiter_WhenExceedThreshold_Return429() throws InterruptedException {
        // 同一个IP（192.168.1.100）连续发4次请求
        // 每秒3个请求，令牌桶容量3，前3次成功，第4次返回429
        for (int i = 0; i < 3; i++) {
            webTestClient.get().uri("/actuator/health")
                    .header("X-Forwarded-For", "192.168.1.100")
                    .exchange()
                    .expectStatus().isOk();
            // 短暂延迟，确保请求被正确处理
            Thread.sleep(50);
        }

        // 第4次：超过阈值，返回429
        webTestClient.get().uri("/actuator/health")
                .header("X-Forwarded-For", "192.168.1.100")
                .exchange()
                .expectStatus().isEqualTo(429)
                .expectBody()
                .jsonPath("$.code").isEqualTo(429)
                .jsonPath("$.message").isEqualTo("请求过于频繁，请稍后再试");
    }

    @Test
    @DisplayName("测试不同IP限流是独立的，互不影响")
    void testRateLimiter_DifferentIp_IsIndependent() throws InterruptedException {
        // IP1发请求到被限流
        for (int i = 0; i < 3; i++) {
            webTestClient.get().uri("/actuator/health")
                    .header("X-Forwarded-For", "192.168.1.200")
                    .exchange()
                    .expectStatus().isOk();
            // 短暂延迟，确保请求被正确处理
            Thread.sleep(50);
        }
        webTestClient.get().uri("/actuator/health")
                .header("X-Forwarded-For", "192.168.1.200")
                .exchange()
                .expectStatus().isEqualTo(429);

        // 换一个IP，应该可以正常请求，不会被前面的IP影响
        webTestClient.get().uri("/actuator/health")
                .header("X-Forwarded-For", "192.168.1.201")
                .exchange()
                .expectStatus().isOk();
    }
}
