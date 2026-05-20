# Vex Owl Core 核心公共库
## 模块概述
全端核心公共依赖库，封装所有通用能力，100%在用户端（vex_owl_client）和管理端（vex_owl_admin）复用，避免重复开发，保证两端逻辑完全一致。
### 核心职责
- ✅ 所有后端API接口统一封装、参数校验、返回值解析
- ✅ 复杂业务逻辑编排、多接口流程封装
- ✅ 本地存储、全局状态管理
- ✅ 通用工具类、通用UI组件封装
- ✅ 统一错误处理、JWT自动管理、请求日志、缓存等公共能力
- ✅ 主题、常量、枚举、国际化统一管理

---
## 目录结构
```
lib/
├── api/                  # 后端API接口统一封装层
│   ├── user_api.dart     # 用户相关接口
│   ├── chat_api.dart     # 对话相关接口
│   ├── memory_api.dart   # 记忆相关接口
│   └── file_api.dart     # 文件上传相关接口
├── service/              # 复杂业务逻辑封装层（多接口编排、流程处理）
│   ├── user_service.dart # 用户业务逻辑
│   ├── chat_service.dart # 对话业务逻辑
│   └── memory_service.dart # 记忆业务逻辑
├── store/                # 本地存储、全局状态管理层
│   ├── user_store.dart   # 用户信息存储
│   ├── chat_store.dart   # 会话历史存储
│   └── config_store.dart # 全局配置存储
├── utils/                # 通用工具类
│   ├── encrypt_util.dart # 加密工具
│   ├── date_util.dart    # 日期处理工具
│   ├── file_util.dart    # 文件处理工具
│   ├── dio_util.dart     # 网络请求封装
│   └── storage_util.dart # 本地存储工具
├── widgets/              # 通用UI组件库
│   ├── common_button.dart # 通用按钮
│   ├── common_input.dart # 通用输入框
│   ├── common_dialog.dart # 通用弹窗
│   └── loading_widget.dart # 加载动画
├── constants/            # 常量、枚举、主题配置
│   ├── api_constants.dart # 接口地址常量
│   ├── colors.dart       # 统一颜色配置
│   ├── fonts.dart        # 统一字体配置
│   └── event_bus.dart    # 全局事件常量
└── core.dart             # 统一导出文件，所有对外暴露的类/方法都在这里导出
```

---
## 初始化步骤
### 1. 创建项目
在 `app/` 根目录下执行，使用package模板创建纯库项目（不需要原生壳）：
```bash
flutter create --template=package vex_owl_core
```

### 2. 配置依赖
修改 `pubspec.yaml`，添加项目通用依赖：
```yaml
name: vex_owl_core
description: Vex Owl 核心公共库
version: 1.0.0
environment:
  sdk: '>=3.22.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter
  # 网络请求
  dio: ^5.4.3
  # 状态管理、路由管理
  get: ^4.6.6
  # 本地持久化
  hive: ^2.2.3
  shared_preferences: ^2.2.2
  # 文件选择
  file_picker: ^8.0.0+1
  # 图片选择
  image_picker: ^1.1.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^3.0.0
```

### 3. 新建上述目录结构
在 `lib/` 下创建对应的目录和文件，完善基础框架。

### 4. 统一导出配置
在 `core.dart` 中统一导出所有对外暴露的类，业务端使用时只需要导入这一个文件即可：
```dart
// API导出
export 'api/user_api.dart';
export 'api/chat_api.dart';
export 'api/memory_api.dart';
export 'api/file_api.dart';

// Service导出
export 'service/user_service.dart';
export 'service/chat_service.dart';
export 'service/memory_service.dart';

// Store导出
export 'store/user_store.dart';
export 'store/chat_store.dart';
export 'store/config_store.dart';

// 工具导出
export 'utils/encrypt_util.dart';
export 'utils/date_util.dart';
export 'utils/file_util.dart';
export 'utils/dio_util.dart';
export 'utils/storage_util.dart';

// 组件导出
export 'widgets/common_button.dart';
export 'widgets/common_input.dart';
export 'widgets/common_dialog.dart';
export 'widgets/loading_widget.dart';

// 常量导出
export 'constants/api_constants.dart';
export 'constants/colors.dart';
export 'constants/fonts.dart';
export 'constants/event_bus.dart';
```

---
## 开发规范
### 1. 代码存放规则
- ✅ 两端都用到的逻辑、组件、工具必须放在core里
- ❌ 仅用户端或者仅管理端独有的业务逻辑不要放在core里
- ❌ 不要耦合任何业务端独有的UI逻辑
- ❌ 不要硬编码任何业务端独有的配置

### 2. 接口封装规范
- 所有后端接口必须在 `api/` 层封装，不允许业务端直接调用Dio请求
- 接口返回值必须解析成对应的Model对象返回，不允许业务端自己解析JSON
- 接口需要的通用参数（比如JWT、签名）统一在Dio拦截器处理，业务端不需要关心
- 接口错误统一在拦截器处理，自动提示错误信息，业务端不需要重复处理错误逻辑

### 3. Service层规范
- 复杂的多接口编排逻辑必须放在 `service/` 层封装
- Service层方法要尽量原子化，每个方法只做一件事
- Service层方法必须有清晰的入参和返回值，禁止隐式传递参数
- Service层方法必须有异常处理，不允许把底层异常抛给业务端

### 4. 版本管理规范
- core版本号和业务端版本号保持同步
- 修改core后需要在业务端重新拉取依赖（执行`melos run get`）
- 大版本改动需要同步更新CHANGELOG.md

---
## 使用示例
业务端（client/admin）只需要导入core包即可直接使用所有能力，不需要单独配置：
```dart
// 只需要导入这一个文件即可
import 'package:vex_owl_core/core.dart';

// 调用接口示例
final user = await UserApi.login(email, password);

// 调用复杂业务逻辑示例
final message = await ChatService.sendMessage(
  content: "你好",
  sessionId: "123",
);

// 使用通用组件示例
CommonButton(
  text: "提交",
  onPressed: () {},
);
```