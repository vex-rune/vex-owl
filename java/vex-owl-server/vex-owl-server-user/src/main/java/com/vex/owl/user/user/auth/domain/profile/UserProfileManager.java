package com.vex.owl.user.user.auth.domain.profile;

import com.vex.owl.user.user.auth.domain.profile.entity.UserProfileEntity;
import com.vex.owl.user.user.auth.domain.profile.repo.UserProfileRepository;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileManager {

    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;

    @Transactional
    public UserProfileEntity create(String userId, String nickname, String email) {
        UserProfileEntity userProfile = new UserProfileEntity();
        userProfile.setId(userId);
        userProfile.setNickname(nickname != null ? nickname : "新用户");
        userProfile.setEmail(email);
        log.info("创建用户档案, userId: {}", userId);
        return userProfileRepository.save(userProfile);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileEntity> findById(String userId) {
        return userProfileRepository.findById(userId);
    }

    @Transactional
    public void delete(String userId) {
        UserProfileEntity userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户档案不存在: " + userId));
        userProfileRepository.delete(userProfile);
        log.info("删除用户档案, userId: {}", userId);
    }

    public List<UserProfileEntity> query(QueriesPageRequest request) {
        log.debug("用户档案通用查询, request: {}", request);
        return JpaQueriesExecutor.of(UserProfileEntity.class, entityManager)
                .page(request);
    }
}