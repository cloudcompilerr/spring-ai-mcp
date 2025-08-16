package com.example.mcplearning.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Educational-specific MCP configuration properties.
 * 
 * This configuration class contains settings specifically for the educational
 * aspects of the MCP Learning Platform, including demonstration modes,
 * example configurations, and learning progression settings.
 */
@ConfigurationProperties(prefix = "mcp.educational")
@Validated
public class MCPEducationalConfiguration {
    
    /**
     * Whether to enable educational demonstrations.
     */
    private boolean enableDemonstrations = true;
    
    /**
     * Whether to enable interactive examples.
     */
    private boolean enableInteractiveExamples = true;
    
    /**
     * Whether to show detailed protocol messages in demonstrations.
     */
    private boolean showProtocolDetails = true;
    
    /**
     * Whether to enable step-by-step learning mode.
     */
    private boolean enableStepByStepMode = false;
    
    /**
     * Delay between demonstration steps in step-by-step mode.
     */
    @NotNull(message = "Step delay is required")
    private Duration stepDelay = Duration.ofSeconds(2);
    
    /**
     * Maximum number of examples to show in demonstrations.
     */
    @Min(value = 1, message = "Max examples must be at least 1")
    private int maxExamples = 10;
    
    /**
     * Whether to enable error simulation for educational purposes.
     */
    private boolean enableErrorSimulation = true;
    
    /**
     * Predefined example scenarios for demonstrations.
     */
    @NotNull(message = "Example scenarios list is required")
    private List<ExampleScenario> exampleScenarios = getDefaultExampleScenarios();
    
    /**
     * Learning progression settings.
     */
    @NotNull(message = "Learning progression is required")
    private LearningProgression learningProgression = new LearningProgression();
    
    // Getters and setters
    
    public boolean isEnableDemonstrations() {
        return enableDemonstrations;
    }
    
    public void setEnableDemonstrations(boolean enableDemonstrations) {
        this.enableDemonstrations = enableDemonstrations;
    }
    
    public boolean isEnableInteractiveExamples() {
        return enableInteractiveExamples;
    }
    
    public void setEnableInteractiveExamples(boolean enableInteractiveExamples) {
        this.enableInteractiveExamples = enableInteractiveExamples;
    }
    
    public boolean isShowProtocolDetails() {
        return showProtocolDetails;
    }
    
    public void setShowProtocolDetails(boolean showProtocolDetails) {
        this.showProtocolDetails = showProtocolDetails;
    }
    
    public boolean isEnableStepByStepMode() {
        return enableStepByStepMode;
    }
    
    public void setEnableStepByStepMode(boolean enableStepByStepMode) {
        this.enableStepByStepMode = enableStepByStepMode;
    }
    
    public Duration getStepDelay() {
        return stepDelay;
    }
    
    public void setStepDelay(Duration stepDelay) {
        this.stepDelay = stepDelay;
    }
    
    public int getMaxExamples() {
        return maxExamples;
    }
    
    public void setMaxExamples(int maxExamples) {
        this.maxExamples = maxExamples;
    }
    
    public boolean isEnableErrorSimulation() {
        return enableErrorSimulation;
    }
    
    public void setEnableErrorSimulation(boolean enableErrorSimulation) {
        this.enableErrorSimulation = enableErrorSimulation;
    }
    
    public List<ExampleScenario> getExampleScenarios() {
        return exampleScenarios;
    }
    
    public void setExampleScenarios(List<ExampleScenario> exampleScenarios) {
        this.exampleScenarios = exampleScenarios != null ? exampleScenarios : getDefaultExampleScenarios();
    }
    
    public LearningProgression getLearningProgression() {
        return learningProgression;
    }
    
    public void setLearningProgression(LearningProgression learningProgression) {
        this.learningProgression = learningProgression != null ? learningProgression : new LearningProgression();
    }
    
