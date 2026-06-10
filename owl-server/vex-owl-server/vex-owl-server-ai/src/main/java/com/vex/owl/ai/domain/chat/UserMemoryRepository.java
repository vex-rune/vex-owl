package com.vex.owl.ai.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMemoryRepository extends JpaRepository<UserMemoryEntity, String> {

    List<UserMemoryEntity> findByUserIdAndActiveTrueOrderByWeightDesc(String userId);

    void deleteByUserIdAndCategory(String userId, String category);
}
