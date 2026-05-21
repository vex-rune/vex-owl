package com.vex.owl.auth.domain.account.repo;

import com.vex.owl.auth.domain.account.model.AccountEntity;
import com.vex.owl.auth.domain.account.model.AccountId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 账号仓储接口
 */
public interface AccountRepository extends JpaRepository<AccountEntity, AccountId> {
}