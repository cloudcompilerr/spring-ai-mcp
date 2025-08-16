package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for MCP Educational components.
 * 
 * This test class validates the complete educational demonstration workflow,
 * including the interaction between different components and the overall
 * user experience. It serves as both a test suite and a comprehensive
 * example of how the educational system works together.
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "mcp.enable-multi-server=true",
    "mcp.verbose-logging=true",
    "mcp.connection-timeout=5s",
    "mcp.max-retries=2"
})
class MCPEducationalIntegrationTest {
    
    private MCPConfiguration configuration;
    private ObjectMapper objectMapper;
    private MCPToolAndResourceDemonstrator toolDemonstrator;
    private MCPDemonstrator demonstrator;
    private MCPLearningController controller;
    
    @BeforeEach
    void setUp() {
        // Create test configuration
        configuration = createTestConfiguration();
        objectMapper = new ObjectMapper();
        
        // Initialize components
        toolDemonstrator = new MCPToolAndResourceDemonstrator();
        demonstrator = new MCPDemonstrator(configuration, objectMapper, toolDemonstrator);
        controller = new MCPLearningController(demonstrator);
    }
    
    @Test
    void shouldProvideCompleteEducationalWorkflow() {
        // Given: A complete educational setup
        
        // When: Following the complete learning workflow
        
        // Step 1: Check initial status
        MCPExampleResult initialStatus = demonstrator.getDemonstrationStatus();
        assertThat(initialStatus).isNotNull();
        assertThat(initialStatus.message()).isNotBlank();
        
        // Step 2: Get available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        assertThat(examples).isNotEmpty();
        assertThat(examples).hasSizeGreaterThanOrEqualTo(4); // At least basic examples
        
        // Step 3: Verify example categories
        List<String> categories = examples.stream()
            .map(MCPExample::getCategory)
            .distinct()
            .toList();
        assertThat(categories).contains("Connection", "Tools", "Error Handling");
        
        // Step 4: Check mode switching capability
        MCPDemonstrator.DemonstrationMode initialMode = demonstrator.getCurrentMode();
        assertThat(initialMode).isEqualTo(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
        
        // Step 5: Switch to multi-server mode
        demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
        assertThat(demonstrator.getCurrentMode()).isEqualTo(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
        
        // Step 6: Verify multi-server examples are available
        List<MCPExample> multiServerExamples = demonstrator.getAvailableExamples();
        assertThat(multiServerExamples).hasSizeGreaterThan(examples.size());
        
        // Then: The complete workflow should be functional
        assertThat(demonstrator.getDemonstrationStatus()).isNotNull();
    }
    
    @Test
    void shouldDemonstrateAllMCPConcepts() {
        // Given: A demonstrator with all concepts
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Examining all examples
        
        // Then: Should cover all major MCP concepts
        assertThat(examples).anyMatch(ex -> ex.title().toLowerCase().contains("connection"));
        assertThat(examples).anyMatch(ex -> ex.title().toLowerCase().contains("tool"));
        assertThat(examples).anyMatch(ex -> ex.title().toLowerCase().contains("error"));
        
        // And: Should have both success and error scenarios
        assertThat(examples).anyMatch(ex -> ex.expectedResult().isSuccess());
        assertThat(examples).anyMatch(ex -> ex.expectedResult().isError());
        
        // And: Should provide educational code examples
        examples.forEach(example -> {
            assertThat(example.code()).isNotBlank();
            assertThat(example.description()).isNotBlank();
            assertThat(example.expectedResult()).isNotNull();
        });
    }
    
    @Test
    void shouldProvideInteractiveExamples() {
        // Given: Available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Examining interactive capabilities
        
        // Then: Examples should be executable and educational
        examples.forEach(example -> {
            // Each example should have executable code
            assertThat(example.code()).contains("MCP");
            
            // Each example should have clear expected results
            MCPExampleResult expectedResult = example.expectedResult();
            assertThat(expectedResult.message()).isNotBlank();
            
            // Each example should provide educational value
            assertThat(example.description()).hasSizeGreaterThan(20); // Meaningful description
        });
    }
    
    @Test
    void shouldHandleErrorScenariosEducationally() {
        // Given: Error examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        List<MCPExample> errorExamples = examples.stream()
            .filter(MCPExample::isErrorExample)
            .toList();
        
        // When: Examining error scenarios
        
        // Then: Should provide educational error handling
        assertThat(errorExamples).isNotEmpty();
        
        errorExamples.forEach(errorExample -> {
            // Error examples should show proper error handling
            assertThat(errorExample.code()).containsAnyOf("try", "catch", "Exception", "error");
            
            // Error examples should explain what went wrong
            assertThat(errorExample.expectedResult().message()).isNotBlank();
            assertThat(errorExample.expectedResult().details()).isNotBlank();
            
            // Error examples should be clearly marked as errors
            assertThat(errorExample.expectedResult().isError()).isTrue();
        });
    }
    
    @Test
    void shouldProvideProgressiveLearningExperience() {
        // Given: All available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Analyzing learning progression
        
        // Then: Should provide progressive complexity
        
        // Basic concepts first
        assertThat(examples).anyMatch(ex -> 
            ex.title().contains("Basic") && ex.getCategory().equals("Connection"));
        
        // Tool operations next
        assertThat(examples).anyMatch(ex -> 
            ex.title().contains("List") && ex.getCategory().equals("Tools"));
        
        // Advanced operations later
        assertThat(examples).anyMatch(ex -> 
            ex.title().contains("Execute") && ex.getCategory().equals("Tools"));
        
        // Error handling throughout
        assertThat(examples).anyMatch(ex -> 
            ex.title().contains("Error") && ex.getCategory().equals("Error Handling"));
    }
    
    @Test
    void shouldSupportBothSingleAndMultiServerModes() {
        // Given: A demonstrator that supports mode switching
        
        // When: Testing both modes
        
        // Single server mode
        demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
        List<MCPExample> singleServerExamples = demonstrator.getAvailableExamples();
        
        // Multi-server mode
        demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
        List<MCPExample> multiServerExamples = demonstrator.getAvailableExamples();
        
        // Then: Should provide mode-specific examples
        assertThat(singleServerExamples).isNotEmpty();
        assertThat(multiServerExamples).hasSizeGreaterThan(singleServerExamples.size());
        
        // Multi-server mode should have additional examples
        assertThat(multiServerExamples).anyMatch(ex -> ex.title().contains("Multi-Server"));
        assertThat(multiServerExamples).anyMatch(ex -> ex.title().contains("Conflict"));
    }
    
    @Test
    void shouldProvideComprehensiveDocumentation() {
        // Given: All examples and components
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Examining documentation quality
        
        // Then: Should provide comprehensive documentation
        examples.forEach(example -> {
            // Each example should have a clear title
            assertThat(example.title()).hasSizeGreaterThan(5);
            
            // Each example should have a detailed description
            assertThat(example.description()).hasSizeGreaterThan(20);
            
            // Each example should have working code
            assertThat(example.code()).contains("MCP");
            assertThat(example.code()).hasSizeGreaterThan(50);
            
            // Each example should have expected results
            assertThat(example.expectedResult().message()).isNotBlank();
        });
    }
    
    @Test
    void shouldHandleRealTimeInteractions() {
        // Given: A demonstrator ready for real-time interactions
        
        // When: Simulating real-time interactions
        MCPExampleResult status1 = demonstrator.getDemonstrationStatus();
        
        // Switch modes
        demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
        MCPExampleResult status2 = demonstrator.getDemonstrationStatus();
        
        // Switch back
        demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
        MCPExampleResult status3 = demonstrator.getDemonstrationStatus();
        
        // Then: Should handle real-time state changes
        assertThat(status1).isNotNull();
        assertThat(status2).isNotNull();
        assertThat(status3).isNotNull();
        
        // Status should reflect current state
        assertThat(status1.details()).contains("SINGLE_SERVER");
        assertThat(status2.details()).contains("MULTI_SERVER");
        assertThat(status3.details()).contains("SINGLE_SERVER");
    }
    
    // Helper methods
    
    private MCPConfiguration createTestConfiguration() {
        return new MCPConfiguration() {
            @Override
            public List<MCPServerConfig> getServers() {
                return List.of(
                    new MCPServerConfig("test-server-1", "Test Server 1", "echo", List.of("test1"), Map.of(), true),
                    new MCPServerConfig("test-server-2", "Test Server 2", "echo", List.of("test2"), Map.of(), true)
                );
            }
            
            @Override
            public boolean isEnableMultiServer() { return true; }
            
            @Override
            public int getMaxRetries() { return 2; }
            
            @Override
            public Duration getConnectionTimeout() { return Duration.ofSeconds(5); }
            
            @Override
            public Duration getRetryDelay() { return Duration.ofSeconds(1); }
            
            @Override
            public Duration getHealthCheckInterval() { return Duration.ofSeconds(10); }
            
            @Override
            public boolean isVerboseLogging() { return true; }
            
            @Override
            public boolean isEnableExamples() { return true; }
            
            @Override
            public List<MCPServerConfig> getEffectiveServers() {
                return getServers().stream().filter(MCPServerConfig::enabled).toList();
            }
        };
    }
}