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
        logger.debug("Listing available tools");
        
        return sendRequest("tools/list", null)
            .thenApply(response -> {
                if (response.isError()) {
                    throw new MCPClientException("Failed to list tools: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object toolsObj = resultMap.get("tools");
                        if (toolsObj != null) {
                            List<MCPTool> tools = objectMapper.convertValue(toolsObj, new TypeReference<List<MCPTool>>() {});
                            logger.debug("Retrieved {} tools", tools.size());
                            return tools;
                        }
                    }
                    return List.of();
                } catch (Exception e) {
                    throw new MCPClientException("Failed to parse tools response", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<MCPToolResult> callTool(String toolName, Map<String, Object> arguments) {
        ensureInitialized();
        logger.debug("Calling tool '{}' with arguments: {}", toolName, arguments);
        
        Map<String, Object> params = Map.of(
            "name", toolName,
            "arguments", arguments != null ? arguments : Map.of()
        );
        
        return sendRequest("tools/call", params)
            .thenApply(response -> {
                if (response.isError()) {
                    throw new MCPClientException("Tool execution failed: " + response.error().message());
                }
                
                try {
                    MCPToolResult result = objectMapper.convertValue(response.result(), MCPToolResult.class);
                    logger.debug("Tool '{}' executed successfully", toolName);
                    return result;
                } catch (Exception e) {
                    throw new MCPClientException("Failed to parse tool result", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<List<MCPResource>> listResources() {
        ensureInitialized();
        logger.debug("Listing available resources");
        
        return sendRequest("resources/list", null)
            .thenApply(response -> {
                if (response.isError()) {
                    throw new MCPClientException("Failed to list resources: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object resourcesObj = resultMap.get("resources");
                        if (resourcesObj != null) {
                            List<MCPResource> resources = objectMapper.convertValue(resourcesObj, new TypeReference<List<MCPResource>>() {});
                            logger.debug("Retrieved {} resources", resources.size());
                            return resources;
                        }
                    }
                    return List.of();
                } catch (Exception e) {
                    throw new MCPClientException("Failed to parse resources response", e);
                }
            });
    }
    
    @Override
    public CompletableFuture<String> readResource(String uri) {
        ensureInitialized();
        logger.debug("Reading resource: {}", uri);
        
        Map<String, Object> params = Map.of("uri", uri);
        
        return sendRequest("resources/read", params)
            .thenApply(response -> {
                if (response.isError()) {
                    throw new MCPClientException("Failed to read resource: " + response.error().message());
                }
                
                try {
                    if (response.result() instanceof Map<?, ?> resultMap) {
                        Object contentsObj = resultMap.get("contents");
                        if (contentsObj instanceof List<?> contentsList && !contentsList.isEmpty()) {
                            Object firstContent = contentsList.get(0);
                            if (firstContent instanceof Map<?, ?> contentMap) {
                                Object text = contentMap.get("text");
                                if (text instanceof String) {
                                    logger.debug("Successfully read resource: {}", uri);
                                    return (String) text;
                                }
                            }
                        }
                    }
                    throw new MCPClientException("Invalid resource content format");
                } catch (Exception e) {
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
        
        return transport.sendRequest(request)
            .exceptionally(throwable -> {
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