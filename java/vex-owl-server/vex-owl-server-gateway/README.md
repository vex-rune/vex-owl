# Gateway 网关服务

## 功能描述
系统统一API入口服务，为所有后端服务提供网关路由和安全防护能力

## 核心功能
- 🚪 **统一请求入口**：所有前端和外部请求都通过网关统一接入
- 🔀 **动态路由转发**：根据请求路径自动路由到对应微服务
- 🔐 **JWT登录鉴权**：全局校验用户登录状态，拦截未认证请求
- 🌐 **跨域处理**：统一配置跨域规则，无需各服务单独处理
- 📤 **文件上传管理**：支持大文件上传、分片上传、文件类型校验
- ⚡ **请求限流**：对高频访问进行限流保护，防止服务雪崩
- 📝 **日志审计**：统一记录请求访问日志，便于问题排查和安全分析

## 技术栈
- Java 21 + Spring Boot 3.2.0
- Spring Cloud Gateway
- Spring Cloud Alibaba (Nacos)
- JWT (JJWT)
- Redis (限流)
- Lombok

## 服务端口
- 默认启动端口：9201

## 项目结构
```
gateway/
├── src/main/java/com/vex/owl/gateway/
│   ├── GatewayApplication.java          # 主启动类
│   ├── config/
│   │   ├── JwtConfig.java               # JWT配置
│   │   ├── CorsConfig.java              # 跨域配置
│   │   ├── RateLimiterConfig.java       # 限流配置
│   │   └── FileUploadConfig.java        # 文件上传配置
│   ├── filter/
│   │   ├── JwtAuthFilter.java           # JWT认证过滤器
│   │   ├── RateLimiterFilter.java       # 限流过滤器
│   │   └── LoggingFilter.java           # 日志审计过滤器
│   └── handler/
│       └── GlobalExceptionHandler.java  # 全局异常处理
├── src/main/resources/
│   └── application.yml                  # 配置文件
├── pom.xml                              # Maven配置
├── Dockerfile                           # Docker镜像构建文件
└── start-gateway.ps1                    # PowerShell启动脚本
```

## 环境要求
- JDK 21+
- Maven 3.6+
- Nacos 2.x (服务注册与配置中心)
- Redis (可选，用于限流)

## 快速开始

### 方式一：使用启动脚本（推荐）
```powershell
# Windows PowerShell
.\start-gateway.ps1
```

### 方式二：手动构建和运行
```bash
# 1. 清理并构建项目
mvn clean package -DskipTests

# 2. 运行应用
java -jar target/gateway-1.0.0.jar
```

### 方式三：使用 Maven 插件运行
```bash
mvn spring-boot:run
```

## Docker 部署

### 构建镜像
```bash
# 1. 先构建项目
mvn clean package -DskipTests

# 2. 构建 Docker 镜像
docker build -t vex-owl/gateway:latest .
```

### 运行容器
```bash
docker run -d \
  --name gateway \
  -p 8080:8080 \
  -e SPRING_CLOUD_NACOS_DISCOVERY_SERVER_ADDR=nacos:8848 \
  -e SPRING_DATA_REDIS_HOST=redis \
  vex-owl/gateway:latest
```

## 配置说明

### 主要配置项 (application.yml)

#### 服务器配置
```yaml
server:
  port: 8080  # 服务端口
```

#### Nacos 配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848  # Nacos地址
        namespace: dev                # 命名空间
```

#### 路由配置
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service     # 负载均衡到用户服务
          predicates:
            - Path=/api/user/**      # 匹配路径
```

#### JWT 配置
```yaml
jwt:
  secret: your-secret-key            # JWT密钥（生产环境请修改）
  expiration: 86400000               # 过期时间（24小时）
  header: Authorization              # Token头名称
  prefix: "Bearer "                  # Token前缀
```

#### 限流配置
```yaml
rate-limiter:
  enabled: true                      # 是否启用限流
  replenish-rate: 10                 # 每秒允许请求数
  burst-capacity: 20                 # 桶容量
  requested-tokens: 1                # 每次请求消耗令牌数
```

## API 路由规则

路由结构：`/api/{服务}/{模块}/{操作}`

| 路径前缀 | 目标服务 | 说明 |
|---------|---------|------|
| /api/user/auth/** | vex-owl-auth-server | 用户服务 - 认证模块（登录、注册、验证码） |
| /api/user/admin/** | vex-owl-auth-server | 用户服务 - 管理模块（账号、用户、日志、主体） |
| /api/notification/admin/** | vex-owl-notification-server | 通知服务 - 管理模块（邮件、模板） |

## 白名单路径
以下路径无需 JWT 认证：
- `/api/user/auth/login` - 登录接口
- `/api/user/auth/register` - 注册接口
- `/api/user/auth/send/register/code` - 发送注册验证码
- `/api/user/auth/send/login/code` - 发送登录验证码
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息

## 监控和管理

### Actuator 端点
- 健康检查: `http://localhost:9201/actuator/health`
- 应用信息: `http://localhost:9201/actuator/info`
- 网关路由: `http://localhost:9201/actuator/gateway`

## 开发说明

### 添加新路由
在 `application.yml` 中添加新的路由配置：
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: new-service
          uri: lb://new-service
          predicates:
            - Path=/api/new/**
          filters:
            - StripPrefix=1
```

### 自定义过滤器
实现 `GlobalFilter` 接口并添加 `@Component` 注解：
```java
@Component
public class CustomFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 过滤逻辑
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return 0; // 优先级
    }
}
```

## 注意事项

1. **生产环境配置**
   - 修改 JWT 密钥为强密码
   - 配置具体的 CORS 域名而非通配符
   - 启用 HTTPS
   - 配置合适的限流参数

2. **依赖服务**
   - 确保 Nacos 服务已启动
   - 确保 Redis 服务已启动（如果使用限流）
   - 确保下游微服务已注册到 Nacos

3. **性能优化**
   - 调整 JVM 参数以适应服务器配置
   - 根据实际流量调整限流参数
   - 配置合适的线程池和连接池

## 常见问题

### 1. 无法连接到 Nacos
检查 Nacos 服务是否启动，确认 `application.yml` 中的 Nacos 地址配置正确。

### 2. JWT 验证失败
确认客户端请求头中包含正确的 `Authorization: Bearer <token>` 格式。

### 3. 跨域问题
检查 `CorsConfig` 配置，确保允许的源、方法和头信息正确。

### 4. 限流不生效
确认 Redis 服务已启动并可连接，检查 `rate-limiter.enabled` 是否为 `true`。

## 许可证
本项目采用 MIT 许可证。
