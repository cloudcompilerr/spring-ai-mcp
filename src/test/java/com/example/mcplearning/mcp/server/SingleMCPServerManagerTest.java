package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SingleMCPServerManager.
 * 
 * These tests verify the complete server lifecycle management functionality
 * using mock MCP servers. They serve both as tests and as educational
 * examples of how MCP server management works.
 */
@SpringBootTest
@Timeout(30) // Prevent tests from hanging
class SingleMCPServerManagerTest {
    
    private SingleMCPServerManager serverManager;
    private MCPConfiguration configuration;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        configuration = createTestConfiguration();
        serverManager = new SingleMCPServerManager(configuration, objectMapper);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        if (serverManager != null && serverManager.isRunning()) {
            serverManager.stop().get(5, TimeUnit.SECONDS);
        }
    }
    
    @Test
    void shouldStartAndStopSuccessfully() throws Exception {
        // When starting the server manager
        CompletableFuture<Void> startFuture = serverManager.start();
        startFuture.get(10, TimeUnit.SECONDS);
        
        // Then it should be running
        assertTrue(serverManager.isRunning());
        
        // When stopping the server manager
        CompletableFuture<Void> stopFuture = serverManager.stop();
        stopFuture.get(10, TimeUnit.SECONDS);
        
        // Then it should not be running
        assertFalse(serverManager.isRunning());
    }
    
    @Test
    void shouldAddServerSuccessfully() throws Exception {
        // Given a valid server configuration
        MCPServerConfig serverConfig = createMockServerConfig("test-server", false);
        
        // When adding the server
        CompletableFuture<Void> addFuture = serverManager.addServer(serverConfig);
        addFuture.get(10, TimeUnit.SECONDS);
        
        // Then the server should be added and ready
        assertTrue(serverManager.getServerIds().contains("test-server"));
        
        // And the server status should be ready
        MCPServerStatus status = serverManager.getServerStatus("test-server");
        assertNotNull(status);
        assertEquals("test-server", status.serverId());
        
        // And we should be able to get a client
        MCPClient client = serverManager.getClient("test-server");
        assertNotNull(client);
        assertTrue(client.isConnected());
    }
    
    @Test
    void shouldHandleServerConnectionFailure() throws Exception {
        // Given a server configuration that will fail
        MCPServerConfig serverConfig = createMockServerConfig("failing-server", true);
        
        // When adding the server
        CompletableFuture<Void> addFuture = serverManager.addServer(serverConfig);
        
        // Then it should complete (even though connection failed)
        assertDoesNotThrow(() -> addFuture.get(15, TimeUnit.SECONDS));
        
        // And the server should be in error state
        MCPServerStatus status = serverManager.getServerStatus("failing-server");
        assertNotNull(status);
        assertEquals(MCPConnectionState.ERROR, status.state());
        assertNotNull(status.errorMessage());
        
        // And no client should be available
        MCPClient client = serverManager.getClient("failing-server");
        assertNull(client);
    }
    
    @Test
    void shouldRemoveServerSuccessfully() throws Exception {
        // Given a connected server
        MCPServerConfig serverConfig = createMockServerConfig("removable-server", false);
        serverManager.addServer(serverConfig).get(10, TimeUnit.SECONDS);
        
        assertTrue(serverManager.getServerIds().contains("removable-server"));
        
        // When removing the server
        CompletableFuture<Void> removeFuture = serverManager.removeServer("removable-server");
        removeFuture.get(10, TimeUnit.SECONDS);
        
        // Then the server should be removed
        assertFalse(serverManager.getServerIds().contains("removable-server"));
        assertNull(serverManager.getServerStatus("removable-server"));
        assertNull(serverManager.getClient("removable-server"));
    }
    
    @Test
    void shouldPerformHealthChecks() throws Exception {
        // Given a connected server
        MCPServerConfig serverConfig = createMockServerConfig("health-test-server", false);
        serverManager.addServer(serverConfig).get(10, TimeUnit.SECONDS);
        
        // When performing a health check
        CompletableFuture<Void> healthFuture = serverManager.healthCheck("health-test-server");
        healthFuture.get(10, TimeUnit.SECONDS);
        
        // Then the health check should complete successfully
        MCPServerStatus status = serverManager.getServerStatus("health-test-server");
        assertNotNull(status);
        assertNotNull(status.lastHealthCheck());
        assertTrue(status.isHealthy());
    }
    
    @Test
    void shouldHandleMultipleServers() throws Exception {
        // Given multiple server configurations
        MCPServerConfig server1 = createMockServerConfig("server-1", false);
        MCPServerConfig server2 = createMockServerConfig("server-2", false);
        
        // When adding multiple servers
        CompletableFuture<Void> add1 = serverManager.addServer(server1);
        CompletableFuture<Void> add2 = serverManager.addServer(server2);
        
        CompletableFuture.allOf(add1, add2).get(15, TimeUnit.SECONDS);
        
        // Then both servers should be managed
        List<String> serverIds = serverManager.getServerIds();
        assertTrue(serverIds.contains("server-1"));
        assertTrue(serverIds.contains("server-2"));
        
        // And both should have clients
        assertNotNull(serverManager.getClient("server-1"));
        assertNotNull(serverManager.getClient("server-2"));
        
        // And both should be ready
        assertTrue(serverManager.isServerReady("server-1"));
        assertTrue(serverManager.isServerReady("server-2"));
    }
    
    @Test
    void shouldGetServerStatuses() throws Exception {
        // Given multiple servers with different states
        MCPServerConfig workingServer = createMockServerConfig("working-server", false);
        MCPServerConfig failingServer = createMockServerConfig("failing-server", true);
        
        CompletableFuture<Void> add1 = serverManager.addServer(workingServer);
        CompletableFuture<Void> add2 = serverManager.addServer(failingServer);
        
        // Wait for both to complete (one success, one failure)
        CompletableFuture.allOf(add1, add2).get(15, TimeUnit.SECONDS);
        
        // When getting all server statuses
        List<MCPServerStatus> statuses = serverManager.getServerStatuses();
        
        // Then we should have status for both servers
        assertEquals(2, statuses.size());
        
        // And they should have different states
        Map<String, MCPConnectionState> statesByServer = statuses.stream()
            .collect(java.util.stream.Collectors.toMap(
                MCPServerStatus::serverId,
                MCPServerStatus::state
            ));
        
        // Working server should be ready, failing server should be in error
        assertTrue(statesByServer.get("working-server") == MCPConnectionState.READY ||
                  statesByServer.get("working-server") == MCPConnectionState.CONNECTED);
        assertEquals(MCPConnectionState.ERROR, statesByServer.get("failing-server"));
    }
    
    @Test
    void shouldSkipDisabledServers() throws Exception {
        // Given a disabled server configuration
        MCPServerConfig disabledConfig = new MCPServerConfig(
            "disabled-server",
            "Disabled Test Server",
            "java",
            List.of("-cp", System.getProperty("java.class.path"), 
                   MockMCPServer.class.getName()),
            Map.of(),
            false // disabled
        );
        
        // When adding the disabled server
        CompletableFuture<Void> addFuture = serverManager.addServer(disabledConfig);
        addFuture.get(5, TimeUnit.SECONDS);
        
        // Then the server should not be managed
        assertFalse(serverManager.getServerIds().contains("disabled-server"));
        assertNull(serverManager.getServerStatus("disabled-server"));
    }
    
    @Test
    void shouldHandleNullServerConfig() {
        // When adding a null server configuration
        CompletableFuture<Void> addFuture = serverManager.addServer(null);
        
        // Then it should fail with an appropriate exception
        assertThrows(Exception.class, () -> addFuture.get(5, TimeUnit.SECONDS));
    }
    
    @Test
    void shouldHandleRemoveNonExistentServer() throws Exception {
        // When removing a server that doesn't exist
        CompletableFuture<Void> removeFuture = serverManager.removeServer("non-existent");
        
        // Then it should complete without error
        assertDoesNotThrow(() -> removeFuture.get(5, TimeUnit.SECONDS));
    }
    
    /**
     * Creates a test configuration with appropriate settings for testing.
     */
    private MCPConfiguration createTestConfiguration() {
        MCPConfiguration config = new MCPConfiguration();
        config.setEnableMultiServer(true);
        config.setConnectionTimeout(Duration.ofSeconds(10));
        config.setMaxRetries(2);
        config.setRetryDelay(Duration.ofSeconds(1));
        config.setHealthCheckInterval(Duration.ofSeconds(30));
        config.setVerboseLogging(true);
        config.setEnableExamples(true);
        return config;
    }
    
    /**
     * Creates a mock server configuration for testing.
     * 
     * @param serverId The server ID
     * @param shouldFail Whether the mock server should fail
     * @return A server configuration that runs the MockMCPServer
     */
    private MCPServerConfig createMockServerConfig(String serverId, boolean shouldFail) {
        List<String> args = List.of(
            "-cp", System.getProperty("java.class.path"),
            MockMCPServer.class.getName()
        );
        
        if (shouldFail) {
            args = List.of(
                "-cp", System.getProperty("java.class.path"),
                MockMCPServer.class.getName(),
                "fail"
            );
        }
        
        return new MCPServerConfig(
            serverId,
            "Test Server " + serverId,
            "java",
            args,
            Map.of(),
            true
        );
    }
}