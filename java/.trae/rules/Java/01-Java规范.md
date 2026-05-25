# Java 语言专项规范

## 适用范围
所有Java模块、所有Java开发人员



## 1. 技术栈标准

| 项目 | 版本 |
|-----|------|
| Java | 21 |
| 构建工具 | Maven 3.x |
| Spring Boot | 3.x |
| 持久化 | JPA |



## 2. 命名规范

### 2.1 类命名
| 类型 | 规范 | 示例 |
|-----|------|------|
| API接口 | XxxApi.java | AuthApi, UserApi |
| 应用服务 | XxxApp.java | AuthApp |
| 应用管理器 | XxxManager.java | UserManager |
| 领域实体 | Xxx.java | User, Order |
| 领域服务 | XxxService.java | UserService |
| 仓储接口 | XxxRepository.java | UserRepository |
| 领域异常 | XxxException.java | UserException |
| 请求DTO | XxxRequest.java | LoginRequest |
| 响应DTO | XxxResponse.java | LoginResponse |

### 2.2 变量命名
- 使用有意义的英文命名
- 采用驼峰命名法（camelCase）
- 布尔变量使用is/has/can前缀
- 常量使用全大写+下划线

### 2.3 包命名
- 根包：com.vex
- 业务：com.vex.owl.{模块名}
- 公共：com.vex.{组件名}



## 3. DDD领域分层

### 3.1 标准分层结构

```
com.vex.owl.{模块名}
├── api                    # 接口层
│   ├── XXXApi.java       # 接口控制器
│   ├── request            # 请求参数
│   └── response           # 返回结果
├── app                    # 应用层
│   └── XXXApp.java       # 应用服务
└── domain                 # 领域层
    ├── entity/            # 领域实体
    ├── service/           # 领域服务
    ├── repository/        # 仓储接口
    └── exception/         # 领域异常
```

### 3.2 各层职责

| 层级 | 职责 | 约束 |
|------|------|------|
| **api** | HTTP接入、参数校验、响应封装 | 依赖 app 层 |
| **app** | 业务流程编排、事务控制、跨领域协作 | 依赖 domain 层 |
| **domain** | 核心业务逻辑、领域模型 | **纯Java，禁止依赖框架** |

### 3.3 领域分包

每个领域包含四个标准组件：
```
domain/
├── {领域名}/
│   ├── entity/           # 领域实体
│   │   └── XxxEntity.java
│   ├── service/         # 领域服务
│   │   └── XxxService.java
│   ├── repository/      # 仓储接口
│   │   └── XxxRepository.java
│   └── exception/      # 领域异常
│       └── XxxException.java
```



## 4. 核心原则

1. **domain是核心**：纯Java实体和接口，不依赖Spring、JPA等框架
2. **领域隔离**：各领域之间无交叉依赖，通过app层协调
3. **依赖单向**：api → app → domain，禁止反向依赖
4. **版本统一**：所有版本在根pom.xml的properties中定义



## 5. Import导入规范

### 5.1 必须使用import
- 所有Java类都必须使用import导入
- 禁止在代码中直接使用全限定类名（全路径）

### 5.2 正确示例
```java
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vex.owl.user.domain.subject.exception.SubjectException;
```

### 5.3 禁止示例
```java
// ❌ 禁止：直接使用全路径
private java.time.LocalDateTime createTime;

// ✅ 正确：使用import
import java.time.LocalDateTime;
private LocalDateTime createTime;
```

### 5.4 允许使用全路径的场景
- JDK内部类与用户自定义类同名冲突时（如自定义List类）
- 静态导入（import static）用于常量或工具方法



## 6. 代码规范（纯示例）

### 6.1 领域实体示例

```java
package com.vex.owl.user.domain.subject.entity;

import java.time.LocalDateTime;

import com.vex.owl.user.domain.subject.exception.SubjectException;

/**
 * 用户主体
 */
public class Subject {
    private String id;
    private String username;
    private String email;
    private LocalDateTime createTime;
    
    public void validate() {
        if (username == null || username.isBlank()) {
            throw new SubjectException("用户名不能为空");
        }
    }
}
```

### 6.2 领域服务示例

```java
package com.vex.owl.user.domain.subject.service;

import com.vex.owl.user.domain.subject.entity.Subject;

/**
 * 用户主体服务
 */
public class SubjectService {
    
    public Subject create(String username, String email) {
        Subject subject = new Subject();
        subject.setUsername(username);
        subject.setEmail(email);
        subject.validate();
        return subject;
    }
}
```

### 6.3 领域异常示例

```java
package com.vex.owl.user.domain.subject.exception;

/**
 * 用户主体异常
 */
public class SubjectException extends RuntimeException {
    
    public SubjectException(String message) {
        super(message);
    }
}
```

### 6.4 仓储接口示例

```java
package com.vex.owl.user.domain.subject.repository;

import com.vex.owl.user.domain.subject.entity.Subject;

/**
 * 用户主体仓储
 */
public interface SubjectRepository {
    Subject findById(String id);
    Subject save(Subject subject);
    void delete(String id);
}
```



## 7. 依赖管理规范

### 7.1 强制规则
- **所有版本只在根pom.xml的properties里定义一次**
- 子模块禁止写version
- 使用dependencyManagement统一管理版本

### 7.2 禁止规则
- 禁止引入来源不明的依赖
- 禁止引入有已知漏洞的包
- 禁止引入与Spring Boot 3.x不兼容的包


alwaysApply: true
scene: java_ddd