package com.vex.owl.ai.domain.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户长期记忆服务
 */
@Service
@RequiredArgsConstructor
public class UserMemoryService {

    private final UserMemoryRepository userMemoryRepository;

    /**
     * 获取用户的所有有效记忆（按权重降序）
     */
    public List<UserMemoryEntity> getMemories(String tenantId) {
        return userMemoryRepository.findByTenantIdAndActiveTrueOrderByWeightDesc(tenantId);
    }

    /**
     * 获取指定分类的记忆
     */
    public List<UserMemoryEntity> getMemories(String tenantId, String category) {
        return userMemoryRepository.findByTenantIdAndActiveTrueOrderByWeightDesc(tenantId)
                .stream()
                .filter(m -> category.equals(m.getCategory()))
                .toList();
    }

    /**
     * 添加记忆
     */
    @Transactional
    public UserMemoryEntity addMemory(String tenantId, String category, String content, int weight) {
        UserMemoryEntity entity = UserMemoryEntity.builder()
                .tenantId(tenantId)
                .category(category)
                .content(content)
                .weight(weight)
                .active(true)
                .build();
        return userMemoryRepository.save(entity);
    }

    /**
     * 清除指定分类的记忆
     */
    @Transactional
    public void clearCategory(String tenantId, String category) {
        userMemoryRepository.deleteByTenantIdAndCategory(tenantId, category);
    }
}
