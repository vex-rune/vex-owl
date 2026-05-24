package com.vex.owl.ai.domain.model.repo;

import com.vex.owl.ai.domain.model.entity.AiRoutingRuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI路由规则仓储
 * <p>管理 ai_routing_rule 表的 CRUD 操作。按优先级降序返回所有规则，
 * 供 KeywordRouter 逐条匹配使用。</p>
 */
@Repository
public interface AiRoutingRuleRepository extends JpaRepository<AiRoutingRuleEntity, String> {

    /**
     * 按优先级降序查询所有路由规则
     *
     * @return 路由规则列表，高优先级在前
     */
    List<AiRoutingRuleEntity> findAllByOrderByPriorityDesc();
}
