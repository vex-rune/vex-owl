# vex_owl_admin

Vex-Owl 管理后台 - Flutter 响应式应用

## 项目概述

- **项目名称**: vex_owl_admin
- **项目类型**: Flutter 响应式后台管理应用
- **目标平台**: Web (桌面/浏览器) + Mobile (手机/平板)
- **核心功能**: 管理员登录、用户管理、通知模板管理

---

## 技术栈

| 分类 | 技术选型 | 说明 |
|-----|---------|------|
| SDK | Flutter ^3.11.5 | 跨平台 UI 框架 |
| 状态管理 | Riverpod | 编译时安全、测试友好 |
| 网络请求 | Dio | 功能丰富的 HTTP 客户端 |
| 路由 | GoRouter | 声明式路由管理 |
| 响应式布局 | Responsive_framework / MediaQuery | 自适应不同屏幕 |
| UI 组件 | Material Design 3 | 统一的视觉风格 |

---

## 项目结构

```
lib/
├── main.dart                    # 应用入口
├── app/
│   ├── app.dart                 # 应用配置
│   └── router.dart              # 路由配置
├── core/
│   ├── theme/                   # 主题配置
│   │   ├── app_theme.dart       # 主题定义
│   │   └── app_colors.dart      # 颜色常量
│   ├── constants/               # 常量定义
│   │   └── app_constants.dart   # 应用常量
│   ├── utils/                  # 工具类
│   │   └── responsive_utils.dart # 响应式工具
│   └── extensions/             # 扩展方法
├── data/
│   ├── api/                    # API 客户端
│   │   ├── api_client.dart     # Dio 配置
│   │   ├── api_constants.dart  # API 地址常量
│   │   └── services/           # 服务层
│   │       ├── auth_service.dart
│   │       ├── user_service.dart
│   │       └── template_service.dart
│   ├── models/                 # 数据模型
│   │   ├── login_request.dart
│   │   ├── token_response.dart
│   │   ├── user_models.dart
│   │   └── template_models.dart
│   └── repositories/           # 仓储层
├── domain/
│   └── providers/             # Riverpod Providers
├── presentation/
│   ├── pages/                 # 页面
│   │   ├── login/             # 登录页
│   │   ├── home/              # 主页
│   │   ├── user/              # 用户管理
│   │   └── template/          # 通知模板管理
│   ├── widgets/               # 通用组件
│   │   ├── common/            # 公共组件
│   │   │   ├── app_scaffold.dart
│   │   │   ├── responsive_layout.dart
│   │   │   ├── data_table_view.dart
│   │   │   └── form_fields.dart
│   │   └── charts/            # 图表组件
│   └── styles/                # 样式
└── shared/
    └── widgets/               # 共享组件
```

---

## 功能模块设计

### 1. 登录模块

#### 页面
- `LoginPage` - 管理员登录页

#### 功能
- 账号密码登录
- 记住登录状态
- 登录状态持久化 (Token 存储)

#### API 对接
- `POST /api/user/auth/login` - 管理员登录

---

### 2. 主页模块

#### 页面
- `HomePage` - 主页 (含侧边导航 + 内容区)

#### 功能
- **快捷入口** (Web: 卡片网格 / Mobile: 列表)
  - 用户管理入口
  - 通知模板管理入口
  - 今日数据统计
- **通知中心**
  - 未读通知数量
  - 通知列表
  - 标记已读

#### 布局结构
```
┌─────────────────────────────────────────┐
│  AppBar (Logo + 管理员信息 + 退出)       │
├──────────┬──────────────────────────────┤
│          │                              │
│  Sidebar │      Content Area            │
│  (导航)   │      (根据路由显示)           │
│          │                              │
│          │                              │
└──────────┴──────────────────────────────┘

Mobile: Drawer 侧滑导航
```

---

### 3. 用户管理模块

#### 页面
| 页面 | 路径 | 功能 |
|-----|------|------|
| `UserListPage` | `/users` | 主体列表 |
| `AccountListPage` | `/users/accounts` | 账号列表 |
| `UserDetailPage` | `/users/detail/:subjectId` | 用户详情 (综合) |
| `LoginLogPage` | `/users/login-logs` | 登录日志列表 |

#### 综合详情页 (主体 ID 查询)
```
┌─────────────────────────────────────────┐
│  用户详情                               │
├─────────────────────────────────────────┤
│  [Tab] 主体信息 │ 用户信息 │ 账号 │ 登录日志 │
├─────────────────────────────────────────┤
│                                         │
│  Tab 内容区域                           │
│                                         │
└─────────────────────────────────────────┘
```

#### API 对接
| 功能 | 接口 | 方法 |
|-----|------|------|
| 主体列表 | `/api/user/admin/subject/query` | POST |
| 账号列表 | `/api/user/admin/account/query` | POST |
| 用户列表 | `/api/user/admin/user/query` | POST |
| 登录日志 | `/api/user/admin/login/log/query` | POST |
| 用户详情 | `/api/user/admin/user/{userId}` | GET |

#### 关系说明
- **主体 (Subject)**: 顶级实体，用户唯一标识
- **用户 (UserProfile)**: 用户档案信息，subjectId = userId
- **账号 (Account)**: 账号信息，关联 subjectId
- **登录日志 (LoginRecord)**: 记录登录行为，关联 subjectId

---

### 4. 通知模板管理模块

#### 页面
| 页面 | 路径 | 功能 |
|-----|------|------|
| `TemplateListPage` | `/templates` | 模板列表 |
| `TemplateEditPage` | `/templates/edit/:id` | 模板修改页 |
| `TemplatePreviewPage` | `/templates/preview/:id` | 模板预览页 |

