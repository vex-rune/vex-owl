package com.vex.owl.ai.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PipelineRequest {

    @NotBlank(message = "prompt 不能为空")
    private String prompt;
}
