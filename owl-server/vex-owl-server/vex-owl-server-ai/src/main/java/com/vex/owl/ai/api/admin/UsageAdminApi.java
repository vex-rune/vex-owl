package com.vex.owl.ai.api.admin;

import java.time.LocalDate;

import com.vex.event.CurrentResolver;
import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.usage.UsageRecordManager;
import com.vex.owl.ai.domain.usage.UsageStatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * 使用量管理
 *
 * <p>面向前端的使用量查询，按租户隔离。</p>
 */
@RestController
@RequestMapping("/api/ai/admin/usage")
@RequiredArgsConstructor
public class UsageAdminApi {

    private final UsageRecordManager usageRecordManager;
    private final CurrentResolver currentUserResolver;

    /**
     * 使用量-查询
     */
    @GetMapping("/query")
    public ApiResponse<UsageStatResponse> query(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String userId = currentUserResolver.resolveCurrentUser().get().getUserId();
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        return ApiResponse.success(usageRecordManager.query(userId, start, end));
    }
}
