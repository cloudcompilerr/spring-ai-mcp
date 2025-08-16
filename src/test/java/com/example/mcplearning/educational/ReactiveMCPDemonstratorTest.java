package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.ReactiveMCPClient;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ReactiveMCPDemonstrator.
 * 
 * These tests verify the reactive demonstration patterns and ensure
 * proper error handling and composition in reactive MCP operations.
 */
@ExtendWith(MockitoExtension.class)
class ReactiveMCPDemonstratorTest {
    
    @Mock
    private ReactiveMCPClient reactiveMCPClient;
    
    private ReactiveMCPDemonstrator demonstrator;
    
    @BeforeEach
    void setUp() {
        demonstrator = new ReactiveMCPDemonstrator(reactiveMCPClient);
    }
    
    @Test
    void shouldDemonstrateBasicReactiveOperations() {
        // Given
        when(reactiveMCPClient.isConnected()).thenReturn(Mono.just(true));
        when(reactiveMCPClient.listTools()).thenReturn(Mono.just(List.of(
                new MCPTool(
                        "demo-tool",
                        "A demo tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                )
        )));
        when(reactiveMCPClient.callTool(anyString(), any()))
                .thenReturn(Mono.just(new MCPToolResult("Tool executed", false, "text/plain")));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateBasicReactiveOperations())
                .expectNextMatches(result -> result.contains("Successfully executed tool"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleDisconnectedClientInBasicDemo() {
        // Given
        when(reactiveMCPClient.isConnected()).thenReturn(Mono.just(false));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateBasicReactiveOperations())
                .expectError(RuntimeException.class)
                .verify();
    }
    
    @Test
    void shouldHandleNoToolsAvailableInBasicDemo() {
        // Given
        when(reactiveMCPClient.isConnected()).thenReturn(Mono.just(true));
        when(reactiveMCPClient.listTools()).thenReturn(Mono.just(List.of()));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateBasicReactiveOperations())
                .expectNext("No tools available for demonstration")
                .verifyComplete();
    }
    
    @Test
    void shouldDemonstrateReactiveComposition() {
        // Given
        when(reactiveMCPClient.listTools()).thenReturn(Mono.just(List.of(
                new MCPTool("tool1", "desc1", new MCPToolInputSchema("object", Map.of(), List.of())),
                new MCPTool("tool2", "desc2", new MCPToolInputSchema("object", Map.of(), List.of()))
        )));
        when(reactiveMCPClient.listResources()).thenReturn(Mono.just(List.of(
                new MCPResource("uri1", "name1", "desc1", "text/plain")
        )));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateReactiveComposition())
                .expectNextMatches(result -> 
                        result.toolCount() == 2 && 
                        result.resourceCount() == 1 &&
                        result.message().contains("successfully"))
                .verifyComplete();
    }
    
    @Test
    void shouldDemonstrateReactiveStreaming() {
        // Given
        List<MCPTool> tools = List.of(
                new MCPTool("tool1", "First tool", new MCPToolInputSchema("object", Map.of(), List.of())),
                new MCPTool("tool2", "Second tool", new MCPToolInputSchema("object", Map.of(), List.of())),
                new MCPTool("tool3", "Third tool", new MCPToolInputSchema("object", Map.of(), List.of()))
        );
        
        when(reactiveMCPClient.streamTools()).thenReturn(Flux.fromIterable(tools));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateReactiveStreaming())
                .expectNextCount(3)
                .verifyComplete();
    }
    
    @Test
    void shouldDemonstrateErrorHandlingAndRetry() {
        // Given
        String toolName = "test-tool";
        when(reactiveMCPClient.callTool(toolName, Map.of()))
                .thenReturn(Mono.error(new RuntimeException("Temporary failure")))
                .thenReturn(Mono.error(new RuntimeException("Another failure")))
                .thenReturn(Mono.just(new MCPToolResult("Success after retries", false, "text/plain")));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateErrorHandlingAndRetry(toolName))
                .expectNextMatches(result -> result.contains("executed successfully"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandlePersistentFailuresInRetryDemo() {
        // Given
        String toolName = "failing-tool";
        when(reactiveMCPClient.callTool(toolName, Map.of()))
                .thenReturn(Mono.error(new RuntimeException("Persistent failure")));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateErrorHandlingAndRetry(toolName))
                .expectNextMatches(result -> result.contains("failed after retries"))
                .verifyComplete();
    }
    
    @Test
    void shouldDemonstrateResourceProcessing() {
        // Given
        List<MCPResource> resources = List.of(
                new MCPResource("file://test1.txt", "Test 1", "desc1", "text/plain"),
                new MCPResource("file://test2.txt", "Test 2", "desc2", "text/plain"),
                new MCPResource("file://binary.bin", "Binary", "desc3", "application/octet-stream")
        );
        
        when(reactiveMCPClient.streamResources()).thenReturn(Flux.fromIterable(resources));
        when(reactiveMCPClient.readResource("file://test1.txt"))
                .thenReturn(Mono.just("Content of test1"));
        when(reactiveMCPClient.readResource("file://test2.txt"))
                .thenReturn(Mono.just("Content of test2"));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateResourceProcessing())
                .expectNextMatches(summary -> 
                        summary.processedCount() == 2 && // Only text resources processed
                        summary.totalContentLength() > 0 &&
                        summary.message().contains("completed"))
                .verifyComplete();
    }
    
    @Test
    void shouldHandleResourceReadErrorsInProcessingDemo() {
        // Given
        List<MCPResource> resources = List.of(
                new MCPResource("file://good.txt", "Good", "desc1", "text/plain"),
                new MCPResource("file://bad.txt", "Bad", "desc2", "text/plain")
        );
        
        when(reactiveMCPClient.streamResources()).thenReturn(Flux.fromIterable(resources));
        when(reactiveMCPClient.readResource("file://good.txt"))
                .thenReturn(Mono.just("Good content"));
        when(reactiveMCPClient.readResource("file://bad.txt"))
                .thenReturn(Mono.error(new RuntimeException("Read error")));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateResourceProcessing())
                .expectNextMatches(summary -> 
                        summary.processedCount() == 2 && // Both resources processed (one with error)
                        summary.totalContentLength() == 12) // Only good content counted
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyToolsStreamInStreamingDemo() {
        // Given
        when(reactiveMCPClient.streamTools()).thenReturn(Flux.empty());
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateReactiveStreaming())
                .verifyComplete();
    }
    
    @Test
    void shouldHandleEmptyResourcesStreamInProcessingDemo() {
        // Given
        when(reactiveMCPClient.streamResources()).thenReturn(Flux.empty());
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateResourceProcessing())
                .expectNextMatches(summary -> 
                        summary.processedCount() == 0 &&
                        summary.totalContentLength() == 0)
                .verifyComplete();
    }
    
    @Test
    void shouldHandleErrorsInCompositionDemo() {
        // Given
        when(reactiveMCPClient.listTools())
                .thenReturn(Mono.error(new RuntimeException("Tools error")));
        when(reactiveMCPClient.listResources())
                .thenReturn(Mono.just(List.of()));
        
        // When & Then
        StepVerifier.create(demonstrator.demonstrateReactiveComposition())
                .expectError(RuntimeException.class)
                .verify();
    }
}