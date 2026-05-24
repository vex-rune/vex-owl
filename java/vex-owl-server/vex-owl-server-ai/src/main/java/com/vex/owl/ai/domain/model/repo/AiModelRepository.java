package com.vex.owl.ai.domain.model.repo;

import com.vex.owl.ai.domain.model.entity.AiModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AI模型仓储
 * <p>管理 ai_model 表的 CRUD 操作。提供按主AI和模型名称的快速查找能力。</p>
 */
@Repository
public interface AiModelRepository extends JpaRepository<AiModelEntity, String> {

    /**
     * 查询当前标记为主AI的模型
     *
     * @return 主AI模型实体
     */
    Optional<AiModelEntity> findByIsPrimaryTrue();

    /**
     * 按模型名称精确查询
     *
     * @param modelName 模型名称
     * @return 匹配的模型实体
     */
    Optional<AiModelEntity> findByModelName(String modelName);
}
