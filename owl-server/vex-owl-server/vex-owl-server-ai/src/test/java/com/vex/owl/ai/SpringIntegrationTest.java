package com.vex.owl.ai;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 集成测试基类
 *
 * <p>使用 H2 内存数据库，自动排除 Redis、Elasticsearch 等外部依赖。</p>
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class SpringIntegrationTest {
}