    /**
     * Gets default example scenarios for educational demonstrations.
     */
    private static List<ExampleScenario> getDefaultExampleScenarios() {
        return List.of(
            new ExampleScenario(
                "basic-connection",
                "Basic MCP Connection",
                "Demonstrates establishing a connection to an MCP server",
                "beginner",
                List.of("connection", "initialization")
            ),
            new ExampleScenario(
                "tool-discovery",
                "Tool Discovery",
                "Shows how to discover available tools from an MCP server",
                "beginner",
                List.of("tools", "discovery")
            ),
            new ExampleScenario(
                "tool-execution",
                "Tool Execution",
                "Demonstrates executing tools with various parameters",
                "intermediate",
                List.of("tools", "execution", "parameters")
            ),
            new ExampleScenario(
                "resource-access",
                "Resource Access",
                "Shows how to list and read resources from MCP servers",
                "intermediate",
                List.of("resources", "reading")
            ),
            new ExampleScenario(
                "multi-server",
                "Multi-Server Management",
                "Demonstrates managing multiple MCP servers simultaneously",
                "advanced",
                List.of("multi-server", "management", "routing")
            ),
            new ExampleScenario(
                "error-handling",
                "Error Handling",
                "Shows proper error handling and recovery mechanisms",
                "intermediate",
                List.of("errors", "recovery", "resilience")
            ),
            new ExampleScenario(
                "reactive-patterns",
                "Reactive Programming Patterns",
                "Demonstrates reactive MCP client usage with WebFlux",
                "advanced",
                List.of("reactive", "webflux", "streams")
            )
        );
    }
    
    /**
     * Configuration for an example scenario.
     */
    public static class ExampleScenario {
        
        /**
         * Unique identifier for the scenario.
         */
        private String id;
        
        /**
         * Human-readable name for the scenario.
         */
        private String name;
        
        /**
         * Description of what the scenario demonstrates.
         */
        private String description;
        
        /**
         * Difficulty level (beginner, intermediate, advanced).
         */
        private String level;
        
        /**
         * Tags for categorizing the scenario.
         */
        private List<String> tags;
        
        public ExampleScenario() {
        }
        
        public ExampleScenario(String id, String name, String description, String level, List<String> tags) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.level = level;
            this.tags = tags;
        }
        
        // Getters and setters
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getLevel() {
            return level;
        }
        
        public void setLevel(String level) {
            this.level = level;
        }
        
        public List<String> getTags() {
            return tags;
        }
        
        public void setTags(List<String> tags) {
            this.tags = tags;
        }
    }
    
    /**
     * Learning progression configuration.
     */
    public static class LearningProgression {
        
        /**
         * Whether to enforce learning progression order.
         */
        private boolean enforceOrder = false;
        
        /**
         * Whether to track user progress through examples.
         */
        private boolean trackProgress = true;
        
        /**
         * Whether to show hints for next steps.
         */
        private boolean showHints = true;
        
        /**
         * Whether to enable adaptive difficulty.
         */
        private boolean adaptiveDifficulty = false;
        
        /**
         * Minimum time to spend on each example (for pacing).
         */
        @NotNull(message = "Minimum example time is required")
        private Duration minimumExampleTime = Duration.ofSeconds(30);
        
        /**
         * Custom learning paths for different user types.
         */
        @NotNull(message = "Learning paths map is required")
        private Map<String, List<String>> learningPaths = getDefaultLearningPaths();
        
        // Getters and setters
        
        public boolean isEnforceOrder() {
            return enforceOrder;
        }
        
        public void setEnforceOrder(boolean enforceOrder) {
            this.enforceOrder = enforceOrder;
        }
        
        public boolean isTrackProgress() {
            return trackProgress;
        }
        
        public void setTrackProgress(boolean trackProgress) {
            this.trackProgress = trackProgress;
        }
        
        public boolean isShowHints() {
            return showHints;
        }
        
        public void setShowHints(boolean showHints) {
            this.showHints = showHints;
        }
        
        public boolean isAdaptiveDifficulty() {
            return adaptiveDifficulty;
        }
        
        public void setAdaptiveDifficulty(boolean adaptiveDifficulty) {
            this.adaptiveDifficulty = adaptiveDifficulty;
        }
        
        public Duration getMinimumExampleTime() {
            return minimumExampleTime;
        }
        
        public void setMinimumExampleTime(Duration minimumExampleTime) {
            this.minimumExampleTime = minimumExampleTime;
        }
        
        public Map<String, List<String>> getLearningPaths() {
            return learningPaths;
        }
        
        public void setLearningPaths(Map<String, List<String>> learningPaths) {
            this.learningPaths = learningPaths != null ? learningPaths : getDefaultLearningPaths();
        }
        
        /**
         * Gets default learning paths for different user types.
         */
        private static Map<String, List<String>> getDefaultLearningPaths() {
            return Map.of(
                "beginner", List.of(
                    "basic-connection",
                    "tool-discovery",
                    "tool-execution",
                    "resource-access",
                    "error-handling"
                ),
                "intermediate", List.of(
                    "tool-execution",
                    "resource-access",
                    "error-handling",
                    "multi-server",
                    "reactive-patterns"
                ),
                "advanced", List.of(
                    "multi-server",
                    "reactive-patterns",
                    "error-handling"
                )
            );
        }
    }
}