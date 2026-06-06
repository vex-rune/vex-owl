# Vex-Owl AI Server

> Vex-Owl 多模型 AI 调用服务，基于 Spring AI + Spring AI Alibaba，集成 DashScope（通义千问）、DeepSeek、MiniMax 等主流大模型

---

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 核心框架 | Spring Boot | 3.5.9 |
| AI SDK | Spring AI BOM | 1.1.6 |
| AI 阿里云 | Spring AI Alibaba BOM | 1.1.2.0 |
| AI 扩展 | Spring AI Alibaba Extensions BOM | 1.1.2.1 |
| Web | Spring Boot WebFlux | — |
| 数据库 | PostgreSQL + pgvector | — |
| 缓存 | Redis | — |
| ORM | Spring Data JPA | — |
| 构建 | Maven 3.x + Java 21 | — |
| 架构 | DDD 领域驱动设计 + 领域分包 | — |

---

## 整体架构

```
用户消息
  │
  ▼
┌─────────────────────────────────────────────────────────┐
│                  ChatAppService (应用层)                 │
│  加载模型 → 路由决策 → 获取 Provider → 调用大模型       │
└─────────────────────────────────────────────────────────┘
  │                           │
  ▼                           ▼
┌──────────────────────┐  ┌──────────────────────────────┐
│    Router 路由层      │  │   AiChatModelProductFactory  │
│  PrimaryRouter        │  │   根据 providerCode 分派     │
│  KeywordRouter        │  │   dashscope/deepseek/minimax │
│  CostRouter           │  └──────────────────────────────┘
└──────────────────────┘              │
        │              ┌──────────────┼──────────────┐
        ▼              ▼              ▼              ▼
  AiModelEntity   DashScope      DeepSeek        MiniMax
  (实体+路由元信息) Provider      Provider        Provider
        │              │              │              │
        └──────────────┴──────────────┴──────────────┘
                              │
                              ▼
                    Spring AI ChatClient
                              │
                              ▼
                    大模型 API (DashScope / DeepSeek / MiniMax)
```

### 调用链详解

```
ChatAppService.chat(userMessage)
  │
  ├─ ① AiModelRepository.findAll()           → 加载全量可用模型
  │
  ├─ ② runRouterChain(models, ctx)           → 按优先级执行路由链
  │     ├─ PrimaryRouter.route()             → 主AI优先，含三级回退
  │     │   └─ isPrimary=true → 直接命中
  │     │   └─ isDefault=true → 回退默认模型
  │     │   └─ priority 最小  → 最终兜底
  │     ├─ KeywordRouter.route()             → 关键词匹配
  │     │   └─ 查 ai_routing_rule 表，命中后路由到指定模型
  │     └─ CostRouter.route()                → 成本优先兜底
  │         └─ costScore 最小者胜出
  │
  ├─ ③ AiChatModelProductFactory.get(code)   → 根据 providerCode 获取工厂
  │     ├─ "dashscope" → DashScopeChatModelProviderFactory
  │     ├─ "deepseek"  → DeepSeekChatModelProviderFactory
  │     └─ "minimax"   → MiniMaxChatModelProviderFactory
  │
  ├─ ④ provider.createClient(model)           → 创建 Spring AI ChatClient
  │
  └─ ⑤ client.prompt(msg).call().content()   → 调用大模型 → 返回结果
```

---

## 项目结构 (DDD)

```
src/main/java/com/vex/owl/ai/
├── OwlAiApplication.java                              # 启动类
│
├── app/
│   └── ChatAppService.java                            # 应用服务 —— 编排完整调用链
│
├── domain/
│   └── model/
│       ├── entity/
│       │   ├── AiModelEntity.java                     # AI模型实体（含路由元数据）
│       │   └── AiRoutingRuleEntity.java               # 关键词路由规则实体
│       │
│       ├── factory/
│       │   ├── AbstractAiChatModelFactory.java        # Provider 工厂接口
│       │   ├── AiChatModelProductFactory.java         # 工厂门面（根据 code 分派）
│       │   ├── DashScopeChatModelProviderFactory.java # 通义千问 Provider
│       │   ├── DeepSeekChatModelProviderFactory.java  # DeepSeek Provider
│       │   └── MiniMaxChatModelProviderFactory.java   # MiniMax Provider
│       │
│       ├── repo/
│       │   ├── ModelProperties.java                   # 模型连接属性接口
│       │   ├── AiModelRepository.java                 # 模型 JPA 仓储
│       │   └── AiRoutingRuleRepository.java           # 路由规则 JPA 仓储
│       │
│       ├── router/
│       │   ├── ChatRouter.java                        # 路由策略接口
│       │   ├── PrimaryRouter.java                     # 主AI优先策略
│       │   ├── KeywordRouter.java                     # 关键词匹配策略
│       │   └── CostRouter.java                        # 成本优先策略
│       │
│       └── service/
│           └── RouteContext.java                       # 路由上下文
```

