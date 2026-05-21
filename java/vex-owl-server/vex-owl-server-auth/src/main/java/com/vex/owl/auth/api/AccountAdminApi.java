package com.vex.owl.auth.api;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.queries.model.queries.model.QueriesPageRequest;

/**
 * 账号管理接口
 */
public class AccountAdminApi {

    private final AccountManager accountManager;

    public AccountAdminApi(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    /**
     * 账号管理 - 通用查询
     */
    public ApiResponse<Object> query(QueriesPageRequest request) {
        return ApiResponse.success(accountManager.query(request));
    }

}