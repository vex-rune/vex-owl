package com.vex.owl.ai.app.tools;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.vex.owl.ai.domain.tools.PublicTools;
import com.vex.owl.ai.domain.tools.ToolContextExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

/**
 * 日期时间工具
 * <p>提供系统时间查询能力，支持多时区，返回 ISO 8601 标准格式含时区偏移。
 * 租户ID由应用层通过 toolContext 注入，不由大模型提供。</p>
 */
@Component
@Slf4j
public class DateTimeTools implements PublicTools {

    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Getter
    public   final String name = "system_dateTime";

    /**
     * 获取当前系统时间
     * <p>根据指定时区返回 ISO 8601 标准格式的当前时间，包含时区偏移信息。</p>
     *
     * @param toolContext Spring AI 自动注入的工具上下文，包含应用层传入的 userId
     * @param zoneId      时区ID，如 "Asia/Shanghai"、"America/New_York"，为空则使用系统默认时区
     * @return ISO 8601 格式的时间字符串，如 "2026-05-25T14:30:00+08:00"
     */
    @Tool(description = "获取当前系统时间。返回 ISO 8601 标准格式的日期时间，包含时区偏移。支持指定时区。")
    public String getCurrentDateTime(
            ToolContext toolContext,
            @ToolParam(description = "时区ID，如 Asia/Shanghai、America/New_York，为空则使用系统默认时区") String zoneId) {

        String userId = toolContextExtractor.getUserId(toolContext).orElse("unknown");
        log.debug("获取系统时间, userId={}, zoneId={}", userId, zoneId);

        ZoneId zone = resolveZoneId(userId, zoneId);
        ZonedDateTime now = ZonedDateTime.now(zone);
        String formattedTime = now.format(FORMATTER);

        log.debug("系统时间返回, userId={}, time={}, zone={}", userId, formattedTime, zone);
        return formattedTime;
    }

    private ZoneId resolveZoneId(String userId, String zoneId) {
        try {
            return (zoneId == null || zoneId.isBlank())
                    ? ZoneId.systemDefault()
                    : ZoneId.of(zoneId);
        } catch (Exception e) {
            log.warn("无效时区ID, userId={}, zoneId={}, 使用系统默认时区", userId, zoneId);
            return ZoneId.systemDefault();
        }
    }

}
