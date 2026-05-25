# 领域驱动设计规范

## 适用范围
Vex-Owl项目DDD架构实现



## 1. 领域层核心原则

### 1.1 领域层能力
- **可使用Spring基础能力**：领域层可使用Spring注入（@Service、@Component）、JPA Repository、事务（@Transactional）等基础能力
- **构建Manager管理类**：领域层可构建Manager类，封装该领域的数据库查询和基础管理逻辑
- **独立测试**：领域层Manager应能通过集成测试验证

### 1.2 领域隔离
- 各领域之间**无交叉依赖**
- 领域间协作通过应用层（app层）协调
- 禁止在领域层直接引用其他领域



## 2. 领域组件规范

### 2.1 领域实体（Entity）
- 包含核心业务数据和业务行为
- 必须有唯一标识（ID）
- 核心业务逻辑在实体中实现
- 示例：Subject（用户主体）、Account（账号）、Order（订单）

### 2.2 领域服务（Service）
- 处理跨实体的业务逻辑
- 不持有状态
- 通过组合多个实体完成任务
- 示例：PasswordService（密码校验服务）

### 2.3 仓储接口（Repository）
- 定义数据访问抽象
- 位于领域层，具体实现在基础设施层
- 方法命名应反映领域语义
- 示例：findById、save、deleteById

### 2.4 领域异常（Exception）
- 定义领域特定异常
- 使用静态工厂方法创建实例
- 包含明确的错误码和消息



## 3. 应用层规范

### 3.1 应用服务（App）
- 协调多个领域服务
- 管理事务边界
- 处理参数转换
- 不包含业务逻辑，仅做编排

### 3.2 应用管理器（Manager）
- 封装通用查询逻辑
- 协调领域和仓储
- 组合常见业务场景

### 3.3 命名规范
- 应用服务：XxxApp.java
- 应用管理器：XxxManager.java



## 4. 示例结构

### 4.1 用户模块领域结构

```
com.vex.owl.user/
├── api/
│   ├── UserApi.java
│   ├── request/
│   └── response/
├── app/
│   ├── UserApp.java
│   └── manager/
│       └── SubjectManager.java
└── domain/
    ├── subject/           # 主体领域
    │   ├── entity/
    │   │   └── Subject.java
    │   ├── service/
    │   │   └── SubjectService.java
    │   ├── repository/
    │   │   └── SubjectRepository.java
    │   └── exception/
    │       └── SubjectException.java
    ├── account/           # 账号领域
    │   ├── entity/
    │   │   └── Account.java
    │   ├── service/
    │   │   └── AccountService.java
    │   ├── repository/
    │   │   └── AccountRepository.java
    │   └── exception/
    │       └── AccountException.java
    ├── login_record/      # 登录日志领域
    │   ├── entity/
    │   │   └── LoginRecord.java
    │   └── ...
    └── password/          # 密码领域
        └── ...
```


alwaysApply: true
scene: ddd_architecture