package com.vex.owl.ai.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * LLM 响应内容事件
 *
 * <p>非流式：finish=true，content 为完整响应</p>
 * <p>流式：每个 chunk 一个事件，finish=true 表示结束</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatContentEvent {

    private String tenantId;
    private String sessionId;
    /** 响应内容（流式时为单个 chunk） */
    private String content;
    /** 是否为流式 chunk */
    private boolean stream;
    /** 是否结束（非流式始终为 true，流式最后一个 chunk 为 true） */
    private boolean finish;
    private long timestamp;
}
