package com.vex.owl.notification.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TextTemplateCreateRequest {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "模板编码不能为空")
    @Size(max = 50)
    private String code;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    private String remark;
}