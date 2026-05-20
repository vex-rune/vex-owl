package com.vex.owl.auth.api;

import com.vex.owl.auth.api.response.ApiResponse;
import com.vex.owl.auth.domain.login_record.LoginRecordManager;
import com.vex.query.criteria.QueriesCriteria;

/**
 * 登录日志管理接口
 * 提供登录日志的查询功能
 */
public class LoginLogManagerApi {

    private final LoginRecordManager loginRecordManager;

    public LoginLogManagerApi(LoginRecordManager loginRecordManager) {
        this.loginRecordManager = loginRecordManager;
    }

    /**
     * 根据ID查询登录日志
     */
    public ApiResponse<Object> getById(Long id) {
        var record = loginRecordManager.findById(id);
        if (record == null) {
            return ApiResponse.error("日志不存在");
        }
        return ApiResponse.success(record);
    }

    /**
     * 条件查询登录日志（使用CriteriaQueryBuilder）
     */
    public ApiResponse<Object> query(QueriesCriteria criteria) {
        var records = loginRecordManager.query(criteria);
        return ApiResponse.success(records);
    }

    /**
     * 分页查询登录日志
     */
    public ApiResponse<Object> queryByPage(int page, int size, String email, String startDate, String endDate) {
        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setPage(page);
        criteria.setSize(size);
        // TODO: 设置查询条件
        var records = loginRecordManager.query(criteria);
        return ApiResponse.success(records);
    }
}