package com.vex.owl.auth.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 账号仓储接口
 */
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    AccountEntity findById(Long id);

    AccountEntity findBySubjectIdAndType(Long subjectId, String accountType);

    void save(AccountEntity account);

    boolean existsActiveAccount(Long subjectId, String accountType);
}