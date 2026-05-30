package com.vex.owl.ai.app.tools.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.definition.ToolDefinition;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class FilterToolCallingManager implements ToolCallingManager {

    final ToolCallingManager toolCallingManager;


    @Override
    public List<ToolDefinition> resolveToolDefinitions(ToolCallingChatOptions chatOptions) {
        log.debug("start resolveToolDefinitions");
        List<ToolDefinition> toolDefinitions = toolCallingManager.resolveToolDefinitions(chatOptions);
        log.debug("end resolveToolDefinitions");
        return toolDefinitions;
    }

    @Override
    public ToolExecutionResult executeToolCalls(Prompt prompt, ChatResponse chatResponse) {
        log.debug("start executeToolCalls");
        ToolExecutionResult result = toolCallingManager.executeToolCalls(prompt, chatResponse);
        log.debug("end executeToolCalls");
        return result;
    }
}
