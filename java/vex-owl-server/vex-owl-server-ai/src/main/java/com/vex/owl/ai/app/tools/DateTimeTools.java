package com.vex.owl.ai.app.tools;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.vex.owl.ai.domain.tools.ToolContextExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;

/**
 * 日期时间工具
 * <p>提供系统时间查询能力，支持多时区，返回 ISO 8601 标准格式含时区偏移。
 * 租户ID由应用层通过 toolContext 注入，不由大模型提供。</p>
 */
@Slf4j
public class DateTimeTools {

    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    /**
     * 获取当前系统时间
     * <p>根据指定时区返回 ISO 8601 标准格式的当前时间，包含时区偏移信息。</p>
     *
     * @param toolContext Spring AI 自动注入的工具上下文，包含应用层传入的 tenantId
     * @param zoneId     时区ID，如 "Asia/Shanghai"、"America/New_York"，为空则使用系统默认时区
     * @return ISO 8601 格式的时间字符串，如 "2026-05-25T14:30:00+08:00"
     */
    @Tool(description = "获取当前系统时间。返回 ISO 8601 标准格式的日期时间，包含时区偏移。支持指定时区。")
    public String getCurrentDateTime(
            ToolContext toolContext,
            @ToolParam(description = "时区ID，如 Asia/Shanghai、America/New_York，为空则使用系统默认时区") String zoneId) {

        String tenantId = toolContextExtractor.getTenantId(toolContext).orElse("unknown");
        log.info("获取系统时间, tenantId={}, zoneId={}", tenantId, zoneId);

        ZoneId zone = resolveZoneId(tenantId, zoneId);
        ZonedDateTime now = ZonedDateTime.now(zone);
        String formattedTime = now.format(FORMATTER);

        log.info("系统时间返回, tenantId={}, time={}, zone={}", tenantId, formattedTime, zone);
        return formattedTime;
    }

    /**
     * 获取当前系统时间（使用系统默认时区）
     *
     * @param toolContext Spring AI 自动注入的工具上下文，包含应用层传入的 tenantId
     * @return ISO 8601 格式的时间字符串，如 "2026-05-25T14:30:00+08:00"
     */
    @Tool(description = "获取当前系统时间。返回 ISO 8601 标准格式的日期时间，包含时区偏移，使用系统默认时区。")
    public String getCurrentDateTime(ToolContext toolContext) {

        String tenantId = toolContextExtractor.getTenantId(toolContext).orElse("unknown");
        log.info("获取系统时间(默认时区), tenantId={}", tenantId);

        ZonedDateTime now = ZonedDateTime.now();
        String formattedTime = now.format(FORMATTER);

        log.info("系统时间返回, tenantId={}, time={}, zone={}",
                tenantId, formattedTime, now.getZone());
        return formattedTime;
    }

    private ZoneId resolveZoneId(String tenantId, String zoneId) {
        try {
            return (zoneId == null || zoneId.isBlank())
                    ? ZoneId.systemDefault()
                    : ZoneId.of(zoneId);
        } catch (Exception e) {
            log.warn("无效时区ID, tenantId={}, zoneId={}, 使用系统默认时区", tenantId, zoneId);
            return ZoneId.systemDefault();
        }
    }
}
