# Controller接口编写规范

## 适用范围
Vex-Owl项目所有REST API接口开发



## 1. 接口命名规范

### 1.1 接口类命名规则
| 接口类型 | 命名规范 | 使用场景 |
|---------|---------|---------|
| 管理端接口 | `XxxAdminApi` | 系统管理员操作接口 |
| 客户端接口 | `XxxAppApi` | 普通用户客户端接口 |
| 聚合接口 | `XxxAggApi` | 聚合复杂业务接口 |

**示例**：
```java
// ✅ 正确示例
public class AccountAdminApi { }        // 账号管理接口
public class UserAppApi { }              // 用户端接口
public class DashboardAggApi { }         // 仪表盘聚合接口

// ❌ 错误示例
public class AdminAccountApi { }         // 命名顺序错误
public class AccountManagerApi { }       // 不应包含Manager
public class AccountController { }        // 不应使用Controller后缀
```

### 1.2 方法命名规则
| 方法 | 请求方式 | 路径 | 说明 |
|-----|---------|------|------|
| query | POST | /query | 分页多条件查询 |
| list | GET | / | 查询列表 |
| get | GET | /{id} | 查询单个 |
| add | POST | / | 新增数据 |
| edit | PUT | /{id} | 修改数据 |
| delete | DELETE | /{id} | 删除数据 |



## 2. 接口编写规则

### 2.1 类级别规范
- 类头部写明所属模块，附带模块业务简述
- 使用@RestController注解
- 使用@RequestMapping定义路由前缀
- 统一使用ApiResponse作为返回封装体

### 2.2 参数规范
- ID参数统一使用String类型
- 新增、修改接口使用@Valid开启参数校验
- 查询参数使用@RequestBody接收



## 3. 注释规范

### 3.1 类注释要求
接口类的类级Java文档注释必须详尽清晰、表意直观，该注释将直接用于生成统一的开放式接口文档。

**必须包含以下内容**：
```java
/**
 * 功能概述
 * <p>
 * 核心功能描述和应用场景说明。
 * </p>
 *
 * <h2>应用场景</h2>
 * <ul>
 *   <li>场景1</li>
 *   <li>场景2</li>
 * </ul>
 *
 * <h2>入参说明</h2>
 * <p>参数说明和约束条件</p>
 *
 * <h2>出参说明</h2>
 * <p>返回数据结构说明</p>
 *
 * <h2>异常处理</h2>
 * <ul>
 *   <li>异常类型及处理策略</li>
 * </ul>
 *
 * <h2>鉴权要求</h2>
 * <p>权限要求和安全注意事项</p>
 *
 * <h2>使用限制</h2>
 * <ul>
 *   <li>性能考量</li>
 *   <li>版本兼容性</li>
 * </ul>
 *
 * @see 相关类
 */
```

### 3.2 方法注释要求
```java
/**
 * 功能名称-操作类型
 * <p>接口用途详细说明</p>
 *
 * @param 参数说明
 * @return 返回值说明
 */
```



## 4. 通用查询规范

### 4.1 控制器层实现
```java
@PostMapping("/query")
public ApiResponse<List<Entity>> query(@Valid @RequestBody QueriesPageRequest request) {
    log.info("功能名称通用查询, request: {}", request);
    List<Entity> result = manager.query(request);
    return ApiResponse.success(result);
}
```

### 4.2 领域层实现
```java
public List<Entity> query(QueriesPageRequest request) {
    log.debug("功能名称通用查询, request: {}", request);
    return JpaQueriesExecutor.of(Entity.class, entityManager)
            .page(request);
}
```

**要点**：
- 控制器使用`@PostMapping`注解，路径统一为`/query`
- 入参使用`@Valid`注解进行参数校验
- 方法体内必须包含日志记录
- 领域层使用`JpaQueriesExecutor`实现分页查询
- 返回类型为`List<实体类>`



## 5. 日志规范

### 5.1 日志级别使用原则
| 级别 | 使用场景 |
|-----|---------|
| info | 关键业务流程节点（如创建、更新、删除操作） |
| debug | 调试信息和通用查询 |
| error | 错误信息并包含完整堆栈 |

