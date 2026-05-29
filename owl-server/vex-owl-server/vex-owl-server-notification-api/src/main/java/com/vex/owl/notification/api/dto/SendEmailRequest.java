package com.vex.owl.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送邮件请求
 * @author Vex
 * @version 1.0.0
 * @since 2023-12-15
 */
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