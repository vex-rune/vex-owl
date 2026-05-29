package com.vex.owl.user.user.auth.domain.login_record;

import com.vex.owl.user.user.auth.domain.login_record.entity.LoginRecordEntity;
import com.vex.owl.user.user.auth.domain.login_record.repository.LoginRecordRepository;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginRecordManager {

    private final LoginRecordRepository loginRecordRepository;
    private final EntityManager entityManager;

    public Optional<LoginRecordEntity> findById(Long id) {
        return loginRecordRepository.findById(id);
    }

    public java.util.List<LoginRecordEntity> query(QueriesPageRequest queriesPageRequest) {
        log.debug("登录日志通用查询, request: {}", queriesPageRequest);
        return JpaQueriesExecutor.of(LoginRecordEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    public void create(LoginRecordEntity record) {
        loginRecordRepository.save(record);
    }
}