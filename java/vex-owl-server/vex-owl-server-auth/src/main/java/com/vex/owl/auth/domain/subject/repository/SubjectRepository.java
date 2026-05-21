package com.vex.owl.auth.domain.subject.repository;

import com.vex.owl.auth.domain.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 主体仓储接口
 */
public interface SubjectRepository extends JpaRepository<SubjectEntity, String> {

    /**
     * 根据邮箱查询主体
     *
     * @param email 邮箱
     * @return 主体信息
     */
    Optional<SubjectEntity> findByEmail(String email);
}