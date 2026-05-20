package com.vex.owl.auth.api;

import com.vex.owl.auth.api.response.ApiResponse;
import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.query.criteria.QueriesCriteria;

/**
 * 账号管理接口
 * 提供账号信息的查询功能
 */
public class AccountManagerApi {

    private final AccountManager accountManager;

    public AccountManagerApi(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * 根据ID查询账号
     */
    public ApiResponse<Object> getById(Long id) {
        var account = accountManager.findById(id);
        if (account == null) {
            return ApiResponse.error("账号不存在");
        }
        return ApiResponse.success(account);
    }

    /**
     * 条件查询账号（使用CriteriaQueryBuilder）
     */
    public ApiResponse<Object> query(QueriesCriteria criteria) {
        var accounts = accountManager.query(criteria);
        return ApiResponse.success(accounts);
    }

    /**
     * 分页查询账号
     */
    public ApiResponse<Object> queryByPage(int page, int size, Long subjectId) {
        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setPage(page);
        criteria.setSize(size);
        var accounts = accountManager.query(criteria);
        return ApiResponse.success(accounts);
    }
}