package com.vex.owl.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置类
 * 支持大文件上传、分片上传
 */
@Configuration
public class FileUploadConfig {

    // Spring Boot 3.x 已自动配置 Multipart
    // 文件大小限制在 application.yml 中配置
    // - spring.servlet.multipart.max-file-size
    // - spring.servlet.multipart.max-request-size
}