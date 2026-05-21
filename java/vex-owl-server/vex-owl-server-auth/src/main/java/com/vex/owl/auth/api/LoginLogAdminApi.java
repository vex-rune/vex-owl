package com.vex.owl.auth.api;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.domain.login_record.LoginRecordManager;
import com.vex.queries.model.queries.model.QueriesPageRequest;

/**
 * 登录日志管理
 * 提供登录日志的查询功能
 */
public class LoginLogAdminApi {

    private final LoginRecordManager loginRecordManager;

    public LoginLogAdminApi(LoginRecordManager loginRecordManager) {
        this.loginRecordManager = loginRecordManager;
    }

    /**
     * 登录日志管理 - 通用查询
     */
    public ApiResponse<Object> query(QueriesPageRequest request) {
        return ApiResponse.success(
                loginRecordManager.query(request)
        );
    }
}