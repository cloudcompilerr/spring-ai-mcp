package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

/**
 * Test class for MCPDemonstrator.
 * 
 * This test class validates the educational demonstration functionality,
 * including mode switching, example generation, and interactive demonstrations.
 * It serves as both a test suite and additional documentation for the
 * demonstration capabilities.
 */
@ExtendWith(MockitoExtension.class)
class MCPDemonstratorTest {
    
    @Mock
    private MCPConfiguration mockConfiguration;
    
    @Mock
    private MCPToolAndResourceDemonstrator mockToolDemonstrator;
    
    private ObjectMapper objectMapper;
    private MCPDemonstrator demonstrator;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        
        // Configure mock configuration with lenient stubbing
        lenient().when(mockConfiguration.isEnableMultiServer()).thenReturn(true);
        lenient().when(mockConfiguration.getMaxRetries()).thenReturn(3);
        lenient().when(mockConfiguration.getConnectionTimeout()).thenReturn(Duration.ofSeconds(30));
        lenient().when(mockConfiguration.getRetryDelay()).thenReturn(Duration.ofSeconds(2));
        lenient().when(mockConfiguration.getHealthCheckInterval()).thenReturn(Duration.ofSeconds(30));
        lenient().when(mockConfiguration.isVerboseLogging()).thenReturn(true);
        lenient().when(mockConfiguration.getEffectiveServers()).thenReturn(List.of());
        
