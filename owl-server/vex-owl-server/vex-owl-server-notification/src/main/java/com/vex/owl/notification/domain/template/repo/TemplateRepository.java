package com.vex.owl.notification.domain.template.repo;

import com.vex.owl.notification.domain.template.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, String> {

    Optional<TemplateEntity> findByCode(String code);

    boolean existsByCode(String code);
}