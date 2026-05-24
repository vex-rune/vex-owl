package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

/**
 * 主AI优先路由策略
 * <p>优先使用 {@code isPrimary=true} 的模型；若主AI不存在，
 * 则回退到 {@code isDefault=true} 的默认模型，最终兜底选择最高优先级的模型。</p>
 *
 * <p>回退链：主AI → 默认模型 → 最高优先级（priority 最小）</p>
 */
public class PrimaryRouter implements ChatRouter {

    private static final Logger log = LoggerFactory.getLogger(PrimaryRouter.class);

    /**
     * 执行主AI优先路由
     *
     * @param models 可用模型列表
     * @param ctx    路由上下文
     * @return 选中的模型
     */
    @Override
    public AiModelEntity route(List<AiModelEntity> models, RouteContext ctx) {
        AiModelEntity primary = models.stream()
                .filter(AiModelEntity::isPrimary)
                .findFirst()
                .orElse(null);

        if (primary == null) {
            log.warn("主AI不可用，回退到默认模型");
            return models.stream()
                    .filter(AiModelEntity::isDefault)
                    .findFirst()
                    .orElseGet(() -> fallbackToHighestPriority(models));
        }

        return primary;
    }

    /**
     * 兜底策略：选择优先级最高的模型
     *
     * @param models 可用模型列表
     * @return 最高优先级模型，priority 最小者胜出
     */
    private AiModelEntity fallbackToHighestPriority(List<AiModelEntity> models) {
        return models.stream()
                .min(Comparator.comparingInt(AiModelEntity::getPriority))
                .orElse(null);
    }
}
