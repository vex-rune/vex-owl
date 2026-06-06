package com.vex.owl.notification.api.client;

import com.vex.model.ApiResponse;
import com.vex.owl.notification.api.dto.SendEmailDirectRequest;
import com.vex.owl.notification.api.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

/**
 * 通知服务客户端
 *
 * <p>提供邮件发送的 Feign 客户端，用于在其他服务中调用通知服务发送邮件</p>
 *
 * <h2>功能说明</h2>
 * <ul>
 *   <li><b>模板邮件发送</b>：使用预定义的邮件模板发送邮件</li>
 *   <li><b>直接邮件发送</b>：直接指定收件人、主题和内容发送邮件</li>
 * </ul>
 *
 * <h2>使用前提</h2>
 * <ul>
 *   <li>通知服务（notification-server）需要正常运行</li>
 *   <li>邮件模板需要在通知服务中预先配置（仅模板发送模式）</li>
 *   <li>邮件发送需要配置有效的 SMTP 服务器</li>
 * </ul>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * @Service
 * public class UserService {
 *     private final NotificationClient notificationClient;
 *
 *     // 使用模板发送邮件
 *     public void sendWelcomeEmail(String email) {
 *         SendEmailRequest request = new SendEmailRequest(
 *             email,
 *             "welcome-template",
 *             Map.of("username", "张三")
 *         );
 *         notificationClient.sendEmail(request);
 *     }
 *
 *     // 直接发送邮件
 *     public void sendVerificationEmail(String email) {
 *         SendEmailDirectRequest request = new SendEmailDirectRequest(
 *             email,
 *             "验证码通知",
 *             "您的验证码是：123456，5分钟内有效"
 *         );
 *         notificationClient.sendEmailDirect(request);
 *     }
 * }
 * }</pre>
 *
 * @author Vex
 * @version 1.0.0
 * @since 2023-12-15
 */
@FeignClient(name = "notification-server", path = "/api/notification/admin/email")
public interface NotificationClient {

    /**
     * 使用模板发送邮件
     *
     * <p>通过邮件模板编码发送邮件，模板中的变量通过 params 参数替换</p>
     *
     * <h3>接口信息</h3>
     * <ul>
     *   <li>接口地址：POST /api/notification/admin/email/send</li>
     *   <li>请求格式：JSON</li>
     *   <li>响应格式：JSON</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>toEmail</b>：收件人邮箱（必填）
     *       <ul>
     *         <li>最大长度：255 字符</li>
     *         <li>必须是有效的邮箱格式</li>
     *         <li>示例：user@example.com</li>
     *       </ul>
     *   </li>
     *   <li><b>templateCode</b>：邮件模板编码（必填）
     *       <ul>
     *         <li>最大长度：50 字符</li>
     *         <li>需要在通知服务中预先配置</li>
     *         <li>示例：welcome-template, verification-code</li>
     *       </ul>
     *   </li>
     *   <li><b>params</b>：模板变量参数（可选）
     *       <ul>
     *         <li>类型：Map&lt;String, String&gt;</li>
     *         <li>用于替换邮件模板中的占位符</li>
     *         <li>示例：{"username": "张三", "code": "123456"}</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回 ApiResponse&lt;Void&gt;，表示操作结果：</p>
     * <ul>
     *   <li>成功：code=200 或 code=0</li>
     *   <li>失败：code=500 或其他错误码</li>
     *   <li>具体错误信息可通过 ApiResponse 的其他字段获取</li>
     * </ul>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // 发送欢迎邮件
     * SendEmailRequest request = new SendEmailRequest(
     *     "user@example.com",
     *     "welcome-template",
     *     Map.of(
     *         "username", "张三",
     *         "activeUrl", "https://example.com/active?token=xxx"
     *     )
     * );
     * ApiResponse<Void> response = notificationClient.sendEmail(request);
     * if (response.isSuccess()) {
     *     System.out.println("邮件发送成功");
     * }
     * }</pre>
     *
     * @param request 发送邮件请求（包含收件人、模板编码和变量参数）
     * @return 操作结果响应
     * @throws jakarta.validation.ConstraintViolationException 如果请求参数校验失败
     */
    @PostMapping("/send")
    ApiResponse<Void> sendEmail(@Valid @RequestBody SendEmailRequest request);

    /**
     * 直接发送邮件
     *
     * <p>直接指定收件人、邮件主题和内容发送邮件，无需配置模板</p>
     *
     * <h3>接口信息</h3>
     * <ul>
     *   <li>接口地址：POST /api/notification/admin/email/send/direct</li>
     *   <li>请求格式：JSON</li>
     *   <li>响应格式：JSON</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <ul>
     *   <li><b>toEmail</b>：收件人邮箱（必填）
     *       <ul>
     *         <li>最大长度：255 字符</li>
     *         <li>必须是有效的邮箱格式</li>
     *         <li>示例：user@example.com</li>
     *       </ul>
     *   </li>
     *   <li><b>subject</b>：邮件主题（必填）
     *       <ul>
     *         <li>邮件的标题，将显示在邮件客户端中</li>
     *         <li>建议简洁明了，概括邮件内容</li>
     *         <li>示例：您的验证码、账户安全通知</li>
     *       </ul>
     *   </li>
     *   <li><b>content</b>：邮件内容（必填）
     *       <ul>
     *         <li>邮件的正文内容</li>
     *         <li>支持 HTML 格式和纯文本</li>
     *         <li>示例："您的验证码是：123456，5分钟内有效"</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <h3>返回值</h3>
     * <p>返回 ApiResponse&lt;Void&gt;，表示操作结果：</p>
     * <ul>
     *   <li>成功：code=200 或 code=0</li>
     *   <li>失败：code=500 或其他错误码</li>
     * </ul>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // 发送验证码邮件
     * SendEmailDirectRequest request = new SendEmailDirectRequest(
     *     "user@example.com",
     *     "您的验证码",
     *     "您的验证码是：123456，5分钟内有效。如非本人操作，请忽略此邮件。"
     * );
     * ApiResponse<Void> response = notificationClient.sendEmailDirect(request);
     * if (response.isSuccess()) {
     *     System.out.println("验证码邮件发送成功");
     * }
     * }</pre>
     *
     * @param request 直接发送邮件请求（包含收件人、主题和内容）
     * @return 操作结果响应
     * @throws jakarta.validation.ConstraintViolationException 如果请求参数校验失败
     */
    @PostMapping("/send/direct")
    ApiResponse<Void> sendEmailDirect(@Valid @RequestBody SendEmailDirectRequest request);
}