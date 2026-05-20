# App 移动端（Flutter）
## 功能描述
跨平台移动端应用，完美适配Android和iOS系统，提供与Web端1:1完全一致的功能体验，支持离线缓存能力
## 核心功能
- 🔐 **账号系统**
  - 邮箱密码登录/注册
  - 自动登录、记住密码
- 💬 **对话功能**
  - 流式对话打字效果
  - 拍照上传、本地文件上传
  - 会话管理、历史消息查看
- ⚙️ **模型配置**
  - 多AI平台切换
  - API Key安全存储
  - 个性化参数设置
- 📚 **记忆管理**
  - 记忆列表查看、搜索
  - 手动新增/编辑/删除记忆
- 📱 **移动端特性**
  - 本地缓存，无网络也可查看历史消息
  - 消息推送通知
  - 深色模式自动适配
  - 手势操作优化
## 技术栈
- Flutter 3.x + Dart
- Hive 本地数据库
- Dio 网络请求库
## 开发环境
- Flutter SDK >= 3.10.0
- 支持平台：Android、iOS
## 常用命令
```bash
# 安装依赖
flutter pub get
# 启动开发调试
flutter run
# 安卓打包
flutter build apk
# iOS打包
flutter build ios
```

---
## 多模块管理（Melos）
### 模块结构
采用三层多模块架构，核心逻辑100%复用：
| 模块 | 说明 |
|------|------|
| `vex_owl_core` | 核心公共库，封装所有通用能力：网络请求、工具类、通用组件、状态管理、API封装、本地存储等，client和admin都依赖此模块 |
| `vex_owl_client` | 用户端业务模块，面向普通用户的功能界面，编译输出用户端Android/iOS APP、用户端Web |
| `vex_owl_admin` | 管理端业务模块，面向管理员的后台功能界面，编译输出管理后台Web、运营端APP |

### Melos 安装
```bash
# 全局安装
dart pub global activate melos

# 验证安装
melos --version
```
> Windows环境如果提示找不到命令，把 `C:\Users\你的用户名\AppData\Local\Pub\Cache\bin` 加到系统环境变量 `PATH` 中，重启终端即可。

### 初始化配置
在 `app/` 根目录下创建 `melos.yaml` 配置文件：
```yaml
name: vex_owl_app
packages:
  - vex_owl_core
  - vex_owl_client
  - vex_owl_admin

scripts:
  # 所有模块一键拉取依赖
  get:
    run: flutter pub get
    description: 所有模块一键拉取pub依赖

  # 所有模块一键清理构建缓存
  clean:
    run: flutter clean
    description: 所有模块一键清理构建缓存

  # 所有模块一键运行单元测试
  test:
    run: flutter test
    description: 所有模块一键跑单元测试

  # 一键打包所有业务模块的Web产物
  build_web:
    run: flutter build web --release --base-href "/web/${MELOS_PACKAGE_NAME}/"
    description: 所有业务模块一键打包正式Web产物，自动按模块名区分访问路径
    select-package:
      ignore:
        - vex_owl_core

  # 代码格式化+静态检查
  lint:
    run: |
      dart format .
      flutter analyze
    description: 所有模块一键格式化代码+静态检查，统一代码规范

dependency_overrides:
  # 全局统一强制依赖版本，避免版本冲突
  dio: ^5.4.3
  get: ^4.6.6
```

初始化工作区（第一次使用执行一次即可）：
```bash
melos bootstrap
# 简写：melos bs
```
> 执行后会自动建立本地模块依赖关联，修改`vex_owl_core`的代码后，`vex_owl_client`和`vex_owl_admin`会实时生效，不需要手动同步。

### 常用命令对照表（和Maven逻辑完全一致）
| Melos 命令 | 对应Maven命令 | 说明 |
|-----------|--------------|------|
| `melos bs` / `melos bootstrap` | `mvn install` | 初始化工作区，建立模块依赖关联，新增模块后执行 |
| `melos run get` | `mvn dependency:resolve` | 所有模块一键拉取最新依赖，不需要分别进入每个目录执行 |
| `melos run clean` | `mvn clean` | 一键清理所有模块的构建缓存、临时文件 |
| `melos run test` | `mvn test` | 所有模块一键运行单元测试，自动汇总结果 |
| `melos run build_web` | `mvn package` | 一键打包`vex_owl_client`和`vex_owl_admin`的Web正式产物，自动生成访问路径：<br>- 用户端：`/web/vex_owl_client/` <br>- 管理端：`/web/vex_owl_admin/` |
| `melos run lint` | `mvn checkstyle:check` | 所有模块一键格式化代码+静态检查，保证代码规范统一 |
| `melos exec "自定义命令"` | - | 在所有模块执行任意自定义命令，比如`melos exec "flutter doctor"` |
| `melos version` | `mvn versions:set` | 统一升级所有模块版本号，自动同步所有依赖版本 |
| `melos publish` | `mvn deploy` | 发布公共模块到私有Pub仓库，跨团队共用时使用 |

### 高级使用技巧
```bash
# 只给指定模块执行命令，比如只打包用户端
melos run build_web --scope vex_owl_client

# 忽略指定模块执行命令，比如跑测试时跳过core模块
melos run test --ignore vex_owl_core
```
