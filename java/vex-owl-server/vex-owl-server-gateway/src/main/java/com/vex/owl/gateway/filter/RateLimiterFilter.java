package com.vex.gateway.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 限流全局过滤器
 * 基于Guava RateLimiter实现本地内存令牌桶限流
 * 参数硬编码：每个IP每秒最多允许3个请求
 */
@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    // 限流参数：硬编码，每个IP每秒3个请求（便于测试）
    private static double PERMITS_PER_SECOND = 3;
    
    // 缓存每个IP的限流器
    private final ConcurrentHashMap<String, RateLimiter> ipLimiters = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取客户端IP作为限流标识
        String clientIp = getClientIp(exchange);
        String path = exchange.getRequest().getURI().getPath();
        
        log.debug("过滤器被执行 | IP: {} | 路径: {}", clientIp, path);
        
        // 获取或创建该IP的限流器
        RateLimiter limiter = ipLimiters.computeIfAbsent(clientIp, key -> RateLimiter.create(PERMITS_PER_SECOND));
        
        // 尝试获取令牌（不等待，立即返回）
        if (limiter.tryAcquire(0, TimeUnit.SECONDS)) {
            log.debug("限流通过 | IP: {} | 路径: {}", clientIp, path);
            
            // 添加过滤器标记到请求头
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Gateway-Filter", "RateLimiterFilter")
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } else {
            log.warn("请求被限流 | IP: {} | 路径: {} | 限流阈值: {}/秒", clientIp, path, PERMITS_PER_SECOND);
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Content-Type", "application/json;charset=UTF-8");
            
            String body = String.format(
                    "{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\",\"timestamp\":%d}",
                    System.currentTimeMillis()
            );
            
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
            );
        }
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

    @Override
    public int getOrder() {
        // 在日志过滤器之后，JWT认证之前执行限流检查
        // 这样可以防止未认证的请求也消耗限流配额
        return -150;
    }
}