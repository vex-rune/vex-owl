package com.vex.owl.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OPTIONS 请求预检过滤器
 *  OPTIONS 请求直接返回200，不经过其他过滤器
 */
@Slf4j
@Component
public class OptionsRequestFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();

        // OPTIONS 请求直接返回200
        if (HttpMethod.OPTIONS.compareTo(method) == 0) {
            String path = exchange.getRequest().getURI().getPath();
            log.debug("OPTIONS预检请求 | 路径: {}", path);
            
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.OK);
            response.getHeaders().add("Access-Control-Allow-Origin", "*");
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.getHeaders().add("Access-Control-Allow-Headers", "*");
            response.getHeaders().add("Access-Control-Max-Age", "3600");
            response.getHeaders().add("X-Gateway-Filter", "OptionsRequestFilter");
            
            return response.setComplete();
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 最高优先级，最先执行
        return -300;
    }
}