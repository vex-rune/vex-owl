package com.vex.owl.ai.domain.skills.plan;

import lombok.Getter;
import org.springframework.ai.tool.annotation.Tool;

/**
 * 计划结果收集器
 * <p>作为 Tool 注册给 LLM，LLM 调用 {@code saveResult(Plan)} 时将计划回传至内存。</p>
 */
public class PlanCollector {

    @Getter
    Plan plan;

    @Tool(name = "saveResult", description = "保存计划结果")
    public String saveResult(Plan plan) {
        this.plan = plan;
        return "success";
    }
}
