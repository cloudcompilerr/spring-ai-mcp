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
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive integration tests for MCP tool execution and resource access functionality.
 * 
 * These tests verify the complete workflow of tool discovery, execution, and resource
 * operations, including edge cases and error scenarios that are important for
 * educational purposes.
 */
@ExtendWith(MockitoExtension.class)
class MCPToolAndResourceIntegrationTest {
    
    @Mock
    private MCPTransport mockTransport;
    
    private ObjectMapper objectMapper;
    private DefaultMCPClient client;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new DefaultMCPClient(mockTransport, objectMapper);
        
        when(mockTransport.isConnected()).thenReturn(true);
        initializeClient();
    }
    
    @Test
    void shouldDiscoverAndExecuteToolsInSequence() throws ExecutionException, InterruptedException {
        // Given - Mock tool discovery
        List<Map<String, Object>> toolsData = List.of(
            Map.of(
                "name", "file_reader",
                "description", "Reads file contents",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "path", Map.of("type", "string", "description", "File path to read")
                    ),
                    "required", List.of("path")
                )
            ),
            Map.of(
                "name", "calculator",
                "description", "Performs mathematical calculations",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "expression", Map.of("type", "string", "description", "Math expression")
                    ),
                    "required", List.of("expression")
                )
            )
        );
        
        when(mockTransport.sendRequest(argThat(req -> "tools/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("2", Map.of("tools", toolsData))));
        
        // When - Discover tools
        List<MCPTool> tools = client.listTools().get();
        
        // Then - Verify tool discovery
        assertEquals(2, tools.size());
        assertEquals("file_reader", tools.get(0).name());
        assertEquals("calculator", tools.get(1).name());
        
        // Given - Mock tool execution for file_reader
        Map<String, Object> fileReaderResult = Map.of(
            "content", "Hello, World!",
            "isError", false,
            "mimeType", "text/plain"
        );
        
        when(mockTransport.sendRequest(argThat(req -> 
            "tools/call".equals(req.method()) && 
            req.params() instanceof Map<?, ?> params &&
            "file_reader".equals(params.get("name")))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("3", fileReaderResult)));
        
        // When - Execute file_reader tool
        MCPToolResult result = client.callTool("file_reader", Map.of("path", "/test/file.txt")).get();
        
        // Then - Verify tool execution
        assertNotNull(result);
        assertFalse(result.isError());
        assertEquals("Hello, World!", result.content());
        assertEquals("text/plain", result.mimeType());
    }
    
    @Test
    void shouldHandleToolExecutionWithComplexArguments() throws ExecutionException, InterruptedException {
        // Given - Complex tool arguments
        Map<String, Object> complexArgs = Map.of(
            "query", "SELECT * FROM users WHERE age > ?",
            "parameters", List.of(25),
            "options", Map.of(
                "limit", 100,
                "orderBy", "name",
                "includeMetadata", true
            )
        );
        
        Map<String, Object> toolResult = Map.of(
            "content", "[{\"id\": 1, \"name\": \"John\", \"age\": 30}]",
            "isError", false,
            "mimeType", "application/json"
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("4", toolResult)));
        
        // When
        MCPToolResult result = client.callTool("database_query", complexArgs).get();
        
        // Then
        assertNotNull(result);
        assertFalse(result.isError());
        assertTrue(result.content().contains("John"));
        assertEquals("application/json", result.mimeType());
        
        // Verify the request contained the complex arguments
        verify(mockTransport).sendRequest(argThat(req -> {
            if (!"tools/call".equals(req.method())) return false;
            if (!(req.params() instanceof Map<?, ?> params)) return false;
            if (!(params.get("arguments") instanceof Map<?, ?> args)) return false;
            return args.containsKey("query") && args.containsKey("parameters") && args.containsKey("options");
        }));
    }
    
    @Test
    void shouldHandleToolExecutionErrors() {
        // Given - Tool execution that returns an error
        Map<String, Object> errorResult = Map.of(
            "content", "File not found: /nonexistent/file.txt",
            "isError", true,
            "mimeType", "text/plain"
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("5", errorResult)));
        
        // When & Then
        assertDoesNotThrow(() -> {
            MCPToolResult result = client.callTool("file_reader", Map.of("path", "/nonexistent/file.txt")).get();
            assertTrue(result.isError());
            assertTrue(result.content().contains("File not found"));
        });
    }
    
    @Test
    void shouldHandleToolNotFoundError() {
        // Given - Server returns tool not found error
        JsonRpcError error = JsonRpcError.create(-32601, "Tool 'nonexistent_tool' not found");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("6", error)));
        
        // When & Then
        MCPClientException exception = assertThrows(MCPClientException.class, () -> {
            try {
                client.callTool("nonexistent_tool", Map.of()).get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Tool execution failed"));
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void shouldDiscoverAndReadResourcesInSequence() throws ExecutionException, InterruptedException {
        // Given - Mock resource discovery
        List<Map<String, Object>> resourcesData = List.of(
            Map.of(
                "uri", "file:///project/README.md",
                "name", "Project README",
                "description", "Main project documentation",
                "mimeType", "text/markdown"
            ),
            Map.of(
                "uri", "file:///project/config.json",
                "name", "Configuration",
                "description", "Application configuration file",
                "mimeType", "application/json"
            )
        );
        
        when(mockTransport.sendRequest(argThat(req -> "resources/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("7", Map.of("resources", resourcesData))));
        
        // When - Discover resources
        List<MCPResource> resources = client.listResources().get();
        
        // Then - Verify resource discovery
        assertEquals(2, resources.size());
        assertEquals("file:///project/README.md", resources.get(0).uri());
        assertEquals("Project README", resources.get(0).name());
        assertEquals("text/markdown", resources.get(0).mimeType());
        
        // Given - Mock resource reading
        String readmeContent = "# My Project\n\nThis is a sample project for MCP learning.";
        
        when(mockTransport.sendRequest(argThat(req -> 
            "resources/read".equals(req.method()) &&
            req.params() instanceof Map<?, ?> params &&
            "file:///project/README.md".equals(params.get("uri")))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("8", Map.of("contents", List.of(
                    Map.of(
                        "uri", "file:///project/README.md",
                        "mimeType", "text/markdown",
                        "text", readmeContent
                    )
                )))));
        
        // When - Read resource
        String content = client.readResource("file:///project/README.md").get();
        
        // Then - Verify resource content
        assertEquals(readmeContent, content);
        assertTrue(content.contains("My Project"));
    }
    
    @Test
    void shouldHandleResourceReadingWithBinaryContent() throws ExecutionException, InterruptedException {
        // Given - Binary resource content (base64 encoded)
        String binaryContent = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==";
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("9", Map.of("contents", List.of(
                    Map.of(
                        "uri", "file:///project/image.png",
                        "mimeType", "image/png",
                        "blob", binaryContent
                    )
                )))));
        
        // When - Try to read binary resource as text (should handle gracefully)
        // Note: This tests the current text-only implementation
        assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("file:///project/image.png").get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
    }
    
    @Test
    void shouldHandleResourceNotFoundError() {
        // Given - Server returns resource not found error
        JsonRpcError error = JsonRpcError.create(-32602, "Resource not found: file:///nonexistent.txt");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("10", error)));
        
        // When & Then
        MCPClientException exception = assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("file:///nonexistent.txt").get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Failed to read resource"));
        assertTrue(exception.getMessage().contains("not found"));
    }
    
    @Test
    void shouldHandleEmptyToolsAndResourcesLists() throws ExecutionException, InterruptedException {
        // Given - Server returns empty lists
        when(mockTransport.sendRequest(argThat(req -> "tools/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("11", Map.of("tools", List.of()))));
        
        when(mockTransport.sendRequest(argThat(req -> "resources/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("12", Map.of("resources", List.of()))));
        
        // When
        List<MCPTool> tools = client.listTools().get();
        List<MCPResource> resources = client.listResources().get();
        
        // Then
        assertTrue(tools.isEmpty());
        assertTrue(resources.isEmpty());
    }
    
    @Test
    void shouldHandleMalformedResponsesGracefully() {
        // Given - Server returns malformed response
        when(mockTransport.sendRequest(argThat(req -> "tools/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("13", Map.of("invalid", "response"))));
        
        // When & Then - Should return empty list instead of throwing
        assertDoesNotThrow(() -> {
            List<MCPTool> tools = client.listTools().get();
            assertTrue(tools.isEmpty());
        });
    }
    
    @Test
    void shouldHandleTransportFailuresDuringToolOperations() {
        // Given - Transport failure
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new MCPTransportException("Connection lost during operation")));
        
        // When & Then
        MCPClientException exception = assertThrows(MCPClientException.class, () -> {
            try {
                client.listTools().get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        assertTrue(exception.getMessage().contains("Transport error"));
    }
    
    /**
     * Helper method to initialize the client for tests.
     */
    private void initializeClient() {
        MCPClientInfo clientInfo = new MCPClientInfo("test-client", "1.0.0");
        MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        JsonRpcResponse mockResponse = JsonRpcResponse.success("1", 
            Map.of("serverInfo", Map.of("name", "test-server", "version", "1.0.0")));
        
        when(mockTransport.sendRequest(argThat(req -> 
            req != null && "initialize".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        
        assertDoesNotThrow(() -> client.initialize(request).get());
    }
}