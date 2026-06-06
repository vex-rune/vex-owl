# PostgreSQL + pgvector 多向量列设计指南

> **适用场景**：PostgreSQL + pgvector + 多租户 + Obsidian 知识库

---

## 核心结论

**同一张表、同一列 `vector` 字段，不建议建多个向量索引**；但可以用「多向量列 + 各自独立索引」实现多维度标记/多语义向量，完全满足多维度数据标记、多套向量的需求。

---

## 一、索引类型：同列多索引 vs 多列多索引

### 1.1 同列多索引 ❌ 不推荐

假设表中有一列 `embedding vector(1536)`，重复创建 IVFFlat / HNSW 多个索引：

```sql
-- 错误示范：同一列多个向量索引
CREATE INDEX idx_emb1 ON t USING ivfflat (embedding vector_cosine_ops);
CREATE INDEX idx_emb2 ON t USING hnsw (embedding vector_cosine_ops);
```

**问题**：

1. **写入爆炸**：新增/更新数据时，所有向量索引都要同步更新，`INSERT/UPDATE` 性能成倍下降
2. **查询不会自动择优**：pgvector 不会智能选最优索引，反而增加索引扫描开销
3. **磁盘、内存占用翻倍**，多租户场景下资源压力极大

> 规则：**一个向量字段，只保留一个向量索引**。

### 1.2 多列多索引 ✅ 标准方案

如果需要**多维度向量化、多套特征标记**，正确做法：

- 新增多个 `vector` 类型字段，对应不同语义/不同模型/不同粒度
- 每个向量列，单独建**专属向量索引**
- 业务根据场景选择用哪一组向量做检索

**适用场景**：

1. 全文向量 + 摘要向量（长短文本双粒度）
2. 通用语义向量 + 领域专属向量（通用问答 / 专业知识检索分离）
3. 不同 Embedding 模型产出的向量（对比效果、灰度切换）
4. 结合标签/分类做**特征增强向量**，补充结构化标记

---

## 二、表结构设计

### 2.1 建表语句（3组向量示例）

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE obsidian_note (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    vault_path VARCHAR(512),
    title VARCHAR(255),
    content TEXT NOT NULL,
    tags TEXT[],
    out_links TEXT[],
    front_matter JSONB,
    create_time TIMESTAMP DEFAULT NOW(),

    -- 多向量字段：不同维度标记/语义
    emb_full vector(1536),        -- 全文向量：整段 Markdown 内容向量化
    emb_summary vector(1536),      -- 摘要向量：标题+核心摘要向量化
    emb_tag_content vector(1536)    -- 标签融合向量：强化标签/分类标记
);

-- 结构化索引
CREATE INDEX idx_tenant_id ON obsidian_note (tenant_id);
CREATE INDEX idx_tenant_vaultpath ON obsidian_note (tenant_id, vault_path);
CREATE INDEX idx_tags_gin ON obsidian_note USING GIN (tags);

-- 向量索引（每个向量列单独索引）
CREATE INDEX idx_emb_full ON obsidian_note
USING ivfflat (emb_full vector_cosine_ops)
WITH (lists = 100);

CREATE INDEX idx_emb_summary ON obsidian_note
USING ivfflat (emb_summary vector_cosine_ops)
WITH (lists = 100);

CREATE INDEX idx_emb_tag_content ON obsidian_note
USING ivfflat (emb_tag_content vector_cosine_ops)
WITH (lists = 100);
```

### 2.2 字段说明

| 字段 | 用途 |
|------|------|
| `emb_full` | 原始全文向量，正常语义问答 |
| `emb_summary` | 标题+摘要向量，快速筛选、标题匹配、粗排 |
| `emb_tag_content` | 标签+正文融合向量，强化标签/分类标记，让向量自带分类属性 |

### 2.3 列数量建议

- **简单增强**：2列（全文 + 标签融合）足够
- **复杂场景**：最多 3~4 列，列太多会大幅提升维护成本

---

## 三、入库逻辑

### 3.1 核心流程

1. 同一条笔记，**生成多份不同文本**（全文 / 摘要 / 标签+内容）
2. 分别调用 Embedding 模型，得到多组向量
3. 一次性写入对应 `vector` 列
4. 所有向量列各自走自身索引，互不干扰

### 3.2 代码示例（Spring AI）

```java
// 一条原始文档
Document originDoc = ...;
String fullText = originDoc.getContent();
String title = originDoc.getMetadata().get("title");
List<String> tags = originDoc.getMetadata().get("tags");

