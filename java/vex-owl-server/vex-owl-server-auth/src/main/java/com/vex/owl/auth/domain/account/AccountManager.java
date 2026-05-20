package com.vex.owl.auth.domain.account;

import com.vex.query.criteria.QueriesCriteria;
import com.vex.query.criteria.jpa.CriteriaQueryBuilder;

/**
 * 账号管理
 * 负责账号信息的查询和管理
 */
public class AccountManager {

    private final AccountRepository accountRepository;

    public AccountManager(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * 根据ID查询账号
     * @param id 账号ID
     * @return 账号信息
     */
    public AccountEntity findById(Long id) {
        return accountRepository.findById(id);
    }

    /**
     * 根据主体ID和类型查询账号
     * @param subjectId 主体ID
     * @param accountType 账号类型
     * @return 账号信息
     */
    public AccountEntity findBySubjectIdAndType(Long subjectId, String accountType) {
        return accountRepository.findBySubjectIdAndType(subjectId, accountType);
    }

    /**
     * 条件查询账号（使用CriteriaQueryBuilder）
     * @param criteria 查询条件
     * @return 账号分页列表
     */
    public java.util.List<AccountEntity> query(QueriesCriteria criteria) {
        CriteriaQueryBuilder.buildQuery(AccountEntity.class
        ,  criteria, criteria.getPredicate());

        accountRepository.findAll( criteria.getCondition())
        return java.util.Collections.emptyList();
    }

    /**
     * 创建账号
     * @param account 账号信息
     */
    public void create(AccountEntity account) {
        accountRepository.save(account);
    }

    /**
     * 更新账号
     * @param account 账号信息
     */
    public void update(AccountEntity account) {
        accountRepository.save(account);
    }

    /**
     * 检查是否存在有效的账号
     * @param subjectId 主体ID
     * @param accountType 账号类型
     * @return 是否存在
     */
    public boolean existsActiveAccount(Long subjectId, String accountType) {
        return accountRepository.existsActiveAccount(subjectId, accountType);
    }
}