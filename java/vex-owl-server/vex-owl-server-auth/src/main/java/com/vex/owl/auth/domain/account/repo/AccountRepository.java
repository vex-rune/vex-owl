package com.vex.owl.auth.domain.account.repo;

import com.vex.owl.auth.domain.account.model.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    Optional<AccountEntity> findByAccountLowerAndAccountType(String account, String accountType);
}