# Vex Comm Security 模块

公共安全模块，提供JWT令牌管理等核心安全功能。

## 特性

- 🎯 **简洁设计** - 核心JwtTokenProvider工具类
- 🔐 **多算法支持** - 支持HS256和RS256签名算法
- 🛡️ **安全可靠** - 完整的Token验证机制
- 📦 **轻量级** - 无强制依赖Spring
- 🔧 **灵活配置** - 支持自定义配置
- ✅ **完整测试** - 43个单元测试，覆盖率100%

## 快速开始

### 1. 依赖引入

```xml
<dependency>
    <groupId>com.vex</groupId>
    <artifactId>vex-comm-security</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 基本使用

```java
import com.vex.security.jwt.JwtTokenProvider;

public class BasicExample {
    public void generateToken() {
        JwtTokenProvider jwtProvider = new JwtTokenProvider();
        
        String token = jwtProvider.generateAccessToken("user123", null);
        System.out.println("Token: " + token);
        
        if (jwtProvider.validateToken(token)) {
            String subject = jwtProvider.getSubjectFromToken(token);
            System.out.println("Subject: " + subject);
        }
    }
}
```

### 3. 自定义配置

```java
String secretKey = "your-256-bit-secret-key-here-minimum-32-chars";
long accessValidity = 3600;
long refreshValidity = 604800;
String issuer = "my-application";

JwtTokenProvider jwtProvider = new JwtTokenProvider(
    secretKey, 
    accessValidity, 
    refreshValidity, 
    issuer
);
```

### 4. JWT生成与验证

```java
String userId = "user123";
Map<String, Object> claims = Map.of(
    "name", "张三",
    "role", "admin",
    "scope", "read write"
);

String token = jwtProvider.generateAccessToken(userId, claims);

if (jwtProvider.validateToken(token)) {
    String subject = jwtProvider.getSubjectFromToken(token);
    Set<String> scopes = jwtProvider.getScopes(token);
    String clientId = jwtProvider.getClientId(token);
}
```

### 5. RSA签名令牌

```java
String rsaToken = jwtProvider.generateRsaToken(
    "user123",
    Map.of("email", "user@example.com"),
    3600,
    "rsa_access"
);

System.out.println("Public Key: " + jwtProvider.getPublicKeyString());
System.out.println("Private Key: " + jwtProvider.getPrivateKeyString());
```

## 核心API

### JwtTokenProvider

| 方法 | 说明 |
|------|------|
| `generateAccessToken(subjectId, claims)` | 生成访问令牌 |
| `generateRefreshToken(subjectId)` | 生成刷新令牌 |
| `generateToken(subjectId, claims, validity, type)` | 生成自定义令牌 |
| `generateRsaToken(subjectId, claims, validity, type)` | 生成RSA签名令牌 |
| `parseToken(token)` | 解析令牌 |
| `parseRsaToken(token)` | 解析RSA令牌 |
| `validateToken(token)` | 验证令牌有效性 |
| `validateRsaToken(token)` | 验证RSA令牌 |
| `getSubjectFromToken(token)` | 提取用户标识 |
| `getScopes(token)` | 提取作用域集合 |
| `getClientId(token)` | 提取客户端ID |
| `getClaimsFromToken(token)` | 获取所有声明 |
| `isTokenExpired(token)` | 检查是否过期 |

## 测试覆盖

```
Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
Coverage: 100%
```

## License

MIT License
