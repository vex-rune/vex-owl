package com.vex.owl.ai.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FreeChatMessageRequest {
    @NotBlank
    private String userMessage;
}
