package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;

/**
 * Reactive MCP client interface using Spring WebFlux for non-blocking operations.
 * 
 * This interface provides the same MCP operations as MCPClient but using
 * reactive streams (Mono/Flux) instead of CompletableFuture for better
 * integration with Spring WebFlux applications.
 * 
 * Key benefits of the reactive approach:
 * - Non-blocking I/O operations
 * - Better resource utilization
 * - Backpressure handling
 * - Composable operations
 * - Integration with reactive web endpoints
 */
public interface ReactiveMCPClient {
    
    /**
     * Reactively initializes the connection with the MCP server.
     * 
     * @param request The initialization request containing protocol version and client info
     * @return A Mono that emits the server's initialization response
     */
    Mono<MCPResponse> initialize(MCPInitializeRequest request);
    
    /**
     * Reactively lists all tools available on the connected MCP server.
     * 
     * @return A Mono that emits the list of available tools
     */
    Mono<List<MCPTool>> listTools();
    
    /**
     * Reactively lists all tools as a stream for better memory efficiency.
     * 
     * @return A Flux that emits each tool individually
     */
    Flux<MCPTool> streamTools();
    
    /**
     * Reactively executes a tool on the MCP server.
     * 
     * @param toolName The name of the tool to execute
     * @param arguments The arguments to pass to the tool
     * @return A Mono that emits the tool execution result
     */
    Mono<MCPToolResult> callTool(String toolName, Map<String, Object> arguments);
    
    /**
     * Reactively lists all resources available on the connected MCP server.
     * 
     * @return A Mono that emits the list of available resources
     */
    Mono<List<MCPResource>> listResources();
    
    /**
     * Reactively lists all resources as a stream for better memory efficiency.
     * 
     * @return A Flux that emits each resource individually
     */
    Flux<MCPResource> streamResources();
    
    /**
     * Reactively reads the content of a specific resource from the MCP server.
     * 
     * @param uri The URI of the resource to read
     * @return A Mono that emits the resource content
     */
    Mono<String> readResource(String uri);
    
    /**
     * Reactively checks if the client is currently connected to an MCP server.
     * 
     * @return A Mono that emits true if connected, false otherwise
     */
    Mono<Boolean> isConnected();
    
    /**
     * Reactively gets the server information after successful initialization.
     * 
     * @return A Mono that emits the server information, or empty if not initialized
     */
    Mono<MCPClientInfo> getServerInfo();
    
    /**
     * Reactively closes the connection to the MCP server.
     * 
     * @return A Mono that completes when the connection is closed
     */
    Mono<Void> close();
}