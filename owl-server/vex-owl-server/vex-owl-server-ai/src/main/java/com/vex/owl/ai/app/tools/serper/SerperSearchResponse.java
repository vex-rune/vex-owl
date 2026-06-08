package com.vex.owl.ai.app.tools.serper;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Serper 搜索 API 响应对象
 *
 * @see <a href="https://serper.dev">Serper API</a>
 */
@Data
public class SerperSearchResponse {

    /** 搜索参数 */
    @JsonProperty("searchParameters")
    private SearchParameters searchParameters;

    /** 知识图谱摘要 */
    @JsonProperty("knowledgeGraph")
    private KnowledgeGraph knowledgeGraph;

    /** 有机搜索结果 */
    private List<OrganicResult> organic;

    /** 相关问题 */
    @JsonProperty("peopleAlsoAsk")
    private List<PeopleAlsoAsk> peopleAlsoAsk;

    /** 相关搜索 */
    @JsonProperty("relatedSearches")
    private List<RelatedSearch> relatedSearches;

    @Data
    public static class SearchParameters {
        private String q;
        private String gl;
        private String hl;
        private boolean autocorrect;
        private int page;
        private String type;
    }

    @Data
    public static class KnowledgeGraph {
        private String title;
        private String type;
        private String website;
        @JsonProperty("imageUrl")
        private String imageUrl;
        private String description;
        @JsonProperty("descriptionSource")
        private String descriptionSource;
        @JsonProperty("descriptionLink")
        private String descriptionLink;
        private Map<String, String> attributes;
    }

    @Data
    public static class OrganicResult {
        private String title;
        private String link;
        private String snippet;
        private String date;
        private int position;
        private Map<String, String> attributes;
        private List<Sitelink> sitelinks;
    }

    @Data
    public static class Sitelink {
        private String title;
        private String link;
    }

    @Data
    public static class PeopleAlsoAsk {
        private String question;
        private String snippet;
        private String title;
        private String link;
    }

    @Data
    public static class RelatedSearch {
        private String query;
    }
}
