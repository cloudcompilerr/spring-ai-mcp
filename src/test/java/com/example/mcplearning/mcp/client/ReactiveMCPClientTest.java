package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReactiveMCPClient implementations.
 * 
 * These tests demonstrate how to test reactive MCP operations using
 * reactor-test's StepVerifier for comprehensive reactive stream testing.
 */
@ExtendWith(MockitoExtension.class)
class ReactiveMCPClientTest {
    
    @Mock
    private MCPClient mockMCPClient;
    
    private ReactiveMCPClient reactiveMCPClient;
    
    @BeforeEach
    void setUp() {
        reactiveMCPClient = new DefaultReactiveMCPClient(mockMCPClient);
    }
    
    @Test
    void shouldInitializeReactively() {
        // Given
        MCPInitializeRequest request = new MCPInitializeRequest(
                "2024-11-05",
                new MCPClientInfo("Test Client", "1.0.0")
        );
        MCPResponse expectedResponse = new MCPResponse(
                "init-1",
                new MCPClientInfo("Test Server", "1.0.0"),
                null
        );
        
        when(mockMCPClient.initialize(any(MCPInitializeRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(expectedResponse));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.initialize(request))
                .expectNext(expectedResponse)
                .verifyComplete();
    }
    
    @Test
    void shouldListToolsReactively() {
        // Given
        List<MCPTool> expectedTools = List.of(
                new MCPTool(
                        "test-tool",
                        "A test tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                ),
                new MCPTool(
                        "another-tool",
                        "Another test tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                )
        );
        
        when(mockMCPClient.listTools())
                .thenReturn(CompletableFuture.completedFuture(expectedTools));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.listTools())
                .expectNext(expectedTools)
                .verifyComplete();
    }
    
    @Test
    void shouldStreamToolsIndividually() {
        // Given
        List<MCPTool> tools = List.of(
                new MCPTool(
                        "tool1",
                        "First tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                ),
                new MCPTool(
                        "tool2",
                        "Second tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                )
        );
        
        when(mockMCPClient.listTools())
                .thenReturn(CompletableFuture.completedFuture(tools));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.streamTools())
                .expectNext(tools.get(0))
                .expectNext(tools.get(1))
                .verifyComplete();
    }
    
    @Test
    void shouldCallToolReactively() {
        // Given
        String toolName = "test-tool";
        Map<String, Object> arguments = Map.of("param1", "value1");
        MCPToolResult expectedResult = new MCPToolResult("Tool executed successfully", false, "text/plain");
        
        when(mockMCPClient.callTool(toolName, arguments))
                .thenReturn(CompletableFuture.completedFuture(expectedResult));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.callTool(toolName, arguments))
                .expectNext(expectedResult)
                .verifyComplete();
    }
    
    @Test
    void shouldListResourcesReactively() {
        // Given
        List<MCPResource> expectedResources = List.of(
                new MCPResource(
                        "file://test1.txt",
                        "Test File 1",
                        "A test file",
                        "text/plain"
                ),
                new MCPResource(
                        "file://test2.txt",
                        "Test File 2",
                        "Another test file",
                        "text/plain"
                )
        );
        
        when(mockMCPClient.listResources())
                .thenReturn(CompletableFuture.completedFuture(expectedResources));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.listResources())
                .expectNext(expectedResources)
                .verifyComplete();
    }
    
    @Test
    void shouldStreamResourcesIndividually() {
        // Given
        List<MCPResource> resources = List.of(
                new MCPResource(
                        "file://resource1.txt",
                        "Resource 1",
                        "First resource",
                        "text/plain"
                ),
                new MCPResource(
                        "file://resource2.txt",
                        "Resource 2",
                        "Second resource",
                        "text/plain"
                )
        );
        
        when(mockMCPClient.listResources())
                .thenReturn(CompletableFuture.completedFuture(resources));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.streamResources())
                .expectNext(resources.get(0))
                .expectNext(resources.get(1))
                .verifyComplete();
    }
    
    @Test
    void shouldReadResourceReactively() {
        // Given
        String uri = "file://test.txt";
        String expectedContent = "This is test content";
        
        when(mockMCPClient.readResource(uri))
                .thenReturn(CompletableFuture.completedFuture(expectedContent));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.readResource(uri))
                .expectNext(expectedContent)
                .verifyComplete();
    }
    
    @Test
    void shouldCheckConnectionStatusReactively() {
        // Given
        when(mockMCPClient.isConnected()).thenReturn(true);
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.isConnected())
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    void shouldGetServerInfoReactively() {
        // Given
        MCPClientInfo expectedInfo = new MCPClientInfo("Test Server", "1.0.0");
        when(mockMCPClient.getServerInfo()).thenReturn(expectedInfo);
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.getServerInfo())
                .expectNext(expectedInfo)
                .verifyComplete();
    }
    
    @Test
    void shouldGetEmptyServerInfoWhenNull() {
        // Given
        when(mockMCPClient.getServerInfo()).thenReturn(null);
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.getServerInfo())
                .verifyComplete();
    }
    
    @Test
    void shouldCloseReactively() {
        // When & Then
        StepVerifier.create(reactiveMCPClient.close())
                .verifyComplete();
    }
    
    @Test
    void shouldHandleErrorsInReactiveOperations() {
        // Given
        RuntimeException expectedException = new RuntimeException("Test error");
        when(mockMCPClient.listTools())
                .thenReturn(CompletableFuture.failedFuture(expectedException));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.listTools())
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleEmptyToolsList() {
        // Given
        when(mockMCPClient.listTools())
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.streamTools())
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyResourcesList() {
        // Given
        when(mockMCPClient.listResources())
                .thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // When & Then
        StepVerifier.create(reactiveMCPClient.streamResources())
                .verifyComplete();
    }
}