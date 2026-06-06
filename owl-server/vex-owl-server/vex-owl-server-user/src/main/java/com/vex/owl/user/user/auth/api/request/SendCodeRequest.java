package com.vex.owl.user.user.auth.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送验证码请求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SendCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    private String email;
}