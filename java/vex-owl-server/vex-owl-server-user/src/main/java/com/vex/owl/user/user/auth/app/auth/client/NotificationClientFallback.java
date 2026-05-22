//package com.vex.owl.auth.app.auth.client;
//
//import com.vex.owl.notification.api.client.NotificationClient;
//import com.vex.owl.notification.api.dto.SendEmailRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//public class NotificationClientFallback implements NotificationClient {
//
//    @Override
//    public void sendEmail(SendEmailRequest request) {
//        log.warn("Notification service unavailable, email not sent. To: {}, Subject: {}",
//                request.getTo(), request.getSubject());
//    }
//}