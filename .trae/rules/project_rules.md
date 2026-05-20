下面给你一版**极简、直白、不重复、可直接作为 .trae 规则文件使用**的版本，只留必须遵守的核心规则，去掉啰嗦和重复说明。

---

# Trae 项目规则（极简版）
项目：Vex-Owl（微服务）  
Java：21  
构建：Maven 3.x  
架构风格：**DDD** - 领域分包

## 1. 项目结构
- 只有两类顶级模块：**vex-comm（公共）**、**vex-owl-server（业务）**
- 业务模块必须继承 vex-owl-server/pom.xml
- 公共模块必须继承 vex-comm/pom.xml
- 禁止新增其他顶级模块
- 源码固定在：src/main/java、src/test/java

## 2. 包名规范
- 根包：**com.vex**
- 业务：com.vex.owl.{模块名}
- 公共：com.vex.{组件名}

## 3. 项目分包结构

### 3.1 标准结构

```
com.vex.owl.{模块名}
├── api                    # 接口层
│   ├── XXXApi.java       # 接口控制器
│   ├── request            # 请求参数
│   └── response           # 返回结果
├── app                    # 应用层
│   └── XXXApp.java            # 应用服务
└── domain                 # 领域层（按业务划分）
    ├── subject             # 主体领域
    ├── account             # 账号领域
    ├── login_record        # 登录日志领域
    └── password            # 密码校验领域
```

### 3.2 分层职责

| 层级 | 职责 | 约束 |
|------|------|------|
| **api** | HTTP接入、参数校验、响应封装 | 依赖 app 层 |
| **app** | 业务流程编排、事务控制、跨领域协作 | 依赖 domain 层 |
| **domain** | 核心业务逻辑、领域模型、领域服务 | **纯Java，禁止依赖任何外部层** |

### 3.3 领域分包

每个领域包含：
- `entity/` - 领域实体
- `service/` - 领域服务
- `repository/` - 仓储接口
- `exception/` - 领域异常

```
domain/
├── subject/              # 主体领域
│   ├── entity/
│   │   └── Subject.java
│   ├── service/
│   │   └── SubjectService.java
│   ├── repository/
│   │   └── SubjectRepository.java
│   └── exception/
│       └── SubjectException.java
├── account/              # 账号领域
│   ├── entity/
│   │   └── Account.java
│   ├── service/
│   │   └── AccountService.java
│   ├── repository/
│   │   └── AccountRepository.java
│   └── exception/
│       └── AccountException.java
├── login_record/         # 登录日志领域
│   ├── entity/
│   │   └── LoginRecord.java
│   ├── service/
│   │   └── LoginRecordService.java
│   ├── repository/
│   │   └── LoginRecordRepository.java
│   └── exception/
│       └── LoginRecordException.java
└── password/            # 密码校验领域
    ├── service/
    │   └── PasswordService.java
    └── exception/
        └── PasswordException.java
```

### 3.4 应用分包

每个应用管理器：
- 负责特定业务领域的流程编排
- 包含查询、新增、修改等操作


### 3.5 命名规范

| 组件 | 命名规范 | 示例 |
|------|---------|------|
| API接口 | `XxxApi.java` | `AuthApi`、`AdminApi` |
| 应用服务 | `XxxApp.java` | `AuthApp` |
| 应用管理器 | `XxxManager.java` | `SubjectManager` |
| 领域实体 | `Xxx.java` | `Subject`、`Account` |
| 领域服务 | `XxxService.java` | `SubjectService` |
| 仓储接口 | `XxxRepository.java` | `SubjectRepository` |
| 领域异常 | `XxxException.java` | `LoginException` |
| 请求DTO | `XxxRequest.java` | `LoginRequest` |
| 响应DTO | `XxxResponse.java` | `LoginResponse` |

### 3.6 核心原则

1. **domain 是核心**：纯Java实体和接口，不依赖 Spring、JPA 等任何框架
2. **领域完全隔离**：各领域之间无交叉依赖，通过 app 层协调
3. **api 扁平化**：接口层直接放控制器，不进一步分包
4. **app 是编排中心**：协调各领域服务完成业务流程

## 4. 依赖管理（强制）
- **所有版本只在根 pom.xml 的 properties 里写一次**
- 子模块**不允许写 version**，全部靠父 pom 管理
- 统一用 dependencyManagement 管控依赖版本
- 禁止引入：来源不明、有已知漏洞、与 Spring Boot 3.x 不兼容的包

## 5. 代码规范
### 5.1 注释
- 类、公共方法必须有注释，说明用途
- 复杂逻辑加行内注释
- 需要让新手能快速理解代码, 不要写复杂的注释

### 5.2 日志
- 用 SLF4J + Lombok @Slf4j
- 日志级别正确：info/debug/warn/error
- **禁止打密码、密钥、手机号等敏感信息**
- 日志输出要过滤敏感数据
- 必要的时候, 需要打印耗时信息

### 5.3 异常
- 禁止空 catch
- 异常要打日志并给出明确提示
- 统一异常处理，不要到处 try-catch

## 6. 安全
- **禁止硬编码密码、密钥、token**
- 接口权限必须校验
- 输入要防 SQL 注入、XSS

## 7. 分支规范
- main：线上稳定版，只合并，不直接提交
- develop：开发主分支
- feature/xxx：新功能
- fix/xxx：bug 修复
- release/vX.Y.Z：发布准备

## 8. 提交规范
- 提交信息格式：**类型: 描述**
  - 类型：feat / fix / docs / refactor / test / chore
- 禁止提交：本地配置、敏感信息、临时文件

## 9. 绝对禁止
- ❌ 硬编码密码、密钥
- ❌ 空 catch 块
- ❌ 日志输出敏感数据
- ❌ 重复工具类
- ❌ 绕过安全校验
- ❌ 依赖写死 version
- ❌ 本地配置提交到仓库