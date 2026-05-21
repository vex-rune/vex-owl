package com.vex.owl.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public record SendEmailDirectRequest(

        @NotBlank(message = "收件人邮箱不能为空")
        @Size(max = 255)
        String toEmail,

        @NotBlank(message = "邮件主题不能为空")
        String subject,

        @NotBlank(message = "邮件内容不能为空")
        String content
) {

}