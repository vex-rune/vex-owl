package com.vex.owl.auth.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 账号仓储接口
 */
public interface AccountRepository extends JpaRepository<AccountBasicWithIdEntity, Long> {

    AccountBasicWithIdEntity findById(Long id);

    AccountBasicWithIdEntity findBySubjectIdAndType(Long subjectId, String accountType);

    void save(AccountBasicWithIdEntity account);

    boolean existsActiveAccount(Long subjectId, String accountType);
}