---

## 数据库设计

### ai_model 表

承载模型的全部元信息——既包含 Provider 连接参数（供 Factory 层消费），也包含路由策略信息（供 Router 层消费）。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 主键，雪花ID，前缀 `model_` |
| `provider_code` | String | Provider 代码：dashscope / deepseek / minimax |
| `model_name` | String | 模型名称：qwen-plus / deepseek-chat 等 |
| `api_key` | String | API 密钥 |
| `base_url` | String | API 基础地址 |
| `is_primary` | boolean | 是否为主 AI（同一时间全局只有一个） |
| `is_default` | boolean | 是否为默认回退模型 |
| `priority` | int | 优先级（数字越小越高，用于故障转移） |
| `fallback_model_id` | String | 故障转移目标模型 ID |
| `cost_score` | double | 成本评分（CostRouter 用） |
| `options` | Map | 扩展参数 |

### ai_routing_rule 表

定义关键词到目标模型的映射规则，KeywordRouter 按优先级逐条匹配。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 主键，雪花ID，前缀 `route_` |
| `keywords` | String | 匹配关键词，逗号分隔 |
| `target_model_id` | String | 命中的目标模型 ID |
| `priority` | int | 优先级（数字越小越高） |

---

## 路由策略

### 路由链优先级

```
PrimaryRouter → KeywordRouter → CostRouter
（命中即停，后续策略不再执行）
```

### PrimaryRouter — 主AI优先

优先使用 `isPrimary=true` 的模型。如果主AI不存在，按三级回退链降级：

```
主AI (isPrimary=true) → 默认模型 (isDefault=true) → 最高优先级 (priority 最小)
```

### KeywordRouter — 关键词匹配

从 `ai_routing_rule` 表加载所有规则，按优先级逐条匹配用户消息中的关键词，命中后路由到规则指定模型。

**匹配规则**：
- `keywords` 字段支持逗号分隔多词，命中任意一个即匹配
- 按 `priority` 排序匹配，高优先级规则优先检查

**示例**：

| keywords | target_model_id | 效果 |
|----------|----------------|------|
| `爬虫,抓取,数据采集` | model_deepseek | 代码/数据类 → DeepSeek |
| `翻译,英语,日语` | model_minimax | 翻译类 → MiniMax |

### CostRouter — 成本优先

从可用模型中选出 `costScore` 最小的那个（`costScore > 0` 才参与比较）。作为路由链的最后兜底。

---

## Provider 工厂

### AbstractAiChatModelFactory — 工厂接口

定义统一的工厂契约，根据 `ModelProperties` 创建 Spring AI `ChatClient`。

### AiChatModelProductFactory — 工厂门面

根据 `providerCode` 分派到对应实现，上游无需感知具体 Provider 类型。

| providerCode | 实现类 | 依赖 |
|-------------|--------|------|
| `dashscope` | DashScopeChatModelProviderFactory | Spring AI Alibaba DashScope |
| `deepseek` | DeepSeekChatModelProviderFactory | Spring AI DeepSeek |
| `minimax` | MiniMaxChatModelProviderFactory | Spring AI MiniMax |

### ModelProperties — 连接属性接口

定义 Factory 创建 ChatClient 所需的最小参数集合：`apiKey`、`modelName`、`baseUrl`。`AiModelEntity` 实现此接口，使得实体对象可以直接传入 Factory。

---

## 配置说明

### bootstrap.yml 关键配置

