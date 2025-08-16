package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPToolInputSchema;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import com.example.mcplearning.mcp.protocol.MCPResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MCPToolRouter.
 * 
 * These tests validate the routing, failover, and aggregation functionality
 * of the tool router in multi-server environments.
 */
@ExtendWith(MockitoExtension.class)
class MCPToolRouterTest {
    
    @Mock
    private MCPServerManager serverManager;
    
    @Mock
    private ServerSelectionStrategy selectionStrategy;
    
    @Mock
    private MCPClient client1;
    
    @Mock
    private MCPClient client2;
    
    private MCPToolRouter toolRouter;
    
    @BeforeEach
    void setUp() {
        when(selectionStrategy.getStrategyName()).thenReturn("Test Strategy");
        toolRouter = new MCPToolRouter(serverManager, selectionStrategy);
    }
    
    @Test
    void shouldInitializeWithStrategy() {
        assertThat(toolRouter).isNotNull();
        verify(selectionStrategy).getStrategyName();
    }
    
    @Test
    void shouldRouteToolCallSuccessfully() {
        String toolName = "test-tool";
        Map<String, Object> arguments = Map.of("arg1", "value1");
        
        // Mock server discovery
        when(serverManager.getServerIds()).thenReturn(List.of("server1", "server2"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        
        // Mock tool availability
        when(serverManager.getClient("server1")).thenReturn(client1);
        when(serverManager.getClient("server2")).thenReturn(client2);
        
        MCPTool tool = new MCPTool(toolName, "Test tool", new MCPToolInputSchema("object", Map.of(), List.of()));
        when(client1.listTools()).thenReturn(CompletableFuture.completedFuture(List.of(tool)));
        when(client2.listTools()).thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // Mock server selection
        when(selectionStrategy.selectServerForTool(eq(toolName), anyList(), eq(serverManager)))
            .thenReturn("server1");
        
        // Mock tool execution
        MCPToolResult expectedResult = MCPToolResult.success("success");
        when(client1.callTool(toolName, arguments))
            .thenReturn(CompletableFuture.completedFuture(expectedResult));
        
        // Execute
        CompletableFuture<MCPToolResult> result = toolRouter.callTool(toolName, arguments);
        
        assertThat(result).isNotNull();
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
    }
    
    @Test
    void shouldFailWhenNoServersProvideTool() {
        String toolName = "non-existent-tool";
        Map<String, Object> arguments = Map.of();
        
        // Mock empty server list
        when(serverManager.getServerIds()).thenReturn(List.of("server1"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        
        // Mock no tools available
        when(client1.listTools()).thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // Execute
        CompletableFuture<MCPToolResult> result = toolRouter.callTool(toolName, arguments);
        
        assertThat(result).isNotNull();
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
            .withThrowableOfType(Exception.class)
            .withMessageContaining("Tool not found");
    }
    
    @Test
    void shouldFailWhenNoServerSelected() {
        String toolName = "test-tool";
        Map<String, Object> arguments = Map.of();
        
        // Mock server discovery
        when(serverManager.getServerIds()).thenReturn(List.of("server1"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        
        // Mock tool availability
        MCPTool tool = new MCPTool(toolName, "Test tool", new MCPToolInputSchema("object", Map.of(), List.of()));
        when(client1.listTools()).thenReturn(CompletableFuture.completedFuture(List.of(tool)));
        
        // Mock no server selection
        when(selectionStrategy.selectServerForTool(eq(toolName), anyList(), eq(serverManager)))
            .thenReturn(null);
        
        // Execute
        CompletableFuture<MCPToolResult> result = toolRouter.callTool(toolName, arguments);
        
        assertThat(result).isNotNull();
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
            .withThrowableOfType(Exception.class)
            .withMessageContaining("No available server");
    }
    
    @Test
    void shouldRouteResourceReadSuccessfully() {
        String resourceUri = "test://resource";
        
        // Mock server discovery
        when(serverManager.getServerIds()).thenReturn(List.of("server1"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        
        // Mock resource availability
        MCPResource resource = new MCPResource(resourceUri, "Test Resource", "Test resource", "text/plain");
        when(client1.listResources()).thenReturn(CompletableFuture.completedFuture(List.of(resource)));
        
        // Mock server selection
        when(selectionStrategy.selectServerForResource(eq(resourceUri), anyList(), eq(serverManager)))
            .thenReturn("server1");
        
        // Mock resource reading
        String expectedContent = "Resource content";
        when(client1.readResource(resourceUri))
            .thenReturn(CompletableFuture.completedFuture(expectedContent));
        
        // Execute
        CompletableFuture<String> result = toolRouter.readResource(resourceUri);
        
        assertThat(result).isNotNull();
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
    }
    
    @Test
    void shouldFailWhenNoServersProvideResource() {
        String resourceUri = "non-existent://resource";
        
        // Mock empty server list
        when(serverManager.getServerIds()).thenReturn(List.of("server1"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        
        // Mock no resources available
        when(client1.listResources()).thenReturn(CompletableFuture.completedFuture(List.of()));
        
        // Execute
        CompletableFuture<String> result = toolRouter.readResource(resourceUri);
        
        assertThat(result).isNotNull();
        assertThat(result).failsWithin(java.time.Duration.ofSeconds(1))
            .withThrowableOfType(Exception.class)
            .withMessageContaining("Resource not found");
    }
    
    @Test
    void shouldListAllToolsAcrossServers() {
        // Mock servers
        when(serverManager.getServerIds()).thenReturn(List.of("server1", "server2"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        when(serverManager.getClient("server2")).thenReturn(client2);
        
        // Mock tools from different servers
        MCPTool tool1 = new MCPTool("tool1", "Tool 1", new MCPToolInputSchema("object", Map.of(), List.of()));
        MCPTool tool2 = new MCPTool("tool2", "Tool 2", new MCPToolInputSchema("object", Map.of(), List.of()));
        MCPTool tool3 = new MCPTool("tool1", "Tool 1 Duplicate", new MCPToolInputSchema("object", Map.of(), List.of())); // Duplicate name
        
        when(client1.listTools()).thenReturn(CompletableFuture.completedFuture(List.of(tool1, tool2)));
        when(client2.listTools()).thenReturn(CompletableFuture.completedFuture(List.of(tool3)));
        
        // Execute
        CompletableFuture<List<MCPTool>> result = toolRouter.listAllTools();
        
        assertThat(result).isNotNull();
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        
        // Should deduplicate tools by name
        List<MCPTool> tools = result.join();
        assertThat(tools).hasSize(2); // tool1 and tool2, with tool1 deduplicated
    }
    
    @Test
    void shouldListAllResourcesAcrossServers() {
        // Mock servers
        when(serverManager.getServerIds()).thenReturn(List.of("server1", "server2"));
        when(serverManager.isServerReady("server1")).thenReturn(true);
        when(serverManager.isServerReady("server2")).thenReturn(true);
        when(serverManager.getClient("server1")).thenReturn(client1);
        when(serverManager.getClient("server2")).thenReturn(client2);
        
        // Mock resources from different servers
        MCPResource resource1 = new MCPResource("uri1", "Resource 1", "Resource 1", "text/plain");
        MCPResource resource2 = new MCPResource("uri2", "Resource 2", "Resource 2", "text/plain");
        MCPResource resource3 = new MCPResource("uri1", "Resource 1 Duplicate", "Resource 1 Duplicate", "text/plain"); // Duplicate URI
        
        when(client1.listResources()).thenReturn(CompletableFuture.completedFuture(List.of(resource1, resource2)));
        when(client2.listResources()).thenReturn(CompletableFuture.completedFuture(List.of(resource3)));
        
        // Execute
        CompletableFuture<List<MCPResource>> result = toolRouter.listAllResources();
        
        assertThat(result).isNotNull();
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        
        // Should deduplicate resources by URI
        List<MCPResource> resources = result.join();
        assertThat(resources).hasSize(2); // uri1 and uri2, with uri1 deduplicated
    }
    
    @Test
    void shouldHandleEmptyServerList() {
        when(serverManager.getServerIds()).thenReturn(List.of());
        
        CompletableFuture<List<MCPTool>> toolsResult = toolRouter.listAllTools();
        CompletableFuture<List<MCPResource>> resourcesResult = toolRouter.listAllResources();
        
        assertThat(toolsResult).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(resourcesResult).succeedsWithin(java.time.Duration.ofSeconds(1));
        
        assertThat(toolsResult.join()).isEmpty();
        assertThat(resourcesResult.join()).isEmpty();
    }
    
    @Test
    void shouldHandleNonReadyServers() {
        when(serverManager.getServerIds()).thenReturn(List.of("server1", "server2"));
        when(serverManager.isServerReady("server1")).thenReturn(false); // Not ready
        when(serverManager.isServerReady("server2")).thenReturn(true);
        when(serverManager.getClient("server2")).thenReturn(client2);
        
        MCPTool tool = new MCPTool("tool1", "Tool 1", new MCPToolInputSchema("object", Map.of(), List.of()));
        when(client2.listTools()).thenReturn(CompletableFuture.completedFuture(List.of(tool)));
        
        CompletableFuture<List<MCPTool>> result = toolRouter.listAllTools();
        
        assertThat(result).succeedsWithin(java.time.Duration.ofSeconds(1));
        assertThat(result.join()).hasSize(1);
    }
}