package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.client.MCPClientException;
import com.example.mcplearning.mcp.protocol.*;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for the MCP Tool and Resource Demonstrator.
 * 
 * These tests verify that the educational demonstrations work correctly
 * and handle various scenarios appropriately.
 */
@ExtendWith(MockitoExtension.class)
class MCPToolAndResourceDemonstratorTest {
    
    @Mock
    private MCPClient mockClient;
    
    private MCPToolAndResourceDemonstrator demonstrator;
    
    @BeforeEach
    void setUp() {
        demonstrator = new MCPToolAndResourceDemonstrator();
    }
    
    @Test
    void shouldDemonstrateToolWorkflowSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        List<MCPTool> mockTools = List.of(
            new MCPTool(
                "echo",
                "Echo tool for testing",
                new MCPToolInputSchema(
                    "object",
                    Map.of("message", Map.of("type", "string", "description", "Message to echo")),
                    List.of("message")
                )
            )
        );
        
        MCPToolResult mockResult = new MCPToolResult(
            "Hello, World!",
            false,
            "text/plain"
        );
        
        when(mockClient.listTools()).thenReturn(CompletableFuture.completedFuture(mockTools));
        when(mockClient.callTool(eq("echo"), any())).thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateToolWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("Tool workflow demonstration completed"));
    }
    
    @Test
    void shouldHandleEmptyToolsList() throws ExecutionException, InterruptedException {
        // Given
        when(mockClient.listTools()).thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateToolWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("no tools available"));
    }
    
    @Test
    void shouldDemonstrateResourceWorkflowSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        List<MCPResource> mockResources = List.of(
            new MCPResource(
                "file:///test.txt",
                "Test File",
                "A test file for demonstration",
                "text/plain"
            )
        );
        
        String mockContent = "This is test file content for MCP demonstration.";
        
        when(mockClient.listResources()).thenReturn(CompletableFuture.completedFuture(mockResources));
        when(mockClient.readResource("file:///test.txt")).thenReturn(CompletableFuture.completedFuture(mockContent));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateResourceWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("Resource workflow demonstration completed"));
    }
    
    @Test
    void shouldHandleEmptyResourcesList() throws ExecutionException, InterruptedException {
        // Given
        when(mockClient.listResources()).thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateResourceWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("no resources available"));
    }
    
    @Test
    void shouldDemonstrateAdvancedToolExecution() throws ExecutionException, InterruptedException {
        // Given
        List<Map<String, Object>> scenarios = List.of(
            Map.of("input", "hello"),
            Map.of("input", "world"),
            Map.of("input", "test")
        );
        
        MCPToolResult mockResult = new MCPToolResult("Success", false, "text/plain");
        
        when(mockClient.callTool(eq("test_tool"), any()))
            .thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateAdvancedToolExecution(
            mockClient, "test_tool", scenarios).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("Advanced execution results"));
        assertTrue(result.message().contains("Scenario 1"));
        assertTrue(result.message().contains("Scenario 2"));
        assertTrue(result.message().contains("Scenario 3"));
    }
    
    @Test
    void shouldDemonstrateErrorHandling() throws ExecutionException, InterruptedException {
        // Given
        when(mockClient.callTool(eq("nonexistent_tool"), any()))
            .thenReturn(CompletableFuture.failedFuture(
                new MCPClientException("Tool not found")));
        
        when(mockClient.readResource(eq("file:///nonexistent/file.txt")))
            .thenReturn(CompletableFuture.failedFuture(
                new MCPClientException("Resource not found")));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateErrorHandling(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("Error Handling Results"));
        assertTrue(result.message().contains("Tool Error"));
        assertTrue(result.message().contains("Resource Error"));
    }
    
    @Test
    void shouldHandleToolWorkflowFailure() throws ExecutionException, InterruptedException {
        // Given
        when(mockClient.listTools()).thenReturn(
            CompletableFuture.failedFuture(new MCPClientException("Connection failed")));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateToolWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertFalse(result.success());
        assertTrue(result.message().contains("Tool workflow failed"));
        assertTrue(result.message().contains("Connection failed"));
    }
    
    @Test
    void shouldHandleResourceWorkflowFailure() throws ExecutionException, InterruptedException {
        // Given
        when(mockClient.listResources()).thenReturn(
            CompletableFuture.failedFuture(new MCPClientException("Connection failed")));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateResourceWorkflow(mockClient).get();
        
        // Then
        assertNotNull(result);
        assertFalse(result.success());
        assertTrue(result.message().contains("Resource workflow failed"));
        assertTrue(result.message().contains("Connection failed"));
    }
    
    @Test
    void shouldHandleAdvancedExecutionWithPartialFailures() throws ExecutionException, InterruptedException {
        // Given
        List<Map<String, Object>> scenarios = List.of(
            Map.of("input", "success"),
            Map.of("input", "failure")
        );
        
        MCPToolResult successResult = new MCPToolResult("Success", false, "text/plain");
        
        when(mockClient.callTool(eq("test_tool"), eq(Map.of("input", "success"))))
            .thenReturn(CompletableFuture.completedFuture(successResult));
        
        when(mockClient.callTool(eq("test_tool"), eq(Map.of("input", "failure"))))
            .thenReturn(CompletableFuture.failedFuture(new MCPClientException("Tool execution failed")));
        
        // When
        MCPExampleResult result = demonstrator.demonstrateAdvancedToolExecution(
            mockClient, "test_tool", scenarios).get();
        
        // Then
        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.message().contains("Scenario 1"));
        assertTrue(result.message().contains("Scenario 2"));
        assertTrue(result.message().contains("FAILED"));
    }
}