# vex-comm-event

事件发布基础设施，零外部依赖。

## 自动加载

| Bean | 方式 | 说明 |
|------|------|------|
| `EventPublisher` | `@Component` 自动扫描 | 事件发布器 |

`TraceIdResolver` 由依赖方实现并注册为 Bean（可选，未注册时使用空实现）。

## 核心类

### EventPublisher

发布 Spring 事件，自动携带链路元数据（traceId / userId / userName）。

```java
@Autowired
private EventPublisher eventPublisher;

// 发布事件，payload 必须实现 Serializable
eventPublisher.publish("ORDER_CREATED", orderEvent);
```

### EventMetadata

不可变事件元数据（record），包含：

| 字段 | 说明 |
|------|------|
| `eventId` | UUID，自动生成 |
| `occurredAt` | 发生时间 |
| `eventType` | 业务事件类型标识 |
| `traceId` | 链路追踪 ID |
| `userId` | 触发用户 ID |
| `userName` | 触发用户名 |

### TraceIdResolver

请求上下文解析器接口，由 `vex-comm-web` 模块实现。未注册时自动降级为空字符串。

```java
// 仅在需要自定义时实现
@Component
public class MyTraceIdResolver implements TraceIdResolver {
    @Override public String resolveTraceId() { ... }
    @Override public String resolveSessionId() { ... }
    @Override public String resolveUserId() { ... }
    @Override public String resolveUserName() { ... }
}
```

## 依赖方接入

```xml
<dependency>
    <groupId>com.vex</groupId>
    <artifactId>vex-comm-event</artifactId>
</dependency>
```

只需引入即可使用 `EventPublisher`。如需链路信息，额外引入 `vex-comm-web` 即可自动注册 `TraceIdResolver`。
