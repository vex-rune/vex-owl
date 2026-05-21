package com.vex.owl.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public record SendEmailRequest(
        @NotBlank(message = "收件人邮箱不能为空")
        @Size(max = 255)
        String toEmail,
        @NotBlank(message = "模板编码不能为空")
        @Size(max = 50)
        String templateCode,
        Map<String, String> params
) {


}