package com.vex.owl.ai.domain.tools;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;

/**
 * Agent Advisor 接口
 *
 * <p>同时支持同步（CallAdvisor）和流式（StreamAdvisor）两种模式。</p>
 */
public interface AgentAdvisor extends CallAdvisor, StreamAdvisor {

    default Advisor toAdvisor() {
        return this;
    }
}
