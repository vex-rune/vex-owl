package com.vex.owl.user.user.auth.domain.login_record.repository;

import com.vex.owl.user.user.auth.domain.login_record.entity.LoginRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 登录日志仓储接口
 */
public interface LoginRecordRepository extends JpaRepository<LoginRecordEntity, Long> {

}