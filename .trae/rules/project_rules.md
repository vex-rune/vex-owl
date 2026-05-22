# Vex-Owl 项目规范

> **场景标签**: `java_ddd`, `rest_api`, `coding_standards`, `project_structure`, `maven_dependency`
> 
> AI在执行Java开发、REST API开发时应主动检索此文件获取项目规范

**项目信息**: Vex-Owl（微服务）  
**技术栈**: Java 21 | Maven 3.x | 架构：DDD - 领域分包



## 规范索引（按场景检索）

### 1. Java开发 (`java_ddd`)
| 分类 | 规范 | 说明 |
|-----|------|------|
| Java | [Java规范](/doc/rules/语言专项规范/Java/01-Java规范.md) | DDD分层、命名、依赖 |
| Java | [异常处理规范](/doc/rules/语言专项规范/Java/02-异常处理规范.md) | 异常分类、处理原则 |
| Java | [日志规范](/doc/rules/语言专项规范/Java/03-日志规范.md) | 级别、脱敏要求 |

### 2. REST API开发 (`rest_api`)
| 分类 | 规范 | 说明 |
|-----|------|------|
| 接口 | [Controller规范](/doc/rules/接口编写规范/01-Controller规范.md) | REST接口编写（包含JavaDoc注释规范、接口命名规范、通用查询规范、日志规范） |
| 接口 | [通用查询规范](/doc/rules/接口编写规范/02-通用查询规范.md) | 分页查询实现 |

### 3. 项目架构 (`project_structure`, `ddd_architecture`)
| 分类 | 规范 | 说明 |
|-----|------|------|
| 架构 | [项目结构规范](/doc/rules/整体架构规范/01-项目结构规范.md) | 模块结构、源码目录 |
| 架构 | [DDD领域规范](/doc/rules/整体架构规范/02-DDD领域规范.md) | 领域层核心原则 |
| 架构 | [依赖管理规范](/doc/rules/整体架构规范/03-依赖管理规范.md) | Maven版本管理 |

### 4. 通用基础 (`coding_standards`)
| 分类 | 规范 | 说明 |
|-----|------|------|
| 通用基础 | [版本控制规范](/doc/rules/通用基础规范/01-版本控制规范.md) | 分支管理、提交规范 |
| 通用基础 | [代码编写通用规范](/doc/rules/通用基础规范/02-代码编写通用规范.md) | 注释、日志、异常 |



## 绝对禁止

> AI在代码生成时必须遵守以下规则，违反将导致严重安全隐患

| 禁止项 | 说明 |
|-------|------|
| ❌ 硬编码密码、密钥、token | 必须使用配置中心或环境变量 |
| ❌ 空catch块 | 必须记录日志或抛出异常 |
| ❌ 日志输出敏感数据 | 敏感信息必须脱敏 |
| ❌ 依赖写死version | 必须使用父pom管理的版本 |
| ❌ 本地配置提交仓库 | 本地配置必须加入.gitignore |



## DDD领域结构

```
模块/
├── api/              # 接口层（Controller）
├── app/              # 应用服务
├── domain/           # 领域层
│   ├── entity/       # 实体
│   ├── repo/         # 仓储
│   └── Manager.java  # 领域服务
└── domain/xxx/      # 聚合根（按领域分包）
```



## API接口规范速查

### 接口命名
| 方法 | 请求方式 | 路径 | 说明 |
|-----|---------|------|------|
| query | POST | /query | 分页多条件查询 |
| list | GET | / | 查询列表 |
| get | GET | /{id} | 查询单个 |
| add | POST | / | 新增数据 |
| edit | PUT | /{id} | 修改数据 |
| delete | DELETE | /{id} | 删除数据 |

### 注解要求
- ✅ ID参数统一使用String类型
- ✅ 新增、修改接口必须使用@Valid
- ✅ 查询参数使用@RequestBody接收
- ✅ 统一使用ApiResponse作为返回封装体
