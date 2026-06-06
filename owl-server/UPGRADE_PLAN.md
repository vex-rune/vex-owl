# Spring AI Alibaba 1.1.2.0 升级方案

**升级日期**：2026-05-22  
**目标版本**：Spring AI Alibaba 1.1.2.0  
**影响范围**：整个 Vex-Owl 项目

---

## 📋 版本升级清单

### 核心组件版本对照

| 组件 | 当前版本 | 升级到 | 变更说明 |
|------|---------|--------|---------|
| **Spring Boot** | 3.3.5 | **3.5.9** | 必须升级，与 SAA 1.1.2.0 匹配 |
| **Spring Cloud** | 2023.0.3 | **2025.0.0** | 必须升级 |
| **Spring Cloud Alibaba** | 2023.0.1.0 | **2025.0.0.0** | 必须升级 |
| **Java** | 21 | 21 | 保持不变 ✅ |

### 依赖库版本升级

| 组件 | 当前版本 | 升级到 | 说明 |
|------|---------|--------|------|
| **Jackson** | 2.15.3 | **2.18.3** | Spring Boot 3.5.x 推荐版本 |
| **Lombok** | 1.18.40 | **1.18.40** | 保持不变 ✅ |
| **SLF4J** | 2.0.9 | **2.0.17** | 兼容性更新 |
| **Spring AI** | 1.0.0 | **1.1.6** | 与 SAA 1.1.2.0 匹配 |
| **Spring AI Alibaba** | 1.0.0.4 | **1.1.2.0** | 目标版本 |

---

## 🔧 修改步骤

### 步骤 1：更新根 pom.xml

**文件位置**：`c:\work\vex\vex-owl\java\pom.xml`

**修改内容**：

```xml
<!-- 1. 修改 Spring Boot 版本 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.9</version>  <!-- 从 3.3.5 升级到 3.5.9 -->
</parent>

<!-- 2. 更新 properties 中的版本 -->
<properties>
    <!-- Spring Cloud 微服务框架 -->
    <spring-cloud.version>2025.0.0</spring-cloud.version>  <!-- 从 2023.0.3 升级 -->
    
    <!-- Spring Cloud Alibaba -->
    <spring-cloud-alibaba.version>2025.0.0.0</spring-cloud-alibaba.version>  <!-- 从 2023.0.1.0 升级 -->
    
    <!-- Jackson -->
    <jackson.version>2.18.3</jackson.version>  <!-- 升级以兼容新版本 -->
    
    <!-- SLF4J -->
    <slf4j.version>2.0.17</slf4j.version>  <!-- 升级 -->
    
    <!-- 其他依赖保持不变或按需升级 -->
</properties>
```

### 步骤 2：更新 vex-owl-server-ai 模块的 pom.xml

**文件位置**：`c:\work\vex\vex-owl\java\vex-owl-server\vex-owl-server-ai\pom.xml`

**修改内容**：

```xml
<!-- 1. 更新依赖管理部分 -->
<dependencyManagement>
    <dependencies>
        <!-- Spring AI Alibaba BOM（必须放在第一位） -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-bom</artifactId>
            <version>1.1.2.0</version>  <!-- 从 1.0.0.4 升级 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Spring AI BOM -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.1.6</version>  <!-- 从 1.0.0 升级 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <!-- Spring AI Alibaba Extensions BOM -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-extensions-bom</artifactId>
            <version>1.1.2.1</version>  <!-- 新增 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 步骤 3：添加必要的扩展依赖

在 `vex-owl-server-ai/pom.xml` 的 `<dependencies>` 部分添加：

```xml
<!-- Spring AI Alibaba Agent Framework（可选但推荐） -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-agent-framework</artifactId>
</dependency>

