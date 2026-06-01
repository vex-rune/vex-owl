package com.vex.owl.ai.app.tools;

import com.vex.owl.ai.domain.tools.PublicTools;
import com.vex.owl.ai.domain.tools.ToolContextExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 思考过程记录工具
 * <p>将模型的中间思考过程转化为结构化日志记录，用于后续调试、审计和流程追溯。
 * 大模型在需要记录推理过程时调用此工具，思考内容由模型自行生成。
 * 租户ID由应用层通过 toolContext 注入，不由大模型提供。</p>
 */
@Component
@Slf4j
public class ThinkRecordLogTools implements PublicTools {

    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();
    @Getter
    public   final String name = "thinkRecordLog";

    /**
     * 记录思考过程
     * <p>将模型当前阶段的推理、分析、判断等思维过程持久化记录。
     * 调用时机：模型在完成关键决策或阶段性推理后记录思考内容。</p>
     *
     * @param toolContext Spring AI 自动注入的工具上下文，包含应用层传入的 tenantId
     * @param content     思考内容，描述当前阶段的推理过程、判断依据或阶段性结论
     * @return 记录结果确认信息
     */
    @Tool(name = "thinkRecordLog", description = "记录模型当前的思考过程。在完成关键推理或阶段性分析后调用，记录思考内容供后续追溯。")
    public String recordThinking(
            ToolContext toolContext,
            @ToolParam(description = "思考内容，描述当前阶段的推理过程、判断依据或阶段性结论") String content) {

        String tenantId = toolContextExtractor.getTenantId(toolContext).orElse("unknown");
        log.info("记录思考过程, tenantId={}, content={}", tenantId, content);

        return "思考过程已记录";
    }

}