### 5.2 日志内容规范
- 删除日志中所有「管理接口」类前缀备注
- 日志内容保留关键有效信息，剔除多余前缀
- 使用中文功能名称

**示例**：
```java
// ✅ 正确
log.info("创建账号, subjectId: {}, account: {}", subjectId, account);
log.debug("账号通用查询, request: {}", request);

// ❌ 错误
log.info("[管理接口] 创建账号, subjectId: {}", subjectId);
log.info("执行查询操作, 请求参数为: {}", request);
```



## 6. 代码示例

### 6.1 完整Controller示例

```java
package com.vex.owl.auth.api.admin;

import com.vex.model.ApiResponse;
import com.vex.owl.auth.domain.account.AccountManager;
import com.vex.owl.auth.domain.account.model.AccountEntity;
import com.vex.queries.model.queries.model.QueriesPageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账号管理接口
 * <p>
 * 提供系统账号的通用查询功能，支持分页、排序和多条件组合查询。
 * 该接口面向系统管理员，用于账号信息的查询、统计和管理操作。
 * </p>
 *
 * <h2>应用场景</h2>
 * <ul>
 *   <li>管理员查询账号列表（支持分页筛选）</li>
 *   <li>按邮箱、类型等条件检索账号</li>
 *   <li>账号数据的统计分析</li>
 * </ul>
 *
 * <h2>入参说明</h2>
 * <p>QueriesPageRequest包含以下可选字段：</p>
 * <ul>
 *   <li>predicate: 查询条件，支持eq、like、in等操作符组合</li>
 *   <li>order: 排序条件，指定排序字段和方向</li>
 *   <li>page: 分页参数，指定页码(page)和每页数量(size)</li>
 * </ul>
 *
 * <h2>出参说明</h2>
 * <p>返回ApiResponse.success()，data为List&lt;AccountEntity&gt;</p>
 *
 * <h2>异常处理</h2>
 * <ul>
 *   <li>参数校验失败：返回400 Bad Request</li>
 *   <li>服务端异常：返回500 Internal Server Error</li>
 * </ul>
 *
 * <h2>鉴权要求</h2>
 * <p>该接口需要管理员权限，请携带有效的JWT Token访问</p>
 *
 * <h2>使用限制</h2>
 * <ul>
 *   <li>分页查询建议每页不超过100条</li>
 *   <li>复杂查询条件建议控制在5个以内</li>
 * </ul>
 *
 * @see AccountEntity
 * @see QueriesPageRequest
 */
@RestController
@RequestMapping("/api/v1/admin/account")
@RequiredArgsConstructor
@Slf4j
public class AccountAdminApi {

    private final AccountManager accountManager;

    /**
     * 账号-通用查询
     * <p>支持分页、排序和多条件组合查询</p>
     *
     * @param request 查询条件参数，包含predicate、order、page
     * @return 账号列表
     */
    @PostMapping("/query")
    public ApiResponse<List<AccountEntity>> query(@Valid @RequestBody QueriesPageRequest request) {
        log.info("账号通用查询, request: {}", request);
        List<AccountEntity> result = accountManager.query(request);
        return ApiResponse.success(result);
    }
}
```

### 6.2 禁用示例

```java
// ❌ 禁止：ID使用非String类型
@GetMapping("/{id}")
public ApiResponse<User> get(@PathVariable Long id) {
    // ...
}

// ❌ 禁止：新增不加@Valid
@PostMapping
public ApiResponse<User> add(@RequestBody User entity) {
    // ...
}

// ❌ 禁止：方法名不规范
@GetMapping("/list")
public ApiResponse<List<User>> listUsers() {
    // ...
}

// ❌ 禁止：日志包含管理接口前缀
log.info("[管理接口] 查询账号, userId: {}", userId);

// ❌ 禁止：接口命名不符合规范
public class AccountController { }  // 应使用AdminApi后缀
public class AdminAccountApi { }     // 命名顺序错误
```


alwaysApply: true
scene: rest_api