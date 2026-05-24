package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.service.RouteContext;

import java.util.List;

/**
 * 聊天路由策略接口
 * <p>定义从模型列表中选用一个模型的策略契约。
 * 每个实现类代表一种路由算法（主AI优先、关键词匹配、成本优先等）。</p>
 */
public interface ChatRouter {

    /**
     * 执行路由决策
     *
     * @param models 可用模型列表
     * @param ctx    路由上下文，包含用户消息等辅助信息
     * @return 选中的模型；未命中时返回 null
     */
    AiModelEntity route(List<AiModelEntity> models, RouteContext ctx);
}
