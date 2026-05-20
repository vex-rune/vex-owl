package com.vex.owl.auth.domain.login_record;

/**
 * 登录日志管理
 * 负责登录日志的记录和查询
 */
public class LoginRecordManager {

    private final com.vex.owl.auth.domain.login_record.repository.LoginRecordRepository loginRecordRepository;

    public LoginRecordManager(com.vex.owl.auth.domain.login_record.repository.LoginRecordRepository loginRecordRepository) {
        this.loginRecordRepository = loginRecordRepository;
    }

    /**
     * 根据ID查询登录日志
     * @param id 日志ID
     * @return 登录日志
     */
    public com.vex.owl.auth.domain.login_record.entity.LoginRecord findById(Long id) {
        return loginRecordRepository.findById(id);
    }

    /**
     * 条件查询登录日志（使用CriteriaQueryBuilder）
     * @param criteria 查询条件
     * @return 登录日志分页列表
     */
    public java.util.List<com.vex.owl.auth.domain.login_record.entity.LoginRecord> query(com.vex.query.criteria.QueriesCriteria criteria) {
        return java.util.Collections.emptyList();
    }

    /**
     * 记录登录日志
     * @param record 登录日志
     */
    public void create(com.vex.owl.auth.domain.login_record.entity.LoginRecord record) {
        loginRecordRepository.save(record);
    }
}