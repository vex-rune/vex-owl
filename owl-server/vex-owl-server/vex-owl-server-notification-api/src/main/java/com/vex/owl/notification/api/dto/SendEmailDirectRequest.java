package com.vex.owl.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 直接发送邮件请求
 *
 * <p>用于直接指定收件人、邮件主题和内容发送邮件，无需配置模板</p>
 *
 * <h2>功能说明</h2>
 * <p>此请求用于直接发送邮件，邮件的主题（subject）和内容（content）
 * 由调用方直接指定，不依赖任何预定义的模板。适用于验证码通知、
 * 系统消息等需要动态生成内容的场景。</p>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 创建直接发送请求
 * SendEmailDirectRequest request = new SendEmailDirectRequest(
 *     "user@example.com",                    // 收件人邮箱
 *     "您的验证码",                            // 邮件主题
 *     "您的验证码是：123456，5分钟内有效。"    // 邮件内容
 * );
 *
 * // 调用通知服务发送邮件
 * notificationClient.sendEmailDirect(request);
 * }</pre>
 *
 * <h2>与模板模式的区别</h2>
 * <table border="1">
 * <tr><th>对比项</th><th>SendEmailRequest（模板模式）</th><th>SendEmailDirectRequest（直接模式）</th></tr>
 * <tr><td>模板</td><td>需要预定义模板</td><td>不需要模板</td></tr>
 * <tr><td>主题</td><td>模板中定义</td><td>直接指定</td></tr>
 * <tr><td>内容</td><td>模板+变量替换</td><td>直接指定</td></tr>
 * <tr><td>适用场景</td><td>格式固定的邮件</td><td>动态内容的邮件</td></tr>
 * </table>
 *
 * @author Vex
 * @version 1.0.0
 * @since 2023-12-15
 */
public record SendEmailDirectRequest(

        /**
         * 收件人邮箱地址
         *
         * <p>邮件将发送到此邮箱地址</p>
         *
         * <h3>验证规则</h3>
         * <ul>
         *   <li>不能为空（{@link NotBlank}）</li>
         *   <li>最大长度：255 字符（{@link Size}）</li>
         *   <li>必须是有效的邮箱格式</li>
         * </ul>
         *
         * <h3>示例值</h3>
         * <ul>
         *   <li>user@example.com</li>
         *   <li>zhang.san@company.com</li>
         *   <li>support@company.com</li>
         * </ul>
         */
        @NotBlank(message = "收件人邮箱不能为空")
        @Size(max = 255)
        String toEmail,

        /**
         * 邮件主题
         *
         * <p>邮件的标题，会显示在邮件客户端的收件箱中</p>
         *
         * <h3>验证规则</h3>
         * <ul>
         *   <li>不能为空（{@link NotBlank}）</li>
         * </ul>
         *
         * <h3>最佳实践</h3>
         * <ul>
         *   <li>主题应简洁明了，概括邮件的主要内容</li>
         *   <li>避免使用特殊字符或过长的主题</li>
         *   <li>建议包含关键信息，便于用户识别</li>
         * </ul>
         *
         * <h3>常用主题示例</h3>
         * <ul>
         *   <li>您的验证码</li>
         *   <li>账户安全通知</li>
         *   <li>订单确认 - 订单号 #12345</li>
         *   <li>密码重置链接</li>
         *   <li>【重要】系统升级通知</li>
         * </ul>
         */
        @NotBlank(message = "邮件主题不能为空")
        String subject,

        /**
         * 邮件内容
         *
         * <p>邮件的正文内容，可以是纯文本或 HTML 格式</p>
         *
         * <h3>验证规则</h3>
         * <ul>
         *   <li>不能为空（{@link NotBlank}）</li>
         * </ul>
         *
         * <h3>内容格式</h3>
         * <p>邮件内容支持两种格式：</p>
         * <ul>
         *   <li><b>纯文本</b>：直接写入文本内容，换行使用 \n</li>
         *   <li><b>HTML</b>：如果通知服务支持，可以使用 HTML 标签格式化内容</li>
         * </ul>
         *
         * <h3>使用示例</h3>
         * <pre>{@code
         * // 纯文本内容示例
         * String content = "您好！\n" +
         *     "您的验证码是：123456\n" +
         *     "验证码有效期为5分钟，请在有效时间内完成验证。\n\n" +
         *     "如果不是您本人操作，请忽略此邮件。\n\n" +
         *     "感谢您的使用！";
         *
         * // HTML 内容示例（如果支持）
         * String htmlContent = "<html><body>" +
         *     "<h2>您好！</h2>" +
         *     "<p>您的验证码是：<strong>123456</strong></p>" +
         *     "<p>验证码有效期为5分钟。</p>" +
         *     "</body></html>";
         * }</pre>
         *
         * <h3>注意事项</h3>
         * <ul>
         *   <li>内容应包含必要的上下文信息，让收件人清楚了解邮件目的</li>
         *   <li>敏感信息（如验证码）应明确说明有效期</li>
         *   <li>建议在内容末尾添加安全提示或联系信息</li>
         * </ul>
         */
        @NotBlank(message = "邮件内容不能为空")
        String content
) {

}