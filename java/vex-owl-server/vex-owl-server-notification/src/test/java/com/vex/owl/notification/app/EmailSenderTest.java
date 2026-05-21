package com.vex.owl.notification.app;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * 邮件发送测试工具
 * <p>不依赖 Spring，直接使用 JavaMail 发送测试邮件</p>
 * <p>使用阿里云企业邮箱 SMTP 配置</p>
 * <p>敏感信息从环境变量读取：
 * <ul>
 *   <li>SMTP_HOST: SMTP服务器地址（默认 smtpdm.aliyun.com）</li>
 *   <li>SMTP_PORT: SMTP端口（默认 465）</li>
 *   <li>SMTP_USER: 用户名/发件邮箱</li>
 *   <li>SMTP_PASSWORD: 密码/授权码</li>
 *   <li>TEST_TO_EMAIL: 测试收件人邮箱</li>
 * </ul>
 * </p>
 */
public class EmailSenderTest {

    private static final String SMTP_HOST = System.getenv("SMTP_HOST") != null ? System.getenv("SMTP_HOST") : "smtpdm.aliyun.com";
    private static final String SMTP_PORT = System.getenv("SMTP_PORT") != null ? System.getenv("SMTP_PORT") : "465";
    private static final String FROM_EMAIL = System.getenv("SMTP_USER");
    private static final String FROM_PASSWORD = System.getenv("SMTP_PASSWORD");
    private static final String TO_EMAIL = System.getenv("TEST_TO_EMAIL") != null ? System.getenv("TEST_TO_EMAIL") : "790770883@qq.com";
    private static final String SUBJECT = "【Vex-Owl】您的注册验证码";
    private static final String CONTENT = """
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
                    .content { padding: 40px 30px; }
                    .code-box { background: linear-gradient(135deg, #f8f9ff 0%, #f0f4ff 100%); border: 2px dashed #667eea; border-radius: 12px; padding: 30px; text-align: center; margin: 30px 0; }
                    .code { font-size: 42px; font-weight: 700; color: #667eea; letter-spacing: 12px; font-family: 'Courier New', monospace; }
                    .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px 20px; border-radius: 6px; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #eee; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 您的注册验证码</h1>
                    </div>
                    <div class="content">
                        <p style="font-size: 18px; color: #333;">尊敬的用户，您好！</p>
                        <p style="color: #666; line-height: 1.8;">我们已收到您的注册申请，请使用以下验证码完成验证：</p>
                        <div class="code-box">
                            <p style="font-size: 14px; color: #666; margin-bottom: 15px;">您的验证码</p>
                            <p class="code">123456</p>
                        </div>
                        <div class="warning">
                            <p style="font-weight: 600; color: #856404; margin: 0 0 5px;">⚠️ 安全提醒</p>
                            <p style="color: #856404; font-size: 14px; margin: 0;">验证码有效期为 <strong>10分钟</strong>，请尽快完成验证。为保护您的账户安全，请勿向他人泄露此验证码。</p>
                        </div>
                    </div>
                    <div class="footer">
                        <p style="font-weight: 600; color: #667eea; margin: 0;">Vex-Owl</p>
                        <p style="color: #999; font-size: 13px; margin: 5px 0 0;">这是一封系统自动发送的邮件，请勿回复</p>
                    </div>
                </div>
            </body>
            </html>
            """;

    public static void main(String[] args) {
        if (FROM_EMAIL == null || FROM_EMAIL.isBlank()) {
            System.err.println("❌ 请设置环境变量 SMTP_USER（发件邮箱）");
            System.err.println("   Windows 设置方法：");
            System.err.println("   1. 右键 '此电脑' -> '属性'");
            System.err.println("   2. 点击 '高级系统设置'");
            System.err.println("   3. 点击 '环境变量'");
            System.err.println("   4. 在 '系统变量' 中新建：");
            System.err.println("      - SMTP_USER = vex@mail.vexrune.top");
            System.err.println("      - SMTP_PASSWORD = 你的密码");
            System.err.println("      - TEST_TO_EMAIL = 你的测试邮箱");
            return;
        }

        if (FROM_PASSWORD == null || FROM_PASSWORD.isBlank()) {
            System.err.println("❌ 请设置环境变量 SMTP_PASSWORD（密码）");
            return;
        }

        System.out.println("=== Vex-Owl 邮件发送测试 ===");
        System.out.println("SMTP服务器: " + SMTP_HOST + ":" + SMTP_PORT);
        System.out.println("发件人: " + FROM_EMAIL);
        System.out.println("收件人: " + TO_EMAIL);
        System.out.println("主题: " + SUBJECT);
        System.out.println();

        try {
            sendEmailBySsl(TO_EMAIL, SUBJECT, CONTENT);
        } catch (Exception e) {
            System.err.println("\n❌ 发送失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 使用 SSL 连接发送邮件（端口465）
     */
    public static void sendEmailBySsl(String to, String subject, String content) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", SMTP_PORT);

        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        };

        Session session = Session.getInstance(props, authenticator);
        session.setDebug(true);

        System.out.println("正在连接邮件服务器 (SSL)...");
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject, "UTF-8");
        message.setContent(content, "text/html; charset=UTF-8");

        System.out.println("正在发送邮件...");
        Transport.send(message);
        System.out.println("\n✅ 邮件发送成功!");
        System.out.println("收件人: " + to);
    }
}