package com.vex.owl.notification.app;

import com.vex.owl.notification.domain.template.TemplateManager;
import com.vex.owl.notification.domain.template.entity.TemplateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateDataInitializer implements CommandLineRunner {

    private final TemplateManager templateManager;

    private static final String CODE_REGISTER = "VEX_MAIL_REGISTER_CODE";
    private static final String CODE_LOGIN = "VEX_MAIL_LOGIN_CODE";

    @Override
    public void run(String... args) {
        initRegisterCodeTemplate();
        initLoginCodeTemplate();
    }

    private void initRegisterCodeTemplate() {
        if (templateManager.findByCode(CODE_REGISTER).isPresent()) {
            log.info("[模板初始化] 注册验证码模板已存在，跳过");
            return;
        }

        TemplateEntity template = TemplateEntity.builder()
                .name("注册验证码")
                .code(CODE_REGISTER)
                .content(buildRegisterEmailContent())
                .remark("用户注册时发送的验证码邮件模板")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        templateManager.create(template);
        log.info("[模板初始化] 注册验证码模板创建成功");
    }

    private void initLoginCodeTemplate() {
        if (templateManager.findByCode(CODE_LOGIN).isPresent()) {
            log.info("[模板初始化] 登录验证码模板已存在，跳过");
            return;
        }

        TemplateEntity template = TemplateEntity.builder()
                .name("登录验证码")
                .code(CODE_LOGIN)
                .content(buildLoginEmailContent())
                .remark("用户登录时发送的验证码邮件模板")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        templateManager.create(template);
        log.info("[模板初始化] 登录验证码模板创建成功");
    }

    private String buildRegisterEmailContent() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>您的注册验证码</title>
                    <style>
                        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }
                        .content { padding: 40px 30px; }
                        .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }
                        .code-box { background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%); border: 2px dashed #667eea; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }
                        .code-label { font-size: 14px; color: #666; margin-bottom: 15px; }
                        .code { font-size: 42px; font-weight: 700; color: #667eea; letter-spacing: 12px; font-family: 'Courier New', monospace; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px 20px; border-radius: 6px; margin: 20px 0; }
                        .warning-title { font-weight: 600; color: #856404; margin-bottom: 5px; }
                        .warning-text { color: #856404; font-size: 14px; margin: 0; line-height: 1.6; }
                        .info { color: #888; font-size: 13px; line-height: 1.8; }
                        .footer { background-color: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #eee; }
                        .footer p { margin: 5px 0; color: #999; font-size: 13px; }
                        .footer .brand { font-weight: 600; color: #667eea; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 您的注册验证码</h1>
                            <p>感谢您选择 Vex-Owl</p>
                        </div>
                        <div class="content">
                            <p class="greeting">尊敬的用户，您好！</p>
                            <p style="color: #666; line-height: 1.8;">我们已收到您的注册申请，请使用以下验证码完成验证：</p>
                            
                            <div class="code-box">
                                <p class="code-label">您的验证码</p>
                                <p class="code">{code}</p>
                            </div>
                            
                            <div class="warning">
                                <p class="warning-title">⚠️ 安全提醒</p>
                                <p class="warning-text">验证码有效期为 <strong>10分钟</strong>，请尽快完成验证。为保护您的账户安全，请勿向他人泄露此验证码。</p>
                            </div>
                            
                            <p class="info">• 如您未进行注册操作，请忽略此邮件<br>
                            • 此验证码仅限一次性使用<br>
                            • 若验证码过期，请重新获取</p>
                        </div>
                        <div class="footer">
                            <p class="brand">Vex-Owl</p>
                            <p>这是一封系统自动发送的邮件，请勿回复</p>
                            <p>© 2024 Vex-Owl. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }

    private String buildLoginEmailContent() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>您的登录验证码</title>
                    <style>
                        body { font-family: 'Microsoft YaHei', Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 20px; }
                        .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); overflow: hidden; }
                        .header { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 40px 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; font-weight: 600; }
                        .header p { margin: 10px 0 0; opacity: 0.9; font-size: 16px; }
                        .content { padding: 40px 30px; }
                        .greeting { font-size: 18px; color: #333; margin-bottom: 20px; }
                        .code-box { background: linear-gradient(135deg, #f0fff4 0%, #e8f8f0 100%); border: 2px dashed #11998e; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }
                        .code-label { font-size: 14px; color: #666; margin-bottom: 15px; }
                        .code { font-size: 42px; font-weight: 700; color: #11998e; letter-spacing: 12px; font-family: 'Courier New', monospace; }
                        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px 20px; border-radius: 6px; margin: 20px 0; }
                        .warning-title { font-weight: 600; color: #856404; margin-bottom: 5px; }
                        .warning-text { color: #856404; font-size: 14px; margin: 0; line-height: 1.6; }
                        .info { color: #888; font-size: 13px; line-height: 1.8; }
                        .footer { background-color: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #eee; }
                        .footer p { margin: 5px 0; color: #999; font-size: 13px; }
                        .footer .brand { font-weight: 600; color: #11998e; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 您的登录验证码</h1>
                            <p>欢迎回来</p>
                        </div>
                        <div class="content">
                            <p class="greeting">尊敬的用户，您好！</p>
                            <p style="color: #666; line-height: 1.8;">我们检测到您的账户正在尝试登录，请使用以下验证码完成身份验证：</p>
                            
                            <div class="code-box">
                                <p class="code-label">您的验证码</p>
                                <p class="code">{code}</p>
                            </div>
                            
                            <div class="warning">
                                <p class="warning-title">⚠️ 安全提醒</p>
                                <p class="warning-text">验证码有效期为 <strong>10分钟</strong>，请尽快完成验证。为保护您的账户安全，请勿向他人泄露此验证码。</p>
                            </div>
                            
                            <p class="info">• 如非本人操作，您的账户可能存在安全风险<br>
                            • 请勿将验证码透露给任何人，包括声称是官方人员<br>
                            • 若验证码过期，请重新获取</p>
                        </div>
                        <div class="footer">
                            <p class="brand">Vex-Owl</p>
                            <p>这是一封系统自动发送的邮件，请勿回复</p>
                            <p>© 2024 Vex-Owl. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}