```yaml
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY:your-api-key-here}
      base-url: https://dashscope.aliyuncs.com/api/v1
    embedding:
      options:
        model: text-embedding-v3
```

### 环境变量

| 变量 | 说明 |
|------|------|
| `DASHSCOPE_API_KEY` | 通义千问 API Key |
| `MINIMAX_API_KEY` | MiniMax API Key |
| `DEEPSEEK_API_KEY` | DeepSeek API Key |

> API Key 存储在数据库 `ai_model` 表中，环境变量作为默认值。

---

## 使用示例

```java
// 注入依赖
ChatAppService service = new ChatAppService(modelRepository, ruleRepository);

// 一行调用 —— 内部自动完成路由、Provider 选择、模型调用
String reply = service.chat("帮我写一个冒泡排序");
```

**内部执行过程**：

1. `modelRepository.findAll()` → 加载所有模型
2. `PrimaryRouter` → 找主AI（如 DeepSeek）
3. 主AI 存在 → 直接返回
4. `AiChatModelProductFactory.get("deepseek")` → 获取 DeepSeek Provider
5. `provider.createClient(model)` → 创建 ChatClient
6. `client.prompt("帮我写一个冒泡排序").call().content()` → 调用大模型

---

---

## 演进规划

### 目标路由链

```
TenantFilter → PrimaryRouter → StickyRouter → UserLevelRouter 
            → KeywordRouter → SemanticRouter → CostRouter
（命中即停，后续策略不再执行）
```

### 规划一：RouteContext 重构

**目标**：将固定的 `primaryModelId` 替换为通用的 `attributes` Map，后续新增 Router 无需改 `RouteContext` 签名。

**改动前**：
```java
RouteContext(String userMessage, String primaryModelId)
```

**改动后**：
```java
RouteContext(String userMessage, Map<String, Object> attributes)
// attributes 承载: tenantId / sessionId / userId / userLevel / messageHistory ...
```

| 改动 | 文件 | 说明 |
|------|------|------|
| 重构 | `RouteContext.java` | `primaryModelId` 替换为 `attributes` Map |
| 修改 | `ChatAppService.java` | 适配新的 `RouteContext` 构造方式 |

---

### 规划二：租户隔离 (TenantFilter)

**目标**：多租户数据完全隔离，每个租户只能看到自己的模型和路由规则。

**设计**：租户隔离不是 Router，而是路由链的**前置过滤器**。在路由链执行前，按 `tenantId` 过滤 `models` 和 `rules`，后续 Router 无需感知租户。

```
ChatAppService.chat(tenantId, ...)
  │
  ├─ modelRepository.findByTenantId(tenantId)   → 过滤后的模型列表
  ├─ ruleRepository.findByTenantId(tenantId)    → 过滤后的规则列表
  │
  └─ runRouterChain(filteredModels, ctx)        → Router 完全不关心租户
```

**改动清单**：

| 类型 | 文件 | 说明 |
|------|------|------|
| 修改 | `AiModelEntity.java` | 新增 `tenantId` 字段 |
| 修改 | `AiRoutingRuleEntity.java` | 新增 `tenantId` 字段 |
| 修改 | `AiModelRepository.java` | 新增 `findByTenantId` |
| 修改 | `AiRoutingRuleRepository.java` | 新增 `findByTenantIdOrderByPriorityDesc` |

**数据库变更**：

| 表 | 新增字段 | 类型 |
|----|---------|------|
| `ai_model` | `tenant_id` | String |
| `ai_routing_rule` | `tenant_id` | String |

---

### 规划三：用户级别路由 (UserLevelRouter)

**目标**：根据用户等级（VIP / 普通 / 试用）分配不同质量的模型。

**新增表 `user_level_routing`**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String | 主键，雪花ID |
| `tenant_id` | String | 租户ID |
| `user_level` | String | 用户等级：vip / normal / trial |
| `target_model_id` | String | 命中的目标模型 ID |
| `priority` | int | 优先级 |

**策略逻辑**：
```
RouteContext.attributes["userLevel"] = "vip"
  ↓
UserLevelRouter 查 user_level_routing 表
  ↓
命中 → 返回 VIP 专属模型（如 DeepSeek）
未命中 → null → 交给下一个 Router
```

**典型配置示例**：

