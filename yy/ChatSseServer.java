import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class ChatSseServer {
    private static final int PORT = 9000;
    private static final String MIMO_API_URL = "https://token-plan-cn.xiaomimimo.com/v1/chat/completions";
    // TODO: 替换为你的 API Key，或通过环境变量 MIMO_API_KEY 设置
    private static final String MIMO_API_KEY = System.getenv("MIMO_API_KEY") != null 
            ? System.getenv("MIMO_API_KEY") 
            : "your-api-key-here";

    public static void main(String[] args) throws IOException {
        System.out.println("========================================");
        System.out.println("MIMO_API_KEY: " + MIMO_API_KEY);
        System.out.println("SSE Chat Server 启动中...");
        System.out.println("监听端口: " + PORT);
        System.out.println("访问地址: http://127.0.0.1:" + PORT + "/api/chat");
        System.out.println("MiMo API: " + MIMO_API_URL);
        System.out.println("========================================");

        java.net.ServerSocket serverSocket = new java.net.ServerSocket(PORT);
        System.out.println("[启动] ServerSocket 已绑定到端口 " + PORT);
        System.out.println("[启动] 等待客户端连接...");

        while (!Thread.currentThread().isInterrupted()) {
            System.out.println("[循环] 阻塞等待 accept()...");
            java.net.Socket client = serverSocket.accept();
            System.out.println("[连接] 收到客户端连接 | remoteAddress=" + client.getRemoteSocketAddress());
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(java.net.Socket socket) {
        System.out.println("[线程] 处理客户端线程启动 | threadId=" + Thread.currentThread().getId());
        
        try (java.net.Socket s = socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             OutputStream out = new BufferedOutputStream(s.getOutputStream())) {

            System.out.println("[读取] 开始读取请求行...");
            String line = in.readLine();
            System.out.println("[读取] 请求行: " + line);
            
            if (line == null) {
                System.out.println("[警告] 请求行为空，忽略");
                return;
            }
            
            String method = line.split(" ")[0];
            String path = line.split(" ")[1];
            System.out.println("[解析] method=" + method + " | path=" + path);

            String corsHeaders = "Access-Control-Allow-Origin: *\r\n" +
                    "Access-Control-Allow-Methods: POST,OPTIONS\r\n" +
                    "Access-Control-Allow-Headers: Content-Type,Authorization\r\n";

            if ("OPTIONS".equals(method)) {
                System.out.println("[CORS] 处理 OPTIONS 预检请求");
                String resp = "HTTP/1.1 204 No Content\r\n" + corsHeaders + "\r\n";
                out.write(resp.getBytes(StandardCharsets.UTF_8));
                out.flush();
                System.out.println("[CORS] OPTIONS 预检完成");
                return;
            }

            if (!("POST".equals(method) && "/api/chat".equals(path))) {
                System.out.println("[拒绝] 不支持的请求 | method=" + method + " | path=" + path);
                String resp = "HTTP/1.1 404 Not Found\r\nContent-Type:application/json;charset=utf-8\r\n" + corsHeaders + "\r\n{\"msg\":\"not found\"}";
                out.write(resp.getBytes(StandardCharsets.UTF_8));
                out.flush();
                return;
            }

            System.out.println("[解析] 开始读取请求头...");
            int contentLen = 0;
            StringBuilder headers = new StringBuilder();
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                headers.append(line).append("\n");
                if (line.startsWith("Content-Length:")) {
                    contentLen = Integer.parseInt(line.split(":")[1].trim());
                    System.out.println("[解析] Content-Length: " + contentLen);
                }
            }
            System.out.println("[解析] 请求头读取完成");

            System.out.println("[读取] 开始读取请求体，长度=" + contentLen);
            char[] buf = new char[contentLen];
            int read = in.read(buf);
            String body = new String(buf, 0, read);
            System.out.println("[读取] 请求体读取完成，实际读取=" + read + " 字符");
            System.out.println("[解析] 请求体内容: " + body);

            // 发送 SSE 响应头
            System.out.println("[发送] 开始发送 SSE 响应头...");
            String sseHeader = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/event-stream;charset=utf-8\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: keep-alive\r\n" +
                    corsHeaders + "\r\n";
            out.write(sseHeader.getBytes(StandardCharsets.UTF_8));
            out.flush();
            System.out.println("[发送] SSE 响应头发送完成");

            System.out.println("[MiMo] 开始调用 MiMo API...");
            callMiMoApi(body, out);
            
            System.out.println("[完成] 请求处理完成");
            System.out.println("[关闭] 准备关闭客户端连接");
            
        } catch (Exception e) {
            System.out.println("[错误] 异常: " + e.getClass().getName() + " | " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[线程] 处理客户端线程结束 | threadId=" + Thread.currentThread().getId());
    }

    /**
     * 调用 MiMo API 并流式返回响应
     */
    private static void callMiMoApi(String requestBody, OutputStream out) throws Exception {
        // 转换请求体格式
        String mimoRequest = convertToMiMoFormat(requestBody);
        
        System.out.println("[MiMo] 请求体(转换后): " + mimoRequest);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MIMO_API_URL))
                .header("Content-Type", "application/json")
                .header("api-key", MIMO_API_KEY)
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(mimoRequest))
                .build();

        System.out.println("[MiMo] 发送请求到: " + MIMO_API_URL);
        System.out.println("[MiMo] 请求头: " + request.headers());
        
        HttpResponse<java.io.InputStream> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofInputStream());

        int statusCode = response.statusCode();
        System.out.println("[MiMo] 响应状态码: " + statusCode);

        if (statusCode != 200) {
            // 读取错误响应体
            ByteArrayOutputStream errorBaos = new ByteArrayOutputStream();
            try (InputStream errIs = response.body()) {
                errIs.transferTo(errorBaos);
            }
            String errorBody = errorBaos.toString(StandardCharsets.UTF_8);
            System.out.println("[MiMo] 错误响应体: " + errorBody);
            
            String errorSse = "data:{\"error\":true,\"message\":" + errorBody + "}\n\n";
            out.write(errorSse.getBytes(StandardCharsets.UTF_8));
            out.flush();
            return;
        }

        // 处理流式响应
        try (InputStream is = response.body()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder fullContent = new StringBuilder();
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[MiMo] 收到原始行: " + line);
                
                // 跳过空行和 SSE 注释行
                if (line.isEmpty() || line.startsWith(":")) {
                    continue;
                }
                
                // 解析 SSE data: 行
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    
                    // 跳过 [DONE] 结束标记
                    if ("[DONE]".equals(data)) {
                        System.out.println("[MiMo] 收到完成标记 [DONE]");
                        String doneSse = "data:[DONE]\n\n";
                        out.write(doneSse.getBytes(StandardCharsets.UTF_8));
                        out.flush();
                        break;
                    }
                    
                    // 尝试解析 JSON
                    try {
                        String content = extractContentFromSSEData(data);
                        if (content != null && !content.isEmpty()) {
                            fullContent.append(content);
                            System.out.println("[MiMo] 提取内容: " + content);
                            
                            // 转换为 SSE 格式发送给客户端
                            String sseData = "data:" + content + "\n\n";
                            out.write(sseData.getBytes(StandardCharsets.UTF_8));
                            out.flush();
                        }
                    } catch (Exception e) {
                        System.out.println("[MiMo] 解析行失败: " + e.getMessage());
                    }
                }
            }
            
            System.out.println("[MiMo] 流式响应完成 | 累计内容长度: " + fullContent.length());
        }
    }

    /**
     * 将简化请求体转换为 MiMo API 格式
     * 
     * 【简化格式】(ChatSseServer 接受的格式): {"system":"系统设定","user":"用户输入"}
     * 【MiMo API 格式】(调用 MiMo 所需的格式):
     *   {"model":"mimo-v2.5-pro","messages":[{"role":"system","content":"..."},{"role":"user","content":[{"type":"text","text":"..."}]}],"stream":true}
     */
    private static String convertToMiMoFormat(String simpleRequest) {
        System.out.println("[转换] 原始请求: " + simpleRequest);
        
        // 提取字段
        String system = extractVal(simpleRequest, "system");
        String user = extractVal(simpleRequest, "user");
        
        // 构建 messages 数组
        StringBuilder messages = new StringBuilder("[");
        
        // 添加 system 消息
        if (!system.isEmpty()) {
            messages.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(system)).append("\"}");
            if (!user.isEmpty()) {
                messages.append(",");
            }
        }
        
        // 添加 user 消息 (使用数组格式)
        if (!user.isEmpty()) {
            messages.append("{\"role\":\"user\",\"content\":[{\"type\":\"text\",\"text\":\"").append(escapeJson(user)).append("\"}]}");
        }
        
        messages.append("]");
        
        // 构建完整请求
        String mimoRequest = "{" +
                "\"model\":\"mimo-v2.5-pro\"," +
                "\"messages\":" + messages + "," +
                "\"max_completion_tokens\":1024," +
                "\"temperature\":1.0," +
                "\"top_p\":0.95," +
                "\"stream\":true," +
                "\"frequency_penalty\":0," +
                "\"presence_penalty\":0" +
                "}";
        
        System.out.println("[转换] 转换后请求: " + mimoRequest);
        return mimoRequest;
    }
    
    /**
     * JSON 字符串转义
     */
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 极简提取json字符串value，适配单层 {"system":"xx","user":"yy"}
     */
    private static String extractVal(String json, String key) {
        String k = "\"" + key + "\":";
        int pos = json.indexOf(k);
        if (pos == -1) return "";
        int start = pos + k.length();
        if (start >= json.length()) return "";
        if (json.charAt(start) == '"') {
            int end = json.indexOf("\"", start + 1);
            if (end == -1) return "";
            return json.substring(start + 1, end);
        }
        return "";
    }

    /**
     * 从 MiMo SSE 数据中提取 content 和 reasoning_content
     * MiMo 流式响应格式:
     *   data: {"id":"...","choices":[{"delta":{"content":"...", "reasoning_content":"..."}}]}
     * 
     * 返回格式: "content:xxx" 或 "reasoning:xxx"
     */
    private static String extractContentFromSSEData(String json) {
        // 尝试提取 reasoning_content
        String reasoning = extractMiMoField(json, "reasoning_content");
        if (reasoning != null && !reasoning.isEmpty()) {
            System.out.println("[MiMo] reasoning: " + reasoning);
            return "reasoning:" + reasoning;
        }
        
        // 尝试提取 content
        String content = extractMiMoField(json, "content");
        if (content != null && !content.isEmpty()) {
            System.out.println("[MiMo] content: " + content);
            return "content:" + content;
        }
        
        return null;
    }
    
    /**
     * 从 MiMo JSON 中提取指定字段
     * MiMo 响应中字段路径: choices[0].delta.{field}
     */
    private static String extractMiMoField(String json, String fieldName) {
        // 查找 "fieldName":" 模式
        String searchPattern = "\"" + fieldName + "\":\"";
        int pos = json.indexOf(searchPattern);
        if (pos == -1) {
            // 可能是 null 值
            return null;
        }
        
        int start = pos + searchPattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        
        String value = json.substring(start, end);
        // 转义字符处理
        value = value.replace("\\\"", "\"")
                    .replace("\\n", "\n")
                    .replace("\\\\", "\\");
        return value;
    }
}