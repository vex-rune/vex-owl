package com.vex.owl.user.user.auth.domain.account.repo;

import com.vex.owl.user.user.auth.domain.account.model.AccountEntity;
import com.vex.owl.user.user.auth.domain.account.model.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    List<AccountEntity> findByAccountLowerAndAccountType(String account, AccountType accountType);
}