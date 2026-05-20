# 规则ID: 002
# 规则名称: POM依赖管理整合规范
## 规则等级: 强制（P0）
## 规则描述
规范化项目POM文件整合流程，确保多层级父POM依赖版本统一管理，避免版本冲突、配置遗漏或重复定义。所有子模块必须统一继承根级POM的依赖管理配置。

## 源文件与目标文件映射
| 源文件 | 目标文件 | 说明 |
|--------|----------|------|
| `servers/vex-owl-server/pom.xml` | `servers/pom.xml` | 业务服务父POM整合到根POM |
| 子模块pom.xml | 父级pom.xml | 子模块禁止自行定义版本号 |

## 整合范围
1. ✅ properties属性定义（Java版本、框架版本等）
2. ✅ dependencyManagement依赖版本管理
3. ✅ pluginManagement插件版本管理
4. ✅ profiles构建 profile 配置
5. ❌ modules模块声明（保留在各自的父POM中）

## 前置检查条件
1. 验证源文件XML语法正确性（无格式错误、标签闭合正确）
2. 检查目标文件是否已存在同名属性或依赖声明
3. 确认目标文件中不存在冲突的版本定义
4. 检查是否影响其他已集成的子模块

## 执行顺序
1. **优先级1**：读取并解析源POM文件，提取所有properties和dependencyManagement
2. **优先级2**：读取目标POM文件，分析现有配置
3. **优先级3**：执行属性合并（去重，保留最新定义）
4. **优先级4**：执行依赖管理合并（按groupId+artifactId去重）
5. **优先级5**：更新所有子模块pom.xml，移除已迁移的版本定义

## 冲突解决策略
| 冲突类型 | 解决方案 |
|----------|----------|
| 同名属性不同值 | 保留更高版本或更明确的值 |
| 同名依赖不同版本 | 优先保留Spring Boot BOM中托管的版本 |
| 重复定义依赖 | 合并去重，保留完整的依赖声明 |
| scope冲突 | 以更严格的scope为准 |

## 整合操作流程
### 步骤1：属性整合
```xml
<!-- 在目标pom.xml的properties中追加以下内容 -->
<properties>
    <!-- 原有属性保留 -->
    <!-- 新增从源POM迁移的属性 -->
    <spring-cloud.version>2023.0.3</spring-cloud.version>
    <spring-cloud-alibaba.version>2023.0.1.0</spring-cloud-alibaba.version>
    <jjwt.version>0.12.3</jjwt.version>
    <guava.version>32.1.3-jre</guava.version>
    <redisson.version>3.27.2</redisson.version>
    <embedded-redis.version>0.7.3</embedded-redis.version>
</properties>
```

### 步骤2：依赖管理整合
```xml
<dependencyManagement>
    <!-- 原有依赖保留 -->
    <!-- 新增从源POM迁移的依赖管理 -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    <!-- ... 其他依赖 ... -->
</dependencyManagement>
```

### 步骤3：子模块更新
检查所有子模块pom.xml，执行以下操作：
- 移除独立定义的parent（指向源POM）
- 更新parent指向根POM `../pom.xml`
- 移除已迁移的version定义（由父POM统一管理）

### 步骤4：构建验证
```bash
# 在目标pom.xml所在目录执行
mvn validate
mvn help:effective-pom > effective-pom.xml
# 检查effective-pom中依赖版本是否正确
```

## 后置验证步骤
1. 验证XML语法：`mvn validate` 无报错
2. 验证依赖解析：`mvn dependency:tree` 确认无版本冲突
3. 验证构建可行性：`mvn compile -DskipTests` 成功
4. 验证模块继承关系：`mvn help:effective-pom | grep -A2 "<parent>"` 确认正确

## 触发机制
| 触发条件 | 执行动作 |
|----------|----------|
| 新增依赖库 | 必须在根POM中定义版本，子模块直接引用 |
| 更新依赖版本 | 修改根POM对应properties，执行版本升级 |
| 新增子模块 | 子模块pom.xml中不定义version，继承父POM |
| POM结构变更 | 必须更新本规则文档，注明变更内容 |

## 验证命令清单
```bash
# XML语法验证
mvn validate

# 依赖树检查
mvn dependency:tree

# 有效POM输出
mvn help:effective-pom

# 编译验证
mvn compile -DskipTests

# 完整构建验证
mvn clean verify
```

## 违反处理
- 代码提交时自动校验子模块pom.xml是否正确继承父POM
- 未在根POM中定义的依赖版本将被拦截
- 重复定义已托管依赖的版本号将被警告

## 相关文件
- 根POM：`servers/pom.xml`
- 业务服务父POM：`servers/vex-owl-server/pom.xml`
- 子模块示例：`servers/vex-owl-server/vex-owl-server-auth/pom.xml`