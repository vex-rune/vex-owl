package com.vex.owl.user.user.auth.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.user.user.auth.domain.subject.SubjectManager;
import com.vex.owl.user.user.auth.domain.subject.entity.SubjectEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 主体管理
 */
@RestController
@RequestMapping("/api/user/admin/subject")
@RequiredArgsConstructor
public class SubjectAdminApi {

    private final SubjectManager subjectManager;

    /**
     * 主体管理 - 通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 主体列表
     */
    @PostMapping("/query")
    public ApiResponse<List<SubjectEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        return ApiResponse.success(
                subjectManager.query(request)
        );
    }
}