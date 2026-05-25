package com.vex.owl.ai.domain.mcp;

/**
 * MCP 访问拒绝异常
 * <p>当租户未开通 MCP 功能或缺少租户ID时抛出。</p>
 */
public class McpAccessDeniedException extends RuntimeException {

    public McpAccessDeniedException(String message) {
        super(message);
    }
}
