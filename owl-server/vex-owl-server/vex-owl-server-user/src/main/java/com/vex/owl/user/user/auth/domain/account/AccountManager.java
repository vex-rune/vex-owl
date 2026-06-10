package com.vex.owl.user.user.auth.domain.account;

import com.vex.owl.user.user.auth.domain.account.model.AccountCreate;
import com.vex.owl.user.user.auth.domain.account.model.AccountEntity;
import com.vex.owl.user.user.auth.domain.account.model.AccountType;
import com.vex.owl.user.user.auth.domain.account.model.AccountUpdate;
import com.vex.owl.user.user.auth.domain.account.repo.AccountRepository;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import com.vex.queries.model.queries.model.QueriesPredicate;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountManager {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;

    public Optional<AccountEntity> findById(String id) {
        return accountRepository.findById(id);
    }

    public List<AccountEntity> query(QueriesPageRequest queriesPageRequest) {
        log.debug("账号通用查询, request: {}", queriesPageRequest);
        return JpaQueriesExecutor.of(AccountEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    public long count(QueriesPredicate queriesPredicate) {
        return JpaQueriesExecutor.of(AccountEntity.class, entityManager)
                .count(queriesPredicate);
    }

    public void create(AccountCreate account) {
        String salt = PasswordEncoder.generateSalt();
        String rawPassword = account.password().get();
        String encryptedPassword = PasswordEncoder.encrypt(rawPassword, salt);

        AccountEntity entity = AccountEntity.builder()
                .subjectId(account.subjectId())
                .accountType(account.accountType())
                .account(account.account())
                .accountLower(account.account().toLowerCase())
                .credential(encryptedPassword)
                .salt(salt)
                .build();
        log.debug("创建账号, subjectId: {}, account: {}", account.subjectId(), account.account());
        accountRepository.save(entity);
    }

    public void update(AccountUpdate accountUpdate) {
        AccountEntity existingAccount = accountRepository.findById(accountUpdate.id())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));

        AccountEntity.AccountEntityBuilder builder = AccountEntity.builder()
                .id(existingAccount.getId())
                .subjectId(existingAccount.getSubjectId())
                .accountType(existingAccount.getAccountType())
                .salt(existingAccount.getSalt());

        if (accountUpdate.account() != null) {
            builder.account(accountUpdate.account())
                    .accountLower(accountUpdate.account().toLowerCase());
        } else {
            builder.account(existingAccount.getAccount())
                    .accountLower(existingAccount.getAccountLower());
        }

        if (accountUpdate.password() != null) {
            String rawPassword = accountUpdate.password().get();
            String encryptedPassword = PasswordEncoder.encrypt(rawPassword, existingAccount.getSalt());
            builder.credential(encryptedPassword);
        } else {
            builder.credential(existingAccount.getCredential());
        }

        log.debug("更新账号, accountId: {}", accountUpdate.id());
        accountRepository.save(builder.build());
    }

    public boolean checkPassword(String id, String password) {
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        return PasswordEncoder.matches(password, account.getSalt(), account.getCredential());
    }

    public Optional<AccountEntity> validByAccount(AccountType type, String account) {
        return accountRepository.findByAccountLowerAndAccountType(account.toLowerCase(), type).stream().findFirst();
    }
}