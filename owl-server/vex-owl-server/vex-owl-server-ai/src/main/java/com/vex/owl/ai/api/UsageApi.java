package com.vex.owl.ai.api;

import java.time.LocalDate;
import java.util.Map;

import com.vex.model.ApiResponse;
import com.vex.owl.ai.domain.usage.UsageRecordRepository;
import com.vex.security.auth.AuthHeaderConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

/**
 * AI 使用量统计接口
 *
 * <p>面向前端的使用量查询，按租户隔离，支持日/周/月统计。</p>
 */
@RestController
@RequestMapping("/api/ai/usage")
@RequiredArgsConstructor
public class UsageApi {

    private final UsageRecordRepository usageRecordRepository;

    /**
     * 查询指定日期范围内的 CHAT Token 使用量汇总
     *
     * @param startDate 开始日期（含），默认当月第一天
     * @param endDate   结束日期（含），默认今天
     */
    @GetMapping("/chat")
    public ApiResponse<Map<String, Object>> chatUsage(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Object[] row = usageRecordRepository.sumChatUsageByTenantIdAndDateRange(userId, start, end);
        return ApiResponse.success(Map.of(
                "startDate", start,
                "endDate", end,
                "promptTokens", row[0],
                "completionTokens", row[1],
                "totalTokens", row[2],
                "callCount", row[3]));
    }

    /**
     * 查询 VOICE 使用量汇总
     */
    @GetMapping("/voice")
    public ApiResponse<Map<String, Object>> voiceUsage(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Object[] row = usageRecordRepository.sumVoiceUsageByTenantIdAndDateRange(userId, start, end);
        return ApiResponse.success(Map.of(
                "startDate", start,
                "endDate", end,
                "callCount", row[0],
                "inputChars", row[1],
                "outputDuration", row[2],
                "outputSize", row[3]));
    }

    /**
     * 查询 IMAGE 使用量汇总
     */
    @GetMapping("/image")
    public ApiResponse<Map<String, Object>> imageUsage(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Object[] row = usageRecordRepository.sumImageUsageByTenantIdAndDateRange(userId, start, end);
        return ApiResponse.success(Map.of(
                "startDate", start,
                "endDate", end,
                "requestCount", row[0],
                "successCount", row[1],
                "failedCount", row[2],
                "inputChars", row[3]));
    }

    /**
     * 查询 MUSIC 使用量汇总
     */
    @GetMapping("/music")
    public ApiResponse<Map<String, Object>> musicUsage(
            @RequestHeader(AuthHeaderConstants.HEADER_USER_ID) String userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        Object[] row = usageRecordRepository.sumMusicUsageByTenantIdAndDateRange(userId, start, end);
        return ApiResponse.success(Map.of(
                "startDate", start,
                "endDate", end,
                "callCount", row[0],
                "inputChars", row[1],
                "outputDuration", row[2],
                "outputSize", row[3]));
    }
}
