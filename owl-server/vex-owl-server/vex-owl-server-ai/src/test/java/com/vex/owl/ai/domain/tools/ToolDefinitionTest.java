package com.vex.owl.ai.domain.tools;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolDefinitionTest {

    @Test
    void of_shouldCreateRecord() {
        ToolDefinition def = ToolDefinition.of("web_search", "SearchTools");

        assertThat(def.name()).isEqualTo("web_search");
        assertThat(def.description()).isEqualTo("SearchTools");
    }

    @Test
    void record_shouldBeEqualByValues() {
        ToolDefinition a = ToolDefinition.of("search", "SearchTools");
        ToolDefinition b = ToolDefinition.of("search", "SearchTools");

        assertThat(a).isEqualTo(b);
    }
}
