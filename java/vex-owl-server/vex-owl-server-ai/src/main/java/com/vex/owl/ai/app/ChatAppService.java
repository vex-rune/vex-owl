package com.vex.owl.ai.app;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import com.vex.owl.ai.domain.model.factory.AiChatModelProductFactory;
import com.vex.owl.ai.domain.model.factory.AbstractAiChatModelFactory;
import com.vex.owl.ai.domain.model.repo.AiModelRepository;
import com.vex.owl.ai.domain.model.repo.AiRoutingRuleRepository;
import com.vex.owl.ai.domain.model.router.ChatRouter;
import com.vex.owl.ai.domain.model.router.CostRouter;
import com.vex.owl.ai.domain.model.router.KeywordRouter;
import com.vex.owl.ai.domain.model.router.PrimaryRouter;
import com.vex.owl.ai.domain.model.service.RouteContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

/**
 * 聊天应用服务
 * <p>编排完整的 AI 对话调用链：加载模型 → 路由决策 → 获取 Provider → 调用大模型 → 返回结果。</p>
 *
 * <p>路由链优先级：PrimaryRouter → KeywordRouter → CostRouter</p>
 */
public class ChatAppService {

    private static final Logger log = LoggerFactory.getLogger(ChatAppService.class);

    /** 模型仓储，加载全量可用模型 */
    private final AiModelRepository modelRepository;

    /** Provider 工厂，根据 providerCode 获取对应厂商的实现 */
    private final AiChatModelProductFactory productFactory;

    /** 路由规则仓储，供 KeywordRouter 使用；不启用关键词路由时可传入 null */
    private final AiRoutingRuleRepository ruleRepository;

    /**
     * 构造服务（不含关键词路由）
     *
     * @param modelRepository 模型仓储
     */
    public ChatAppService(AiModelRepository modelRepository) {
        this.modelRepository = modelRepository;
        this.productFactory = new AiChatModelProductFactory();
        this.ruleRepository = null;
    }

    /**
     * 构造服务（含关键词路由）
     *
     * @param modelRepository 模型仓储
     * @param ruleRepository  路由规则仓储
     */
    public ChatAppService(AiModelRepository modelRepository,
                          AiRoutingRuleRepository ruleRepository) {
        this.modelRepository = modelRepository;
        this.productFactory = new AiChatModelProductFactory();
        this.ruleRepository = ruleRepository;
    }

    /**
     * 发起一次 AI 对话
     *
     * @param userMessage 用户消息
     * @return AI 回复文本
     */
    public String chat(String userMessage) {
        List<AiModelEntity> models = modelRepository.findAll();
        if (models.isEmpty()) {
            log.warn("无可用AI模型");
            return "服务暂不可用，请稍后再试";
        }

        AiModelEntity primary = models.stream()
                .filter(AiModelEntity::isPrimary)
                .findFirst()
                .orElse(null);

        String primaryModelId = primary != null ? primary.getId() : null;
        RouteContext ctx = new RouteContext(userMessage, primaryModelId);

        AiModelEntity selected = runRouterChain(models, ctx);

        if (selected == null) {
            log.warn("路由未匹配到任何模型");
            return "未找到合适的AI模型处理您的请求";
        }

        log.info("路由结果: provider={}, model={}", selected.getProviderCode(), selected.getModelName());

        AbstractAiChatModelFactory provider = productFactory.get(selected.getProviderCode());
        if (provider == null) {
            log.warn("Provider '{}' 未注册", selected.getProviderCode());
            return "模型提供商暂不可用";
        }

        ChatClient client = provider.createClient(selected);
        return client.prompt(userMessage).call().content();
    }

    /**
     * 按优先级依次执行路由链
     * <p>路由链：PrimaryRouter → KeywordRouter → CostRouter。
     * 任一策略命中即返回，都不命中则返回 CostRouter 的结果。</p>
     *
     * @param models 可用模型列表
     * @param ctx    路由上下文
     * @return 选中的模型
     */
    private AiModelEntity runRouterChain(List<AiModelEntity> models, RouteContext ctx) {
        ChatRouter primaryRouter = new PrimaryRouter();
        AiModelEntity result = primaryRouter.route(models, ctx);
        if (result != null) {
            return result;
        }

        if (ruleRepository != null) {
            ChatRouter keywordRouter = new KeywordRouter(ruleRepository);
            result = keywordRouter.route(models, ctx);
            if (result != null) {
                return result;
            }
        }

        ChatRouter costRouter = new CostRouter();
        return costRouter.route(models, ctx);
    }
}
