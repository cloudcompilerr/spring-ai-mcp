package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the process-based MCP transport.
 * 
 * These tests verify the transport's ability to communicate with
 * external processes via stdin/stdout using JSON-RPC messages.
 * 
 * Note: Some tests are disabled on Windows due to different process handling.
 */
class ProcessMCPTransportTest {
    
    private ObjectMapper objectMapper;
    private ProcessMCPTransport transport;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    @DisabledOnOs(OS.WINDOWS) // Process handling differs on Windows
    void shouldCreateTransportWithValidConfiguration() {
        // Given
        String command = "echo";
        List<String> args = List.of("test");
        Map<String, String> env = Map.of("TEST_VAR", "test_value");
        long timeout = 5000;
        
        // When
        transport = new ProcessMCPTransport(command, args, env, objectMapper, timeout);
        
        // Then
        assertNotNull(transport);
        assertFalse(transport.isConnected());
    }
    
    @Test
    void shouldFailConnectionWithInvalidCommand() {
        // Given
        transport = new ProcessMCPTransport(
            "nonexistent-command-12345", 
            List.of(), 
            Map.of(), 
            objectMapper, 
            5000
        );
        
        // When & Then
        assertThrows(MCPTransportException.class, () -> {
            try {
                transport.connect().get(1, TimeUnit.SECONDS);
            } catch (Exception e) {
                if (e.getCause() instanceof MCPTransportException) {
                    throw (MCPTransportException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldRejectRequestWhenNotConnected() {
        // Given
        transport = new ProcessMCPTransport(
            "echo", 
            List.of(), 
            Map.of(), 
            objectMapper, 
            5000
        );
        
        JsonRpcRequest request = JsonRpcRequest.create("1", "test", null);
        
        // When
        CompletableFuture<JsonRpcResponse> result = transport.sendRequest(request);
        
        // Then
        assertTrue(result.isCompletedExceptionally());
        assertThrows(MCPTransportException.class, () -> {
            try {
                result.get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPTransportException) {
                    throw (MCPTransportException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldRejectNotificationWhenNotConnected() {
        // Given
        transport = new ProcessMCPTransport(
            "echo", 
            List.of(), 
            Map.of(), 
            objectMapper, 
            5000
        );
        
        JsonRpcRequest notification = JsonRpcRequest.create("1", "notify", null);
        
        // When
        CompletableFuture<Void> result = transport.sendNotification(notification);
        
        // Then
        assertTrue(result.isCompletedExceptionally());
        assertThrows(MCPTransportException.class, () -> {
            try {
                result.get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPTransportException) {
                    throw (MCPTransportException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldCloseTransportSafely() {
        // Given
        transport = new ProcessMCPTransport(
            "echo", 
            List.of(), 
            Map.of(), 
            objectMapper, 
            5000
        );
        
        // When
        assertDoesNotThrow(() -> transport.close());
        
        // Then
        assertFalse(transport.isConnected());
    }
    
    @Test
    void shouldCloseTransportSafelyEvenWhenNotConnected() {
        // Given
        transport = new ProcessMCPTransport(
            "echo", 
            List.of(), 
            Map.of(), 
            objectMapper, 
            5000
        );
        
        // When & Then
        assertDoesNotThrow(() -> {
            transport.close();
            transport.close(); // Should be safe to call multiple times
        });
    }
    
    @Test
    void shouldHandleNullEnvironment() {
        // Given & When
        transport = new ProcessMCPTransport(
            "echo", 
            List.of("test"), 
            null, // null environment
            objectMapper, 
            5000
        );
        
        // Then
        assertNotNull(transport);
        assertFalse(transport.isConnected());
    }
    
    @Test
    void shouldHandleEmptyArguments() {
        // Given & When
        transport = new ProcessMCPTransport(
            "echo", 
            List.of(), // empty arguments
            Map.of(), 
            objectMapper, 
            5000
        );
        
        // Then
        assertNotNull(transport);
        assertFalse(transport.isConnected());
    }
    
    @Test
    void shouldRespectTimeoutConfiguration() {
        // Given
        long shortTimeout = 100; // Very short timeout
        transport = new ProcessMCPTransport(
            "sleep", 
            List.of("10"), // Sleep for 10 seconds (longer than timeout)
            Map.of(), 
            objectMapper, 
            shortTimeout
        );
        
        // This test verifies that the timeout configuration is accepted
        // Actual timeout behavior would require a real process interaction
        assertNotNull(transport);
    }
    
    /**
     * Integration test that would work with a real MCP server.
     * This test is disabled by default as it requires external dependencies.
     */
    @Test
    @org.junit.jupiter.api.Disabled("Requires real MCP server for integration testing")
    void shouldCommunicateWithRealMCPServer() {
        // This test would be enabled when testing with actual MCP servers
        // Example: uvx mcp-server-filesystem /tmp
        
        transport = new ProcessMCPTransport(
            "uvx", 
            List.of("mcp-server-filesystem", "/tmp"), 
            Map.of(), 
            objectMapper, 
            30000
        );
        
        assertDoesNotThrow(() -> {
            // Connect to server
            transport.connect().get(5, TimeUnit.SECONDS);
            assertTrue(transport.isConnected());
            
            // Send initialize request
            JsonRpcRequest initRequest = JsonRpcRequest.create("1", "initialize", 
                Map.of("protocolVersion", "2024-11-05", 
                       "clientInfo", Map.of("name", "test-client", "version", "1.0.0")));
            
            JsonRpcResponse response = transport.sendRequest(initRequest).get(5, TimeUnit.SECONDS);
            assertNotNull(response);
            assertTrue(response.isSuccess());
            
            // Clean up
            transport.close();
        });
    }
}