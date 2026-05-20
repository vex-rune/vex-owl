package com.vex.owl.auth.domain.account;

import com.vex.query.criteria.QueriesPageRequest;
import com.vex.query.criteria.QueriesPredicate;
import com.vex.query.criteria.jpa.JpaQueriesExecutor;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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
    public AccountBasicWithIdEntity findById(Long id) {
        return accountRepository.findById(id);
    }

    /**
     * 根据主体ID和类型查询账号
     *
     * @param subjectId   主体ID
     * @param accountType 账号类型
     * @return 账号信息
     */
    public AccountBasicWithIdEntity findBySubjectIdAndType(Long subjectId, String accountType) {
        return accountRepository.findBySubjectIdAndType(subjectId, accountType);
    }

    /**
     * 条件查询账号（使用CriteriaQueryBuilder）
     *
     * @param queriesPageRequest 查询条件
     * @return 账号分页列表
     */
    public List<AccountBasicWithIdEntity> query(QueriesPageRequest queriesPageRequest) {
        return JpaQueriesExecutor.of(AccountBasicWithIdEntity.class, entityManager)
                .page(queriesPageRequest);
    }

    /**
     * 条件计数查询
     *
     * @param queriesPredicate 查询条件
     * @return 符合条件的记录总数
     */
    public long count(QueriesPredicate queriesPredicate) {
        return JpaQueriesExecutor.of(AccountBasicWithIdEntity.class, entityManager)
                .count(queriesPredicate);
    }

    /**
     * 创建账号
     *
     * @param account 账号信息
     */
    public void create(AccountBasicWithIdEntity account) {
        accountRepository.save(account);
    }

    /**
     * 更新账号
     *
     * @param account 账号信息
     */
    public void update(AccountBasicWithIdEntity account) {
        accountRepository.save(account);
    }

    /**
     * 检查是否存在有效的账号
     *
     * @param subjectId   主体ID
     * @param accountType 账号类型
     * @return 是否存在
     */
    public boolean existsActiveAccount(Long subjectId, String accountType) {
        return accountRepository.existsActiveAccount(subjectId, accountType);
    }
}