// 1. 构造多份不同文本
String summaryText = title + "：" + fullText.substring(0, 200); // 摘要
String tagMixText = String.join(" ", tags) + "\n" + fullText;  // 标签+内容融合

// 2. 分别向量化
float[] embFull = embeddingClient.embed(fullText);
float[] embSummary = embeddingClient.embed(summaryText);
float[] embTagMix = embeddingClient.embed(tagMixText);

// 3. 写入数据库
ObsidianNote note = new ObsidianNote();
note.setTenantId(TenantContext.get());
note.setEmbFull(embFull);
note.setEmbSummary(embSummary);
note.setEmbTagContent(embTagMix);

noteRepo.save(note);
```

> 说明：如果用 Spring AI 原生 `VectorStore`，默认只支持单向量列；多向量场景建议**自主控制 Embedding + JPA 写入**，灵活性最高。

---

## 四、检索策略

### 4.1 按场景选用不同向量

#### 通用问答 → `emb_full`

```sql
SELECT * FROM obsidian_note
WHERE tenant_id = 'xxx'
ORDER BY emb_full <-> $query_vector
LIMIT 3;
```

#### 快速粗筛 / 标题类查询 → `emb_summary`

```sql
SELECT * FROM obsidian_note
WHERE tenant_id = 'xxx'
ORDER BY emb_summary <-> $query_vector
LIMIT 5;
```

#### 按标签/领域增强检索 → `emb_tag_content`

```sql
SELECT * FROM obsidian_note
WHERE tenant_id = 'xxx'
ORDER BY emb_tag_content <-> $query_vector
LIMIT 3;
```

### 4.2 多向量加权混合检索（效果最优）

```sql
SELECT *,
  (emb_full <-> $v1) * 0.5 +
  (emb_summary <-> $v2) * 0.3 +
  (emb_tag_content <-> $v3) * 0.2 AS score
FROM obsidian_note
WHERE tenant_id = 'xxx'
ORDER BY score ASC
LIMIT 3;
```

### 4.3 轻量化方案：元数据过滤

如果只是**附加标签标记**，不想新增向量列：

```sql
SELECT * FROM obsidian_note
WHERE tenant_id = 'xxx'
  AND tags @> ARRAY['SpringAI']
ORDER BY emb_full <-> $vec
LIMIT 3;
```

- ✅ 优点：零额外向量开销，架构最简单
- ✅ 适用：标记仅做"筛选条件"，不需要融入向量语义

---

## 五、索引优化：IVFFlat vs HNSW

### 5.1 原理对比

| 维度 | IVFFlat | HNSW |
|------|---------|------|
| **数据结构** | 聚类中心 + 倒排列表 | 多层图结构 |
| **构建速度** | 快（K-Means 单次） | 慢（逐条建图） |
| **查询速度** | 中等 | 快 |
| **查询精度** | 依赖 lists + probes | 可调，精度高 |
| **内存占用** | 低 | 高 |
| **写入友好度** | 友好 | 不友好 |
| **适用场景** | 写入频繁、更新多 | 写入少、查询快 |

**类比**：

- IVFFlat = 图书馆分类系统，先按类别分组，再去相关区找
- HNSW = GPS 导航，从高速路 → 城市道路 → 街道，逐层精细

### 5.2 按数据量选择

| 数据量（单表向量条数） | 推荐索引 | 原因 |
|----------------------|---------|------|
| < 1万 | 不需要向量索引 | 数据量小，暴力搜索也快 |
| 1万 ~ 100万 | IVFFlat | 聚类效果好，资源消耗可控 |
| 100万 ~ 1000万 | IVFFlat 或 HNSW | 视读写比例定 |
| > 1000万 | HNSW 或分区 + IVFFlat | 需分区或分布式 |

### 5.3 lists 参数详解

`lists` 是 IVFFlat 的**聚类数量**，把全部向量分成 N 个簇。

```
数据：100万条向量，lists = 100
→ K-Means 把 100万向量分成 100 个簇
→ 每个簇平均 1万条向量
→ 查询时只搜索最近的 3~5 个簇 ≈ 只搜索 3~5万条
```

**lists 设置公式**：`lists ≈ 数据量 / 1000`

| 数据量 | 建议 lists | 说明 |
|--------|-----------|------|
| 1万 | 10 | 数据少，10个簇足够 |
| 10万 | 100 | |
| 100万 | 1000 | |
| 500万 | 5000 | |
| 1000万 | 10000 | 内存压力开始变大 |

**lists 如何影响性能**：

| lists 值 | 搜索范围 | 速度 | 精度 |
|---------|---------|------|------|
| 很小（如 10） | 搜索大量数据 | 慢 | 高 |
| 适中（如 data/1000） | 平衡 | 好 | 好 |
| 很大（如 10000） | 只搜几个簇 | 最快 | 可能遗漏 |

### 5.4 调优方法

```sql
-- 1. 查看当前数据量
SELECT COUNT(*) FROM obsidian_note WHERE tenant_id = 'xxx';

