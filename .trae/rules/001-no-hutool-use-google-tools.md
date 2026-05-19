# 规则ID: 001
# 规则名称: 禁止使用Hutool，优先使用Google系列工具类
## 规则等级: 强制（P0）
## 规则描述
禁止在项目中引入和使用Hutool工具类库，所有工具类需求优先使用Google Guava、Apache Commons等经过工业级验证的成熟工具库。
## 禁止项
1. ❌ 禁止在pom.xml中引入任何hutool相关依赖
2. ❌ 禁止在代码中import任何`cn.hutool`开头的类
## 推荐替代方案
| Hutool功能                  | 替代方案                                                                 |
|----------------------------|--------------------------------------------------------------------------|
| 字符串处理、判空、转换      | 优先使用 Google Guava `com.google.common.base.Strings`/`Preconditions`/`MoreObjects` |
| 集合工具、不可变集合        | Google Guava `com.google.common.collect.*` 全套集合工具                   |
| 时间日期工具                | Java 8+ 原生 `java.time.*` 包（标准JDK实现，无依赖）                    |
| JSON处理                    | 直接使用 Spring Boot 默认集成的 Jackson `com.fasterxml.jackson.*`        |
| 加密、编码工具              | Apache Commons Codec `org.apache.commons.codec.*`                        |
| IO、文件工具                | Apache Commons IO `org.apache.commons.io.*`                              |
| 反射、类型转换工具          | Spring 自带工具类 `org.springframework.util.*` + Guava `com.google.common.reflect.*` |
## 违反处理
代码提交时自动校验，包含hutool依赖或引用的代码将被拦截，无法提交。
