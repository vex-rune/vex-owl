package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 成本优先路由策略
 * <p>从可用模型中选出 costScore 最小的那个（costScore &gt; 0 的模型才参与比较）。
 * 适用于对成本敏感的调用场景。</p>
 */
public class CostRouter implements ChatRouter {

    private static final Logger log = LoggerFactory.getLogger(CostRouter.class);

    /**
     * 执行成本优先路由
     *
     * @param models 可用模型列表
     * @param ctx    路由上下文
     * @return 成本最低的模型；无有效 costScore 时返回 null
     */
    @Override
    public AiModelEntity route(List<AiModelEntity> models, RouteContext ctx) {
        AiModelEntity cheapest = models.stream()
                .filter(m -> m.getCostScore() > 0)
                .min(Comparator.comparingDouble(AiModelEntity::getCostScore))
                .orElse(null);

        if (cheapest != null) {
            log.debug("成本最优模型: {} (costScore={})", cheapest.getModelName(), cheapest.getCostScore());
        }

        return cheapest;
    }
}
