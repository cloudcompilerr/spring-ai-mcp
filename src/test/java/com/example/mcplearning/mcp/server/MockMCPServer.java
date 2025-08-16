package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.transport.JsonRpcRequest;
import com.example.mcplearning.mcp.transport.JsonRpcResponse;
import com.example.mcplearning.mcp.transport.JsonRpcError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mock MCP Server for testing purposes.
 * 
 * This class simulates an MCP server by implementing the basic MCP protocol
 * over stdin/stdout. It's designed to be used in integration tests to verify
 * the server manager functionality without requiring real MCP servers.
 */
public class MockMCPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(MockMCPServer.class);
    
    private final ObjectMapper objectMapper;
    private final ExecutorService executor;
    private final boolean shouldFail;
    private final long responseDelay;
    
    private volatile boolean running = false;
    private BufferedReader reader;
    private PrintWriter writer;
    
    /**
     * Creates a mock MCP server.
     * 
     * @param objectMapper JSON object mapper
     * @param shouldFail Whether the server should simulate failures
     * @param responseDelay Delay in milliseconds before responding
     */
    public MockMCPServer(ObjectMapper objectMapper, boolean shouldFail, long responseDelay) {
        this.objectMapper = objectMapper;
        this.shouldFail = shouldFail;
        this.responseDelay = responseDelay;
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Creates a successful mock MCP server with no delay.
     */
    public MockMCPServer(ObjectMapper objectMapper) {
        this(objectMapper, false, 0);
    }
    
    /**
     * Starts the mock server, reading from stdin and writing to stdout.
     */
    public void start() {
        if (running) {
            return;
        }
        
        running = true;
        reader = new BufferedReader(new InputStreamReader(System.in));
        writer = new PrintWriter(System.out, true);
        
        logger.debug("Mock MCP server starting (shouldFail={}, responseDelay={}ms)", shouldFail, responseDelay);
        
        executor.submit(this::processRequests);
    }
    
    /**
     * Stops the mock server.
     */
    public void stop() {
        running = false;
        executor.shutdown();
        
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing mock server streams", e);
        }
        
        logger.debug("Mock MCP server stopped");
    }
    
    /**
     * Main request processing loop.
     */
    private void processRequests() {
        try {
            String line;
            while (running && (line = reader.readLine()) != null) {
                processRequest(line.trim());
            }
        } catch (IOException e) {
            if (running) {
                logger.error("Error reading request", e);
            }
        }
    }
    
    /**
     * Processes a single JSON-RPC request.
     */
    private void processRequest(String requestLine) {
        if (requestLine.isEmpty()) {
            return;
        }
        
        try {
            // Add response delay if configured
            if (responseDelay > 0) {
                Thread.sleep(responseDelay);
            }
            
            JsonRpcRequest request = objectMapper.readValue(requestLine, JsonRpcRequest.class);
            logger.debug("Mock server received request: {} {}", request.method(), request.id());
            
            JsonRpcResponse response = handleRequest(request);
            String responseJson = objectMapper.writeValueAsString(response);
            
            writer.println(responseJson);
            writer.flush();
            
            logger.debug("Mock server sent response for request {}", request.id());
            
        } catch (Exception e) {
            logger.error("Error processing request: {}", requestLine, e);
            
            // Send error response
            try {
                JsonRpcResponse errorResponse = JsonRpcResponse.error("unknown", JsonRpcError.internalError("Internal server error"));
                String responseJson = objectMapper.writeValueAsString(errorResponse);
                writer.println(responseJson);
                writer.flush();
            } catch (Exception ex) {
                logger.error("Failed to send error response", ex);
            }
        }
    }
    
    /**
     * Handles a specific MCP request and returns the appropriate response.
     */
    private JsonRpcResponse handleRequest(JsonRpcRequest request) {
        if (shouldFail) {
            return JsonRpcResponse.error(request.id(), JsonRpcError.internalError("Mock server configured to fail"));
        }
        
        return switch (request.method()) {
            case "initialize" -> handleInitialize(request);
            case "tools/list" -> handleListTools(request);
            case "tools/call" -> handleCallTool(request);
            case "resources/list" -> handleListResources(request);
            case "resources/read" -> handleReadResource(request);
            default -> JsonRpcResponse.error(request.id(), JsonRpcError.methodNotFound(request.method()));
        };
    }
    
    /**
     * Handles the initialize request.
     */
    private JsonRpcResponse handleInitialize(JsonRpcRequest request) {
        Map<String, Object> result = Map.of(
            "protocolVersion", "2024-11-05",
            "capabilities", Map.of(
                "tools", Map.of(),
                "resources", Map.of()
            ),
            "serverInfo", Map.of(
                "name", "Mock MCP Server",
                "version", "1.0.0"
            )
        );
        
        return JsonRpcResponse.success(request.id(), result);
    }
    
    /**
     * Handles the tools/list request.
     */
    private JsonRpcResponse handleListTools(JsonRpcRequest request) {
        List<Map<String, Object>> tools = List.of(
            Map.of(
                "name", "echo",
                "description", "Echo the input message",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "message", Map.of("type", "string")
                    ),
                    "required", List.of("message")
                )
            ),
            Map.of(
                "name", "add",
                "description", "Add two numbers",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "a", Map.of("type", "number"),
                        "b", Map.of("type", "number")
                    ),
                    "required", List.of("a", "b")
                )
            )
        );
        
        Map<String, Object> result = Map.of("tools", tools);
        return JsonRpcResponse.success(request.id(), result);
    }
    
    /**
     * Handles the tools/call request.
     */
    private JsonRpcResponse handleCallTool(JsonRpcRequest request) {
        if (request.params() instanceof Map<?, ?> params) {
            String toolName = (String) params.get("name");
            Map<?, ?> arguments = (Map<?, ?>) params.get("arguments");
            
            Object result = switch (toolName) {
                case "echo" -> Map.of(
                    "content", List.of(Map.of(
                        "type", "text",
                        "text", "Echo: " + arguments.get("message")
                    ))
                );
                case "add" -> {
                    double a = ((Number) arguments.get("a")).doubleValue();
                    double b = ((Number) arguments.get("b")).doubleValue();
                    yield Map.of(
                        "content", List.of(Map.of(
                            "type", "text",
                            "text", "Result: " + (a + b)
                        ))
                    );
                }
                default -> Map.of(
                    "content", List.of(Map.of(
                        "type", "text",
                        "text", "Unknown tool: " + toolName
                    )),
                    "isError", true
                );
            };
            
            return JsonRpcResponse.success(request.id(), result);
        }
        
        return JsonRpcResponse.error(request.id(), JsonRpcError.invalidParams("Invalid params"));
    }
    
    /**
     * Handles the resources/list request.
     */
    private JsonRpcResponse handleListResources(JsonRpcRequest request) {
        List<Map<String, Object>> resources = List.of(
            Map.of(
                "uri", "mock://test.txt",
                "name", "Test Resource",
                "description", "A test resource for demonstration",
                "mimeType", "text/plain"
            )
        );
        
        Map<String, Object> result = Map.of("resources", resources);
        return JsonRpcResponse.success(request.id(), result);
    }
    
    /**
     * Handles the resources/read request.
     */
    private JsonRpcResponse handleReadResource(JsonRpcRequest request) {
        if (request.params() instanceof Map<?, ?> params) {
            String uri = (String) params.get("uri");
            
            Map<String, Object> content = Map.of(
                "uri", uri,
                "mimeType", "text/plain",
                "text", "This is the content of " + uri
            );
            
            Map<String, Object> result = Map.of("contents", List.of(content));
            return JsonRpcResponse.success(request.id(), result);
        }
        
        return JsonRpcResponse.error(request.id(), JsonRpcError.invalidParams("Invalid params"));
    }
    
    /**
     * Main method to run the mock server as a standalone process.
     */
    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        boolean shouldFail = args.length > 0 && "fail".equals(args[0]);
        long responseDelay = args.length > 1 ? Long.parseLong(args[1]) : 0;
        
        MockMCPServer server = new MockMCPServer(objectMapper, shouldFail, responseDelay);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        
        server.start();
        
        // Keep the main thread alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}