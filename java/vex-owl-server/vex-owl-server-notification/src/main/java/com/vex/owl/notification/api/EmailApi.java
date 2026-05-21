package com.vex.owl.notification.api;

import com.vex.owl.notification.api.client.NotificationClient;
import com.vex.owl.notification.app.NotificationApp;
import com.vex.owl.notification.api.dto.SendEmailDirectRequest;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import com.vex.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 通知模块
 * <p>邮件发送相关业务接口</p>
 */
@RestController
@RequestMapping("/api/v1/notification/email")
@RequiredArgsConstructor
public class EmailApi implements NotificationClient {

    private final NotificationApp notificationApp;

    /**
     * 通知-邮件发送
     * <p>使用模板发送邮件</p>
     */
    @PostMapping("/send")
    public ApiResponse<Void> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        notificationApp.sendEmailByTemplate(request.toEmail(), request.templateCode(), request.params());
        return ApiResponse.success();
    }

    /**
     * 通知-邮件发送
     * <p>直接发送邮件</p>
     */
    @PostMapping("/send/direct")
    public ApiResponse<Void> sendEmailDirect(@Valid @RequestBody SendEmailDirectRequest request) {
        notificationApp.sendEmail(request.toEmail(), request.subject(), request.content());
        return ApiResponse.success();
    }
}