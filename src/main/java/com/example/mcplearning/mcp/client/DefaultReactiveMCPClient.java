package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.transport.MCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default implementation of ReactiveMCPClient that wraps the existing MCPClient
 * and provides reactive operations using Spring WebFlux.
 * 
 * This implementation demonstrates how to integrate MCP with reactive programming
 * patterns while maintaining compatibility with existing synchronous code.
 */
@Component
public class DefaultReactiveMCPClient implements ReactiveMCPClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultReactiveMCPClient.class);
    
    private final MCPClient mcpClient;
    private final AtomicReference<MCPClientInfo> serverInfo = new AtomicReference<>();
    
    /**
     * Creates a reactive MCP client wrapping an existing MCPClient.
     * 
     * @param mcpClient The underlying MCP client to wrap
     */
    public DefaultReactiveMCPClient(MCPClient mcpClient) {
        this.mcpClient = mcpClient;
        logger.info("Created reactive MCP client wrapper");
    }
    
    @Override
    public Mono<MCPResponse> initialize(MCPInitializeRequest request) {
        logger.debug("Reactively initializing MCP connection with request: {}", request);
        
        return Mono.fromFuture(() -> mcpClient.initialize(request))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(response -> {
                    logger.info("MCP initialization completed successfully");
                    // Store server info for later retrieval
                    if (response.result() instanceof MCPClientInfo clientInfo) {
                        serverInfo.set(clientInfo);
                    }
                })
                .doOnError(error -> logger.error("MCP initialization failed", error));
    }
    
    @Override
    public Mono<List<MCPTool>> listTools() {
        logger.debug("Reactively listing MCP tools");
        
        return Mono.fromFuture(() -> mcpClient.listTools())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(tools -> logger.info("Listed {} MCP tools", tools.size()))
                .doOnError(error -> logger.error("Failed to list MCP tools", error));
    }
    
    @Override
    public Flux<MCPTool> streamTools() {
        logger.debug("Streaming MCP tools");
        
        return listTools()
                .flatMapMany(Flux::fromIterable)
                .doOnNext(tool -> logger.debug("Streaming tool: {}", tool.name()))
                .doOnComplete(() -> logger.debug("Completed streaming tools"));
    }
    
    @Override
    public Mono<MCPToolResult> callTool(String toolName, Map<String, Object> arguments) {
        logger.debug("Reactively calling MCP tool: {} with arguments: {}", toolName, arguments);
        
        return Mono.fromFuture(() -> mcpClient.callTool(toolName, arguments))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(result -> logger.info("Tool {} executed successfully", toolName))
                .doOnError(error -> logger.error("Failed to execute tool: {}", toolName, error));
    }
    
    @Override
    public Mono<List<MCPResource>> listResources() {
        logger.debug("Reactively listing MCP resources");
        
        return Mono.fromFuture(() -> mcpClient.listResources())
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(resources -> logger.info("Listed {} MCP resources", resources.size()))
                .doOnError(error -> logger.error("Failed to list MCP resources", error));
    }
    
    @Override
    public Flux<MCPResource> streamResources() {
        logger.debug("Streaming MCP resources");
        
        return listResources()
                .flatMapMany(Flux::fromIterable)
                .doOnNext(resource -> logger.debug("Streaming resource: {}", resource.uri()))
                .doOnComplete(() -> logger.debug("Completed streaming resources"));
    }
    
    @Override
    public Mono<String> readResource(String uri) {
        logger.debug("Reactively reading MCP resource: {}", uri);
        
        return Mono.fromFuture(() -> mcpClient.readResource(uri))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(content -> logger.info("Read resource {} ({} characters)", uri, content.length()))
                .doOnError(error -> logger.error("Failed to read resource: {}", uri, error));
    }
    
    @Override
    public Mono<Boolean> isConnected() {
        return Mono.fromCallable(() -> mcpClient.isConnected())
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<MCPClientInfo> getServerInfo() {
        return Mono.fromCallable(() -> mcpClient.getServerInfo())
                .filter(info -> info != null)
                .subscribeOn(Schedulers.boundedElastic());
    }
    
    @Override
    public Mono<Void> close() {
        logger.info("Reactively closing MCP connection");
        
        return Mono.fromRunnable(() -> mcpClient.close())
                .subscribeOn(Schedulers.boundedElastic())
                .then()
                .doOnSuccess(v -> logger.info("MCP connection closed successfully"))
                .doOnError(error -> logger.error("Failed to close MCP connection", error));
    }
}