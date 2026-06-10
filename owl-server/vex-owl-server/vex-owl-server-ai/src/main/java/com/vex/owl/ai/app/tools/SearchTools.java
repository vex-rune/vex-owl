package com.vex.owl.ai.app.tools;

import com.vex.owl.ai.app.tools.serper.SerperSearchResponse;
import com.vex.owl.ai.domain.tools.PublicTools;
import com.vex.owl.ai.domain.tools.ToolContextExtractor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 搜索工具
 *
 * <p>通过 Serper (serper.dev) Google 搜索 API 获取网络信息，返回结构化搜索结果。
 * 免费额度：2,500 次/月。</p>
 */
@Slf4j
@Component
public class SearchTools implements PublicTools {

    private final ToolContextExtractor toolContextExtractor = ToolContextExtractor.getInstance();
    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    public final String name = "web_search";

    @Value("${vex.ai.search.api-key:}")
    private String apiKey;

    @Value("${vex.ai.search.max-results:5}")
    private int maxResults;

    @Tool(description = "搜索网络信息。根据关键词获取最新的 Google 搜索结果，返回标题、链接和摘要。适用于查询实时信息、新闻、技术文档等。")
    public String search(
            ToolContext toolContext,
            @ToolParam(description = "搜索关键词") String query) {

        String userId = toolContextExtractor.getUserId(toolContext).orElse("unknown");
        log.debug("执行搜索, userId={}, query={}", userId, query);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("搜索 API Key 未配置, userId={}", userId);
            return "搜索服务未配置，请联系管理员配置 SERPER_API_KEY";
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://google.serper.dev/search?q=" + encodedQuery
                    + "&hl=zh-cn&apiKey=" + apiKey;

            ResponseEntity<SerperSearchResponse> response = restTemplate.getForEntity(url, SerperSearchResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String result = formatResults(response.getBody());
                log.debug("搜索完成, userId={}, query={}", userId, query);
                return result;
            }

            log.error("搜索 API 返回异常, userId={}, status={}", userId, response.getStatusCode());
            return "搜索请求失败，状态码：" + response.getStatusCode();

        } catch (Exception e) {
            log.error("搜索异常, userId={}, query={}", userId, query, e);
            return "搜索过程发生异常：" + e.getMessage();
        }
    }

    private String formatResults(SerperSearchResponse resp) {
        StringBuilder output = new StringBuilder();

        // 1. Knowledge Graph
        SerperSearchResponse.KnowledgeGraph kg = resp.getKnowledgeGraph();
        if (kg != null && kg.getTitle() != null) {
            output.append("【").append(kg.getTitle()).append("】");
            if (kg.getType() != null) output.append(" - ").append(kg.getType());
            output.append("\n");
            if (kg.getDescription() != null) output.append(kg.getDescription()).append("\n");
            if (kg.getAttributes() != null) {
                kg.getAttributes().forEach((k, v) -> output.append("  ").append(k).append(": ").append(v).append("\n"));
            }
            output.append("\n");
        }

        // 2. Organic results
        List<SerperSearchResponse.OrganicResult> organic = resp.getOrganic();
        if (organic != null && !organic.isEmpty()) {
            int index = 1;
            for (SerperSearchResponse.OrganicResult item : organic) {
                if (index > maxResults) break;
                if (item.getTitle() == null && item.getSnippet() == null) continue;

                output.append(index).append(". ").append(item.getTitle()).append("\n");
                output.append("   链接: ").append(item.getLink()).append("\n");
                if (item.getSnippet() != null) {
                    output.append("   摘要: ").append(item.getSnippet()).append("\n");
                }
                if (item.getAttributes() != null) {
                    item.getAttributes().forEach((k, v) -> output.append("   ").append(k).append(": ").append(v).append("\n"));
                }
                output.append("\n");
                index++;
            }
        }

        // 3. People Also Ask
        List<SerperSearchResponse.PeopleAlsoAsk> paa = resp.getPeopleAlsoAsk();
        if (paa != null && !paa.isEmpty()) {
            output.append("【相关问题】\n");
            for (SerperSearchResponse.PeopleAlsoAsk item : paa) {
                if (item.getQuestion() != null) output.append("  Q: ").append(item.getQuestion()).append("\n");
                if (item.getSnippet() != null) output.append("  A: ").append(item.getSnippet()).append("\n");
            }
            output.append("\n");
        }

        // 4. Related Searches
        List<SerperSearchResponse.RelatedSearch> related = resp.getRelatedSearches();
        if (related != null && !related.isEmpty()) {
            output.append("【相关搜索】");
            List<String> queries = new ArrayList<>();
            for (SerperSearchResponse.RelatedSearch item : related) {
                if (item.getQuery() != null) queries.add(item.getQuery());
            }
            if (!queries.isEmpty()) {
                output.append(String.join(" | ", queries)).append("\n");
            }
        }

        String result = output.toString().trim();
        return result.isEmpty() ? "未找到相关搜索结果" : result;
    }
}
