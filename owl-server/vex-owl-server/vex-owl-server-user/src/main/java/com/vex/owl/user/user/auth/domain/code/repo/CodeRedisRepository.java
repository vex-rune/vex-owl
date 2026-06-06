package com.vex.owl.user.user.auth.domain.code.repo;

import com.vex.owl.user.user.auth.domain.code.model.CodeEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CodeRedisRepository extends CrudRepository<CodeEntity, String> {
    Optional<CodeEntity> findByIdAndCode(String id, String code);
}