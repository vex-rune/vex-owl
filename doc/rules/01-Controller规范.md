# Controller接口编写规范

## 适用范围
Vex-Owl项目所有REST API接口开发



## 1. 接口编写规则

### 1.1 类级别规范
- 类头部写明所属模块，附带模块业务简述
- 使用@RestController注解
- 使用@RequestMapping定义路由前缀
- 统一使用ApiResponse作为返回封装体

### 1.2 方法级别规范
| 方法 | 请求方式 | 路径 | 说明 |
|-----|---------|------|------|
| query | POST | /query | 分页多条件查询 |
| list | GET | / | 查询列表 |
| get | GET | /{id} | 查询单个 |
| add | POST | / | 新增数据 |
| edit | PUT | /{id} | 修改数据 |
| delete | DELETE | /{id} | 删除数据 |

### 1.3 参数规范
- ID参数统一使用String类型
- 新增、修改接口使用@Valid开启参数校验
- 查询参数使用@RequestBody接收



## 2. 注释规范

### 2.1 类注释
```java
/**
 * XX模块
 * <p>XX相关业务接口</p>
 */
```

### 2.2 方法注释
```java
/**
 * XX-功能名称
 * <p>接口用途说明</p>
 */
```



## 3. 代码规范（纯示例）

### 3.1 完整Controller示例

```java
package com.vex.owl.user.api;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vex.common.api.ApiResponse;
import com.vex.owl.user.app.UserApp;
import com.vex.owl.user.app.manager.SubjectManager;
import com.vex.owl.user.domain.subject.entity.SubjectEntity;

import lombok.RequiredArgsConstructor;

/**
 * 用户模块
 * <p>用户相关业务接口</p>
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserApi {

    private final UserApp userApp;
    private final SubjectManager subjectManager;

    /**
     * 用户-通用查询
     * <p>分页+多条件组合查询</p>
     */
    @PostMapping("/query")
    public ApiResponse<UserEntity> query(@RequestBody QueriesPageRequest request) {
        return ApiResponse.success(subjectManager.query(request));
    }

    /**
     * 用户-查询列表
     * <p>查询全部用户列表</p>
     */
    @GetMapping
    public ApiResponse<UserEntity> list() {
        return ApiResponse.success(userApp.listUsers());
    }

    /**
     * 用户-查询单个
     * <p>根据ID查询用户详情</p>
     */
    @GetMapping("/{id}")
    public ApiResponse<UserEntity> get(@PathVariable String id) {
        return ApiResponse.success(userApp.getUser(id));
    }

    /**
     * 用户-新增数据
     * <p>创建新用户</p>
     */
    @PostMapping
    public ApiResponse<UserEntity> add(@Valid @RequestBody UserEntity entity) {
        return ApiResponse.success(userApp.createUser(entity));
    }

    /**
     * 用户-修改数据
     * <p>根据ID更新用户信息</p>
     */
    @PutMapping("/{id}")
    public ApiResponse<UserEntity> edit(@PathVariable String id, @Valid @RequestBody UserEntity entity) {
        entity.setId(id);
        return ApiResponse.success(userApp.updateUser(entity));
    }

    /**
     * 用户-删除数据
     * <p>根据ID删除用户</p>
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        userApp.deleteUser(id);
        return ApiResponse.success();
    }
}
```

### 3.2 禁用示例

```java
// ❌ 禁止：ID使用非String类型
@GetMapping("/{id}")
public ApiResponse<User> get(@PathVariable Long id) {  // 应使用String
    // ...
}

// ❌ 禁止：新增不加@Valid
@PostMapping
public ApiResponse<User> add(@RequestBody User entity) {  // 应加@Valid
    // ...
}

// ❌ 禁止：方法名不规范
@GetMapping("/list")  // 应直接用/
public ApiResponse<List<User>> listUsers() {
    // ...
}
```


alwaysApply: true
scene: rest_api