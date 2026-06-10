//package com.vex.owl.ai.domain.tools;
//
//import com.vex.owl.ai.SpringIntegrationTest;
//import com.vex.owl.ai.domain.chat.ChatManager;
//import com.vex.owl.ai.domain.chat.ChatMessageEntity;
//import com.vex.owl.ai.domain.chat.UserMemoryService;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.lang.reflect.InvocationTargetException;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class MemoryAdvisorTest extends SpringIntegrationTest {
//
//    @Autowired
//    private MemoryAdvisor memoryAdvisor;
//
//    @Autowired
//    private UserMemoryService userMemoryService;
//
//    @Autowired
//    private ChatManager chatManager;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @AfterEach
//    void cleanup() {
//        jdbcTemplate.execute("DELETE FROM ai_chat_message");
//        jdbcTemplate.execute("DELETE FROM ai_chat_session");
//        jdbcTemplate.execute("DELETE FROM ai_user_memory");
//    }
//
//    private String invokeBuildEnrichedPrompt(String userId, String sessionId, String prompt) {
//        try {
//            var method = MemoryAdvisor.class.getDeclaredMethod("buildEnrichedPrompt", String.class, String.class, String.class);
//            method.setAccessible(true);
//            return (String) method.invoke(memoryAdvisor, userId, sessionId, prompt);
//        } catch (InvocationTargetException e) {
//            throw new RuntimeException("buildEnrichedPrompt failed: " + e.getCause().getMessage(), e.getCause());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Test
//    void buildEnrichedPrompt_shouldInjectUserMemoryAndHistory() {
//        String userId = "advisor-tenant-1";
//        String sessionId = "advisor-session-1";
//
//        userMemoryService.addMemory(userId, "preference", "用户喜欢简洁回答", 90);
//        userMemoryService.addMemory(userId, "fact", "用户是程序员", 80);
//
//        chatManager.createSession(userId, "测试");
//        chatManager.saveMessages(List.of(
//                ChatMessageEntity.builder().userId(userId).conversationId(sessionId).messageType("USER").textContent("之前的问题").build(),
//                ChatMessageEntity.builder().userId(userId).conversationId(sessionId).messageType("ASSISTANT").textContent("之前的回答").build()
//        ));
//
//        String result = invokeBuildEnrichedPrompt(userId, sessionId, "你好");
//
//        assertThat(result).contains("用户喜欢简洁回答");
//        assertThat(result).contains("用户是程序员");
//        assertThat(result).contains("之前的问题");
//        assertThat(result).contains("你好");
//    }
//
//    @Test
//    void buildEnrichedPrompt_shouldHandleEmptyMemory() {
//        String result = invokeBuildEnrichedPrompt("empty-tenant", "empty-session", "你好");
//
//        assertThat(result).contains("你好");
//    }
//
//    @Test
//    void getName_shouldReturnMemoryAdvisor() {
//        assertThat(memoryAdvisor.getName()).isEqualTo("MemoryAdvisor");
//    }
//
//    @Test
//    void getOrder_shouldBeZero() {
//        assertThat(memoryAdvisor.getOrder()).isEqualTo(0);
//    }
//
//    @Test
//    void buildEnrichedPrompt_shouldLimitHistory() {
//        String userId = "advisor-limit-tenant";
//        String sessionId = "advisor-limit-session";
//
//        chatManager.createSession(userId, "测试");
//        for (int i = 0; i < 30; i++) {
//            chatManager.saveMessages(List.of(
//                    ChatMessageEntity.builder().userId(userId).conversationId(sessionId).messageType("USER").textContent("消息" + i).build()
//            ));
//        }
//
//        String result = invokeBuildEnrichedPrompt(userId, sessionId, "新消息");
//
//        assertThat(result).contains("消息29");
//        assertThat(result).doesNotContain("消息0");
//        assertThat(result).contains("新消息");
//    }
//}
