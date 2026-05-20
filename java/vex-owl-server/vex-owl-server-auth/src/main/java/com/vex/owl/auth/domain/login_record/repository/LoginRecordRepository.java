package com.vex.owl.auth.domain.login_record.repository;

import com.vex.owl.auth.domain.login_record.entity.LoginRecord;

/**
 * 登录日志仓储接口
 */
public interface LoginRecordRepository {

    void save(LoginRecord record);

    LoginRecord findById(Long id);
}