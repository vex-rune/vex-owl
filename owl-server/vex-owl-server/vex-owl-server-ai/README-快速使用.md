# 语音助手快速使用指南

## 🎯 快速开始

### 1. 配置API密钥

在 `src/test/resources/application.yml` 或启动参数中添加：

```yaml
spring:
  ai:
    deepseek:
      api-key: your-deepseek-api-key
```

### 2. 运行程序

```bash
# 方式1: 命令行参数启动
java -jar target/owl-ai-1.0.0.jar --voice

# 方式2: 在IDE中运行
# 右键点击 VoiceAssistantStarter.java，选择 Run
```

### 3. 使用方式

程序启动后会：
1. 播放欢迎语："语音助手已启动，请说话。"
2. 进入等待循环
3. 您可以：
   - **直接说话**：使用麦克风进行语音输入
   - **输入 `s` + 回车**：跳过语音，直接输入文字
   - **输入 `q` + 回车**：退出程序

### 4. 测试流程示例

```
=== 语音助手启动 ===
语音助手已启动，请说话。

[等待语音输入...]
输入 'q' 退出，输入 's' 跳过语音直接输入文字
s
请输入问题: 你好，请介绍一下自己

[DeepSeek回复]
你好！我是基于人工智能技术的智能助手...
语音助手播报AI回复...
```

## 📁 创建的文件清单

```
src/main/java/com/vex/owl/ai/app/voice/
├── WindowsSpeechRecognition.java  # Windows语音识别组件
├── WindowsTextToSpeech.java      # Windows语音合成组件
├── DeepSeekClient.java           # DeepSeek AI客户端
├── VoiceAssistant.java           # 语音助手主控制器
└── VoiceAssistantStarter.java    # 启动引导类

src/main/resources/
└── (配置文件参考 docs/语音助手示例.md)

docs/语音助手示例.md              # 详细技术文档
README-快速使用.md                # 本文档
```

## 🔧 核心功能说明

### 1. 语音识别 (WindowsSpeechRecognition)
- **输入**: 麦克风语音
- **输出**: 识别的文字
- **超时**: 10秒（可配置）
- **依赖**: Windows Speech API

### 2. 语音合成 (WindowsTextToSpeech)
- **输入**: 文字内容
- **输出**: 语音播放
- **默认语音**: Microsoft Yaoyao
- **依赖**: Windows TTS Engine

### 3. AI对话 (DeepSeekClient)
- **输入**: 用户问题
- **输出**: AI回复
- **模式**: 支持同步和流式输出
- **API**: DeepSeek Chat API

### 4. 主控制器 (VoiceAssistant)
- **功能**: 整合所有组件
- **交互**: 支持语音/文字两种输入
- **异常处理**: 自动恢复和播报

## ⚠️ 注意事项

### Windows语音设置
1. 打开 Windows 设置 → 隐私 → 语音识别
2. 启用"在线语音识别"
3. 确保麦克风被识别为默认设备

### 网络要求
- 语音识别需要网络连接（在线模式）
- DeepSeek API调用需要网络连接
- 建议在稳定网络环境下使用

### 常见问题
| 问题 | 解决方案 |
|------|---------|
| 语音识别不工作 | 检查Windows隐私设置中的语音权限 |
| TTS声音奇怪 | 在Windows声音设置中更换语音 |
| API调用失败 | 检查API密钥是否正确配置 |
| 识别速度慢 | 在安静环境使用，使用高质量麦克风 |

## 🎨 扩展建议

### 1. 更换语音
```java
// 使用其他Windows语音
tts.speak("Hello", "Microsoft Huihui");
```

### 2. 调整超时时间
```java
// 缩短超时为5秒
recognizer.recognizeFromMicrophone(Duration.ofSeconds(5));
```

### 3. 集成到Web API
```java
@RestController
@RequestMapping("/api/voice")
public class VoiceController {
    
    @PostMapping("/ask")
    public String ask(@RequestBody String question) {
        return deepSeekClient.chat(question);
    }
}
```

### 4. 流式输出
```java
// 实现打字机效果
deepSeekClient.chatStream(question)
    .subscribe(chunk -> {
        System.out.print(chunk);
    });
```

## 📊 技术栈

- **Java**: 21
- **Spring Boot**: 3.x
- **Spring AI**: 1.1.x
- **Windows SAPI**: System.Speech
- **DeepSeek API**: Chat Completions

## 🐛 调试建议

### 启用详细日志
在 `application.yml` 中配置：
```yaml
logging:
  level:
    com.vex.owl.ai.app.voice: DEBUG
```

### 检查PowerShell脚本
```powershell
# 手动测试语音识别
Add-Type -AssemblyName System.Speech
$recognizer = New-Object System.Speech.Recognition.SpeechRecognitionEngine
$recognizer.SetInputToDefaultAudioDevice()
$grammar = New-Object System.Speech.Recognition.DictationGrammar
$recognizer.LoadGrammar($grammar)
$recognizer.RecognizeAsync()
```

## 🎯 下一步

- 阅读 `docs/语音助手示例.md` 获取完整技术文档
- 根据实际需求调整超时时间和提示语
- 考虑添加唤醒词功能（如"小助手"）
- 探索流式输出实现打字机效果

## 📞 技术支持

如遇问题，请检查：
1. Windows语音识别是否启用
2. API密钥是否正确配置
3. 网络连接是否稳定
4. 麦克风是否正常工作

---

**创建时间**: 2026-06-01  
**版本**: 1.0  
**维护者**: Vex Team
