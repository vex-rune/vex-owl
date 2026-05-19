package com.vex.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志审计全局过滤器
 * 统一记录请求访问日志，便于问题排查和安全分析
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String queryString = request.getURI().getQuery();
        String clientIp = getClientIp(exchange);
        
        // 获取用户ID（由JWT过滤器添加）
        String userId = request.getHeaders().getFirst("X-User-Id");
        
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        String requestId = generateRequestId();
        
        // 构建请求日志 - DEBUG级别显示详细信息
        log.debug("请求开始 | ID: {} | {} {} | IP: {} | 用户: {}", 
                requestId, method, path, clientIp, 
                userId != null ? userId : "未认证");
        
        if (queryString != null && !queryString.isEmpty()) {
            log.debug("请求参数 | ID: {} | {}", requestId, queryString);
        }

        // 记录请求头信息（敏感信息脱敏）
        if (log.isDebugEnabled()) {
            log.debug("请求头 | ID: {} | {}", requestId, request.getHeaders());
        }

        // 添加过滤器标记到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Gateway-Filter", "LoggingFilter")
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build()).doOnSuccess(aVoid -> {
            // 计算响应时间
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() 
                    : 0;
            
            // 根据状态码选择图标
            String icon = getStatusCodeIcon(statusCode);
            
            // 记录响应日志
            log.debug("请求完成 | ID: {} | {} {} | 耗时: {}ms | 状态: {}",
                    requestId, method, path, duration, statusCode);
            
            // 慢请求告警（超过3秒）
            if (duration > 3000) {
                log.warn("慢请求 | ID: {} | {} {} | 耗时: {}ms | 用户: {}",
                        requestId, method, path, duration, userId);
            }
        }).doOnError(error -> {
            // 计算响应时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录错误日志
            log.error("请求异常 | ID: {} | {} {} | 耗时: {}ms | 错误: {}",
                    requestId, method, path, duration, error.getMessage(), error);
        });
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                    : "unknown";
        }
        // 取第一个IP（代理情况下可能有多个IP）
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return LocalDateTime.now().format(FORMATTER) + "-" + 
               String.format("%04d", (int)(Math.random() * 10000));
    }

    /**
     * 根据状态码获取图标
     */
    private String getStatusCodeIcon(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "✅";
        } else if (statusCode >= 300 && statusCode < 400) {
            return "🔄";
        } else if (statusCode >= 400 && statusCode < 500) {
            return "⚠️";
        } else if (statusCode >= 500) {
            return "❌";
        }
        return "❓";
    }

    @Override
    public int getOrder() {
        // 最高优先级，确保最先执行
        return -200;
    }
}
