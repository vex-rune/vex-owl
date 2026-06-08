package com.vex.owl.ai.domain.chat;

import com.vex.owl.ai.SpringIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserMemoryServiceTest extends SpringIntegrationTest {

    @Autowired
    private UserMemoryService userMemoryService;

    @Test
    void addMemory_shouldPersist() {
        UserMemoryEntity memory = userMemoryService.addMemory(
                "tenant-1", "preference", "用户喜欢简洁回答", 80);

        assertThat(memory.getId()).isNotBlank();
        assertThat(memory.getTenantId()).isEqualTo("tenant-1");
        assertThat(memory.getCategory()).isEqualTo("preference");
        assertThat(memory.getContent()).isEqualTo("用户喜欢简洁回答");
        assertThat(memory.getWeight()).isEqualTo(80);
        assertThat(memory.getActive()).isTrue();
    }

    @Test
    void getMemories_shouldReturnByWeightDesc() {
        userMemoryService.addMemory("tenant-1", "preference", "低权重", 30);
        userMemoryService.addMemory("tenant-1", "fact", "高权重", 90);
        userMemoryService.addMemory("tenant-1", "context", "中权重", 60);

        List<UserMemoryEntity> memories = userMemoryService.getMemories("tenant-1");

        assertThat(memories).hasSize(3);
        assertThat(memories.get(0).getWeight()).isEqualTo(90);
        assertThat(memories.get(1).getWeight()).isEqualTo(60);
        assertThat(memories.get(2).getWeight()).isEqualTo(30);
    }

    @Test
    void getMemories_byCategory_shouldFilter() {
        userMemoryService.addMemory("tenant-1", "preference", "偏好1", 80);
        userMemoryService.addMemory("tenant-1", "fact", "事实1", 90);
        userMemoryService.addMemory("tenant-1", "preference", "偏好2", 70);

        List<UserMemoryEntity> preferences = userMemoryService.getMemories("tenant-1", "preference");

        assertThat(preferences).hasSize(2);
        assertThat(preferences).allMatch(m -> "preference".equals(m.getCategory()));
    }

    @Test
    void getMemories_shouldNotReturnInactive() {
        userMemoryService.addMemory("tenant-1", "preference", "有效记忆", 80);

        List<UserMemoryEntity> before = userMemoryService.getMemories("tenant-1");
        assertThat(before).hasSize(1);
    }

    @Test
    void clearCategory_shouldDeleteAll() {
        userMemoryService.addMemory("tenant-1", "preference", "偏好1", 80);
        userMemoryService.addMemory("tenant-1", "preference", "偏好2", 70);
        userMemoryService.addMemory("tenant-1", "fact", "事实1", 90);

        userMemoryService.clearCategory("tenant-1", "preference");

        List<UserMemoryEntity> remaining = userMemoryService.getMemories("tenant-1");
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getCategory()).isEqualTo("fact");
    }

    @Test
    void getMemories_should隔离tenant() {
        userMemoryService.addMemory("tenant-1", "preference", "租户1记忆", 80);
        userMemoryService.addMemory("tenant-2", "preference", "租户2记忆", 80);

        List<UserMemoryEntity> tenant1 = userMemoryService.getMemories("tenant-1");
        List<UserMemoryEntity> tenant2 = userMemoryService.getMemories("tenant-2");

        assertThat(tenant1).hasSize(1);
        assertThat(tenant1.get(0).getContent()).isEqualTo("租户1记忆");
        assertThat(tenant2).hasSize(1);
        assertThat(tenant2.get(0).getContent()).isEqualTo("租户2记忆");
    }
}
