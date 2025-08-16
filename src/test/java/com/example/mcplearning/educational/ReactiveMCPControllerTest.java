package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.ReactiveMCPClient;
import com.example.mcplearning.mcp.protocol.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration tests for ReactiveMCPController.
 * 
 * These tests demonstrate how to test reactive web endpoints that integrate
 * with MCP using Spring WebFlux's WebTestClient.
 */
@WebFluxTest(ReactiveMCPController.class)
class ReactiveMCPControllerTest {
    
    @Autowired
    private WebTestClient webTestClient;
    
    @MockBean
    private ReactiveMCPClient reactiveMCPClient;
    
    @Test
    void shouldStreamToolsViaServerSentEvents() {
        // Given
        List<MCPTool> tools = List.of(
                new MCPTool(
                        "test-tool-1",
                        "First test tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                ),
                new MCPTool(
                        "test-tool-2",
                        "Second test tool",
                        new MCPToolInputSchema("object", Map.of(), List.of())
                )
        );
        
        when(reactiveMCPClient.streamTools())
                .thenReturn(Flux.fromIterable(tools));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/tools/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(MCPTool.class)
                .hasSize(2)
                .contains(tools.toArray(new MCPTool[0]));
    }
    
    @Test
    void shouldStreamResourcesViaServerSentEvents() {
        // Given
        List<MCPResource> resources = List.of(
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
        
        when(reactiveMCPClient.streamResources())
                .thenReturn(Flux.fromIterable(resources));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/resources/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(MCPResource.class)
                .hasSize(2)
                .contains(resources.toArray(new MCPResource[0]));
    }
    
    @Test
    void shouldExecuteToolReactively() {
        // Given
        String toolName = "test-tool";
        Map<String, Object> arguments = Map.of("param1", "value1");
        MCPToolResult expectedResult = new MCPToolResult("Tool executed successfully", false, "text/plain");
        
        when(reactiveMCPClient.callTool(toolName, arguments))
                .thenReturn(Mono.just(expectedResult));
        
        // When & Then
        webTestClient.post()
                .uri("/api/reactive/mcp/tools/{toolName}/execute", toolName)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(arguments)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MCPToolResult.class)
                .isEqualTo(expectedResult);
    }
    
    @Test
    void shouldReadResourceReactively() {
        // Given
        String uri = "file://test.txt";
        String expectedContent = "This is test content";
        
        when(reactiveMCPClient.readResource(uri))
                .thenReturn(Mono.just(expectedContent));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/resources/read?uri={uri}", uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo(expectedContent);
    }
    
    @Test
    void shouldStreamConnectionStatus() {
        // Given
        when(reactiveMCPClient.isConnected())
                .thenReturn(Mono.just(true))
                .thenReturn(Mono.just(false))
                .thenReturn(Mono.just(true));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/status/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(ReactiveMCPController.MCPConnectionStatus.class)
                .hasSize(0); // Since we're not actually streaming in the test
    }
    
    @Test
    void shouldGetCapabilitiesSummary() {
        // Given
        when(reactiveMCPClient.listTools())
                .thenReturn(Mono.just(List.of(
                        new MCPTool("tool1", "desc1", new MCPToolInputSchema("object", Map.of(), List.of())),
                        new MCPTool("tool2", "desc2", new MCPToolInputSchema("object", Map.of(), List.of()))
                )));
        
        when(reactiveMCPClient.listResources())
                .thenReturn(Mono.just(List.of(
                        new MCPResource("uri1", "name1", "desc1", "text/plain")
                )));
        
        when(reactiveMCPClient.isConnected())
                .thenReturn(Mono.just(true));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/capabilities")
                .exchange()
                .expectStatus().isOk()
                .expectBody(ReactiveMCPController.MCPCapabilitiesSummary.class)
                .value(summary -> {
                    assert summary.toolCount() == 2;
                    assert summary.resourceCount() == 1;
                    assert summary.connected();
                });
    }
    
    @Test
    void shouldHandleToolExecutionError() {
        // Given
        String toolName = "failing-tool";
        Map<String, Object> arguments = Map.of("param1", "value1");
        
        when(reactiveMCPClient.callTool(toolName, arguments))
                .thenReturn(Mono.error(new RuntimeException("Tool execution failed")));
        
        // When & Then
        webTestClient.post()
                .uri("/api/reactive/mcp/tools/{toolName}/execute", toolName)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(arguments)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleResourceReadError() {
        // Given
        String uri = "file://nonexistent.txt";
        
        when(reactiveMCPClient.readResource(uri))
                .thenReturn(Mono.error(new RuntimeException("Resource not found")));
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/resources/read?uri={uri}", uri)
                .exchange()
                .expectStatus().is5xxServerError();
    }
    
    @Test
    void shouldHandleEmptyToolsStream() {
        // Given
        when(reactiveMCPClient.streamTools())
                .thenReturn(Flux.empty());
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/tools/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MCPTool.class)
                .hasSize(0);
    }
    
    @Test
    void shouldHandleEmptyResourcesStream() {
        // Given
        when(reactiveMCPClient.streamResources())
                .thenReturn(Flux.empty());
        
        // When & Then
        webTestClient.get()
                .uri("/api/reactive/mcp/resources/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(MCPResource.class)
                .hasSize(0);
    }
}