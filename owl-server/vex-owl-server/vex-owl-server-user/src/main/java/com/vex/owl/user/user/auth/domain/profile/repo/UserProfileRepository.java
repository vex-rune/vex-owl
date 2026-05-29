package com.vex.owl.user.user.auth.domain.profile.repo;

import com.vex.owl.user.user.auth.domain.profile.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {

    Optional<UserProfileEntity> findById(String id);

    Optional<UserProfileEntity> findByEmail(String email);
}