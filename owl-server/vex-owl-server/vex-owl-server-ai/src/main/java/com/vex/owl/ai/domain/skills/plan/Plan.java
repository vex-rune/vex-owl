package com.vex.owl.ai.domain.skills.plan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 计划
 * <p>完整的计划输出：标题、范围、行动项清单、开放问题。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    /// 计划标题/意图描述
    private String title;
    /// 范围（包含/排除）
    private List<String> scope;
    /// 行动项列表
    private List<PlanItem> actionItems;
    /// 开放问题列表
    private List<String> openQuestions;
}
