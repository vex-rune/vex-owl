package com.vex.owl.user.user.auth.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.user.user.auth.domain.account.AccountManager;
import com.vex.owl.user.user.auth.domain.account.model.AccountEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账号管理
 */
@RestController
@RequestMapping("/api/user/admin/account")
@RequiredArgsConstructor
@Slf4j
public class AccountAdminApi {

    private final AccountManager accountManager;

    /**
     * 账号-通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 账号列表
     */
    @PostMapping("/query")
    public ApiResponse<List<AccountEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        log.info("账号通用查询, request: {}", request);
        List<AccountEntity> result = accountManager.query(request);
        return ApiResponse.success(result);
    }

}