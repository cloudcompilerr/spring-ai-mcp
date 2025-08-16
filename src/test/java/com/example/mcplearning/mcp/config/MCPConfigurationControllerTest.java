package com.example.mcplearning.mcp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for MCP configuration controller.
 * 
 * These tests verify that the configuration controller correctly exposes
 * configuration information and validation results through REST endpoints.
 */
class MCPConfigurationControllerTest {
    
    private MCPConfigurationController controller;
    private MCPConfiguration mcpConfiguration;
    private MCPEducationalConfiguration educationalConfiguration;
    private MCPConfigurationValidator configurationValidator;
    private MCPConfigurationDocumentationGenerator documentationGenerator;
    
    @BeforeEach
    void setUp() {
        mcpConfiguration = createTestMCPConfiguration();
        educationalConfiguration = createTestEducationalConfiguration();
        configurationValidator = mock(MCPConfigurationValidator.class);
        documentationGenerator = mock(MCPConfigurationDocumentationGenerator.class);
        
        controller = new MCPConfigurationController(
            mcpConfiguration,
            educationalConfiguration,
            configurationValidator,
            documentationGenerator
        );
    }
    
    @Test
    @DisplayName("Get configuration should return current MCP configuration")
    void getConfigurationShouldReturnCurrentMCPConfiguration() {
        ResponseEntity<MCPConfiguration> response = controller.getConfiguration();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(mcpConfiguration);
    }
    
    @Test
    @DisplayName("Get educational configuration should return current educational configuration")
    void getEducationalConfigurationShouldReturnCurrentEducationalConfiguration() {
        ResponseEntity<MCPEducationalConfiguration> response = controller.getEducationalConfiguration();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(educationalConfiguration);
    }
    
    @Test
    @DisplayName("Validate configuration should return validation result")
    void validateConfigurationShouldReturnValidationResult() {
        MCPConfigurationValidator.ValidationResult mockResult = 
            new MCPConfigurationValidator.ValidationResult(List.of(), List.of("Test warning"));
        when(configurationValidator.validateConfigurationProgrammatically()).thenReturn(mockResult);
        
        ResponseEntity<MCPConfigurationValidator.ValidationResult> response = controller.validateConfiguration();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(mockResult);
    }
    
    @Test
    @DisplayName("Get configuration summary should return correct summary")
    void getConfigurationSummaryShouldReturnCorrectSummary() {
        ResponseEntity<MCPConfigurationController.ConfigurationSummary> response = 
            controller.getConfigurationSummary();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        MCPConfigurationController.ConfigurationSummary summary = response.getBody();
        assertThat(summary).isNotNull();
        assertThat(summary.totalServers()).isEqualTo(2);
        assertThat(summary.enabledServers()).isEqualTo(1);
        assertThat(summary.multiServerEnabled()).isFalse();
        assertThat(summary.verboseLogging()).isTrue();
        assertThat(summary.examplesEnabled()).isTrue();
        assertThat(summary.reactiveEnabled()).isTrue();
        assertThat(summary.demonstrationsEnabled()).isTrue();
        assertThat(summary.interactiveExamplesEnabled()).isTrue();
    }
    
    @Test
    @DisplayName("Get effective servers should return correct server information")
    void getEffectiveServersShouldReturnCorrectServerInformation() {
        ResponseEntity<Map<String, Object>> response = controller.getEffectiveServers();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("multiServerEnabled")).isEqualTo(false);
        assertThat(body.get("totalServers")).isEqualTo(2);
        assertThat(body.get("enabledServers")).isEqualTo(1);
        assertThat(body.get("effectiveServers")).isEqualTo(1);
        
        @SuppressWarnings("unchecked")
        List<MCPServerConfig> servers = (List<MCPServerConfig>) body.get("servers");
        assertThat(servers).hasSize(1);
        assertThat(servers.get(0).enabled()).isTrue();
    }
    
    @Test
    @DisplayName("Get configuration documentation should return documentation")
    void getConfigurationDocumentationShouldReturnDocumentation() {
        String mockDocumentation = "# Test Documentation\nThis is test documentation.";
        when(documentationGenerator.generateConfigurationDocumentation()).thenReturn(mockDocumentation);
        
        ResponseEntity<String> response = controller.getConfigurationDocumentation();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(mockDocumentation);
    }
    
    @Test
    @DisplayName("Get example configuration should return example YAML")
    void getExampleConfigurationShouldReturnExampleYAML() {
        String mockExample = "mcp:\n  connection-timeout: 30s\n  max-retries: 3";
        when(documentationGenerator.generateExampleConfiguration()).thenReturn(mockExample);
        
        ResponseEntity<String> response = controller.getExampleConfiguration();
        
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(mockExample);
    }
    
    /**
     * Creates a test MCP configuration.
     */
    private MCPConfiguration createTestMCPConfiguration() {
        MCPConfiguration config = new MCPConfiguration();
        config.setConnectionTimeout(Duration.ofSeconds(30));
        config.setMaxRetries(3);
        config.setRetryDelay(Duration.ofSeconds(5));
        config.setHealthCheckInterval(Duration.ofMinutes(1));
        config.setEnableMultiServer(false);
        config.setVerboseLogging(true);
        config.setEnableExamples(true);
        
        // Set up reactive configuration
        MCPConfiguration.ReactiveConfig reactive = new MCPConfiguration.ReactiveConfig();
        reactive.setEnabled(true);
        reactive.setBufferSize(256);
        reactive.setTimeout(Duration.ofSeconds(30));
        reactive.setEnableServerSentEvents(true);
        reactive.setSseHeartbeatInterval(Duration.ofSeconds(30));
        config.setReactive(reactive);
        
        // Set up server configurations
        MCPServerConfig enabledServer = MCPServerConfig.simple("server1", "Server 1", "command1");
        MCPServerConfig disabledServer = MCPServerConfig.disabled("server2", "Server 2", "command2");
        config.setServers(List.of(enabledServer, disabledServer));
        
        return config;
    }
    
    /**
     * Creates a test educational configuration.
     */
    private MCPEducationalConfiguration createTestEducationalConfiguration() {
        MCPEducationalConfiguration config = new MCPEducationalConfiguration();
        config.setEnableDemonstrations(true);
        config.setEnableInteractiveExamples(true);
        config.setShowProtocolDetails(true);
        config.setEnableStepByStepMode(false);
        config.setStepDelay(Duration.ofSeconds(2));
        config.setMaxExamples(10);
        config.setEnableErrorSimulation(true);
        
        return config;
    }
}