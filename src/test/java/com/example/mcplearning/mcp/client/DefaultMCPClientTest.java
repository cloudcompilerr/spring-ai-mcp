package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Unit tests for the default MCP client implementation.
 * 
 * These tests verify the client's behavior for all MCP operations,
 * including initialization, tool operations, resource operations,
 * and error handling scenarios.
 */
@ExtendWith(MockitoExtension.class)
class DefaultMCPClientTest {
    
    @Mock
    private MCPTransport mockTransport;
    
    private ObjectMapper objectMapper;
    private DefaultMCPClient client;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new DefaultMCPClient(mockTransport, objectMapper);
        
        // Mock transport as connected by default (lenient to avoid unnecessary stubbing errors)
        lenient().when(mockTransport.isConnected()).thenReturn(true);
    }
    
    @Test
    void shouldInitializeSuccessfully() {
        // Given
        MCPClientInfo clientInfo = new MCPClientInfo("test-client", "1.0.0");
        MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("1", 
            Map.of("serverInfo", Map.of("name", "test-server", "version", "1.0.0")));
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<MCPResponse> result = client.initialize(request);
        
        // Then
        assertDoesNotThrow(() -> {
            MCPResponse response = result.get();
            assertNotNull(response);
            assertTrue(client.isConnected());
        });
        
        verify(mockTransport).sendRequest(argThat(req -> 
            "initialize".equals(req.method()) && req.params() != null));
    }
    
    @Test
    void shouldFailInitializationOnError() {
        // Given
        MCPClientInfo clientInfo = new MCPClientInfo("test-client", "1.0.0");
        MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        JsonRpcError error = JsonRpcError.internalError("Server initialization failed");
        JsonRpcResponse mockResponse = JsonRpcResponse.error("1", error);
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<MCPResponse> result = client.initialize(request);
        
        // Then
        assertDoesNotThrow(() -> {
            MCPResponse response = result.get();
            assertNotNull(response);
            assertFalse(client.isConnected()); // Should not be initialized on error
        });
    }
    
    @Test
    void shouldListToolsAfterInitialization() {
        // Given
        initializeClient();
        
        List<Map<String, Object>> toolsData = List.of(
            Map.of("name", "echo", "description", "Echo tool", 
                   "inputSchema", Map.of("type", "object", "properties", Map.of())),
            Map.of("name", "calc", "description", "Calculator", 
                   "inputSchema", Map.of("type", "object", "properties", Map.of()))
        );
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("2", 
            Map.of("tools", toolsData));
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<List<MCPTool>> result = client.listTools();
        
        // Then
        assertDoesNotThrow(() -> {
            List<MCPTool> tools = result.get();
            assertEquals(2, tools.size());
            assertEquals("echo", tools.get(0).name());
            assertEquals("calc", tools.get(1).name());
        });
        
        verify(mockTransport).sendRequest(argThat(req -> "tools/list".equals(req.method())));
    }
    
    @Test
    void shouldFailListToolsWhenNotInitialized() {
        // Given - client not initialized
        
        // When & Then
        assertThrows(MCPClientException.class, () -> client.listTools());
    }
    
    @Test
    void shouldCallToolSuccessfully() {
        // Given
        initializeClient();
        
        String toolName = "echo";
        Map<String, Object> arguments = Map.of("message", "hello world");
        
        Map<String, Object> toolResultData = Map.of(
            "content", "hello world",
            "isError", false,
            "mimeType", "text/plain"
        );
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("3", toolResultData);
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<MCPToolResult> result = client.callTool(toolName, arguments);
        
        // Then
        assertDoesNotThrow(() -> {
            MCPToolResult toolResult = result.get();
            assertNotNull(toolResult);
            assertFalse(toolResult.isError());
        });
        
        verify(mockTransport).sendRequest(argThat(req -> 
            "tools/call".equals(req.method()) && req.params() != null));
    }
    
    @Test
    void shouldHandleToolExecutionError() {
        // Given
        initializeClient();
        
        JsonRpcError error = JsonRpcError.create(-1000, "Tool execution failed");
        JsonRpcResponse mockResponse = JsonRpcResponse.error("4", error);
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When & Then
        assertThrows(MCPClientException.class, () -> {
            try {
                client.callTool("failing-tool", Map.of()).get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldListResourcesAfterInitialization() {
        // Given
        initializeClient();
        
        List<Map<String, Object>> resourcesData = List.of(
            Map.of("uri", "file:///test.txt", "name", "Test File", 
                   "description", "A test file", "mimeType", "text/plain"),
            Map.of("uri", "file:///data.json", "name", "Data File", 
                   "description", "JSON data", "mimeType", "application/json")
        );
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("5", 
            Map.of("resources", resourcesData));
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<List<MCPResource>> result = client.listResources();
        
        // Then
        assertDoesNotThrow(() -> {
            List<MCPResource> resources = result.get();
            assertEquals(2, resources.size());
            assertEquals("file:///test.txt", resources.get(0).uri());
            assertEquals("file:///data.json", resources.get(1).uri());
        });
        
        verify(mockTransport).sendRequest(argThat(req -> "resources/list".equals(req.method())));
    }
    
    @Test
    void shouldReadResourceSuccessfully() {
        // Given
        initializeClient();
        
        String uri = "file:///test.txt";
        String expectedContent = "Hello, World!";
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("6", 
            Map.of("contents", List.of(
                Map.of("uri", uri, "mimeType", "text/plain", "text", expectedContent)
            )));
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        // When
        CompletableFuture<String> result = client.readResource(uri);
        
        // Then
        assertDoesNotThrow(() -> {
            String content = result.get();
            assertEquals(expectedContent, content);
        });
        
        verify(mockTransport).sendRequest(argThat(req -> 
            "resources/read".equals(req.method()) && req.params() != null));
    }
    
    @Test
    void shouldHandleTransportException() {
        // Given
        initializeClient();
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new MCPTransportException("Connection lost")));
        
        // When & Then
        assertThrows(MCPClientException.class, () -> {
            try {
                client.listTools().get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldCloseClientProperly() {
        // Given
        initializeClient();
        
        // When
        client.close();
        
        // Then
        assertFalse(client.isConnected());
        assertNull(client.getServerInfo());
        verify(mockTransport).close();
    }
    
    @Test
    void shouldReturnCorrectConnectionStatus() {
        // Given - not initialized
        assertFalse(client.isConnected());
        
        // When initialized
        initializeClient();
        assertTrue(client.isConnected());
        
        // When transport disconnected
        when(mockTransport.isConnected()).thenReturn(false);
        assertFalse(client.isConnected());
    }
    
    /**
     * Helper method to initialize the client for tests.
     */
    private void initializeClient() {
        MCPClientInfo clientInfo = new MCPClientInfo("test-client", "1.0.0");
        MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("1", 
            Map.of("serverInfo", Map.of("name", "test-server", "version", "1.0.0")));
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        assertDoesNotThrow(() -> client.initialize(request).get());
    }
}