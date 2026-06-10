# vex-comm-auth

JWT 令牌管理 + 共享数据模型。

## 自动加载

| Bean | 方式 | 条件 |
|------|------|------|
| `JwtTokenProvider` | `JwtSecurityAutoConfiguration` | `vex.jwt.enabled=true`（默认） |

通过 `vex.jwt.*` 配置属性控制行为。

## 配置项

```yaml
vex:
  jwt:
    enabled: true                    # 是否启用 JWT
    secret: your-secret-key          # 签名密钥（生产环境必须修改）
    access-token-validity: 3600      # Access Token 有效期（秒）
    refresh-token-validity: 604800   # Refresh Token 有效期（秒）
    issuer: vex-owl                  # 签发者
    header: Authorization            # HTTP 请求头名称
    prefix: "Bearer "               # Token 前缀
```

## 核心类

### JwtTokenProvider

JWT 令牌的生成与验证。

```java
@Autowired
private JwtTokenProvider jwtTokenProvider;

// 生成令牌
VexToken token = jwtTokenProvider.generateToken(currentUser);

// 验证令牌
Claims claims = jwtTokenProvider.validateToken(jwtString);

// 从令牌中提取用户
CurrentUser user = jwtTokenProvider.getUserFromToken(jwtString);
```

### CurrentUser

当前用户视图对象（`com.vex.security.auth.CurrentUser`）。

| 字段 | 说明 |
|------|------|
| `subjectId` | 用户唯一标识 |
| `nickName` | 昵称 |
| `phone` | 手机号 |
| `email` | 邮箱 |
| `authorities` | 权限/角色集合 |
| `role` | 角色 |

### AuthHeaders / AuthHeaderConstants

HTTP 请求头数据模型与常量定义，供 Gateway 转发和 Web 层使用。

### JwtClaimConstants

JWT Payload 中的 Claim 名称常量（`userId`、`userName`、`loginTime` 等）。

## 依赖方接入

```xml
<dependency>
    <groupId>com.vex</groupId>
    <artifactId>vex-comm-auth</artifactId>
</dependency>
```

配置 `vex.jwt.secret` 即可，`JwtTokenProvider` 自动注入。
