# vex-comm-web

WebFlux 请求过滤器与上下文持有器，依赖 `vex-comm-auth` + `vex-comm-event`。

## 自动加载

| Bean | 方式 | 条件 |
|------|------|------|
| `TraceIdFilter` | `TraceIdFilterAutoConfiguration` | `vex.auth.trace-filter.enabled=true`（默认） |
| `AuthUserFilter` | `AuthUserFilterAutoConfiguration` | `vex.auth.user-filter.enabled=true`（默认） |
| `TraceIdResolverImpl` | `@Component` 自动扫描 | 始终注册 |

关闭示例：

```yaml
vex:
  auth:
    trace-filter:
      enabled: false
    user-filter:
      enabled: false
```

## 过滤器执行顺序

```
请求进入
  ↓
TraceIdFilter（HIGHEST_PRECEDENCE - 1）最先执行
  ├─ 读 Cookie sessionId → 没有则生成 UUID 并 Set-Cookie（30天）
  ├─ 生成 traceId（UUID）
  ├─ 注入 Header: X-Session-Id + X-Trace-Id
  └─ 写入 Reactor Context（auth.headers）
  ↓
AuthUserFilter（HIGHEST_PRECEDENCE）
  ├─ 读 Header: X-User-Id / X-User-Name / ...
  ├─ 合并已有的 sessionId + traceId
  └─ 写入 Reactor Context（auth.headers）
  ↓
下游 Controller / Service
```

## 核心类

### RequestUserHolder

请求级上下文持有器，基于 Reactor Context，整个请求链路可直接获取。

```java
// 同步场景（Service / Controller / @EventListener）
String userId    = RequestUserHolder.getUserId();
String userName  = RequestUserHolder.getUserName();
String sessionId = RequestUserHolder.getSessionId();
String traceId   = RequestUserHolder.getTraceId();

// 一次性获取所有信息
AuthHeaders headers = RequestUserHolder.current();

// 响应式链路
RequestUserHolder.currentMono()
    .map(AuthHeaders::getUserId)
    .subscribe(userId -> ...);
```

### AuthHeaders

请求上下文数据模型（`com.vex.security.auth.AuthHeaders`），包含：

| 字段 | 说明 |
|------|------|
| `authEnabled` | 是否已认证 |
| `userId` | 用户 ID |
| `userName` | 用户名 |
| `userGroup` | 用户组 |
| `loginTime` | 登录时间 |
| `role` | 角色 |
| `email` | 邮箱 |
| `nickname` | 昵称 |
| `sessionId` | 会话 ID（Cookie） |
| `traceId` | 追踪 ID（每次请求生成） |

### TraceIdResolverImpl

实现 `com.vex.event.TraceIdResolver` 接口，桥接 `RequestUserHolder` → `EventPublisher`，使事件自动携带用户上下文。

## 注入的 Header

| Header | 来源 |
|--------|------|
| `X-Session-Id` | Cookie 或新生成 |
| `X-Trace-Id` | 每次请求生成 |
| `X-User-Id` | Gateway 转发 |
| `X-User-Name` | Gateway 转发 |
| `X-User-Group` | Gateway 转发 |
| `X-Login-Time` | Gateway 转发 |
| `X-User-Role` | Gateway 转发 |
| `X-User-Email` | Gateway 转发 |
| `X-User-Nickname` | Gateway 转发 |

## 依赖方接入

```xml
<dependency>
    <groupId>com.vex</groupId>
    <artifactId>vex-comm-web</artifactId>
</dependency>
```

无需任何额外配置，Filter 和 TraceIdResolver 自动生效。
