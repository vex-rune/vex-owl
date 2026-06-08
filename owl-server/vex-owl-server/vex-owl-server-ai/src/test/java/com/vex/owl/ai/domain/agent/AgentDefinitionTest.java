package com.vex.owl.ai.domain.agent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentDefinitionTest {

    @Test
    void of_shouldCreateRecord() {
        AgentDefinition def = AgentDefinition.of("node", "TestAgent", "测试助手");

        assertThat(def.type()).isEqualTo("node");
        assertThat(def.name()).isEqualTo("TestAgent");
        assertThat(def.description()).isEqualTo("测试助手");
    }

    @Test
    void record_shouldBeEqualByValues() {
        AgentDefinition a = AgentDefinition.of("node", "A", "desc");
        AgentDefinition b = AgentDefinition.of("node", "A", "desc");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void record_shouldNotBeEqualWhenDifferent() {
        AgentDefinition a = AgentDefinition.of("node", "A", "desc");
        AgentDefinition b = AgentDefinition.of("result", "A", "desc");

        assertThat(a).isNotEqualTo(b);
    }
}