-- 2. 查看索引使用情况
SELECT * FROM pg_stat_user_indexes WHERE indexname LIKE '%emb_%';

-- 3. 用 EXPLAIN ANALYZE 看查询计划
EXPLAIN ANALYZE
SELECT * FROM obsidian_note
WHERE tenant_id = 'xxx'
ORDER BY emb_full <-> '[0.1, 0.2, ...]'
LIMIT 10;

-- 观察输出：
-- - "Index Scan using idx_emb_full" → 走了向量索引 ✅
-- - "Seq Scan" → 全表扫描，需要优化 ❌

-- 4. 调整 lists（重建索引）
DROP INDEX idx_emb_full;
CREATE INDEX idx_emb_full ON obsidian_note
USING ivfflat (emb_full vector_cosine_ops)
WITH (lists = 500);
```

### 5.5 动态调整时机

| 信号 | 说明 | 操作 |
|------|------|------|
| 查询变慢 | 数据增长超过 lists 承受范围 | 增大 lists 重建索引 |
| 命中率低 | 搜索跳过了太多正确结果 | 增大 lists |
| 内存不足 | lists 太大导致内存爆 | 减小 lists 或用 HNSW |

---

## 六、数据增长应对策略

### 6.1 阶段一：参数调优（最容易）

```sql
-- 数据量 1万 → lists=10
-- 数据量 10万 → lists=100~200
-- 数据量 100万 → lists=500~1000
```

### 6.2 阶段二：分区表策略（中等成本）

当单表超过 500万~1000万时，按 `tenant_id` 或时间分区：

```sql
-- 按租户哈希分区
CREATE TABLE obsidian_note (...) PARTITION BY HASH (tenant_id);

-- 创建 16 个分区
CREATE TABLE obsidian_note_p0 PARTITION OF obsidian_note FOR VALUES WITH (MODULUS 16, REMAINDER 0);
-- ... 依此类推到 p15
```

**好处**：

- 查询自动只扫描相关分区
- 索引更小，搜索更快
- 可以单独重建某个分区的索引

### 6.3 阶段三：冷热数据分离（高级）

```
┌─────────────────────────────────────────┐
│ 热数据分区（最近3个月）                  │
│ ├─ IVFFlat 索引                         │
│ └─ SSD / 高性能存储                     │
├─────────────────────────────────────────┤
│ 温数据分区（3-12个月）                   │
│ ├─ IVFFlat 索引                         │
│ └─ 普通存储                             │
├─────────────────────────────────────────┤
│ 冷数据分区（1年前）                      │
│ ├─ 归档到对象存储                        │
│ └─ 按需查询时加载                        │
└─────────────────────────────────────────┘
```

### 6.4 阶段四：分布式向量数据库（终极方案）

| 方案 | 适用场景 | 代表产品 |
|------|---------|---------|
| pgvector 集群 | 不想换技术栈 | pgvector 4.0+ 分片 |
| Milvus | 超大规模、混合检索 | 国产开源，支持亿级 |
| Qdrant | Rust 实现，高性能 | 高性能向量数据库 |
| Weaviate | 轻量易用 | 快速上手 |

---

## 七、实战建议

### 当前阶段（数据量 < 100万）

- 用 IVFFlat，`lists = 数据量 / 1000`
- 每半年检查一次数据量，按需调整
- 优先两列起步：`emb_full` + `emb_tag_content`

### 数据量到 500万~1000万

- 考虑按租户分区
- 或某些只读列换成 HNSW

### 数据量破千万

- 评估是否迁移到专门向量数据库
- 或按时间分区 + 冷热分离

---

## 八、总结

| 需求 | 方案 |
|------|------|
| 只想按标记筛选 | 原有字段 + 过滤（最简单） |
| 标记融入向量语义、提升召回精准度 | 多向量列 + 每列独立索引 |
| 写入频繁 | IVFFlat |
| 查询为主、写入少 | HNSW |
| 数据量持续增长 | 分区 + 冷热分离 + 参数调优 |