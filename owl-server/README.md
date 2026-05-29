# Vex-Owl 后端服务 (Java)

微服务后端架构文档

## 1. 项目概述

Vex-Owl 是一个基于 DDD（领域驱动设计）的微服务架构系统，提供用户认证、通知等功能。

## 2. 技术栈

| 分类 | 技术 | 说明 |
|------|------|------|
| 核心框架 | Java 21 | 开发语言和版本 |
| 构建工具 | Maven 3.x | 项目构建和依赖管理 |
| 服务架构 | Spring Boot 3.x | 核心框架 |
| 服务发现 | Nacos | 微服务注册与配置中心 |
| API 网关 | Spring Cloud Gateway | 统一网关路由 |
| 数据库 | PostgreSQL | 关系型数据存储 |
| 缓存 | Redis | 分布式缓存 |
| ORM | Spring Data JPA | 数据持久化 |
| HTTP 客户端 | OpenFeign | 服务间通信 |
| 负载均衡 | Spring Cloud LoadBalancer | 客户端负载均衡 |

## 3. 服务模块

| 服务 | 端口 | 说明 |
|------|------|------|
| [Gateway](#41-网关服务-9201) | 9201 | API 网关，统一入口 |
| [User Service](#42-用户服务-9202) | 9202 | 用户认证、账号管理 |
| [Notification Service](#43-通知服务-9206) | 9206 | 邮件、短信通知 |
| [AI Service](#44-ai-服务-9203) | 9203 | AI 相关功能 |

### 3.1 服务通信架构

```
┌─────────────┐
│   Client     │
└──────┬──────┘
       │
       ▼
┌──────────────┐
│   Gateway    │ :9201
└──────┬──────┘
       │
       ├──────────────────────────────┐
       ▼                              ▼
┌──────────────┐          ┌──────────────┐
│ User Service │ :9202     │Notification │ :9206
└──────────────┘            └──────────────┘
```

## 4. 服务详情

### 4.1 网关服务 (9201)

**端口**: 9201

**职责**: 统一入口、路由转发、认证鉴权

**依赖组件**:
- Spring Cloud Gateway
- Nacos Client
- JJWT (Token 验证)

**路由配置**:

| 路由规则 | 目标服务 | 路径处理 |
|----------|----------|----------|
| `/api/user/**` | user-server | 去除 `/api` 前缀 |
| `/api/notification/**` | notification-server | 去除 `/api` 前缀 |

**环境变量**:

| 变量 | 默认值 | 说明 |
|------|--------|------|
| NACOS_ADDR | 127.0.0.1:8848 | Nacos 服务地址 |
| NACOS_USERNAME | nacos | Nacos 用户名 |
| NACOS_PASSWORD | nacos | Nacos 密码 |
| JWT_SECRET | (配置值 | JWT 签名密钥 |

### 4.2 用户服务 (9202)

**端口**: 9202

**职责**: 用户档案、账号管理、登录认证、登录日志

**数据源配置**:
```yaml
数据库: localhost:5432/vex_owl
用户名: vex_user
密码: vex_password
```

**缓存配置**:
```yaml
Redis: localhost:6379
密码: VexOwl2026@Redis
数据库: 0
```

**功能模块**:
- 用户档案管理 (UserProfile)
- 账号管理 (Account)
- 登录认证 (Auth)
- 登录日志 (LoginRecord)

### 4.3 通知服务 (9206)

**端口**: 9206

**职责**: 邮件发送、模板管理

**依赖组件**:
- PostgreSQL
- Nacos Config

**邮件配置**:
```yaml
SMTP 主机: smtpdm.aliyun.com
端口: 465
发件人: noreply@example.com
```

**功能模块**:
- 邮件发送 (Email)
- 模板管理 (Template)
- 通知客户端 (NotificationClient)

### 4.4 AI 服务 (9203)

**端口**: 9203

**职责**: AI 相关功能扩展

## 5. DDD 分层架构

每个业务服务遵循以下分层结构:

```
src/main/java/
└── com.vex.owl.{module}/
    ├── api/                      # 接口层
    │   └── {domain}/              # 按领域分包
    │       └── admin/            # 管理端接口
    │           └── XxxAdminApi.java
    ├── app/                      # 应用服务层
    │   └── XxxApp.java
    ├── domain/                   # 领域层
    │   ├── entity/               # 实体
    │   ├── repo/                 # 仓储接口
    │   ├── Manager.java           # 领域服务
    │   └── {domain}/           # 按领域分包
    │       └── manager/
    └── repository/               # 基础设施层
        └── impl/                # 仓储实现
```

**分层职责**:
| 层级 | 职责 |
|------|------|
| api | HTTP 请求处理、参数校验、日志记录 |
| app | 业务编排、事务管理 |
| domain | 核心业务逻辑 |
| repository | 数据访问实现 |

## 6. API 路由

### 6.1 认证模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /auth/login | 用户登录 |
| POST | /auth/logout | 用户登出 |

### 6.2 用户模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /admin/subject/query | 主体通用查询 |
| POST | /admin/account/query | 账号通用查询 |
| POST | /admin/user/query | 用户档案通用查询 |
| GET | /admin/user/{id} | 查询单个用户 |
| POST | /admin/login/log/query | 登录日志查询 |

### 6.3 通知模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /admin/template/query | 模板列表查询 |
| GET | /admin/template/{id} | 查询模板详情 |
| POST | /admin/template | 创建模板 |
| PUT | /admin/template/{id} | 更新模板 |
| DELETE | /admin/template/{id} | 删除模板 |
| POST | /admin/email/send | 发送邮件 |

## 7. 通用查询规范

### 7.1 QueriesPageRequest 结构

```json
{
  "predicate": [
    {"field": "字段名", "op": "操作符", "value": "值"}
  ],
  "order": [
    {"field": "排序字段", "direction": "asc|desc"}
  ],
  "page": {"page": 0, "size": 20}
}
```

### 7.2 操作符 (op)

| op | 说明 |
|----|------|
| eq | 等于 |
| ne | 不等于 |
| like | 模糊匹配 |
| gt | 大于 |
| gte | 大于等于 |
| lt | 小于 |
| lte | 小于等于 |
| in | IN 查询 |

## 8. 启动说明

### 8.1 前置条件

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8848 | 配置中心和服务注册 |
| PostgreSQL | 5432 | 关系型数据库 |
| Redis | 6379 | 分布式缓存 |

### 8.2 Docker Compose 启动 (推荐)

```bash
cd docker/vex-group
docker-compose -f nacos.yml -f postgres.yml -f redis.yml up -d
```

### 8.3 Maven 启动

```bash
cd java/vex-owl-server/vex-owl-server-gateway
mvn spring-boot:run
```

## 9. 日志规范

### 9.1 日志级别

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| INFO | 关键业务流程节点 | 接口调用、任务开始/完成 |
| DEBUG | 开发调试信息 | SQL 语句、变量值 |
| WARN | 潜在问题 | 参数校验失败、缺少数据 |
| ERROR | 错误异常 | 方法失败、异常捕获 |

### 9.2 敏感信息保护

**禁止日志输出**:
- 用户密码、支付密码
- 密钥、Token、SecretKey
- 完整手机号、身份证号、银行卡号

**脱敏原则**:
- 手机号: `138****5678`
- 身份证: `110101****1234****`
- 银行卡: `****1234` (仅保留后 4 位)

## 10. 开发规范

### 10.1 代码注释

| 类型 | 注释位置 | 说明 |
|------|----------|------|
| 类注释 | 类开头 | 模块功能描述 |
| 方法注释 | 方法定义前 | 参数说明、返回值说明 |
| 行内注释 | 代码行尾 | 复杂逻辑说明 |

### 10.2 异常处理

```java
// 正确示例
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("功能名称失败, error: {}", e.getMessage(), e);
    throw new BusinessException("错误信息");
}

// 错误示例 (禁止空 catch)
catch (Exception e) {
    // 空 catch 块
}
```

### 10.3 配置文件

敏感配置必须使用环境变量或 Nacos 配置中心，**禁止硬编码**:
```yaml
# 正确
password: ${DB_PASSWORD}

# 错误
password: my_secret_password
```

## 11. 项目结构

```
java/
├── vex-comm/                        # 公共模块
│   ├── vex-comm-auth/              # 认证公共模块
│   ├── vex-comm-criteria/           # 查询条件构建
│   ├── vex-comm-criteria-jpa/       # JPA 查询支持
│   └── vex-comm-model/             # 通用模型
│
├── vex-owl-server/                  # 业务服务
│   ├── pom.xml
│   ├── vex-owl-server-gateway/      # 网关服务
│   ├── vex-owl-server-user/        # 用户服务
│   ├── vex-owl-server-notification/ # 通知服务
│   │   ├── api/                    # Feign 客户端接口
│   │   └── impl/                 # 服务端实现
│   └── vex-owl-server-ai/        # AI 服务
│
└── doc/rules/                      # 开发规范文档
```