#### 模板列表
- 分页列表展示
- 搜索功能 (按名称/编码)
- 启用/禁用状态筛选
- 操作按钮: 编辑、预览、删除

#### 模板修改页
- 表单字段: 名称、编码、内容、备注、启用状态
- 变量占位符提示: `{{param}}`
- 保存/取消按钮

#### 模板预览页
- 模拟参数输入
- 实时渲染预览
- 变量高亮显示

#### API 对接
| 功能 | 接口 | 方法 |
|-----|------|------|
| 模板列表 | `/api/notification/admin/template/query` | POST |
| 模板详情 | `/api/notification/admin/template/{id}` | GET |
| 按编码查询 | `/api/notification/admin/template/code/{code}` | GET |
| 创建模板 | `/api/notification/admin/template` | POST |
| 修改模板 | `/api/notification/admin/template/{id}` | PUT |
| 删除模板 | `/api/notification/admin/template/{id}` | DELETE |

---

## 响应式布局设计

### 断点定义

| 断点 | 宽度范围 | 布局模式 |
|-----|---------|---------|
| Mobile | < 600px | 单列布局，底部导航 |
| Tablet | 600px - 1024px | 侧边栏收起，顶部导航 |
| Desktop | > 1024px | 完整侧边栏 + 内容区 |

### 响应式策略

1. **布局适配**
   - 使用 `LayoutBuilder` + `Breakpoints` 判断屏幕宽度
   - 桌面端: 侧边导航 (240px) + 内容区
   - 平板端: 折叠侧边栏 (icon only) + 内容区
   - 手机端: Drawer 导航 + 全宽内容

2. **组件适配**
   - DataTable → CardList (移动端)
   - 宽表单 → 分步骤表单
   - 大图 → 小图缩略

3. **导航适配**
   - Web: 左侧 Sidebar
   - Mobile: BottomNavigationBar 或 Drawer

---

## 数据模型

### 认证相关
```dart
// 登录请求
class LoginRequest {
  String principal;    // 账号
  String credentials;  // 密码
  String loginType;    // "admin"
}

// Token 响应
class TokenResponse {
  String token;
  String subjectId;
  DateTime? expiresAt;
}
```

### 用户相关
```dart
// 主体
class SubjectEntity {
  String id;
  String name;
  String type;
  DateTime createdAt;
}

// 用户档案
class UserProfileEntity {
  String id;           // = subjectId
  String nickname;
  String avatar;
  String email;
  String phone;
}

// 账号
class AccountEntity {
  String id;
  String subjectId;
  String account;
  String type;
  Boolean enabled;
}

// 登录日志
class LoginRecordEntity {
  String id;
  String subjectId;
  String accountId;
  Long loginTime;
  String loginType;
}
```

### 通知模板
```dart
class TemplateEntity {
  String id;
  String name;
  String code;
  String content;
  String remark;
  Boolean enabled;
  DateTime createdAt;
  DateTime updatedAt;
}
```

---

## 通用查询请求

后端使用 `QueriesPageRequest` 进行分页查询:

```dart
class QueriesPageRequest {
  List<Predicate>? predicate;   // 查询条件
  List<Order>? order;          // 排序条件
  Page? page;                   // 分页参数
}

class Page {
  int page = 0;
  int size = 20;
}
```

查询条件示例:
```dart
// 查询启用状态的用户
{
  "predicate": [
    {"field": "enabled", "op": "eq", "value": true}
  ],
  "page": {"page": 0, "size": 20}
}
```

---

## 页面路由

| 路径 | 页面 | 权限 |
|-----|------|------|
| `/login` | LoginPage | 公开 |
| `/` | HomePage | 需要登录 |
| `/users` | UserListPage | 需要登录 |
| `/users/accounts` | AccountListPage | 需要登录 |
| `/users/detail/:subjectId` | UserDetailPage | 需要登录 |
| `/users/login-logs` | LoginLogPage | 需要登录 |
| `/templates` | TemplateListPage | 需要登录 |
| `/templates/edit/:id` | TemplateEditPage | 需要登录 |
| `/templates/preview/:id` | TemplatePreviewPage | 需要登录 |

---

## 开发计划

### Phase 1: 基础架构
- [ ] 项目初始化与依赖配置
- [ ] 主题和颜色配置
- [ ] 路由配置
- [ ] API 客户端封装 (Dio)

### Phase 2: 认证模块
- [ ] 登录页面 UI
- [ ] 登录 API 对接
- [ ] Token 存储与刷新
- [ ] 登录状态拦截

### Phase 3: 主页模块
- [ ] 响应式布局骨架
- [ ] 侧边导航 / Drawer
- [ ] 快捷入口卡片
- [ ] 通知中心

### Phase 4: 用户管理
- [ ] 主体列表页
- [ ] 账号列表页
- [ ] 用户详情综合页 (Tab)
- [ ] 登录日志列表页

### Phase 5: 通知模板管理
- [ ] 模板列表页
- [ ] 模板修改页
- [ ] 模板预览页

### Phase 6: 优化与适配
- [ ] 移动端适配优化
- [ ] 错误处理与提示
- [ ] 加载状态优化
- [ ] 响应式组件封装

---

## 环境配置

```yaml
# .env.example
API_BASE_URL=http://localhost:9200
API_USER_PREFIX=/api/user
API_NOTIFICATION_PREFIX=/api/notification
```

---

## 构建与运行

```bash
# Web
flutter run -d chrome

# Android
flutter run -d android

# iOS
flutter run -d ios

# Release
flutter build web --release
flutter build apk --release
```