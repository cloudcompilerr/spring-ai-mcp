package com.example.mcplearning.mcp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MCP educational configuration.
 * 
 * These tests verify that educational configuration properties are correctly
 * bound from configuration sources and provide expected default values.
 */
class MCPEducationalConfigurationTest {
    
    @Test
    @DisplayName("Default configuration should have expected values")
    void defaultConfigurationShouldHaveExpectedValues() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        
        assertThat(config.isEnableDemonstrations()).isTrue();
        assertThat(config.isEnableInteractiveExamples()).isTrue();
        assertThat(config.isShowProtocolDetails()).isTrue();
        assertThat(config.isEnableStepByStepMode()).isFalse();
        assertThat(config.getStepDelay()).isEqualTo(Duration.ofSeconds(2));
        assertThat(config.getMaxExamples()).isEqualTo(10);
        assertThat(config.isEnableErrorSimulation()).isTrue();
        
        // Check learning progression defaults
        MCPEducationalConfiguration.LearningProgression progression = config.getLearningProgression();
        assertThat(progression.isEnforceOrder()).isFalse();
        assertThat(progression.isTrackProgress()).isTrue();
        assertThat(progression.isShowHints()).isTrue();
        assertThat(progression.isAdaptiveDifficulty()).isFalse();
        assertThat(progression.getMinimumExampleTime()).isEqualTo(Duration.ofSeconds(30));
        