        demonstrator = new MCPDemonstrator(mockConfiguration, objectMapper, mockToolDemonstrator);
    }
    
    @Test
    void shouldInitializeWithSingleServerMode() {
        // Given: A new demonstrator instance
        
        // When: Checking the initial mode
        MCPDemonstrator.DemonstrationMode currentMode = demonstrator.getCurrentMode();
        
        // Then: Should start in single server mode
        assertThat(currentMode).isEqualTo(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
    }
    
    @Test
    void shouldProvideCorrectModeInformation() {
        // Given: Demonstration modes
        MCPDemonstrator.DemonstrationMode singleMode = MCPDemonstrator.DemonstrationMode.SINGLE_SERVER;
        MCPDemonstrator.DemonstrationMode multiMode = MCPDemonstrator.DemonstrationMode.MULTI_SERVER;
        
        // When: Getting mode information
        
        // Then: Should provide correct display names and descriptions
        assertThat(singleMode.getDisplayName()).isEqualTo("Single Server Mode");
        assertThat(singleMode.getDescription()).contains("basic MCP operations");
        
        assertThat(multiMode.getDisplayName()).isEqualTo("Multi Server Mode");
        assertThat(multiMode.getDescription()).contains("multiple concurrent servers");
    }
    
    @Test
    void shouldSwitchModeSuccessfully() {
        // Given: Starting in single server mode
        assertThat(demonstrator.getCurrentMode()).isEqualTo(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
        
        // When: Switching to multi-server mode
        CompletableFuture<MCPExampleResult> switchResult = demonstrator.switchMode(
            MCPDemonstrator.DemonstrationMode.MULTI_SERVER
        );
        
        // Then: Should complete successfully and switch mode
        assertThat(switchResult).isNotNull();
        // Wait for the switch to complete
        switchResult.join();
        assertThat(demonstrator.getCurrentMode()).isEqualTo(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
    }
    
    @Test
    void shouldNotSwitchToSameMode() {
        // Given: Starting in single server mode
        MCPDemonstrator.DemonstrationMode currentMode = demonstrator.getCurrentMode();
        
        // When: Switching to the same mode
        CompletableFuture<MCPExampleResult> switchResult = demonstrator.switchMode(currentMode);
        
        // Then: Should complete immediately with success
        assertThat(switchResult).succeedsWithin(Duration.ofSeconds(1));
        MCPExampleResult result = switchResult.join();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.message()).contains("Already in");
    }
    
    @Test
    void shouldProvideAvailableExamples() {
        // Given: A demonstrator instance
        
        // When: Getting available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // Then: Should provide basic examples
        assertThat(examples).isNotEmpty();
        assertThat(examples).anyMatch(example -> example.title().contains("Connection"));
        assertThat(examples).anyMatch(example -> example.title().contains("Tool"));
        assertThat(examples).anyMatch(example -> example.title().contains("Error"));
    }
    
    @Test
    void shouldProvideMultiServerExamplesInMultiMode() {
        // Given: Switched to multi-server mode
        CompletableFuture<MCPExampleResult> switchResult = demonstrator.switchMode(MCPDemonstrator.DemonstrationMode.MULTI_SERVER);
        switchResult.join(); // Wait for switch to complete
        
        // When: Getting available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // Then: Should include multi-server specific examples
        assertThat(examples).anyMatch(example -> example.title().contains("Multi-Server"));
        assertThat(examples).anyMatch(example -> example.title().contains("Conflict"));
    }
    
    @Test
    void shouldProvideStatusInformation() {
        // Given: A demonstrator instance
        
        // When: Getting demonstration status
        MCPExampleResult status = demonstrator.getDemonstrationStatus();
        
        // Then: Should provide status information
        assertThat(status).isNotNull();
        assertThat(status.message()).isNotBlank();
        assertThat(status.details()).contains("Mode:");
        assertThat(status.details()).contains("Running:");
        assertThat(status.details()).contains("Servers:");
    }
    
    @Test
    void shouldHandleDemonstrationWithoutServers() {
        // Given: A demonstrator with no active servers
        
        // When: Attempting to demonstrate tool execution
        CompletableFuture<MCPExampleResult> result = demonstrator.demonstrateToolExecution();
        
        // Then: Should handle gracefully with appropriate error message
        assertThat(result).succeedsWithin(Duration.ofSeconds(1));
        MCPExampleResult executionResult = result.join();
        assertThat(executionResult.isError()).isTrue();
        assertThat(executionResult.message()).containsAnyOf("No servers available", "No active server manager");
    }
    
    @Test
    void shouldHandleErrorHandlingDemonstrationWithoutServers() {
        // Given: A demonstrator with no active servers
        
        // When: Attempting to demonstrate error handling
        CompletableFuture<MCPExampleResult> result = demonstrator.demonstrateErrorHandling();
        
        // Then: Should handle gracefully with appropriate error message
        assertThat(result).succeedsWithin(Duration.ofSeconds(1));
        MCPExampleResult errorResult = result.join();
        assertThat(errorResult.isError()).isTrue();
        assertThat(errorResult.message()).containsAnyOf("No servers available", "No active server manager");
    }
    
    @Test
    void shouldHandleResourceAccessDemonstrationWithoutServers() {
        // Given: A demonstrator with no active servers
        
        // When: Attempting to demonstrate resource access
        CompletableFuture<MCPExampleResult> result = demonstrator.demonstrateResourceAccess();
        
        // Then: Should handle gracefully with appropriate error message
        assertThat(result).succeedsWithin(Duration.ofSeconds(1));
        MCPExampleResult resourceResult = result.join();
        assertThat(resourceResult.isError()).isTrue();
        assertThat(resourceResult.message()).containsAnyOf("No servers available", "No active server manager");
    }
    
    @Test
    void shouldCategorizeExamplesCorrectly() {
        // Given: Available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Checking example categories
        
        // Then: Examples should have appropriate categories
        examples.forEach(example -> {
            String category = example.getCategory();
            assertThat(category).isIn("Connection", "Tools", "Resources", "Error Handling", "General");
        });
    }
    
    @Test
    void shouldIdentifyErrorExamples() {
        // Given: Available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Filtering error examples
        List<MCPExample> errorExamples = examples.stream()
            .filter(MCPExample::isErrorExample)
            .toList();
        
        // Then: Should have at least one error example
        assertThat(errorExamples).isNotEmpty();
        assertThat(errorExamples).allMatch(example -> example.expectedResult().isError());
    }
    
    @Test
    void shouldProvideFormattedResults() {
        // Given: Available examples
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        
        // When: Getting formatted results
        
        // Then: All examples should have properly formatted expected results
        examples.forEach(example -> {
            String formatted = example.expectedResult().getFormattedResult();
            assertThat(formatted).isNotBlank();
            assertThat(formatted).containsAnyOf("✓", "✗");
        });
    }
    
    @Test
    void shouldHandleNullConfigurationGracefully() {
        // Given: A configuration that might return null values
        lenient().when(mockConfiguration.getEffectiveServers()).thenReturn(List.of());
        
        // When: Creating a new demonstrator
        MCPDemonstrator nullConfigDemonstrator = new MCPDemonstrator(
            mockConfiguration, objectMapper, mockToolDemonstrator
        );
        
        // Then: Should handle gracefully
        assertThat(nullConfigDemonstrator.getCurrentMode()).isNotNull();
        assertThat(nullConfigDemonstrator.getAvailableExamples()).isNotEmpty();
    }
    
    @Test
    void shouldProvideConsistentStatusAcrossCalls() {
        // Given: A demonstrator instance
        
        // When: Getting status multiple times
        MCPExampleResult status1 = demonstrator.getDemonstrationStatus();
        MCPExampleResult status2 = demonstrator.getDemonstrationStatus();
        
        // Then: Should provide consistent information
        assertThat(status1.success()).isEqualTo(status2.success());
        assertThat(status1.message()).isEqualTo(status2.message());
    }
}