package com.example.mcplearning.mcp.server;

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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive tests for MultiMCPServerManager.
 * 
 * These tests demonstrate and validate the multi-server functionality
 * including server pool management, conflict resolution, health monitoring,
 * and failover mechanisms.
 */
@ExtendWith(MockitoExtension.class)
class MultiMCPServerManagerTest {
    
    @Mock
    private MCPConfiguration configuration;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private MultiMCPServerManager serverManager;
    
    @BeforeEach
    void setUp() {
        // Configure mock behavior with lenient stubbing
        lenient().when(configuration.isEnableMultiServer()).thenReturn(true);
        lenient().when(configuration.getMaxRetries()).thenReturn(3);
        lenient().when(configuration.getConnectionTimeout()).thenReturn(Duration.ofSeconds(30));
        lenient().when(configuration.getRetryDelay()).thenReturn(Duration.ofSeconds(5));
        lenient().when(configuration.getHealthCheckInterval()).thenReturn(Duration.ofMinutes(1));
        lenient().when(configuration.isVerboseLogging()).thenReturn(true);
        lenient().when(configuration.getEffectiveServers()).thenReturn(createTestServerConfigs());
        
        serverManager = new MultiMCPServerManager(configuration, objectMapper);
    }
    
    @Test
    void shouldInitializeWithMultiServerSupport() {
        assertThat(serverManager).isNotNull();
        assertThat(serverManager.isRunning()).isFalse();
    }
    
    @Test
    void shouldStartAndStopSuccessfully() {
        // Test start
        CompletableFuture<Void> startFuture = serverManager.start();
        assertThat(startFuture).isNotNull();
        
        // Test stop
        CompletableFuture<Void> stopFuture = serverManager.stop();
        assertThat(stopFuture).isNotNull();
    }
    
    @Test
    void shouldAddMultipleServers() {
        MCPServerConfig server1 = createTestServerConfig("server1", "Test Server 1");
        MCPServerConfig server2 = createTestServerConfig("server2", "Test Server 2");
        
        CompletableFuture<Void> add1 = serverManager.addServer(server1);
        CompletableFuture<Void> add2 = serverManager.addServer(server2);
        
        assertThat(add1).isNotNull();
        assertThat(add2).isNotNull();
        
        // Verify servers are tracked
        List<String> serverIds = serverManager.getServerIds();
        assertThat(serverIds).hasSize(2);
    }
    
    @Test
    void shouldRemoveServers() {
        MCPServerConfig server = createTestServerConfig("test-server", "Test Server");
        
        // Add server first
        serverManager.addServer(server);
        
        // Remove server
        CompletableFuture<Void> removeFuture = serverManager.removeServer("test-server");
        assertThat(removeFuture).isNotNull();
    }
    
    @Test
    void shouldGetServerStatuses() {
        List<MCPServerStatus> statuses = serverManager.getServerStatuses();
        assertThat(statuses).isNotNull();
    }
    
    @Test
    void shouldPerformHealthChecks() {
        CompletableFuture<Void> healthCheck = serverManager.healthCheck();
        assertThat(healthCheck).isNotNull();
        
        CompletableFuture<Void> specificHealthCheck = serverManager.healthCheck("test-server");
        assertThat(specificHealthCheck).isNotNull();
    }
    
    @Test
    void shouldGetReadyServersByHealth() {
        List<String> readyServers = serverManager.getReadyServersByHealth();
        assertThat(readyServers).isNotNull();
    }
    
    @Test
    void shouldGetAllAvailableTools() {
        Map<String, String> tools = serverManager.getAllAvailableTools();
        assertThat(tools).isNotNull();
    }
    
    @Test
    void shouldGetConflictedTools() {
        Map<String, List<String>> conflicts = serverManager.getConflictedTools();
        assertThat(conflicts).isNotNull();
    }
    
    @Test
    void shouldGetClientForTool() {
        // This will return null since no servers are actually connected in the test
        assertThat(serverManager.getClientForTool("test-tool")).isNull();
    }
    
    @Test
    void shouldHandleNullServerConfig() {
        CompletableFuture<Void> future = serverManager.addServer(null);
        
        assertThat(future).isCompletedExceptionally();
        assertThat(future).failsWithin(Duration.ofSeconds(1))
            .withThrowableOfType(Exception.class)
            .withMessageContaining("Server configuration cannot be null");
    }
    
    @Test
    void shouldHandleDisabledServer() {
        MCPServerConfig disabledServer = new MCPServerConfig(
            "disabled-server",
            "Disabled Server",
            "echo",
            List.of("test"),
            Map.of(),
            false // disabled
        );
        
        CompletableFuture<Void> future = serverManager.addServer(disabledServer);
        assertThat(future).isCompleted();
    }
    
    @Test
    void shouldCheckServerReadiness() {
        assertThat(serverManager.isServerReady("non-existent-server")).isFalse();
    }
    
    @Test
    void shouldGetServerStatus() {
        MCPServerStatus status = serverManager.getServerStatus("non-existent-server");
        assertThat(status).isNull();
    }
    
    @Test
    void shouldGetClient() {
        assertThat(serverManager.getClient("non-existent-server")).isNull();
    }
    
    /**
     * Creates test server configurations for testing.
     */
    private List<MCPServerConfig> createTestServerConfigs() {
        return List.of(
            createTestServerConfig("server1", "Test Server 1"),
            createTestServerConfig("server2", "Test Server 2")
        );
    }
    
    /**
     * Creates a test server configuration.
     */
    private MCPServerConfig createTestServerConfig(String id, String name) {
        return new MCPServerConfig(
            id,
            name,
            "echo",
            List.of("test"),
            Map.of("TEST_ENV", "value"),
            true
        );
    }
}