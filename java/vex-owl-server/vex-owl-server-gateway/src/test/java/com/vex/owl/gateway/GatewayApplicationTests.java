package com.vex.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Gateway 应用上下文测试
 */
@SpringBootTest
@DisplayName("网关服务测试")
class GatewayApplicationTests {

    @Test
    @DisplayName("应用上下文加载测试")
    void contextLoads() {
        // 验证 Spring 应用上下文能够正常加载
    }
}
