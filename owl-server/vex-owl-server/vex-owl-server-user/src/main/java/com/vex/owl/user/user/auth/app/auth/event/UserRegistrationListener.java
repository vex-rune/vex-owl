package com.vex.owl.user.user.auth.app.auth.event;

import com.vex.owl.user.user.auth.domain.profile.UserProfileManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationListener {

    private final UserProfileManager userProfileManager;

    @Async("userSyncExecutor")
    @EventListener
    @Retryable(
            retryFor = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void handleUserRegistration(UserRegistrationEvent event) {
        log.info("收到用户注册事件, userId: {}, email: {}, nickname: {}",
                event.getUserId(), event.getEmail(), event.getNickname());

        try {
            userProfileManager.create(
                    event.getUserId(),
                    event.getNickname(),
                    event.getEmail()
            );
            log.info("用户档案同步成功, userId: {}", event.getUserId());
        } catch (Exception e) {
            log.error("用户档案同步失败, userId: {}, error: {}",
                    event.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @Recover
    public void recover(Exception e, UserRegistrationEvent event) {
        log.error("用户档案同步最终失败, userId: {}, email: {}, nickname: {}, error: {}",
                event.getUserId(), event.getEmail(), event.getNickname(), e.getMessage(), e);
    }
}