        // Check example scenarios
        List<MCPEducationalConfiguration.ExampleScenario> scenarios = config.getExampleScenarios();
        assertThat(scenarios).isNotEmpty();
        assertThat(scenarios).anyMatch(scenario -> "basic-connection".equals(scenario.getId()));
        assertThat(scenarios).anyMatch(scenario -> "tool-discovery".equals(scenario.getId()));
        assertThat(scenarios).anyMatch(scenario -> "multi-server".equals(scenario.getId()));
    }
    
    @Test
    @DisplayName("Configuration should bind from properties")
    void configurationShouldBindFromProperties() {
        Map<String, Object> properties = new java.util.HashMap<>();
        properties.put("mcp.educational.enable-demonstrations", false);
        properties.put("mcp.educational.enable-interactive-examples", false);
        properties.put("mcp.educational.show-protocol-details", false);
        properties.put("mcp.educational.enable-step-by-step-mode", true);
        properties.put("mcp.educational.step-delay", "5s");
        properties.put("mcp.educational.max-examples", 20);
        properties.put("mcp.educational.enable-error-simulation", false);
        properties.put("mcp.educational.learning-progression.enforce-order", true);
        properties.put("mcp.educational.learning-progression.track-progress", false);
        properties.put("mcp.educational.learning-progression.show-hints", false);
        properties.put("mcp.educational.learning-progression.adaptive-difficulty", true);
        properties.put("mcp.educational.learning-progression.minimum-example-time", "60s");
        
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);
        
        MCPEducationalConfiguration config = binder.bind("mcp.educational", MCPEducationalConfiguration.class)
                .orElse(new MCPEducationalConfiguration());
        
        assertThat(config.isEnableDemonstrations()).isFalse();
        assertThat(config.isEnableInteractiveExamples()).isFalse();
        assertThat(config.isShowProtocolDetails()).isFalse();
        assertThat(config.isEnableStepByStepMode()).isTrue();
        assertThat(config.getStepDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(config.getMaxExamples()).isEqualTo(20);
        assertThat(config.isEnableErrorSimulation()).isFalse();
        
        MCPEducationalConfiguration.LearningProgression progression = config.getLearningProgression();
        assertThat(progression.isEnforceOrder()).isTrue();
        assertThat(progression.isTrackProgress()).isFalse();
        assertThat(progression.isShowHints()).isFalse();
        assertThat(progression.isAdaptiveDifficulty()).isTrue();
        assertThat(progression.getMinimumExampleTime()).isEqualTo(Duration.ofSeconds(60));
    }
    
    @Test
    @DisplayName("Example scenarios should have required fields")
    void exampleScenariosShouldHaveRequiredFields() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        List<MCPEducationalConfiguration.ExampleScenario> scenarios = config.getExampleScenarios();
        
        for (MCPEducationalConfiguration.ExampleScenario scenario : scenarios) {
            assertThat(scenario.getId()).isNotBlank();
            assertThat(scenario.getName()).isNotBlank();
            assertThat(scenario.getDescription()).isNotBlank();
            assertThat(scenario.getLevel()).isNotBlank();
            assertThat(scenario.getTags()).isNotNull();
        }
    }
    
    @Test
    @DisplayName("Learning paths should contain valid scenario IDs")
    void learningPathsShouldContainValidScenarioIds() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        List<MCPEducationalConfiguration.ExampleScenario> scenarios = config.getExampleScenarios();
        MCPEducationalConfiguration.LearningProgression progression = config.getLearningProgression();
        
        List<String> scenarioIds = scenarios.stream()
                .map(MCPEducationalConfiguration.ExampleScenario::getId)
                .toList();
        
        Map<String, List<String>> learningPaths = progression.getLearningPaths();
        
        for (Map.Entry<String, List<String>> entry : learningPaths.entrySet()) {
            String pathName = entry.getKey();
            List<String> pathScenarios = entry.getValue();
            
            assertThat(pathScenarios).as("Learning path '%s' should not be empty", pathName)
                    .isNotEmpty();
            
            for (String scenarioId : pathScenarios) {
                assertThat(scenarioIds).as("Scenario ID '%s' in path '%s' should exist", scenarioId, pathName)
                        .contains(scenarioId);
            }
        }
    }
    
    @Test
    @DisplayName("Scenario levels should be valid")
    void scenarioLevelsShouldBeValid() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        List<MCPEducationalConfiguration.ExampleScenario> scenarios = config.getExampleScenarios();
        
        List<String> validLevels = List.of("beginner", "intermediate", "advanced");
        
        for (MCPEducationalConfiguration.ExampleScenario scenario : scenarios) {
            assertThat(validLevels).as("Scenario '%s' should have valid level", scenario.getId())
                    .contains(scenario.getLevel());
        }
    }
    
    @Test
    @DisplayName("Learning paths should cover all difficulty levels")
    void learningPathsShouldCoverAllDifficultyLevels() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        MCPEducationalConfiguration.LearningProgression progression = config.getLearningProgression();
        
        Map<String, List<String>> learningPaths = progression.getLearningPaths();
        
        assertThat(learningPaths).containsKey("beginner");
        assertThat(learningPaths).containsKey("intermediate");
        assertThat(learningPaths).containsKey("advanced");
        
        assertThat(learningPaths.get("beginner")).isNotEmpty();
        assertThat(learningPaths.get("intermediate")).isNotEmpty();
        assertThat(learningPaths.get("advanced")).isNotEmpty();
    }
    
    @Test
    @DisplayName("Example scenario constructor should work correctly")
    void exampleScenarioConstructorShouldWorkCorrectly() {
        String id = "test-scenario";
        String name = "Test Scenario";
        String description = "A test scenario for unit testing";
        String level = "beginner";
        List<String> tags = List.of("test", "example");
        
        MCPEducationalConfiguration.ExampleScenario scenario = 
                new MCPEducationalConfiguration.ExampleScenario(id, name, description, level, tags);
        
        assertThat(scenario.getId()).isEqualTo(id);
        assertThat(scenario.getName()).isEqualTo(name);
        assertThat(scenario.getDescription()).isEqualTo(description);
        assertThat(scenario.getLevel()).isEqualTo(level);
        assertThat(scenario.getTags()).isEqualTo(tags);
    }
    
    @Test
    @DisplayName("Null example scenarios should be replaced with defaults")
    void nullExampleScenariosShouldBeReplacedWithDefaults() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        config.setExampleScenarios(null);
        
        List<MCPEducationalConfiguration.ExampleScenario> scenarios = config.getExampleScenarios();
        assertThat(scenarios).isNotNull();
        assertThat(scenarios).isNotEmpty();
    }
    
    @Test
    @DisplayName("Null learning progression should be replaced with defaults")
    void nullLearningProgressionShouldBeReplacedWithDefaults() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        config.setLearningProgression(null);
        
        MCPEducationalConfiguration.LearningProgression progression = config.getLearningProgression();
        assertThat(progression).isNotNull();
        assertThat(progression.getLearningPaths()).isNotNull();
        assertThat(progression.getLearningPaths()).isNotEmpty();
    }
}