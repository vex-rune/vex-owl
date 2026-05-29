package com.vex.owl.notification.api.client;

import com.vex.model.ApiResponse;
import com.vex.owl.notification.api.dto.SendEmailDirectRequest;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@FeignClient(name = "vex-owl-notification-server", path = "/api/v1/notification")
public interface NotificationClient {

    @PostMapping("/email/send")
    ApiResponse<Void> sendEmail(@Valid @RequestBody SendEmailRequest request);

    @PostMapping("/email/send/direct")
    ApiResponse<Void> sendEmailDirect(@Valid @RequestBody SendEmailDirectRequest request);
}