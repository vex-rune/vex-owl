# 灰度发布方案

## 1. 灰度方案概述

基于 Header 染色实现灰度发布，支持按用户维度灰度。

## 2. 灰度 Header

| Header 名称 | 类型 | 说明 |
|-------------|------|------|
| X-Gray-Switch | String | 灰度开关: on/off |
| X-Gray-Version | String | 目标版本: v1/v2 |
| X-Gray-Users | String | 灰度用户ID列表 | 

## 3. 灰度规则

### 3.1 Header 传递

网关透传灰度 Header 到下游服务。

### 3.2 灰度逻辑

```
请求 → 网关染色 → 路由分发
                  ↓
        ┌──────────────────────┐
        │                      │
        │   X-Gray-Switch=on    │
        │   X-Gray-Users包含   │
        │   当前用户ID?        │
        └──────────────────────┘
                  ↓
           ┌────────────────┐
           │      Yes        │ No (默认路由 v1)
           ↓                ↓
        灰度路由          标准路由
        /api/v2/xxx       /api/xxx
```

## 4. 网关灰度过滤器

```java
package com.vex.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class GrayReleaseFilter implements GatewayFilter, Ordered {

    public static final int GATEWAY_FILTER_ORDER = -100;

    @Override
    public Mono<Void> doFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        HttpHeaders headers = request.getHeaders();
        String graySwitch = headers.getFirst("X-Gray-Switch");
        String version = headers.getFirst("X-Gray-Version");
        String grayUsers = headers.getFirst("X-Gray-Users");

        ServerHttpRequest.Builder builder = request.mutate();

        if ("on".equalsIgnoreCase(graySwitch) && "v2".equalsIgnoreCase(version)) {
            builder.header("X-Gray-Switch", "on");
            builder.header("X-Gray-Version", "v2");
            if (grayUsers != null) {
                builder.header("X-Gray-Users", grayUsers);
            }
            log.info("灰度请求 | 版本: v2 | 用户: {}", grayUsers);
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    @Override
    public int getOrder() {
        return GATEWAY_FILTER_ORDER;
    }
}
```

## 5. 路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        # V1 路由
        - id: notification-v1
          uri: http://notification-v1-service:9206
          predicates:
            - Path=/api/notification/admin/template/**
        # V2 路由
        - id: notification-v2
          uri: http://notification-v2-service:9207
          predicates:
            - Header=X-Gray-Switch, on
            - Header=X-Gray-Version, v2
```

## 6. 服务版本标记

| 版本 | 端口 | 说明 |
|------|------|------|
| V1 | 9206 | 正式版本 |
| V2 | 9207 | 灰度版本 |

## 7. 使用方式

### 7.1 开启灰度

```bash
curl -X POST http://localhost:9201/api/notification/admin/template/query \
  -H "X-Gray-Switch: on" \
  -H "X-Gray-Version: v2" \
  -H "Authorization: Bearer xxx"
```

### 7.2 按用户灰度

```bash
curl -X POST http://localhost:9201/api/notification/admin/template/query \
  -H "X-Gray-Switch: on" \
  -H "X-Gray-Version: v2" \
  -H "X-Gray-Users: user123,user456" \
  -H "Authorization: Bearer xxx"
```

## 8. 注意事项

- 灰度 Header 仅影响通知模板接口
- 其他接口暂不支持灰度
- 灰度用户列表为空时，仅验证版本号
- 灰度开关关闭时，默认路由 V1