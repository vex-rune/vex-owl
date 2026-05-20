package com.vex.owl.auth.api;

import com.vex.owl.auth.api.response.ApiResponse;
import com.vex.owl.auth.domain.subject.SubjectManager;
import com.vex.query.criteria.QueriesCriteria;

/**
 * 主体管理接口
 * 提供主体信息的查询功能
 */
public class SubjectManagerApi {

    private final SubjectManager subjectManager;

    public SubjectManagerApi(SubjectManager subjectManager) {
        this.subjectManager = subjectManager;
    }

    /**
     * 根据ID查询主体
     */
    public ApiResponse<Object> getById(Long id) {
        var subject = subjectManager.findById(id);
        if (subject == null) {
            return ApiResponse.error("主体不存在");
        }
        return ApiResponse.success(subject);
    }

    /**
     * 条件查询主体（使用CriteriaQueryBuilder）
     */
    public ApiResponse<Object> query(QueriesCriteria criteria) {
        var subjects = subjectManager.query(criteria);
        return ApiResponse.success(subjects);
    }

    /**
     * 分页查询主体
     */
    public ApiResponse<Object> queryByPage(int page, int size, String keyword) {
        QueriesCriteria criteria = new QueriesCriteria();
        criteria.setPage(page);
        criteria.setSize(size);
        if (keyword != null && !keyword.isEmpty()) {
            criteria.setKeyword(keyword);
        }
        var subjects = subjectManager.query(criteria);
        return ApiResponse.success(subjects);
    }
}