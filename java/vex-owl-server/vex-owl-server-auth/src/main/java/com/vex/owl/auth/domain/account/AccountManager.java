package com.vex.owl.auth.domain.account;

import com.vex.owl.auth.domain.account.model.AccountCreate;
import com.vex.owl.auth.domain.account.model.AccountEntity;
import com.vex.owl.auth.domain.account.model.AccountId;
import com.vex.owl.auth.domain.account.model.AccountUpdate;
import com.vex.owl.auth.domain.account.repo.AccountRepository;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import com.vex.queries.model.queries.model.QueriesPredicate;
import com.vex.queries.jpa.queries.JpaQueriesExecutor;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 账号管理
 * 负责账号信息的查询和管理
 */
@Component
@RequiredArgsConstructor
public class AccountManager {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;


    /**
     * 根据ID查询账号
     *
     * @param id 账号ID
     * @return 账号信息
     */
    public Optional<AccountEntity> findById(AccountId id) {
        return accountRepository.findById(id);
    }

    /**
     * 条件查询账号（使用CriteriaQueryBuilder）
     *
     * @param queriesPageRequest 查询条件
     * @return 账号分页列表
     */
    public List<AccountEntity> query(QueriesPageRequest queriesPageRequest) {
        return JpaQueriesExecutor.of(AccountEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    /**
     * 条件计数查询
     *
     * @param queriesPredicate 查询条件
     * @return 符合条件的记录总数
     */
    public long count(QueriesPredicate queriesPredicate) {
        return JpaQueriesExecutor.of(AccountEntity.class, entityManager)
                .count(queriesPredicate);
    }

    /**
     * 创建账号
     *
     * @param account 账号信息
     */
    public void create(AccountCreate account) {
        // 生成随机盐值并加密密码
        String salt = PasswordEncoder.generateSalt();
        String rawPassword = account.password().get();
        String encryptedPassword = PasswordEncoder.encrypt(rawPassword, salt);
        
        AccountEntity entity = AccountEntity.builder()
                .id(new AccountId(account.subjectId(), account.accountType()))
                .account(account.account())
                .credential(encryptedPassword)
                .salt(salt)
                .build();
        accountRepository.save(entity);
    }

    /**
     * 更新账号
     *
     * @param accountUpdate 账号更新信息
     */
    public void update(AccountUpdate accountUpdate) {
        // 查询现有账号
        AccountEntity existingAccount = accountRepository.findById(accountUpdate.id())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
        
        // 构建更新后的实体
        AccountEntity.AccountEntityBuilder builder = AccountEntity.builder()
                .id(existingAccount.getId())
                .salt(existingAccount.getSalt()); // 保留原有盐值
        
        // 更新账号（如果提供）
        if (accountUpdate.account() != null) {
            builder.account(accountUpdate.account());
        } else {
            builder.account(existingAccount.getAccount());
        }
        
        // 更新密码（如果提供）
        if (accountUpdate.password() != null) {
            String rawPassword = accountUpdate.password().get();
            String encryptedPassword = PasswordEncoder.encrypt(rawPassword, existingAccount.getSalt());
            builder.credential(encryptedPassword);
        } else {
            builder.credential(existingAccount.getCredential());
        }
        
        AccountEntity updatedAccount = builder.build();
        accountRepository.save(updatedAccount);
    }

    /**
     * 检查密码
     *
     * @param id       账号ID
     * @param password 待验证的密码
     * @return 密码是否正确
     */
    public boolean checkPassword(AccountId id, String password) {
        // 查询账号信息
        AccountEntity account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));
            
        // 使用PasswordEncoder验证密码
        return PasswordEncoder.matches(password, account.getSalt(), account.getCredential());
    }

}