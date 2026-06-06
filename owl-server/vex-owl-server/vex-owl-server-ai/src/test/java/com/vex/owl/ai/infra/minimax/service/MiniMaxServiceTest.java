package com.vex.owl.ai.infra.minimax.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@org.springframework.context.annotation.Import(com.vex.owl.ai.infra.minimax.service.TestConfig.class)
class MiniMaxServiceTest {

    @Autowired
    MiniMaxService miniMaxService;

    String apiKey = System.getenv("MINIMAX_API_KEY");

    /**
     * 使用 Java Sound API 播放 WAV 音频数据
     *
     * @param audioData WAV 格式的音频数据
     * @throws LineUnavailableException      如果音频线路不可用
     * @throws UnsupportedAudioFileException 如果音频格式不支持
     * @throws IOException                   如果读取失败
     */
    private void playAudio(byte[] audioData) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bais);
        AudioFormat format = audioInputStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open(format);
        sourceDataLine.start();

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
            sourceDataLine.write(buffer, 0, bytesRead);
        }

        sourceDataLine.drain();
        sourceDataLine.stop();
        sourceDataLine.close();
        audioInputStream.close();
    }

    @Test
    void textToSpeech() throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        // MiniMax 音色配置
        String voiceId = "female-shaonv"; // 女性少女音色
        String format = "wav";

        byte[] audioData = miniMaxService.textToSpeech(
                "你好",
                voiceId,
                format,
                apiKey,
                Map.of("tenantId", "test-tenant", "sessionId", "test-session")
        );

        playAudio(audioData);
    }

    @Test
    void textToSpeechs() throws IOException {
        // 检查 API Key 配置
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("\n========================================");
            System.err.println("警告：未配置 MiniMax API Key！");
            System.err.println("请设置环境变量 MINIMAX_API_KEY2");
            System.err.println("========================================\n");
            fail("MiniMax API Key 未配置，请设置 MINIMAX_API_KEY2 环境变量");
        }

        // MiniMax 音色配置
        String voiceId = "female-shaonv"; // 女性少女音色
        String format = "wav";

        // 临时目录用于存放生成的音频文件
        Path outputDir = Paths.get("target/test-audio");
        Files.createDirectories(outputDir);

        System.out.println("========================================");
        System.out.println("开始生成语音文件...");
        System.out.println("输出目录: " + outputDir.toAbsolutePath());
        System.out.println("========================================\n");

        // 存储文本到语音文件的映射关系
        Map<String, String> audioFiles = Map.ofEntries(
                // 基础词汇
                Map.entry("t1.wav", "当前温度"),
                Map.entry("t2.wav", "摄氏度"),
                Map.entry("h1.wav", "当前湿度"),
                Map.entry("h2.wav", "百分之"),

                // 数字 0-9
                Map.entry("num0.wav", "零"),
                Map.entry("num1.wav", "一"),
                Map.entry("num2.wav", "二"),
                Map.entry("num3.wav", "三"),
                Map.entry("num4.wav", "四"),
                Map.entry("num5.wav", "五"),
                Map.entry("num6.wav", "六"),
                Map.entry("num7.wav", "七"),
                Map.entry("num8.wav", "八"),
                Map.entry("num9.wav", "九"),

                // 警告提示
                Map.entry("warn_temp.wav", "温度过高，请注意"),
                Map.entry("warn_humi.wav", "湿度过大，请注意"),

                // 录音相关
                Map.entry("rec_start.wav", "滴，开始录音"),
                Map.entry("rec_stop.wav", "滴，录音结束"),
                Map.entry("play_back.wav", "正在回放录音"),

                // 游戏相关
                Map.entry("game_start.wav", "抢答游戏开始，准备"),
                Map.entry("ready.wav", "三、二、一，开始"),
                Map.entry("right.wav", "回答正确，加一分"),
                Map.entry("wrong.wav", "抢答错误，本轮作废"),
                Map.entry("game_over.wav", "游戏结束，您的得分是"),

                // 系统提示
                Map.entry("key_click.wav", "叮咚"),
                Map.entry("exit.wav", "已返回待机模式")
        );

        // 生成所有语音文件
        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, String> entry : audioFiles.entrySet()) {
            String fileName = entry.getKey();
            String text = entry.getValue();

            try {
                // 调用 MiniMax TTS 服务生成语音
                byte[] audioData = miniMaxService.textToSpeech(
                        text,
                        voiceId,
                        format,
                        apiKey,
                        Map.of("tenantId", "test-tenant", "sessionId", "test-session")
                );

                // 保存为 wav 文件
                Path outputPath = outputDir.resolve(fileName);
                try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                    fos.write(audioData);
                }

                System.out.println("✓ 生成成功: " + fileName + " <- \"" + text + "\"");
                successCount++;

                // 自动播放刚生成的音频
                try {
                    System.out.println("  ▶ 正在播放: " + text);
                    playAudio(audioData);
                    System.out.println("  ✓ 播放完成");
                } catch (Exception e) {
                    System.err.println("  ✗ 播放失败: " + e.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("✗ 生成失败: " + fileName + " <- \"" + text + "\" - " + e.getMessage());
                failCount++;
            }
        }

        // 输出统计信息
        System.out.println("\n========================================");
        System.out.println("生成完成！");
        System.out.println("成功: " + successCount + " 个");
        System.out.println("失败: " + failCount + " 个");
        System.out.println("输出目录: " + outputDir.toAbsolutePath());
        System.out.println("========================================");

        // 全部生成后，依次播放所有生成的音频文件
        if (successCount > 0) {
            System.out.println("\n========================================");
            System.out.println("开始依次播放所有生成的音频文件...");
            System.out.println("========================================\n");

            for (Map.Entry<String, String> entry : audioFiles.entrySet()) {
                String fileName = entry.getKey();
                String text = entry.getValue();
                Path filePath = outputDir.resolve(fileName);

                if (Files.exists(filePath)) {
                    try {
                        System.out.println("▶ 播放 [" + fileName + "]: \"" + text + "\"");
                        byte[] audioData = Files.readAllBytes(filePath);
                        playAudio(audioData);

                        // 每个音频播放后稍作停顿
                        Thread.sleep(300);

                    } catch (Exception e) {
                        System.err.println("✗ 播放失败 [" + fileName + "]: " + e.getMessage());
                    }
                }
            }

            System.out.println("\n========================================");
            System.out.println("全部播放完成！");
            System.out.println("========================================");
        }

        // 如果有失败，测试不算完全通过
        assertEquals(0, failCount, "部分语音文件生成失败");
    }

    @Test
    void textToSpeechUrl() {
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("警告: 未设置 MINIMAX_API_KEY 环境变量，跳过测试");
            return;
        }

        String url = miniMaxService.textToSpeechUrl(
                "你好，这是一个返回URL的语音合成测试。",
                "female-shaonv",
                "mp3",
                apiKey,
                Map.of("tenantId", "test-tenant", "sessionId", "test-url")
        );

        System.out.println("语音文件 URL: " + url);
        assertNotNull(url, "URL 不应为空");
        assertTrue(url.startsWith("http"), "URL 应以 http 开头");
    }
}