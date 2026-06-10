package com.vex.owl.notification.app;

import com.vex.owl.notification.domain.template.TemplateManager;
import com.vex.owl.notification.domain.template.entity.TemplateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApp {

    private final TemplateManager templateManager;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@vex.com}")
    private String defaultFromEmail;

    public void sendEmailByTemplate(String toEmail, String templateCode, Map<String, String> params) {
        TemplateEntity template = templateManager.findByCode(templateCode)
                .orElseThrow(() -> new IllegalArgumentException("模板不存在: " + templateCode));

        if (!template.getEnabled()) {
            throw new IllegalStateException("模板已禁用: " + templateCode);
        }

        String subject = templateManager.render(template.getName(), params);
        String content = templateManager.render(template.getContent(), params);
        sendMail(toEmail, subject, content);
    }

    public void sendEmail(String toEmail, String subject, String content) {
        sendMail(toEmail, subject, content);
    }

    private void sendMail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(defaultFromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
            log.debug("邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}