package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.ReactiveMCPClient;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPResource;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Reactive controller demonstrating Spring WebFlux integration with MCP.
 * 
 * This controller showcases:
 * - Non-blocking MCP operations using reactive streams
 * - Server-sent events for real-time MCP updates
 * - Streaming responses for large datasets
 * - Integration patterns between WebFlux and MCP
 */
@RestController
@RequestMapping("/api/reactive/mcp")
public class ReactiveMCPController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactiveMCPController.class);
    
    private final ReactiveMCPClient reactiveMCPClient;
    
    public ReactiveMCPController(ReactiveMCPClient reactiveMCPClient) {
        this.reactiveMCPClient = reactiveMCPClient;
        logger.info("Reactive MCP Controller initialized");
    }
    
    /**
     * Streams all available MCP tools as server-sent events.
     * 
     * This endpoint demonstrates how to use reactive streams to provide
     * real-time updates to web clients. Each tool is sent as a separate
     * server-sent event, allowing for progressive loading in the UI.
     * 
     * @return A Flux of MCPTool objects as server-sent events
     */
    @GetMapping(value = "/tools/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MCPTool> streamTools() {
        logger.info("Starting to stream MCP tools via server-sent events");
        
        return reactiveMCPClient.streamTools()
                .delayElements(Duration.ofMillis(100)) // Add small delay for demonstration
                .doOnNext(tool -> logger.debug("Streaming tool: {}", tool.name()))
                .doOnComplete(() -> logger.info("Completed streaming tools"))
                .doOnError(error -> logger.error("Error streaming tools", error));
    }
    
    /**
     * Streams all available MCP resources as server-sent events.
     * 
     * Similar to tools streaming, but for resources. This is particularly
     * useful when dealing with large numbers of resources that would be
     * inefficient to load all at once.
     * 
     * @return A Flux of MCPResource objects as server-sent events
     */
    @GetMapping(value = "/resources/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MCPResource> streamResources() {
        logger.info("Starting to stream MCP resources via server-sent events");
        
        return reactiveMCPClient.streamResources()
                .delayElements(Duration.ofMillis(150)) // Add small delay for demonstration
                .doOnNext(resource -> logger.debug("Streaming resource: {}", resource.uri()))
                .doOnComplete(() -> logger.info("Completed streaming resources"))
                .doOnError(error -> logger.error("Error streaming resources", error));
    }
    
    /**
     * Executes an MCP tool reactively and returns the result.
     * 
     * This endpoint demonstrates non-blocking tool execution using
     * reactive programming patterns.
     * 
     * @param toolName The name of the tool to execute
     * @param arguments The arguments to pass to the tool
     * @return A Mono containing the tool execution result
     */
    @PostMapping("/tools/{toolName}/execute")
    public Mono<MCPToolResult> executeTool(
            @PathVariable String toolName,
            @RequestBody Map<String, Object> arguments) {
        
        logger.info("Reactively executing tool: {} with arguments: {}", toolName, arguments);
        
        return reactiveMCPClient.callTool(toolName, arguments)
                .doOnSuccess(result -> logger.info("Tool {} executed successfully", toolName))
                .doOnError(error -> logger.error("Failed to execute tool: {}", toolName, error));
    }
    
    /**
     * Reads an MCP resource reactively.
     * 
     * @param uri The URI of the resource to read (URL encoded)
     * @return A Mono containing the resource content
     */
    @GetMapping("/resources/read")
    public Mono<String> readResource(@RequestParam String uri) {
        logger.info("Reactively reading resource: {}", uri);
        
        return reactiveMCPClient.readResource(uri)
                .doOnSuccess(content -> logger.info("Read resource {} ({} characters)", uri, content.length()))
                .doOnError(error -> logger.error("Failed to read resource: {}", uri, error));
    }
    
    /**
     * Provides real-time MCP connection status updates via server-sent events.
     * 
     * This endpoint demonstrates how to create a continuous stream of
     * status updates that clients can subscribe to for monitoring purposes.
     * 
     * @return A Flux of connection status updates
     */
    @GetMapping(value = "/status/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MCPConnectionStatus> streamConnectionStatus() {
        logger.info("Starting to stream MCP connection status");
        
        return Flux.interval(Duration.ofSeconds(2))
                .flatMap(tick -> reactiveMCPClient.isConnected()
                        .map(connected -> new MCPConnectionStatus(
                                connected,
                                LocalDateTime.now(),
                                "Connection check #" + tick
                        )))
                .doOnNext(status -> logger.debug("Streaming status: {}", status))
                .doOnError(error -> logger.error("Error streaming connection status", error));
    }
    
    /**
     * Demonstrates reactive composition by combining multiple MCP operations.
     * 
     * This endpoint shows how reactive streams can be composed to create
     * complex workflows that remain non-blocking and efficient.
     * 
     * @return A Mono containing a summary of MCP capabilities
     */
    @GetMapping("/capabilities")
    public Mono<MCPCapabilitiesSummary> getCapabilities() {
        logger.info("Getting MCP capabilities summary reactively");
        
        Mono<Integer> toolCount = reactiveMCPClient.listTools()
                .map(tools -> tools.size());
        
        Mono<Integer> resourceCount = reactiveMCPClient.listResources()
                .map(resources -> resources.size());
        
        Mono<Boolean> connectionStatus = reactiveMCPClient.isConnected();
        
        return Mono.zip(toolCount, resourceCount, connectionStatus)
                .map(tuple -> new MCPCapabilitiesSummary(
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        LocalDateTime.now()
                ))
                .doOnSuccess(summary -> logger.info("Generated capabilities summary: {}", summary))
                .doOnError(error -> logger.error("Failed to get capabilities summary", error));
    }
    
    /**
     * Record representing MCP connection status for streaming.
     */
    public record MCPConnectionStatus(
            boolean connected,
            LocalDateTime timestamp,
            String message
    ) {}
    
    /**
     * Record representing a summary of MCP capabilities.
     */
    public record MCPCapabilitiesSummary(
            int toolCount,
            int resourceCount,
            boolean connected,
            LocalDateTime timestamp
    ) {}
}