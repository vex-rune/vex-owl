package com.vex.owl.notification.app;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationApp单元测试")
class NotificationAppTest {

    @Mock
    private JavaMailSender mailSender;

    private static final String TEST_EMAIL = "790770883@qq.com";
    private static final String TEST_SUBJECT = "测试邮件";
    private static final String TEST_CONTENT = "测试内容";

    @Test
    @DisplayName("测试sendEmail - createMimeMessage返回null导致失败")
    void testSendEmail_CreateMimeMessageReturnsNull() {
        NotificationApp notificationApp = new NotificationApp(null, mailSender);

        when(mailSender.createMimeMessage()).thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationApp.sendEmail(TEST_EMAIL, TEST_SUBJECT, TEST_CONTENT)
        );

        assertTrue(exception.getMessage().contains("邮件发送失败"));
    }
}