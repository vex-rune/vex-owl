package com.vex.owl.user.user.auth.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.user.user.auth.domain.login_record.LoginRecordManager;
import com.vex.owl.user.user.auth.domain.login_record.entity.LoginRecordEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理
 */
@RestController
@RequestMapping("/api/user/admin/login/log")
@RequiredArgsConstructor
@Slf4j
public class LoginLogAdminApi {

    private final LoginRecordManager loginRecordManager;

    /**
     * 登录日志-通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 登录日志列表
     */
    @PostMapping("/query")
    public ApiResponse<List<LoginRecordEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        log.info("登录日志通用查询, request: {}", request);
        List<LoginRecordEntity> result = loginRecordManager.query(request);
        return ApiResponse.success(result);
    }

}