package com.vex.owl.ai.domain.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * AI 使用量统计 Repository
 */
@Repository
public interface UsageRecordRepository extends JpaRepository<UsageRecordEntity, String> {

    /**
     * 根据租户、日期、AI 类型查找统计记录
     */
    Optional<UsageRecordEntity> findByTenantIdAndStatDateAndUsageType(
            String tenantId, LocalDate statDate, String usageType);

    /**
     * 查询租户在某日期范围内的所有统计记录
     */
    List<UsageRecordEntity> findByTenantIdAndStatDateBetween(
            String tenantId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询某日期范围内的所有统计记录（按类型分组）
     */
    List<UsageRecordEntity> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 查询某租户在某日期范围内的使用量汇总
     */
    @Query("""
        SELECT COALESCE(SUM(u.promptTokens), 0),
               COALESCE(SUM(u.completionTokens), 0),
               COALESCE(SUM(u.totalTokens), 0),
               COALESCE(SUM(u.chatCallCount), 0)
        FROM UsageRecordEntity u
        WHERE u.tenantId = :tenantId
          AND u.statDate BETWEEN :startDate AND :endDate
          AND u.usageType = 'CHAT'
    """)
    Object[] sumChatUsageByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询某租户在某日期范围内的 VOICE 使用量汇总
     */
    @Query("""
        SELECT COALESCE(SUM(u.voiceCallCount), 0),
               COALESCE(SUM(u.inputChars), 0),
               COALESCE(SUM(u.outputDuration), 0),
               COALESCE(SUM(u.outputSize), 0)
        FROM UsageRecordEntity u
        WHERE u.tenantId = :tenantId
          AND u.statDate BETWEEN :startDate AND :endDate
          AND u.usageType = 'VOICE'
    """)
    Object[] sumVoiceUsageByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询某租户在某日期范围内的 IMAGE 使用量汇总
     */
    @Query("""
        SELECT COALESCE(SUM(u.imageRequestCount), 0),
               COALESCE(SUM(u.imageSuccessCount), 0),
               COALESCE(SUM(u.imageFailedCount), 0),
               COALESCE(SUM(u.inputChars), 0)
        FROM UsageRecordEntity u
        WHERE u.tenantId = :tenantId
          AND u.statDate BETWEEN :startDate AND :endDate
          AND u.usageType = 'IMAGE'
    """)
    Object[] sumImageUsageByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 查询某租户在某日期范围内的 MUSIC 使用量汇总
     */
    @Query("""
        SELECT COALESCE(SUM(u.musicCallCount), 0),
               COALESCE(SUM(u.inputChars), 0),
               COALESCE(SUM(u.outputDuration), 0),
               COALESCE(SUM(u.outputSize), 0)
        FROM UsageRecordEntity u
        WHERE u.tenantId = :tenantId
          AND u.statDate BETWEEN :startDate AND :endDate
          AND u.usageType = 'MUSIC'
    """)
    Object[] sumMusicUsageByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
