package com.vex.owl.ai.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PipelineRequest {

    @NotBlank(message = "prompt 不能为空")
    private String prompt;
}