| user_level | target_model_id | 说明 |
|-----------|----------------|------|
| `vip` | model_deepseek | VIP 用质量最好的模型 |
| `normal` | model_dashscope | 普通用户用性价比模型 |
| `trial` | model_minimax | 试用用户用最便宜的 |

**新增文件**：

| 文件 | 说明 |
|------|------|
| `UserLevelRouter.java` | 用户等级路由策略 |
| `UserLevelRoutingEntity.java` | 用户等级路由规则实体 |
| `UserLevelRoutingRepository.java` | 用户等级规则仓储 |

---

### 规划四：会话粘滞 (StickyRouter)

**目标**：同一会话内的多轮对话始终用同一个模型，保证上下文一致性。

**存储**：Redis，key 格式 `sticky:{sessionId}`，TTL 30 分钟。

```
第 1 轮对话 → Router 链选出模型 → 写 Redis: sticky:abc → "model_deepseek"
第 2 轮对话 → StickyRouter 命中 → 直接用 DeepSeek（跳过后续 Router）
第 N 轮对话 → （同上）

超时 30min 后 → Redis key 过期 → StickyRouter 返回 null → 走后续 Router
```

**策略逻辑**：
```java
// StickyRouter — 读取优先
String cachedModelId = redis.get("sticky:" + sessionId);
if (cachedModelId != null) return 找到对应模型;

// ChatAppService — 路由完成后写入
redis.set("sticky:" + sessionId, selected.getId(), TTL 30min);
```

**新增文件**：

| 文件 | 说明 |
|------|------|
| `StickyRouter.java` | 会话粘滞路由策略 |

**修改文件**：

| 文件 | 改动 |
|------|------|
| `ChatAppService.java` | 路由完成后写 Redis 粘滞映射 |

---

### 规划五：对话复杂度路由 (SemanticRouter)

**目标**：根据任务复杂度自动分派模型——简单任务走便宜模型，复杂任务走高质量模型。

**不调用额外模型做语义判断**，用消息特征做启发式判定：

| 判定维度 | 判定标准 | 路由目标 |
|---------|---------|---------|
| 消息长度 | > 500 字 — 复杂任务 | 贵模型 |
| 消息长度 | < 50 字 — 简单任务 | 便宜模型 |
| 历史轮次 | > 5 轮 — 长对话需要好记忆 | 贵模型 |
| 关键词 | 含"代码/算法/排序" — 技术型 | 代码模型 |

**模型角色标记**（通过 `ai_model.options` 字段）：

```json
// ai_model 表的 options 字段
{"role": "complex"}   // 处理复杂任务的模型
{"role": "simple"}    // 处理简单任务的模型
```

```
简单任务流程：                       复杂任务流程：
────────────────                    ────────────────
用户: "你好"                        用户: "帮我设计一个微服务架构方案，包含网关、服务发现、分布式事务..."
  ↓                                  ↓
SemanticRouter: 消息短 → simple     SemanticRouter: 消息长(>500字) → complex
  ↓                                  ↓
选 options.role="simple" 的模型     选 options.role="complex" 的模型
```

**新增文件**：

| 文件 | 说明 |
|------|------|
| `SemanticRouter.java` | 对话复杂度路由策略 |

---

### 实施批次

| 批次 | 内容 | 风险 | 改动范围 |
|------|------|:--:|---------|
| **第一批** | RouteContext 重构 + 租户隔离 | 低 | 修改现有文件为主 |
| **第二批** | StickyRouter + UserLevelRouter + SemanticRouter | 低 | 全部新增文件 |

> 每批改动都是新增文件为主 + 修改现有文件为辅，Router 链 `命中即停` 的机制保证新 Router 不影响已有策略。编译通过即可上线。
>
> **设计原则**：数据库表管理规则 → 运行时动态变更，无需重启服务。

---

## 参考项目

- [Spring AI Alibaba](https://java2ai.com/) — Spring AI 阿里云集成
- [LiteLLM](https://github.com/BerriAI/litellm) — Python 多模型代理/路由网关（架构对标参考）
- [Trae Agent](https://github.com/bytedance/trae-agent) — Multi-LLM 支持架构
- [Claude Code Mux](https://github.com/9j/claude-code-mux) — Provider 架构设计
