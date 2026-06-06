package com.vex.owl.ai.domain.tools;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * 工具回调服务
 * <p>按租户管理本地Tool和MCP Tool可用列表，预留扩展点</p>
 */
@Component
@RequiredArgsConstructor
public class ToolServer {

    private final List<Tools> tools;

    public List<ToolCallback> getPublicTools() {
        return tools.stream()
                .filter(tool -> tool instanceof PublicTools)
                .map(tool -> Arrays.stream(ToolCallbacks.from(tool)).toList())
                .flatMap(List::stream)
                .toList();
    }
}
