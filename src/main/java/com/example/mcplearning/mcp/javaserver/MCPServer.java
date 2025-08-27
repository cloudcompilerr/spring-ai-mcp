package com.example.mcplearning.mcp.javaserver;

import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.transport.JsonRpcRequest;
import com.example.mcplearning.mcp.transport.JsonRpcResponse;
import com.example.mcplearning.mcp.transport.JsonRpcError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java-based MCP Server Framework
 * 
 * EDUCATIONAL OVERVIEW:
 * This class demonstrates how to implement an MCP server in Java. Unlike the client
 * implementation that connects to external servers, this creates a server that can
 * be connected to by MCP clients.
 * 
 * KEY MCP SERVER CONCEPTS:
 * - Server-side JSON-RPC handling
 * - Tool registration and execution
 * - Resource management and access
 * - Protocol initialization and capability negotiation
 * - Error handling and response formatting
 * 
 * ARCHITECTURE:
 * MCP Client ←→ stdin/stdout ←→ Java MCP Server (this class)
 * 
 * This server communicates via stdin/stdout using JSON-RPC 2.0 protocol,
 * making it compatible with any MCP client implementation.
 */
public abstract class MCPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPServer.class);
    
    private final String serverName;
    private final String serverVersion;
    private final ObjectMapper objectMapper;
    
    // Tool and resource registries
    private final Map<String, MCPServerTool> tools = new ConcurrentHashMap<>();
    private final Map<String, MCPServerResource> resources = new ConcurrentHashMap<>();
    
    // Server state
    private volatile boolean initialized = false;
    private volatile boolean running = false;
    private MCPClientInfo clientInfo;
    
    /**
     * Creates a new MCP server with the specified name and version.
     * 
     * @param serverName The name of the server
     * @param serverVersion The version of the server
     */
    public MCPServer(String serverName, String serverVersion) {
        this.serverName = serverName;
        this.serverVersion = serverVersion;
        this.objectMapper = new ObjectMapper();
        
        logger.info("🚀 MCP Server '{}' v{} created", serverName, serverVersion);
    }
    
    /**
     * Starts the MCP server and begins processing requests from stdin.
     * This method blocks until the server is stopped.
     */
    public void start() {
        logger.info("🌟 Starting MCP Server '{}'...", serverName);
        running = true;
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter writer = new PrintWriter(System.out, true)) {
            
            logger.info("✅ MCP Server '{}' ready for connections", serverName);
            
            String line;
            while (running && (line = reader.readLine()) != null) {
                try {
                    processRequest(line.trim(), writer);
                } catch (Exception e) {
                    logger.error("❌ Error processing request: {}", line, e);
                    sendErrorResponse(writer, "unknown", -32603, "Internal server error: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            logger.error("❌ MCP Server I/O error", e);
        } finally {
            running = false;
            logger.info("🛑 MCP Server '{}' stopped", serverName);
        }
    }
    
    /**
     * Stops the MCP server.
     */
    public void stop() {
        logger.info("🛑 Stopping MCP Server '{}'...", serverName);
        running = false;
    }
    
    /**
     * Registers a tool with the server.
     * 
     * @param tool The tool to register
     */
    protected void registerTool(MCPServerTool tool) {
        tools.put(tool.getName(), tool);
        logger.debug("🔧 Registered tool: {}", tool.getName());
    }
    
    /**
     * Registers a resource with the server.
     * 
     * @param resource The resource to register
     */
    protected void registerResource(MCPServerResource resource) {
        resources.put(resource.getUri(), resource);
        logger.debug("📄 Registered resource: {}", resource.getUri());
    }
    
    /**
     * Called during server initialization. Subclasses should override this
     * to register their tools and resources.
     */
    protected abstract void initializeServer();
    
    /**
     * Processes a JSON-RPC request from the client.
     */
    private void processRequest(String requestLine, PrintWriter writer) {
        if (requestLine.isEmpty()) {
            return;
        }
        
        logger.debug("📥 Received request: {}", requestLine);
        
        try {
            JsonRpcRequest request = objectMapper.readValue(requestLine, JsonRpcRequest.class);
            JsonRpcResponse response = handleRequest(request);
            
            String responseJson = objectMapper.writeValueAsString(response);
            writer.println(responseJson);
            writer.flush();
            
            logger.debug("📤 Sent response: {}", responseJson);
            
        } catch (Exception e) {
            logger.error("❌ Failed to process request: {}", requestLine, e);
            sendErrorResponse(writer, "unknown", -32700, "Parse error");
        }
    }
    
    /**
     * Handles a specific JSON-RPC request and returns the appropriate response.
     */
    private JsonRpcResponse handleRequest(JsonRpcRequest request) {
        String method = request.method();
        String requestId = request.id();
        
        logger.debug("🔄 Handling method: {}", method);
        
        try {
            return switch (method) {
                case "initialize" -> handleInitialize(requestId, request.params());
                case "tools/list" -> handleListTools(requestId);
                case "tools/call" -> handleCallTool(requestId, request.params());
                case "resources/list" -> handleListResources(requestId);
                case "resources/read" -> handleReadResource(requestId, request.params());
                default -> JsonRpcResponse.error(requestId, 
                    JsonRpcError.create(-32601, "Method not found: " + method));
            };
        } catch (Exception e) {
            logger.error("❌ Error handling method {}: {}", method, e.getMessage(), e);
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32603, "Internal error: " + e.getMessage()));
        }
    }
    
    /**
     * Handles the initialize request.
     */
    private JsonRpcResponse handleInitialize(String requestId, Object params) {
        logger.info("🤝 Initializing MCP Server connection");
        
        try {
            if (params instanceof Map<?, ?> paramsMap) {
                Object clientInfoObj = paramsMap.get("clientInfo");
                if (clientInfoObj != null) {
                    clientInfo = objectMapper.convertValue(clientInfoObj, MCPClientInfo.class);
                    logger.info("📋 Client info: {} v{}", clientInfo.name(), clientInfo.version());
                }
            }
            
            // Initialize server-specific functionality
            if (!initialized) {
                initializeServer();
                initialized = true;
                logger.info("✅ Server initialization complete");
            }
            
            // Return server capabilities
            Map<String, Object> result = Map.of(
                "serverInfo", Map.of(
                    "name", serverName,
                    "version", serverVersion
                ),
                "capabilities", Map.of(
                    "tools", Map.of("listChanged", false),
                    "resources", Map.of("subscribe", false, "listChanged", false)
                )
            );
            
            return JsonRpcResponse.success(requestId, result);
            
        } catch (Exception e) {
            logger.error("❌ Initialization failed", e);
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32603, "Initialization failed: " + e.getMessage()));
        }
    }
    
    /**
     * Handles the tools/list request.
     */
    private JsonRpcResponse handleListTools(String requestId) {
        logger.debug("🔧 Listing {} tools", tools.size());
        
        List<Map<String, Object>> toolList = tools.values().stream()
            .map(tool -> Map.of(
                "name", tool.getName(),
                "description", tool.getDescription(),
                "inputSchema", tool.getInputSchema()
            ))
            .toList();
        
        Map<String, Object> result = Map.of("tools", toolList);
        return JsonRpcResponse.success(requestId, result);
    }
    
    /**
     * Handles the tools/call request.
     */
    private JsonRpcResponse handleCallTool(String requestId, Object params) {
        if (!(params instanceof Map<?, ?> paramsMap)) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32602, "Invalid params for tools/call"));
        }
        
        String toolName = (String) paramsMap.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) paramsMap.get("arguments");
        
        if (toolName == null) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32602, "Missing tool name"));
        }
        
        logger.debug("🔧 Executing tool: {} with args: {}", toolName, arguments);
        
        MCPServerTool tool = tools.get(toolName);
        if (tool == null) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32601, "Tool not found: " + toolName));
        }
        
        try {
            MCPToolResult result = tool.execute(arguments != null ? arguments : Map.of());
            
            Map<String, Object> response = Map.of(
                "content", result.content(),
                "isError", result.isError(),
                "mimeType", result.mimeType() != null ? result.mimeType() : "text/plain"
            );
            
            return JsonRpcResponse.success(requestId, response);
            
        } catch (Exception e) {
            logger.error("❌ Tool execution failed: {}", toolName, e);
            Map<String, Object> errorResult = Map.of(
                "content", "Tool execution failed: " + e.getMessage(),
                "isError", true,
                "mimeType", "text/plain"
            );
            return JsonRpcResponse.success(requestId, errorResult);
        }
    }
    
    /**
     * Handles the resources/list request.
     */
    private JsonRpcResponse handleListResources(String requestId) {
        logger.debug("📄 Listing {} resources", resources.size());
        
        List<Map<String, Object>> resourceList = resources.values().stream()
            .map(resource -> Map.<String, Object>of(
                "uri", resource.getUri(),
                "name", resource.getName(),
                "description", resource.getDescription(),
                "mimeType", resource.getMimeType()
            ))
            .toList();
        
        Map<String, Object> result = Map.of("resources", resourceList);
        return JsonRpcResponse.success(requestId, result);
    }
    
    /**
     * Handles the resources/read request.
     */
    private JsonRpcResponse handleReadResource(String requestId, Object params) {
        if (!(params instanceof Map<?, ?> paramsMap)) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32602, "Invalid params for resources/read"));
        }
        
        String uri = (String) paramsMap.get("uri");
        if (uri == null) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32602, "Missing resource URI"));
        }
        
        logger.debug("📄 Reading resource: {}", uri);
        
        MCPServerResource resource = resources.get(uri);
        if (resource == null) {
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32602, "Resource not found: " + uri));
        }
        
        try {
            String content = resource.read();
            
            Map<String, Object> resourceContent = Map.of(
                "uri", uri,
                "mimeType", resource.getMimeType(),
                "text", content
            );
            
            Map<String, Object> result = Map.of("contents", List.of(resourceContent));
            return JsonRpcResponse.success(requestId, result);
            
        } catch (Exception e) {
            logger.error("❌ Resource read failed: {}", uri, e);
            return JsonRpcResponse.error(requestId, 
                JsonRpcError.create(-32603, "Failed to read resource: " + e.getMessage()));
        }
    }
    
    /**
     * Sends an error response to the client.
     */
    private void sendErrorResponse(PrintWriter writer, String requestId, int errorCode, String message) {
        try {
            JsonRpcResponse errorResponse = JsonRpcResponse.error(requestId, 
                JsonRpcError.create(errorCode, message));
            String responseJson = objectMapper.writeValueAsString(errorResponse);
            writer.println(responseJson);
            writer.flush();
        } catch (Exception e) {
            logger.error("❌ Failed to send error response", e);
        }
    }
    
    // Getters
    public String getServerName() { return serverName; }
    public String getServerVersion() { return serverVersion; }
    public boolean isInitialized() { return initialized; }
    public boolean isRunning() { return running; }
    public MCPClientInfo getClientInfo() { return clientInfo; }
}