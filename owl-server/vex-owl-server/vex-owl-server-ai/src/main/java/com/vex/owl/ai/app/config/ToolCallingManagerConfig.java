package com.vex.owl.ai.app.config;

import com.vex.event.EventPublisher;
import com.vex.owl.ai.domain.tools.EventPublishingToolCallingManager;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 工具调用管理器配置
 *
 * <p>注册 {@link EventPublishingToolCallingManager} 为全局 Bean，
 * 替代默认的 {@link DefaultToolCallingManager}，在工具执行前后发布事件。</p>
 */
@Configuration
public class ToolCallingManagerConfig {

    @Bean
    public ToolCallingManager toolCallingManager(EventPublisher eventPublisher) {
        DefaultToolCallingManager defaultManager = DefaultToolCallingManager.builder().build();
        return new EventPublishingToolCallingManager(defaultManager, eventPublisher);
    }
}
