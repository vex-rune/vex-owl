package com.vex.owl.notification.api.client;

import com.vex.model.ApiResponse;
import com.vex.owl.notification.api.dto.SendEmailDirectRequest;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@FeignClient(name = "notification-server", path = "/api/notification/admin/email")
public interface NotificationClient {

    @PostMapping("/send")
    ApiResponse<Void> sendEmail(@Valid @RequestBody SendEmailRequest request);

    @PostMapping("/send/direct")
    ApiResponse<Void> sendEmailDirect(@Valid @RequestBody SendEmailDirectRequest request);
}