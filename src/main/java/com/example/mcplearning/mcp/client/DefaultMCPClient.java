package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.transport.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the MCP client interface.
 * 
 * This implementation uses a pluggable transport layer to communicate with
 * MCP servers via JSON-RPC. It handles message serialization, request/response
 * correlation, and error handling.
 * 
 * The client maintains state about the connection and server capabilities
 * after successful initialization.
 */
public class DefaultMCPClient implements MCPClient {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultMCPClient.class);
    
    private final MCPTransport transport;
    private final ObjectMapper objectMapper;
    private final AtomicLong requestIdCounter = new AtomicLong(1);
    
    private volatile boolean initialized = false;
    private volatile MCPClientInfo serverInfo;
    
    /**
     * Creates a new MCP client with the specified transport.
     * 
     * @param transport The transport layer to use for communication
     * @param objectMapper The JSON object mapper for serialization
     */
    public DefaultMCPClient(MCPTransport transport, ObjectMapper objectMapper) {
        this.transport = transport;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public CompletableFuture<MCPResponse> initialize(MCPInitializeRequest request) {
        logger.debug("Initializing MCP client with request: {}", request);
        
        return sendRequest("initialize", request)
            .thenApply(response -> {
                if (response.isSuccess()) {
                    initialized = true;
                    // Extract server info from response if available
                    try {
                        if (response.result() instanceof Map<?, ?> resultMap) {
                            Object serverInfoObj = resultMap.get("serverInfo");
                            if (serverInfoObj != null) {
                                serverInfo = objectMapper.convertValue(serverInfoObj, MCPClientInfo.class);
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to extract server info from initialize response", e);
                    }
                    logger.info("MCP client initialized successfully");
                } else {
                    logger.error("MCP initialization failed: {}", response.error());
                    throw new MCPClientException("MCP initialization failed: " + response.error().message());
                }
                return MCPResponse.success(response.id(), response.result());
            })
            .exceptionally(throwable -> {
                logger.error("MCP initialization failed with exception", throwable);
                throw new MCPClientException("Failed to initialize MCP client", throwable);
            });
    }
    
    @Override
    public CompletableFuture<List<MCPTool>> listTools() {
        ensureInitialized();
        logger.info("🔧 MCP Protocol: Requesting tool discovery from server");
        logger.debug("Sending tools/list request to discover available MCP tools");
        
        return sendRequest("tools/list", null)
            .thenApply(response -> {
                if (response.isError()) {
                    logger.error("❌ MCP Protocol: Tool discovery failed - {}", response.error().message());
                    throw new MCPClientException("Failed to list tools: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object toolsObj = resultMap.get("tools");
                        if (toolsObj != null) {
                            List<MCPTool> tools = objectMapper.convertValue(toolsObj, new TypeReference<List<MCPTool>>() {});
                            logger.info("✅ MCP Protocol: Discovered {} tools from server", tools.size());
                            
                            // Educational logging: show tool details
                            if (logger.isDebugEnabled()) {
                                tools.forEach(tool -> 
                                    logger.debug("  📋 Tool: '{}' - {}", tool.name(), tool.description()));
                            }
                            
                            return tools;
                        }
                    }
                    logger.warn("⚠️ MCP Protocol: Server returned empty tools list");
                    return List.of();
                } catch (Exception e) {
                    logger.error("❌ MCP Protocol: Failed to parse tools response", e);
                    throw new MCPClientException("Failed to parse tools response", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<MCPToolResult> callTool(String toolName, Map<String, Object> arguments) {
        ensureInitialized();
        logger.info("🚀 MCP Protocol: Executing tool '{}' with {} arguments", 
                   toolName, arguments != null ? arguments.size() : 0);
        
        if (logger.isDebugEnabled() && arguments != null && !arguments.isEmpty()) {
            logger.debug("  📝 Tool arguments: {}", arguments);
        }
        
        Map<String, Object> params = Map.of(
            "name", toolName,
            "arguments", arguments != null ? arguments : Map.of()
        );
        
        return sendRequest("tools/call", params)
            .thenApply(response -> {
                if (response.isError()) {
                    logger.error("❌ MCP Protocol: Tool '{}' execution failed - {}", 
                               toolName, response.error().message());
                    throw new MCPClientException("Tool execution failed: " + response.error().message());
                }
                
                try {
                    MCPToolResult result = objectMapper.convertValue(response.result(), MCPToolResult.class);
                    
                    if (result.isError()) {
                        logger.warn("⚠️ MCP Protocol: Tool '{}' returned error result", toolName);
                    } else {
                        logger.info("✅ MCP Protocol: Tool '{}' executed successfully", toolName);
                    }
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("  📤 Tool result content length: {} characters", 
                                   result.content() != null ? result.content().length() : 0);
                    }
                    
                    return result;
                } catch (Exception e) {
                    logger.error("❌ MCP Protocol: Failed to parse tool result for '{}'", toolName, e);
                    throw new MCPClientException("Failed to parse tool result", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<List<MCPResource>> listResources() {
        ensureInitialized();
        logger.info("📚 MCP Protocol: Requesting resource discovery from server");
        logger.debug("Sending resources/list request to discover available MCP resources");
        
        return sendRequest("resources/list", null)
            .thenApply(response -> {
                if (response.isError()) {
                    logger.error("❌ MCP Protocol: Resource discovery failed - {}", response.error().message());
                    throw new MCPClientException("Failed to list resources: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object resourcesObj = resultMap.get("resources");
                        if (resourcesObj != null) {
                            List<MCPResource> resources = objectMapper.convertValue(resourcesObj, new TypeReference<List<MCPResource>>() {});
                            logger.info("✅ MCP Protocol: Discovered {} resources from server", resources.size());
                            
                            // Educational logging: show resource details
                            if (logger.isDebugEnabled()) {
                                resources.forEach(resource -> 
                                    logger.debug("  📄 Resource: '{}' ({}) - {}", 
                                               resource.name(), resource.mimeType(), resource.description()));
                            }
                            
                            return resources;
                        }
                    }
                    logger.warn("⚠️ MCP Protocol: Server returned empty resources list");
                    return List.of();
                } catch (Exception e) {
                    logger.error("❌ MCP Protocol: Failed to parse resources response", e);
                    throw new MCPClientException("Failed to parse resources response", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<String> readResource(String uri) {
        ensureInitialized();
        logger.info("📖 MCP Protocol: Reading resource content from '{}'", uri);
        
        Map<String, Object> params = Map.of("uri", uri);
        
        return sendRequest("resources/read", params)
            .thenApply(response -> {
                if (response.isError()) {
                    logger.error("❌ MCP Protocol: Failed to read resource '{}' - {}", 
                               uri, response.error().message());
                    throw new MCPClientException("Failed to read resource: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object contentsObj = resultMap.get("contents");
                        if (contentsObj instanceof List<?> contentsList && !contentsList.isEmpty()) {
                            Object firstContent = contentsList.get(0);
                            if (firstContent instanceof Map<?, ?> contentMap) {
                                Object text = contentMap.get("text");
                                if (text instanceof String textContent) {
                                    logger.info("✅ MCP Protocol: Successfully read resource '{}' ({} characters)", 
                                              uri, textContent.length());
                                    
                                    if (logger.isDebugEnabled() && textContent.length() < 500) {
                                        logger.debug("  📄 Resource content preview: {}", 
                                                   textContent.substring(0, Math.min(200, textContent.length())) + 
                                                   (textContent.length() > 200 ? "..." : ""));
                                    }
                                    
                                    return textContent;
                                }
                            }
                        }
                    }
                    logger.error("❌ MCP Protocol: Invalid resource content format for '{}'", uri);
                    throw new MCPClientException("Invalid resource content format");
                } catch (Exception e) {
                    logger.error("❌ MCP Protocol: Failed to parse resource content for '{}'", uri, e);
                    throw new MCPClientException("Failed to parse resource content", e);
                }
            });
    }
    
    @Override
    public boolean isConnected() {
        return transport.isConnected() && initialized;
    }
    
    @Override
    public MCPClientInfo getServerInfo() {
        return serverInfo;
    }
    
    @Override
    public void close() {
        logger.debug("Closing MCP client");
        initialized = false;
        serverInfo = null;
        transport.close();
    }
    
    /**
     * Sends a JSON-RPC request and returns the response.
     * 
     * @param method The method name
     * @param params The method parameters
     * @return A future that completes with the JSON-RPC response
     */
    private CompletableFuture<JsonRpcResponse> sendRequest(String method, Object params) {
        String requestId = String.valueOf(requestIdCounter.getAndIncrement());
        JsonRpcRequest request = JsonRpcRequest.create(requestId, method, params);
        
        logger.debug("📡 MCP Protocol: Sending JSON-RPC request [{}] method='{}' params={}", 
                    requestId, method, params != null ? "present" : "null");
        
        long startTime = System.currentTimeMillis();
        
        return transport.sendRequest(request)
            .whenComplete((response, throwable) -> {
                long duration = System.currentTimeMillis() - startTime;
                
                if (throwable != null) {
                    logger.error("📡 MCP Protocol: Request [{}] failed after {}ms - {}", 
                               requestId, duration, throwable.getMessage());
                } else if (response.isError()) {
                    logger.warn("📡 MCP Protocol: Request [{}] returned error after {}ms - {}", 
                              requestId, duration, response.error().message());
                } else {
                    logger.debug("📡 MCP Protocol: Request [{}] completed successfully after {}ms", 
                               requestId, duration);
                }
            })
            .exceptionally(throwable -> {
                logger.error("📡 MCP Protocol: Transport error during request [{}]", requestId, throwable);
                throw new MCPClientException("Transport error during request", throwable);
            });
    }
    
    /**
     * Ensures that the client has been initialized before performing operations.
     * 
     * @throws MCPClientException if the client is not initialized
     */
    private void ensureInitialized() {
        if (!initialized) {
            throw new MCPClientException("MCP client must be initialized before performing operations");
        }
    }
}