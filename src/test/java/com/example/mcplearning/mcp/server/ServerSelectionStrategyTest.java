package com.example.mcplearning.mcp.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for server selection strategies.
 * 
 * These tests validate the different algorithms used for selecting
 * servers in multi-server environments, demonstrating load balancing
 * and health-based selection concepts.
 */
@ExtendWith(MockitoExtension.class)
class ServerSelectionStrategyTest {
    
    @Mock
    private MCPServerManager serverManager;
    
    private HealthBasedSelectionStrategy healthBasedStrategy;
    private RoundRobinSelectionStrategy roundRobinStrategy;
    
    @BeforeEach
    void setUp() {
        healthBasedStrategy = new HealthBasedSelectionStrategy();
        roundRobinStrategy = new RoundRobinSelectionStrategy();
    }
    
    @Test
    void healthBasedStrategy_shouldReturnNullForEmptyList() {
        String result = healthBasedStrategy.selectServer(List.of(), serverManager);
        assertThat(result).isNull();
    }
    
    @Test
    void healthBasedStrategy_shouldReturnNullForNullList() {
        String result = healthBasedStrategy.selectServer(null, serverManager);
        assertThat(result).isNull();
    }
    
    @Test
    void healthBasedStrategy_shouldSelectReadyServer() {
        List<String> servers = List.of("server1", "server2", "server3");
        
        // Mock server readiness
        when(serverManager.isServerReady("server1")).thenReturn(false);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        when(serverManager.isServerReady("server3")).thenReturn(false);
        
        // Mock server status for ready server
        MCPServerStatus status = MCPServerStatus.ready(
            createTestServerConfig("server2", "Server 2"),
            Duration.ofMillis(100)
        );
        when(serverManager.getServerStatus("server2")).thenReturn(status);
        
        String result = healthBasedStrategy.selectServer(servers, serverManager);
        assertThat(result).isEqualTo("server2");
    }
    
    @Test
    void healthBasedStrategy_shouldSelectServerWithBetterHealth() {
        List<String> servers = List.of("server1", "server2");
        
        // Both servers are ready
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        
        // Server1 has slower response time
        MCPServerStatus status1 = MCPServerStatus.ready(
            createTestServerConfig("server1", "Server 1"),
            Duration.ofMillis(500)
        );
        when(serverManager.getServerStatus("server1")).thenReturn(status1);
        
        // Server2 has faster response time
        MCPServerStatus status2 = MCPServerStatus.ready(
            createTestServerConfig("server2", "Server 2"),
            Duration.ofMillis(50)
        );
        when(serverManager.getServerStatus("server2")).thenReturn(status2);
        
        String result = healthBasedStrategy.selectServer(servers, serverManager);
        assertThat(result).isEqualTo("server2");
    }
    
    @Test
    void healthBasedStrategy_shouldHaveCorrectMetadata() {
        assertThat(healthBasedStrategy.getStrategyName()).isEqualTo("Health-Based Selection");
        assertThat(healthBasedStrategy.getDescription()).isNotBlank();
    }
    
    @Test
    void roundRobinStrategy_shouldReturnNullForEmptyList() {
        String result = roundRobinStrategy.selectServer(List.of(), serverManager);
        assertThat(result).isNull();
    }
    
    @Test
    void roundRobinStrategy_shouldReturnNullForNullList() {
        String result = roundRobinStrategy.selectServer(null, serverManager);
        assertThat(result).isNull();
    }
    
    @Test
    void roundRobinStrategy_shouldCycleThroughServers() {
        List<String> servers = List.of("server1", "server2", "server3");
        
        // All servers are ready
        when(serverManager.isServerReady(anyString())).thenReturn(true);
        
        // Reset counter for predictable testing
        roundRobinStrategy.resetCounter();
        
        // Test cycling through servers
        String first = roundRobinStrategy.selectServer(servers, serverManager);
        String second = roundRobinStrategy.selectServer(servers, serverManager);
        String third = roundRobinStrategy.selectServer(servers, serverManager);
        String fourth = roundRobinStrategy.selectServer(servers, serverManager); // Should cycle back
        
        assertThat(first).isEqualTo("server1");
        assertThat(second).isEqualTo("server2");
        assertThat(third).isEqualTo("server3");
        assertThat(fourth).isEqualTo("server1"); // Cycled back
    }
    
    @Test
    void roundRobinStrategy_shouldSkipNonReadyServers() {
        List<String> servers = List.of("server1", "server2", "server3");
        
        // Only server2 is ready
        when(serverManager.isServerReady("server1")).thenReturn(false);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        when(serverManager.isServerReady("server3")).thenReturn(false);
        
        String result = roundRobinStrategy.selectServer(servers, serverManager);
        assertThat(result).isEqualTo("server2");
    }
    
    @Test
    void roundRobinStrategy_shouldReturnNullWhenNoServersReady() {
        List<String> servers = List.of("server1", "server2", "server3");
        
        // No servers are ready
        when(serverManager.isServerReady(anyString())).thenReturn(false);
        
        String result = roundRobinStrategy.selectServer(servers, serverManager);
        assertThat(result).isNull();
    }
    
    @Test
    void roundRobinStrategy_shouldHaveCorrectMetadata() {
        assertThat(roundRobinStrategy.getStrategyName()).isEqualTo("Round-Robin Selection");
        assertThat(roundRobinStrategy.getDescription()).isNotBlank();
    }
    
    @Test
    void roundRobinStrategy_shouldResetCounter() {
        List<String> servers = List.of("server1", "server2");
        when(serverManager.isServerReady(anyString())).thenReturn(true);
        
        // Make some selections to advance counter
        roundRobinStrategy.selectServer(servers, serverManager);
        roundRobinStrategy.selectServer(servers, serverManager);
        
        int counterBefore = roundRobinStrategy.getCurrentCounter();
        assertThat(counterBefore).isGreaterThan(0);
        
        // Reset counter
        roundRobinStrategy.resetCounter();
        
        int counterAfter = roundRobinStrategy.getCurrentCounter();
        assertThat(counterAfter).isEqualTo(0);
    }
    
    @Test
    void strategies_shouldSupportToolSelection() {
        List<String> servers = List.of("server1");
        when(serverManager.isServerReady("server1")).thenReturn(true);
        
        MCPServerStatus status = MCPServerStatus.ready(
            createTestServerConfig("server1", "Server 1"),
            Duration.ofMillis(100)
        );
        when(serverManager.getServerStatus("server1")).thenReturn(status);
        
        String healthResult = healthBasedStrategy.selectServerForTool("test-tool", servers, serverManager);
        String roundRobinResult = roundRobinStrategy.selectServerForTool("test-tool", servers, serverManager);
        
        assertThat(healthResult).isEqualTo("server1");
        assertThat(roundRobinResult).isEqualTo("server1");
    }
    
    /**
     * Creates a test server configuration.
     */
    private com.example.mcplearning.mcp.config.MCPServerConfig createTestServerConfig(String id, String name) {
        return new com.example.mcplearning.mcp.config.MCPServerConfig(
            id,
            name,
            "echo",
            List.of("test"),
            java.util.Map.of(),
            true
        );
    }
}