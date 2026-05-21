package com.vex.owl.auth.domain.login_record;

import com.vex.owl.auth.domain.login_record.entity.LoginRecordEntity;
import com.vex.owl.auth.domain.login_record.repository.LoginRecordRepository;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 登录日志管理
 * 负责登录日志的记录和查询
 */
@Component
@RequiredArgsConstructor
public class LoginRecordManager {

    private final LoginRecordRepository loginRecordRepository;
    private final EntityManager entityManager;

    /**
     * 根据ID查询登录日志
     * @param id 日志ID
     * @return 登录日志
     */
    public Optional<LoginRecordEntity> findById(Long id) {
        return loginRecordRepository.findById(id);
    }

    /**
     * 条件查询登录日志（使用CriteriaQueryBuilder）
     * @param queriesPageRequest 查询条件
     * @return 登录日志分页列表
     */
    public java.util.List<LoginRecordEntity> query(QueriesPageRequest queriesPageRequest) {
        return JpaQueriesExecutor.of(LoginRecordEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    /**
     * 记录登录日志
     * @param record 登录日志
     */
    public void create(LoginRecordEntity record) {
        loginRecordRepository.save(record);
    }
}