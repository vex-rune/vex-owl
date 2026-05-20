# Vex-Comm-Security API Reference

公共安全模块，提供JWT令牌管理等核心安全功能。

## 自动配置

本模块实现了 Spring Boot 自动配置，当添加依赖后会自动配置 `JwtTokenProvider` Bean。

### 快速开始

#### 1. 添加依赖

```xml
<dependency>
    <groupId>com.vex</groupId>
    <artifactId>vex-comm-security</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 2. 配置文件

在 `application.yml` 中添加配置：

```yaml
vex:
  jwt:
    secret: your-256-bit-secret-key-here-minimum-32-chars
    access-token-validity: 3600    # 访问令牌有效期(秒)
    refresh-token-validity: 604800  # 刷新令牌有效期(秒)
    issuer: my-application
    enabled: true
```

#### 3. 使用

```java
@RestController
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public String login(@RequestParam String userId) {
        Map<String, Object> claims = Map.of("role", "admin");
        return jwtTokenProvider.generateAccessToken(userId, claims);
    }

    @GetMapping("/validate")
    public boolean validate(@RequestParam String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
```

## 配置属性

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `vex.jwt.secret` | String | `defaultSecretKeyForDevEnvironmentOnly12345` | JWT签名密钥 |
| `vex.jwt.access-token-validity` | Long | `3600` | 访问令牌有效期(秒) |
| `vex.jwt.refresh-token-validity` | Long | `604800` | 刷新令牌有效期(秒) |
| `vex.jwt.issuer` | String | `vex-owl` | JWT签发者 |
| `vex.jwt.header` | String | `Authorization` | HTTP请求头名称 |
| `vex.jwt.prefix` | String | `Bearer ` | Token前缀 |
| `vex.jwt.enabled` | Boolean | `true` | 是否启用JWT功能 |

## JwtTokenProvider API

### 令牌生成

```java
// 生成访问令牌
String accessToken = jwtTokenProvider.generateAccessToken(userId, claims);

// 生成刷新令牌
String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

// 生成自定义令牌
String customToken = jwtTokenProvider.generateToken(
    subjectId,    // 用户标识
    claims,       // 自定义声明
    validity,     // 有效期(秒)
    tokenType     // 令牌类型
);

// 生成RSA签名令牌
String rsaToken = jwtTokenProvider.generateRsaToken(subjectId, claims, validity, type);
```

### 令牌验证

```java
// 验证令牌有效性
boolean isValid = jwtTokenProvider.validateToken(token);

// 验证RSA令牌
boolean isRsaValid = jwtTokenProvider.validateRsaToken(token);

// 检查令牌是否过期
boolean isExpired = jwtTokenProvider.isTokenExpired(token);
```

### 令牌解析

```java
// 解析令牌获取声明
Claims claims = jwtTokenProvider.parseToken(token);

// 解析RSA令牌
Claims rsaClaims = jwtTokenProvider.parseRsaToken(token);

// 获取用户标识
String subject = jwtTokenProvider.getSubjectFromToken(token);

// 获取所有声明
Map<String, Object> allClaims = jwtTokenProvider.getClaimsFromToken(token);

// 获取令牌类型
String type = jwtTokenProvider.getTokenType(token);

// 获取令牌ID
String jti = jwtTokenProvider.getTokenId(token);

// 获取过期时间
Date expiration = jwtTokenProvider.getExpirationFromToken(token);
```

### 作用域提取

```java
// 获取作用域集合
Set<String> scopes = jwtTokenProvider.getScopes(token);

// 获取客户端ID
String clientId = jwtTokenProvider.getClientId(token);
```

### RSA密钥管理

```java
// 生成RSA密钥对
jwtTokenProvider.generateRsaKeyPair();

// 设置RSA密钥对
jwtTokenProvider.setRsaKeyPair(keyPair);

// 获取RSA密钥对
KeyPair keyPair = jwtTokenProvider.getRsaKeyPair();

// 获取公钥(Base64格式)
String publicKey = jwtTokenProvider.getPublicKeyString();

// 获取私钥(Base64格式)
String privateKey = jwtTokenProvider.getPrivateKeyString();
```

## 禁用配置

如需禁用自动配置，可以在配置文件中设置：

```yaml
vex:
  jwt:
    enabled: false
```

## 条件自动配置

本模块会在以下条件满足时自动配置：

1. `vex.jwt.enabled=true`（默认值）
2. 类路径中存在 `JwtTokenProvider`

## 测试

运行单元测试：

```bash
mvn test
```

测试覆盖：
- Tests run: 43
- Failures: 0
- Errors: 0
- Coverage: 100%