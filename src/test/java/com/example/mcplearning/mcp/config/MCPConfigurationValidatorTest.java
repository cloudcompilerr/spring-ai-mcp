package com.example.mcplearning.mcp.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MCP configuration validation.
 * 
 * These tests verify that the configuration validator correctly identifies
 * configuration issues and provides helpful error messages.
 */
class MCPConfigurationValidatorTest {
    
    private MCPConfigurationValidator validator;
    private MCPConfiguration configuration;
    
    @BeforeEach
    void setUp() {
        configuration = createValidConfiguration();
        validator = new MCPConfigurationValidator(configuration);
    }
    
    @Test
    @DisplayName("Valid configuration should pass validation")
    void validConfigurationShouldPass() {
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.hasWarnings()).isFalse();
    }
    
    @Test
    @DisplayName("Negative connection timeout should cause error")
    void negativeConnectionTimeoutShouldCauseError() {
        configuration.setConnectionTimeout(Duration.ofSeconds(-1));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Connection timeout must be positive"));
    }
    
    @Test
    @DisplayName("Very long connection timeout should cause warning")
    void veryLongConnectionTimeoutShouldCauseWarning() {
        configuration.setConnectionTimeout(Duration.ofSeconds(400));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("Connection timeout is very long"));
    }
    
    @Test
    @DisplayName("Negative max retries should cause error")
    void negativeMaxRetriesShouldCauseError() {
        configuration.setMaxRetries(-1);
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Max retries cannot be negative"));
    }
    
    @Test
    @DisplayName("Very high max retries should cause warning")
    void veryHighMaxRetriesShouldCauseWarning() {
        configuration.setMaxRetries(15);
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("Max retries is very high"));
    }
    
    @Test
    @DisplayName("Empty servers list should cause warning")
    void emptyServersListShouldCauseWarning() {
        configuration.setServers(List.of());
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("No MCP servers configured"));
    }
    
    @Test
    @DisplayName("Duplicate server IDs should cause error")
    void duplicateServerIdsShouldCauseError() {
        MCPServerConfig server1 = MCPServerConfig.simple("duplicate-id", "Server 1", "command1");
        MCPServerConfig server2 = MCPServerConfig.simple("duplicate-id", "Server 2", "command2");
        configuration.setServers(List.of(server1, server2));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Duplicate server IDs found"));
    }
    
    @Test
    @DisplayName("All servers disabled should cause warning")
    void allServersDisabledShouldCauseWarning() {
        MCPServerConfig server1 = MCPServerConfig.disabled("server1", "Server 1", "command1");
        MCPServerConfig server2 = MCPServerConfig.disabled("server2", "Server 2", "command2");
        configuration.setServers(List.of(server1, server2));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("No MCP servers are enabled"));
    }
    
    @Test
    @DisplayName("Empty server command should cause error")
    void emptyServerCommandShouldCauseError() {
        MCPServerConfig server = new MCPServerConfig(
            "server1", "Server 1", "", List.of(), Map.of(), true);
        configuration.setServers(List.of(server));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Command cannot be empty"));
    }
    
    @Test
    @DisplayName("Null server arguments should cause error")
    void nullServerArgumentsShouldCauseError() {
        MCPServerConfig server = new MCPServerConfig(
            "server1", "Server 1", "command", null, Map.of(), true);
        configuration.setServers(List.of(server));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Arguments list cannot be null"));
    }
    
    @Test
    @DisplayName("Null server environment should cause error")
    void nullServerEnvironmentShouldCauseError() {
        MCPServerConfig server = new MCPServerConfig(
            "server1", "Server 1", "command", List.of(), null, true);
        configuration.setServers(List.of(server));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Environment variables map cannot be null"));
    }
    
    @Test
    @DisplayName("Negative reactive buffer size should cause error")
    void negativeReactiveBufferSizeShouldCauseError() {
        MCPConfiguration.ReactiveConfig reactive = configuration.getReactive();
        reactive.setBufferSize(-1);
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasErrors()).isTrue();
        assertThat(result.errors()).anyMatch(error -> 
            error.contains("Reactive buffer size must be positive"));
    }
    
    @Test
    @DisplayName("Very large reactive buffer size should cause warning")
    void veryLargeReactiveBufferSizeShouldCauseWarning() {
        MCPConfiguration.ReactiveConfig reactive = configuration.getReactive();
        reactive.setBufferSize(20000);
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("Reactive buffer size is very large"));
    }
    
    @Test
    @DisplayName("Multi-server with few enabled servers should cause warning")
    void multiServerWithFewEnabledServersShouldCauseWarning() {
        configuration.setEnableMultiServer(true);
        MCPServerConfig server = MCPServerConfig.simple("server1", "Server 1", "command1");
        configuration.setServers(List.of(server));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("Multi-server mode is enabled but only"));
    }
    
    @Test
    @DisplayName("Very short health check interval should cause warning")
    void veryShortHealthCheckIntervalShouldCauseWarning() {
        configuration.setHealthCheckInterval(Duration.ofSeconds(5));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("Health check interval is very short"));
    }
    
    @Test
    @DisplayName("Very short SSE heartbeat interval should cause warning")
    void veryShortSseHeartbeatIntervalShouldCauseWarning() {
        MCPConfiguration.ReactiveConfig reactive = configuration.getReactive();
        reactive.setSseHeartbeatInterval(Duration.ofSeconds(2));
        
        MCPConfigurationValidator.ValidationResult result = 
            validator.validateConfigurationProgrammatically();
        
        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.warnings()).anyMatch(warning -> 
            warning.contains("SSE heartbeat interval is very short"));
    }
    
    /**
     * Creates a valid configuration for testing.
     */
    private MCPConfiguration createValidConfiguration() {
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
        
        // Set up server configuration
        MCPServerConfig server = MCPServerConfig.simple("test-server", "Test Server", "echo");
        config.setServers(List.of(server));
        
        return config;
    }
}