# Gateway 过滤器请求头标记说明

## 📋 概述

每个过滤器在处理请求时，都会在请求头中添加特定的标记，方便下游服务识别请求经过了哪些过滤器处理，以及获取相关的上下文信息。

## 🎯 过滤器标记列表

### 1. OptionsRequestFilter

**添加的响应头**：
```
X-Gateway-Filter: OptionsRequestFilter
```

**说明**：标识该响应是由 OPTIONS 预检过滤器处理的。

---

### 2. LoggingFilter

**添加的请求头**：
```
X-Gateway-Filter: LoggingFilter
X-Request-ID: 2026-05-18 19:30:15.123-0001
```

**说明**：
- `X-Gateway-Filter`: 标识请求经过了日志过滤器
- `X-Request-ID`: 唯一的请求追踪 ID，用于全链路日志追踪

**下游服务使用**：
```java
// 在下游服务中获取 Request ID
String requestId = request.getHeader("X-Request-ID");
log.info("处理请求 | RequestID: {}", requestId);
```

---

### 3. RateLimiterFilter

**添加的请求头**：
```
X-Gateway-Filter: RateLimiterFilter
X-RateLimit-Remaining: 2
```

**说明**：
- `X-Gateway-Filter`: 标识请求经过了限流过滤器
- `X-RateLimit-Remaining`: 当前剩余的令牌数量（可用于客户端显示限流状态）

**下游服务使用**：
```java
// 获取剩余令牌数
String remaining = request.getHeader("X-RateLimit-Remaining");
if (remaining != null) {
    int permits = Integer.parseInt(remaining);
    log.debug("限流剩余令牌: {}", permits);
}
```

---

### 4. JwtAuthFilter

**添加的请求头**：
```
X-Gateway-Filter: JwtAuthFilter
X-User-Id: user123
X-User-Claims: {sub=user123, role=admin, ...}
```

**说明**：
- `X-Gateway-Filter`: 标识请求经过了 JWT 认证过滤器
- `X-User-Id`: 从 JWT 中解析出的用户 ID
- `X-User-Claims`: JWT 中的所有声明信息（JSON 格式）

**下游服务使用**：
```java
// 获取用户 ID
String userId = request.getHeader("X-User-Id");

// 获取用户角色
String claims = request.getHeader("X-User-Claims");
// 解析 JSON 获取角色等信息
```

---

## 🔄 请求头传递流程

```
客户端请求
    ↓
┌─────────────────────────────────┐
│ OptionsRequestFilter            │
│ → X-Gateway-Filter (响应头)     │ ← 仅 OPTIONS 请求
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ LoggingFilter                   │
│ → X-Gateway-Filter              │
│ → X-Request-ID                  │
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ RateLimiterFilter               │
│ → X-Gateway-Filter              │
│ → X-RateLimit-Remaining         │
└─────────────────────────────────┘
    ↓
┌─────────────────────────────────┐
│ JwtAuthFilter                   │
│ → X-Gateway-Filter              │
│ → X-User-Id                     │
│ → X-User-Claims                 │
└─────────────────────────────────┘
    ↓
路由转发到下游服务
    ↓
下游服务可以读取所有请求头
```

## 💡 使用场景

### 场景 1：全链路日志追踪

通过 `X-Request-ID` 实现跨服务的日志关联：

**Gateway**:
```
2026-05-18 19:30:15.123 DEBUG [LoggingFilter] 请求开始 | ID: 2026-05-18 19:30:15.123-0001 | GET /api/user/profile
```

**User Service**:
```java
@RestController
public class UserController {
    
    @GetMapping("/profile")
    public UserProfile getProfile(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-ID");
        log.info("查询用户资料 | RequestID: {}", requestId);
        // ... 业务逻辑
    }
}
```

**输出**:
```
2026-05-18 19:30:15.456 INFO [UserController] 查询用户资料 | RequestID: 2026-05-18 19:30:15.123-0001
```

通过 `RequestID` 可以将 Gateway 和 User Service 的日志关联起来！

### 场景 2：下游服务获取用户信息

下游服务无需再次验证 JWT，直接使用 Gateway 传递的用户信息：

```java
@Service
public class OrderService {
    
    public Order createOrder(HttpServletRequest request, OrderDTO orderDTO) {
        // 直接从请求头获取用户 ID
        String userId = request.getHeader("X-User-Id");
        
        // 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(orderDTO.getItems());
        
        return orderRepository.save(order);
    }
}
```

### 场景 3：监控和审计

通过 `X-Gateway-Filter` 标记，可以统计请求经过了哪些过滤器：

```java
@Component
public class AuditFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletRequest request = (HttpServletRequest) req;
        
        String gatewayFilter = request.getHeader("X-Gateway-Filter");
        String userId = request.getHeader("X-User-Id");
        String requestId = request.getHeader("X-Request-ID");
        
        // 记录审计日志
        auditLog.info("请求审计 | RequestID: {} | Filter: {} | UserId: {} | Path: {}", 
                requestId, gatewayFilter, userId, request.getRequestURI());
        
        chain.doFilter(req, res);
    }
}
```

### 场景 4：限流状态反馈

客户端可以通过 `X-RateLimit-Remaining` 了解当前限流状态：

```javascript
// 前端代码
fetch('/api/data')
  .then(response => {
    const remaining = response.headers.get('X-RateLimit-Remaining');
    if (remaining && parseInt(remaining) < 2) {
      console.warn('请求即将达到限流，请减缓请求频率');
    }
    return response.json();
  });
```

## ⚠️ 注意事项

### 1. 请求头覆盖问题

后面的过滤器会覆盖前面过滤器设置的 `X-Gateway-Filter` 头。

**当前行为**：
```
LoggingFilter 设置: X-Gateway-Filter: LoggingFilter
RateLimiterFilter 设置: X-Gateway-Filter: RateLimiterFilter  ← 覆盖
JwtAuthFilter 设置: X-Gateway-Filter: JwtAuthFilter          ← 最终值
```

**如果需要保留所有过滤器标记**，可以改为追加模式：

```java
// 修改为追加而不是覆盖
.header("X-Gateway-Filters", "LoggingFilter,RateLimiterFilter,JwtAuthFilter")
```

### 2. 安全性考虑

- `X-User-Id` 和 `X-User-Claims` 是敏感信息
- 确保下游服务在内网环境中，防止信息泄露
- 不要将这些信息暴露给前端客户端

### 3. 性能影响

添加请求头对性能影响极小（微秒级别），可以放心使用。

## 🔧 自定义扩展

如果需要添加更多标记，可以在过滤器中继续添加：

```java
// 示例：添加时间戳
ServerHttpRequest modifiedRequest = request.mutate()
        .header("X-Gateway-Filter", "MyFilter")
        .header("X-Filter-Timestamp", String.valueOf(System.currentTimeMillis()))
        .header("X-Custom-Info", "custom-value")
        .build();
```

## 📊 完整的请求头示例

一个经过所有过滤器的请求，到达下游服务时的请求头：

```
GET /api/user/profile HTTP/1.1
Host: user-service:8080
X-Gateway-Filter: JwtAuthFilter
X-Request-ID: 2026-05-18 19:30:15.123-0001
X-RateLimit-Remaining: 2
X-User-Id: user123
X-User-Claims: {"sub":"user123","role":"admin","exp":1716105600}
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

下游服务可以直接使用这些信息，无需重复解析 JWT 或重新计算限流状态！✨
