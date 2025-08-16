package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.config.MCPServerConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MCPServerStatus.
 * 
 * These tests verify the behavior of server status records and their
 * factory methods. They serve as both tests and documentation of the
 * expected status lifecycle.
 */
class MCPServerStatusTest {
    
    private final MCPServerConfig testConfig = new MCPServerConfig(
        "test-server",
        "Test Server",
        "test-command",
        List.of("arg1", "arg2"),
        Map.of("ENV_VAR", "value"),
        true
    );
    
    @Test
    void shouldCreateDisconnectedStatus() {
        // When creating a disconnected status
        MCPServerStatus status = MCPServerStatus.disconnected(testConfig);
        
        // Then it should have the correct properties
        assertEquals("test-server", status.serverId());
        assertEquals("Test Server", status.serverName());
        assertEquals(MCPConnectionState.DISCONNECTED, status.state());
        assertNull(status.lastHealthCheck());
        assertNull(status.errorMessage());
        assertNull(status.responseTime());
        assertEquals(0, status.connectionAttempts());
        assertNull(status.lastConnectionAttempt());
        assertEquals(testConfig, status.config());
        
        // And it should not be healthy
        assertFalse(status.isHealthy());
        assertFalse(status.isError());
    }
    
    @Test
    void shouldCreateConnectingStatus() {
        // When creating a connecting status
        MCPServerStatus status = MCPServerStatus.connecting(testConfig, 2);
        
        // Then it should have the correct properties
        assertEquals(MCPConnectionState.CONNECTING, status.state());
        assertEquals(2, status.connectionAttempts());
        assertNotNull(status.lastConnectionAttempt());
        
        // And it should not be healthy
        assertFalse(status.isHealthy());
        assertFalse(status.isError());
    }
    
    @Test
    void shouldCreateConnectedStatus() {
        // Given a response time
        Duration responseTime = Duration.ofMillis(100);
        
        // When creating a connected status
        MCPServerStatus status = MCPServerStatus.connected(testConfig, responseTime);
        
        // Then it should have the correct properties
        assertEquals(MCPConnectionState.CONNECTED, status.state());
        assertEquals(responseTime, status.responseTime());
        assertNotNull(status.lastHealthCheck());
        
        // And it should be healthy
        assertTrue(status.isHealthy());
        assertFalse(status.isError());
    }
    
    @Test
    void shouldCreateInitializingStatus() {
        // When creating an initializing status
        MCPServerStatus status = MCPServerStatus.initializing(testConfig);
        
        // Then it should have the correct properties
        assertEquals(MCPConnectionState.INITIALIZING, status.state());
        
        // And it should not be healthy yet
        assertFalse(status.isHealthy());
        assertFalse(status.isError());
    }
    
    @Test
    void shouldCreateReadyStatus() {
        // Given a response time
        Duration responseTime = Duration.ofMillis(50);
        
        // When creating a ready status
        MCPServerStatus status = MCPServerStatus.ready(testConfig, responseTime);
        
        // Then it should have the correct properties
        assertEquals(MCPConnectionState.READY, status.state());
        assertEquals(responseTime, status.responseTime());
        assertNotNull(status.lastHealthCheck());
        
        // And it should be healthy
        assertTrue(status.isHealthy());
        assertFalse(status.isError());
    }
    
    @Test
    void shouldCreateErrorStatus() {
        // Given an error message and attempt count
        String errorMessage = "Connection failed";
        int attempts = 3;
        
        // When creating an error status
        MCPServerStatus status = MCPServerStatus.error(testConfig, errorMessage, attempts);
        
        // Then it should have the correct properties
        assertEquals(MCPConnectionState.ERROR, status.state());
        assertEquals(errorMessage, status.errorMessage());
        assertEquals(attempts, status.connectionAttempts());
        assertNotNull(status.lastHealthCheck());
        assertNotNull(status.lastConnectionAttempt());
        
        // And it should be in error state
        assertFalse(status.isHealthy());
        assertTrue(status.isError());
    }
    
    @Test
    void shouldUpdateHealthCheck() {
        // Given an existing status
        MCPServerStatus originalStatus = MCPServerStatus.ready(testConfig, Duration.ofMillis(100));
        Instant originalHealthCheck = originalStatus.lastHealthCheck();
        
        // When updating with a health check
        Duration newResponseTime = Duration.ofMillis(75);
        MCPServerStatus updatedStatus = originalStatus.withHealthCheck(newResponseTime);
        
        // Then it should have updated health information
        assertEquals(newResponseTime, updatedStatus.responseTime());
        assertNotNull(updatedStatus.lastHealthCheck());
        assertTrue(updatedStatus.lastHealthCheck().isAfter(originalHealthCheck));
        
        // And other properties should remain the same
        assertEquals(originalStatus.serverId(), updatedStatus.serverId());
        assertEquals(originalStatus.state(), updatedStatus.state());
    }
    
    @Test
    void shouldUpdateWithError() {
        // Given an existing ready status
        MCPServerStatus originalStatus = MCPServerStatus.ready(testConfig, Duration.ofMillis(100));
        
        // When updating with an error
        String errorMessage = "Health check failed";
        MCPServerStatus errorStatus = originalStatus.withError(errorMessage);
        
        // Then it should be in error state
        assertEquals(MCPConnectionState.ERROR, errorStatus.state());
        assertEquals(errorMessage, errorStatus.errorMessage());
        assertEquals(1, errorStatus.connectionAttempts()); // incremented
        assertNotNull(errorStatus.lastConnectionAttempt());
        
        // And it should be in error state
        assertTrue(errorStatus.isError());
        assertFalse(errorStatus.isHealthy());
    }
    
    @Test
    void shouldUpdateState() {
        // Given an existing status
        MCPServerStatus originalStatus = MCPServerStatus.connecting(testConfig, 1);
        
        // When updating the state
        MCPServerStatus updatedStatus = originalStatus.withState(MCPConnectionState.CONNECTED);
        
        // Then the state should be updated
        assertEquals(MCPConnectionState.CONNECTED, updatedStatus.state());
        
        // And other properties should remain the same
        assertEquals(originalStatus.serverId(), updatedStatus.serverId());
        assertEquals(originalStatus.connectionAttempts(), updatedStatus.connectionAttempts());
    }
    
    @Test
    void shouldProvideCorrectDescriptions() {
        // Test descriptions for different states
        assertEquals("Server is disconnected", 
            MCPServerStatus.disconnected(testConfig).getDescription());
        
        assertEquals("Connecting to server (attempt 2)", 
            MCPServerStatus.connecting(testConfig, 2).getDescription());
        
        assertEquals("Connected to server", 
            MCPServerStatus.connected(testConfig, Duration.ofMillis(100)).getDescription());
        
        assertEquals("Initializing MCP protocol", 
            MCPServerStatus.initializing(testConfig).getDescription());
        
        assertEquals("Server is ready and operational", 
            MCPServerStatus.ready(testConfig, Duration.ofMillis(100)).getDescription());
        
        assertEquals("Server error: Connection timeout", 
            MCPServerStatus.error(testConfig, "Connection timeout", 1).getDescription());
    }
    
    @Test
    void shouldHandleNullErrorMessage() {
        // When creating an error status with null message
        MCPServerStatus status = MCPServerStatus.error(testConfig, null, 1);
        
        // Then the description should handle the null gracefully
        assertEquals("Server error: Unknown error", status.getDescription());
    }
}