package com.vex.owl.ai.domain.model.router;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.entity.AiRoutingRuleEntity;
import com.vex.owl.ai.domain.model.repo.AiRoutingRuleRepository;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 关键词匹配路由策略
 * <p>从数据库加载所有路由规则，按优先级降序逐条匹配用户消息中的关键词。
 * 命中后路由到规则指定模型。关键词支持逗号分隔的多词匹配。</p>
 *
 * <p>匹配规则：消息中包含任一关键词即命中</p>
 */
public class KeywordRouter implements ChatRouter {

    private static final Logger log = LoggerFactory.getLogger(KeywordRouter.class);

    /** 路由规则仓储，用于加载关键词→模型的映射规则 */
    private final AiRoutingRuleRepository ruleRepository;

    /**
     * @param ruleRepository 路由规则仓储
     */
    public KeywordRouter(AiRoutingRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * 执行关键词匹配路由
     *
     * @param models 可用模型列表
     * @param ctx    路由上下文，从中提取用户消息进行关键词匹配
     * @return 命中的模型；无匹配时返回 null
     */
    @Override
    public AiModelEntity route(List<AiModelEntity> models, RouteContext ctx) {
        List<AiRoutingRuleEntity> rules = ruleRepository.findAllByOrderByPriorityDesc();

        for (AiRoutingRuleEntity rule : rules) {
            if (containsKeyword(ctx.getUserMessage(), rule.getKeywords())) {
                return models.stream()
                        .filter(m -> m.getId().equals(rule.getTargetModelId()))
                        .findFirst()
                        .orElse(null);
            }
        }

        log.debug("无关键词匹配，跳过关键词路由");
        return null;
    }

    /**
     * 判断消息中是否包含任意一个关键词
     *
     * @param message  用户消息
     * @param keywords 逗号分隔的关键词列表
     * @return 命中任一关键词返回 true
     */
    private boolean containsKeyword(String message, String keywords) {
        if (message == null || keywords == null) {
            return false;
        }
        for (String keyword : keywords.split(",")) {
            if (message.contains(keyword.trim())) {
                log.debug("关键词匹配成功: {}", keyword.trim());
                return true;
            }
        }
        return false;
    }
}
