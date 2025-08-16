package com.example.mcplearning.mcp.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MCP configuration classes.
 * 
 * These tests verify that configuration objects work correctly
 * and that their helper methods provide expected functionality.
 */
class MCPConfigurationTest {
    
    @Test
    void testMCPServerConfigFactoryMethods() {
        // Test simple configuration
        var simpleConfig = MCPServerConfig.simple("test-id", "Test Server", "uvx");
        assertThat(simpleConfig.id()).isEqualTo("test-id");
        assertThat(simpleConfig.enabled()).isTrue();
        assertThat(simpleConfig.args()).isEmpty();
        assertThat(simpleConfig.env()).isEmpty();
        
        // Test configuration with args
        var withArgsConfig = MCPServerConfig.withArgs("test-id", "Test Server", "uvx", 
            List.of("mcp-server-filesystem", "/tmp"));
        assertThat(withArgsConfig.args()).hasSize(2);
        assertThat(withArgsConfig.getFullCommandLine()).isEqualTo("uvx mcp-server-filesystem /tmp");
        
        // Test disabled configuration
        var disabledConfig = MCPServerConfig.disabled("test-id", "Test Server", "uvx");
        assertThat(disabledConfig.enabled()).isFalse();
    }
    
    @Test
    void testMCPServerConfigModification() {
        // Given
        var originalConfig = MCPServerConfig.simple("test-id", "Test Server", "uvx");
        
        // When - enable/disable
        var disabledConfig = originalConfig.withEnabled(false);
        assertThat(disabledConfig.enabled()).isFalse();
        assertThat(originalConfig.enabled()).isTrue(); // Original unchanged
        
        // When - add environment variables
        var withEnvConfig = originalConfig.withAdditionalEnv(Map.of("DEBUG", "true"));
        assertThat(withEnvConfig.env()).containsEntry("DEBUG", "true");
        assertThat(originalConfig.env()).isEmpty(); // Original unchanged
    }
    
    @Test
    void testMCPConfigurationServerFiltering() {
        // Given
        var enabledServer1 = MCPServerConfig.simple("server1", "Server 1", "cmd1");
        var enabledServer2 = MCPServerConfig.simple("server2", "Server 2", "cmd2");
        var disabledServer = MCPServerConfig.disabled("server3", "Server 3", "cmd3");
        
        var config = new MCPConfiguration();
        config.setServers(List.of(enabledServer1, enabledServer2, disabledServer));
        
        // When/Then - enabled servers only
        assertThat(config.getEnabledServers()).hasSize(2);
        assertThat(config.hasEnabledServers()).isTrue();
        
        // When/Then - first enabled server
        assertThat(config.getFirstEnabledServer()).isEqualTo(enabledServer1);
    }
    
    @Test
    void testMCPConfigurationMultiServerMode() {
        // Given
        var server1 = MCPServerConfig.simple("server1", "Server 1", "cmd1");
        var server2 = MCPServerConfig.simple("server2", "Server 2", "cmd2");
        
        var config = new MCPConfiguration();
        config.setServers(List.of(server1, server2));
        
        // When multi-server is disabled (default)
        config.setEnableMultiServer(false);
        assertThat(config.getEffectiveServers()).hasSize(1);
        assertThat(config.getEffectiveServers().get(0)).isEqualTo(server1);
        
        // When multi-server is enabled
        config.setEnableMultiServer(true);
        assertThat(config.getEffectiveServers()).hasSize(2);
    }
    
    @Test
    void testMCPConfigurationDefaults() {
        // Given
        var config = new MCPConfiguration();
        
        // Then - verify defaults
        assertThat(config.getServers()).isEmpty();
        assertThat(config.isEnableMultiServer()).isFalse();
        assertThat(config.getConnectionTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(config.getMaxRetries()).isEqualTo(3);
        assertThat(config.isVerboseLogging()).isTrue();
        assertThat(config.isEnableExamples()).isTrue();
        assertThat(config.getRetryDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(config.getHealthCheckInterval()).isEqualTo(Duration.ofMinutes(1));
    }
    
    @Test
    void testMCPConfigurationWithNoEnabledServers() {
        // Given
        var disabledServer = MCPServerConfig.disabled("server1", "Server 1", "cmd1");
        var config = new MCPConfiguration();
        config.setServers(List.of(disabledServer));
        
        // Then
        assertThat(config.hasEnabledServers()).isFalse();
        assertThat(config.getFirstEnabledServer()).isNull();
        assertThat(config.getEffectiveServers()).isEmpty();
    }
}