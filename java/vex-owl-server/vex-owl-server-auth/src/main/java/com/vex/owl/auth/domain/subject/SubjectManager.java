package com.vex.owl.auth.domain.subject;

import com.vex.owl.auth.domain.subject.entity.SubjectEntity;
import com.vex.owl.auth.domain.subject.repository.SubjectRepository;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 主体管理
 * 负责主体信息的查询和管理
 */
@Component
@RequiredArgsConstructor
public class SubjectManager {

    private final SubjectRepository subjectRepository;
    private final EntityManager entityManager;

    /**
     * 根据ID查询主体
     *
     * @param id 主体ID
     * @return 主体信息
     */
    public Optional<SubjectEntity> findById(String  id) {
        return subjectRepository.findById(id);
    }

    /**
     * 根据邮箱查询主体
     *
     * @param email 邮箱
     * @return 主体信息
     */
    public Optional<SubjectEntity> findByEmail(String email) {
        return subjectRepository.findByEmail(email);
    }

    /**
     * 条件查询主体（使用CriteriaQueryBuilder）
     *
     * @param queriesPageRequest 查询条件
     * @return 主体分页列表
     */
    public java.util.List<SubjectEntity> query(QueriesPageRequest queriesPageRequest) {
        return JpaQueriesExecutor.of(SubjectEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    /**
     * 创建主体
     *
     * @param subject 主体信息
     */
    public void create(SubjectEntity subject) {
        subjectRepository.save(subject);
    }

    /**
     * 更新主体
     *
     * @param subject 主体信息
     */
    public void update(SubjectEntity subject) {
        subjectRepository.save(subject);
    }

    /**
     * 删除主体
     *
     * @param id 主体ID
     */
    public void delete(String id) {
        subjectRepository.deleteById(id);
    }

}