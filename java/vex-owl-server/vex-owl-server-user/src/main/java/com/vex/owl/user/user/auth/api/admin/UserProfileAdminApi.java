package com.vex.owl.user.user.auth.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.user.user.auth.domain.profile.UserProfileManager;
import com.vex.owl.user.user.auth.domain.profile.entity.UserProfileEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户档案管理
 */
@RestController
@RequestMapping("/api/user/admin/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileAdminApi {

    private final UserProfileManager userProfileManager;

    /**
     * 用户档案-通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 用户档案列表
     */
    @PostMapping("/query")
    public ApiResponse<List<UserProfileEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        log.info("用户档案通用查询, request: {}", request);
        List<UserProfileEntity> result = userProfileManager.query(request);
        return ApiResponse.success(result);
    }

    /**
     * 用户档案-查询单个
     * <p>根据用户ID查询用户档案详情</p>
     *
     * @param userId 用户ID
     * @return 用户档案信息
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileEntity> getUserProfile(@PathVariable("userId") String userId) {
        log.info("查询用户档案, userId: {}", userId);
        UserProfileEntity userProfile = userProfileManager.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户档案不存在: " + userId));
        return ApiResponse.success(userProfile);
    }

    /**
     * 用户档案-删除数据
     * <p>根据用户ID删除用户档案</p>
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUserProfile(@PathVariable("userId") String userId) {
        log.info("删除用户档案, userId: {}", userId);
        userProfileManager.delete(userId);
        return ApiResponse.success();
    }
}