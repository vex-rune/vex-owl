package com.vex.owl.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 发送邮件请求（模板模式）
 *
 * <p>用于通过邮件模板发送邮件，支持模板变量替换</p>
 *
 * <h2>功能说明</h2>
 * <p>此请求用于通过预定义的邮件模板发送邮件。邮件模板需要提前在通知服务中配置好，
 * 通过 templateCode 指定要使用的模板，params 中的参数会替换模板中的占位符。</p>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 创建发送请求
 * SendEmailRequest request = new SendEmailRequest(
 *     "user@example.com",           // 收件人邮箱
 *     "welcome-template",            // 模板编码
 *     Map.of(
 *         "username", "张三",
 *         "activeUrl", "https://example.com/active?token=xxx"
 *     )
 * );
 *
 * // 调用通知服务发送邮件
 * notificationClient.sendEmail(request);
 * }</pre>
 *
 * <h2>验证规则</h2>
 * <ul>
 *   <li>toEmail：不能为空，最大长度 255 字符</li>
 *   <li>templateCode：不能为空，最大长度 50 字符</li>
 * </ul>
 *
 * @author Vex
 * @version 1.0.0
 * @since 2023-12-15
 */
public record SendEmailRequest(
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
         * </ul>
         */
        @NotBlank(message = "收件人邮箱不能为空")
        @Size(max = 255)
        String toEmail,

        /**
         * 邮件模板编码
         *
         * <p>指定要使用的邮件模板，需要在通知服务中预先配置</p>
         *
         * <h3>验证规则</h3>
         * <ul>
         *   <li>不能为空（{@link NotBlank}）</li>
         *   <li>最大长度：50 字符（{@link Size}）</li>
         * </ul>
         *
         * <h3>常用模板编码示例</h3>
         * <ul>
         *   <li>welcome-template：欢迎邮件模板</li>
         *   <li>verification-code：验证码邮件模板</li>
         *   <li>password-reset：密码重置邮件模板</li>
         *   <li>order-confirmation：订单确认邮件模板</li>
         * </ul>
         */
        @NotBlank(message = "模板编码不能为空")
        @Size(max = 50)
        String templateCode,

        /**
         * 模板变量参数
         *
         * <p>用于替换邮件模板中的占位符变量</p>
         *
         * <h3>使用说明</h3>
         * <p>Map 的 key 对应模板中的变量名（占位符），value 是要替换的值。
         * 例如，如果模板中有 {@code ${username}} 和 {@code ${activeUrl}} 两个占位符，
         * 则 params 应该包含 "username" 和 "activeUrl" 两个键值对。</p>
         *
         * <h3>参数示例</h3>
         * <pre>{@code
         * Map<String, String> params = Map.of(
         *     "username", "张三",
         *     "activeUrl", "https://example.com/active?token=abc123",
         *     "expireTime", "24小时"
         * );
         * }</pre>
         *
         * <h3>注意事项</h3>
         * <ul>
         *   <li>如果模板中需要的变量在 params 中不存在，该变量将保持原样（不会被替换）</li>
         *   <li>参数值应该是字符串类型</li>
         *   <li>可以传 null 或空 Map，表示不使用任何变量</li>
         * </ul>
         */
        Map<String, String> params
) {


}