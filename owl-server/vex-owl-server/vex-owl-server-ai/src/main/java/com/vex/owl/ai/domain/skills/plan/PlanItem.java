package com.vex.owl.ai.domain.skills.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 计划项
 * <p>计划中的单条可执行步骤。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanItem {
    /// 序号
    private int order;
    /// 行动内容
    private String content;
}
