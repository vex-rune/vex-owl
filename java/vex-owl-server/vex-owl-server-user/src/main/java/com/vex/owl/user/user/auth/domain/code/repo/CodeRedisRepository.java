package com.vex.owl.user.user.auth.domain.code.repo;

import com.vex.owl.user.user.auth.domain.code.model.CodeEntity;
import com.vex.owl.user.user.auth.domain.code.model.CodeId;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CodeRedisRepository extends CrudRepository<CodeEntity, CodeId> {
    Optional<CodeEntity> findByIdAndCode(CodeId id, String code);
}