<!-- Spring AI Alibaba Graph Core（用于复杂工作流） -->
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-graph-core</artifactId>
</dependency>
```

---

## ⚠️ 重要注意事项

### 1. Spring Boot 3.5.x 变更

Spring Boot 3.5.x 相比 3.3.x 有以下重要变更：

- ✅ **性能提升**：改进了启动速度和内存使用
- ✅ **安全性增强**：默认启用更多安全特性
- ⚠️ **API 变更**：少量 deprecated API 被移除
- ⚠️ **配置变更**：某些默认配置行为改变

### 2. Spring Cloud 2025.0.0 变更

- ✅ **基于 Spring Boot 3.5.x**
- ✅ **改进的负载均衡**
- ⚠️ **部分组件版本升级**
- ⚠️ **移除了对 Spring Boot 3.4.x 以下版本的支持**

### 3. Spring AI 1.1.x 新特性

- ✅ **Model Context Protocol (MCP)** 支持
- ✅ **Prompt Caching**（成本降低 90%）
- ✅ **Recursive Advisors**（自我反思）
- ✅ **增强的 RAG 支持**
- ✅ **Multi-agent 支持**

---

## 🔍 兼容性测试清单

### 升级前检查

- [ ] 备份当前项目代码
- [ ] 记录当前版本信息
- [ ] 检查所有自定义依赖
- [ ] 准备回滚方案

### 升级后测试

#### 1. 基础功能测试
- [ ] 所有服务启动正常
- [ ] Nacos 服务发现正常
- [ ] 配置中心正常读取
- [ ] Feign 服务调用正常
- [ ] LoadBalancer 负载均衡正常

#### 2. AI 模块专项测试
- [ ] 豆包 API 调用成功
- [ ] 流式输出正常
- [ ] 聊天记忆存储正常
- [ ] 向量检索正常
- [ ] MCP 客户端正常工作

#### 3. 集成测试
- [ ] 跨服务调用正常
- [ ] 分布式事务正常（如果使用 Seata）
- [ ] 消息队列正常（如果使用 RocketMQ）

#### 4. 性能测试
- [ ] 启动时间测试
- [ ] 内存占用测试
- [ ] 响应时间测试

---

## 🛠️ 潜在问题和解决方案

### 问题 1：依赖冲突

**症状**：Maven 构建失败，提示依赖冲突

**解决方案**：
```bash
# 查看依赖树
mvn dependency:tree > dependency.txt

# 分析冲突
mvn dependency:analyze

# 排除冲突依赖
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <exclusions>
        <exclusion>
            <groupId>冲突的依赖</groupId>
            <artifactId>冲突的依赖</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 问题 2：API 不兼容

**症状**：代码编译失败

**解决方案**：
1. 查看 Spring Boot 3.5 升级指南
2. 更新 deprecated API 调用
3. 调整配置属性名称

### 问题 3：运行时异常

**症状**：应用启动失败或运行时错误

**解决方案**：
1. 查看日志错误信息
2. 对比新旧版本配置差异
3. 参考官方迁移文档

---

## 📚 官方文档链接

- [Spring Boot 3.5 升级指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes)
- [Spring Cloud 2025.0 发行说明](https://spring.io/projects/spring-cloud)
- [Spring Cloud Alibaba 2025.0 版本说明](https://sca.aliyun.com/docs/2025/overview/version-explain/)
- [Spring AI Alibaba 1.1.2.0 官方文档](https://java2ai.com/en/docs/1.0.0.2/)

---

## 📋 升级检查清单

### 升级前
- [ ] 备份项目
- [ ] 创建升级分支：`git checkout -b upgrade/spring-ai-1.1.2.0`
- [ ] 通知团队成员
- [ ] 准备测试环境

### 升级中
- [ ] 更新根 pom.xml
- [ ] 更新 vex-owl-server-ai/pom.xml
- [ ] 运行 `mvn clean compile`
- [ ] 修复编译错误
- [ ] 运行单元测试
- [ ] 运行集成测试

### 升级后
- [ ] 功能测试通过
- [ ] 性能测试通过
- [ ] 安全扫描通过
- [ ] 合并到主分支
- [ ] 更新部署文档

---

## 🎯 建议的实施计划

### Day 1：准备和评估
- 创建升级分支
- 分析依赖关系
- 制定详细测试计划

### Day 2-3：执行升级
- 更新 pom.xml
- 修复编译错误
- 运行基础测试

### Day 4-5：测试验证
- 功能测试
- 集成测试
- 性能测试

### Day 6：问题修复
- 修复发现的问题
- 优化配置
- 文档更新

### Day 7：部署上线
- 部署到测试环境
- 最终验证
- 合并到主分支

---

## ⚠️ 风险评估

| 风险项 | 可能性 | 影响 | 应对措施 |
|-------|--------|------|----------|
| 依赖冲突 | 高 | 中 | 准备依赖排除方案 |
| API 不兼容 | 中 | 高 | 预留时间修复代码 |
| 性能下降 | 低 | 中 | 性能测试和优化 |
| 安全问题 | 低 | 高 | 安全扫描和修复 |

---

**结论**：升级可行，建议按计划执行。

如需帮助执行升级或解决升级中的问题，随时告诉我！😊
