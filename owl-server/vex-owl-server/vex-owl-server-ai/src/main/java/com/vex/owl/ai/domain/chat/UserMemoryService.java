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
    public List<UserMemoryEntity> getMemories(String userId) {
        return userMemoryRepository.findByUserIdAndActiveTrueOrderByWeightDesc(userId);
    }

    /**
     * 获取指定分类的记忆
     */
    public List<UserMemoryEntity> getMemories(String userId, String category) {
        return userMemoryRepository.findByUserIdAndActiveTrueOrderByWeightDesc(userId)
                .stream()
                .filter(m -> category.equals(m.getCategory()))
                .toList();
    }

    /**
     * 添加记忆
     */
    @Transactional
    public UserMemoryEntity addMemory(String userId, String category, String content, int weight) {
        UserMemoryEntity entity = UserMemoryEntity.builder()
                .userId(userId)
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
    public void clearCategory(String userId, String category) {
        userMemoryRepository.deleteByUserIdAndCategory(userId, category);
    }
}
