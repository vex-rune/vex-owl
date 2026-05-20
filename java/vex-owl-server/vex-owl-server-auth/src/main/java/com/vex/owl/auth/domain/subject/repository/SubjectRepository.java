package com.vex.owl.auth.domain.subject.repository;

import com.vex.owl.auth.domain.subject.entity.Subject;

/**
 * 主体仓储接口
 */
public interface SubjectRepository {

    Subject findById(Long id);

    Subject findByEmail(String email);

    boolean existsByEmail(String email);

    void save(Subject subject);

    void delete(Subject subject);
}