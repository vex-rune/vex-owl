package com.vex.owl.auth.api;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.domain.subject.SubjectManager;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * 主体管理接口
 * 提供主体信息的查询功能
 */
@RestController
@RequiredArgsConstructor
public class SubjectManagerApi {

    private final SubjectManager subjectManager;

    /**
     * 登录日志管理 - 通用查询
     */
    public ApiResponse<Object> query(QueriesPageRequest request) {
        return ApiResponse.success(
                subjectManager.query(request)
        );
    }
}