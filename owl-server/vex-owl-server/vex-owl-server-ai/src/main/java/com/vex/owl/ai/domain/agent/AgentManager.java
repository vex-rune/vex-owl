package com.vex.owl.ai.domain.agent;

import java.util.List;
import java.util.Optional;

/**
 * Agent 管理器接口
 *
 * <p>管理 Agent 的注册和获取</p>
 */
public interface AgentManager {

    /**
     * 获取所有可用的 Agent 定义
     *
     * @return Agent 定义列表
     */
    List<AgentDefinition> getAvailableAgents();

    /**
     * 根据名称获取 Agent 实例
     *
     * @param name Agent 名称
     * @return Agent 实例，不存在返回 null
     */
    Optional<Agent> getAgent(String name);

    /**
     * 根据类型获取 Agent 实例
     *
     * @param type Agent 类型
     * @return Agent 实例，不存在返回空 Optional
     */
    <T extends Agent> Optional<T> getAgent(Class<T> type);
}
