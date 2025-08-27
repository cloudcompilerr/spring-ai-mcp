package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.MCPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for MCP Learning Platform.
 * 
 * This controller provides REST endpoints for exploring MCP concepts interactively
 * through a web interface. It demonstrates how to integrate MCP functionality
 * with Spring Boot web applications and provides educational examples.
 * 
 * Key endpoints:
 * - Interactive examples for different MCP concepts
 * - Mode switching between single and multi-server demonstrations
 * - Real-time tool execution and resource access
 * - Error scenario demonstrations
 * - Server status and health monitoring
 */
@RestController
@RequestMapping("/api/mcp")
@CrossOrigin(origins = "*") // For educational purposes - configure properly in production
public class MCPLearningController {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPLearningController.class);
    
    private final MCPDemonstrator demonstrator;
    
    public MCPLearningController(MCPDemonstrator demonstrator) {
        this.demonstrator = demonstrator;
        logger.info("MCPLearningController initialized - REST endpoints ready");
    }
    
    /**
     * Gets the current demonstration status.
     * 
     * @return Current status of the MCP demonstration system
     */
    @GetMapping("/status")
    public ResponseEntity<MCPExampleResult> getStatus() {
        logger.debug("📊 Status request received");
        MCPExampleResult status = demonstrator.getDemonstrationStatus();
        return ResponseEntity.ok(status);
    }
    
    /**
     * Gets all available demonstration examples.
     * 
     * @return List of available MCP examples
     */
    @GetMapping("/examples")
    public ResponseEntity<List<MCPExample>> getExamples() {
        logger.debug("📚 Examples request received");
        List<MCPExample> examples = demonstrator.getAvailableExamples();
        return ResponseEntity.ok(examples);
    }
    
    /**
     * Gets examples filtered by category.
     * 
     * @param category The category to filter by
     * @return List of examples in the specified category
     */
    @GetMapping("/examples/category/{category}")
    public ResponseEntity<List<MCPExample>> getExamplesByCategory(@PathVariable String category) {
        logger.debug("📚 Category examples request: {}", category);
        
        List<MCPExample> allExamples = demonstrator.getAvailableExamples();
        List<MCPExample> filteredExamples = allExamples.stream()
            .filter(example -> example.getCategory().equalsIgnoreCase(category))
            .toList();
        
        return ResponseEntity.ok(filteredExamples);
    }
    
    /**
     * Demonstrates single server MCP operations.
     * 
     * @return Result of the single server demonstration
     */
    @PostMapping("/examples/single-server")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> singleServerExample() {
        logger.info("🎓 Single server demonstration requested");
        
        return demonstrator.demonstrateSingleServer()
            .thenApply(result -> {
                logger.debug("Single server demonstration completed: {}", result.isSuccess());
                return ResponseEntity.ok(result);
            })
            .exceptionally(throwable -> {
                logger.error("Single server demonstration failed", throwable);
                MCPExampleResult errorResult = MCPExampleResult.error(
                    "Single server demonstration failed", throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(errorResult);
            });
    }
    
    /**
     * Demonstrates multi-server MCP operations.
     * 
     * @return Result of the multi-server demonstration
     */
    @PostMapping("/examples/multi-server")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> multiServerExample() {
        logger.info("🎓 Multi-server demonstration requested");
        
        return demonstrator.demonstrateMultiServer()
            .thenApply(result -> {
                logger.debug("Multi-server demonstration completed: {}", result.isSuccess());
                return ResponseEntity.ok(result);
            })
            .exceptionally(throwable -> {
                logger.error("Multi-server demonstration failed", throwable);
                MCPExampleResult errorResult = MCPExampleResult.error(
                    "Multi-server demonstration failed", throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(errorResult);
            });
    }
    
    /**
     * Demonstrates tool execution patterns.
     * 
     * @return Result of the tool execution demonstration
     */
    @PostMapping("/examples/tool-execution")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> toolExecutionExample() {
        logger.info("🎓 Tool execution demonstration requested");
        
        return demonstrator.demonstrateToolExecution()
            .thenApply(result -> {
                logger.debug("Tool execution demonstration completed: {}", result.isSuccess());
                return ResponseEntity.ok(result);
            })
            .exceptionally(throwable -> {
                logger.error("Tool execution demonstration failed", throwable);
                MCPExampleResult errorResult = MCPExampleResult.error(
                    "Tool execution demonstration failed", throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(errorResult);
            });
    }
    
    /**
     * Demonstrates error handling patterns.
     * 
     * @return Result of the error handling demonstration
     */
    @PostMapping("/examples/error-handling")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> errorHandlingExample() {
        logger.info("🎓 Error handling demonstration requested");
        
        return demonstrator.demonstrateErrorHandling()
            .thenApply(result -> {
                logger.debug("Error handling demonstration completed: {}", result.isSuccess());
                return ResponseEntity.ok(result);
            })
            .exceptionally(throwable -> {
                logger.error("Error handling demonstration failed", throwable);
                MCPExampleResult errorResult = MCPExampleResult.error(
                    "Error handling demonstration failed", throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(errorResult);
            });
    }
    
    /**
     * Demonstrates resource access patterns.
     * 
     * @return Result of the resource access demonstration
     */
    @PostMapping("/examples/resource-access")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> resourceAccessExample() {
        logger.info("🎓 Resource access demonstration requested");
        
        return demonstrator.demonstrateResourceAccess()
            .thenApply(result -> {
                logger.debug("Resource access demonstration completed: {}", result.isSuccess());
                return ResponseEntity.ok(result);
            })
            .exceptionally(throwable -> {
                logger.error("Resource access demonstration failed", throwable);
                MCPExampleResult errorResult = MCPExampleResult.error(
                    "Resource access demonstration failed", throwable.getMessage()
                );
                return ResponseEntity.internalServerError().body(errorResult);
            });
    }
    
    /**
     * Switches demonstration mode.
     * 
     * @param request The mode switch request
     * @return Result of the mode switch operation
     */
    @PostMapping("/mode/switch")
    public CompletableFuture<ResponseEntity<MCPExampleResult>> switchMode(@RequestBody ModeSwitchRequest request) {
        logger.info("🔄 Mode switch requested: {}", request.mode());
        
        try {
            MCPDemonstrator.DemonstrationMode mode = MCPDemonstrator.DemonstrationMode.valueOf(request.mode().toUpperCase());
            
            return demonstrator.switchMode(mode)
                .thenApply(result -> {
                    logger.debug("Mode switch completed: {}", result.isSuccess());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    logger.error("Mode switch failed", throwable);
                    MCPExampleResult errorResult = MCPExampleResult.error(
                        "Mode switch failed", throwable.getMessage()
                    );
                    return ResponseEntity.internalServerError().body(errorResult);
                });
                
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid mode requested: {}", request.mode());
            MCPExampleResult errorResult = MCPExampleResult.error(
                "Invalid mode", "Valid modes are: SINGLE_SERVER, MULTI_SERVER"
            );
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResult));
        }
    }
    
    /**
     * Gets the current demonstration mode.
     * 
     * @return Current demonstration mode information
     */
    @GetMapping("/mode/current")
    public ResponseEntity<ModeInfo> getCurrentMode() {
        logger.debug("📋 Current mode request received");
        
        MCPDemonstrator.DemonstrationMode currentMode = demonstrator.getCurrentMode();
        ModeInfo modeInfo = new ModeInfo(
            currentMode.name(),
            currentMode.getDisplayName(),
            currentMode.getDescription()
        );
        
        return ResponseEntity.ok(modeInfo);
    }
    
    /**
     * Gets available demonstration modes.
     * 
     * @return List of available demonstration modes
     */
    @GetMapping("/mode/available")
    public ResponseEntity<List<ModeInfo>> getAvailableModes() {
        logger.debug("📋 Available modes request received");
        
        List<ModeInfo> modes = List.of(
            new ModeInfo(
                MCPDemonstrator.DemonstrationMode.SINGLE_SERVER.name(),
                MCPDemonstrator.DemonstrationMode.SINGLE_SERVER.getDisplayName(),
                MCPDemonstrator.DemonstrationMode.SINGLE_SERVER.getDescription()
            ),
            new ModeInfo(
                MCPDemonstrator.DemonstrationMode.MULTI_SERVER.name(),
                MCPDemonstrator.DemonstrationMode.MULTI_SERVER.getDisplayName(),
                MCPDemonstrator.DemonstrationMode.MULTI_SERVER.getDescription()
            )
        );
        
        return ResponseEntity.ok(modes);
    }
    
    /**
     * Executes a specific tool with provided arguments.
     * 
     * @param request The tool execution request
     * @return Result of the tool execution
     */
    @PostMapping("/examples/tool-call")
    public CompletableFuture<ResponseEntity<ToolExecutionResponse>> executeToolExample(@RequestBody ToolCallRequest request) {
        logger.info("🔧 Tool execution requested: {} with {} parameters", 
            request.toolName(), request.arguments().size());
        
        // Get the appropriate client based on current mode
        MCPClient client = getClientForTool(request.toolName());
        
        if (client == null) {
            ToolExecutionResponse errorResponse = new ToolExecutionResponse(
                false, "No client available for tool: " + request.toolName(), null, null
            );
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body(errorResponse));
        }
        
        return client.callTool(request.toolName(), request.arguments())
            .thenApply(result -> {
                logger.debug("Tool execution completed: {} - Success: {}", 
                    request.toolName(), !result.isError());
                
                ToolExecutionResponse response = new ToolExecutionResponse(
                    !result.isError(),
                    result.isError() ? "Tool execution failed" : "Tool executed successfully",
                    result.content(),
                    result.isError() ? "Error occurred during tool execution" : null
                );
                
                return ResponseEntity.ok(response);
            })
            .exceptionally(throwable -> {
                logger.error("Tool execution failed for {}", request.toolName(), throwable);
                
                ToolExecutionResponse errorResponse = new ToolExecutionResponse(
                    false, "Tool execution failed", null, throwable.getMessage()
                );
                
                return ResponseEntity.internalServerError().body(errorResponse);
            });
    }
    
    /**
     * Lists all available tools from MCP servers.
     * 
     * @param serverId Optional server ID to list tools from specific server
     * @return List of available tools with server information
     */
    @GetMapping("/examples/tools")
    public ResponseEntity<Map<String, Object>> listToolsForDashboard(@RequestParam(required = false) String serverId) {
        logger.debug("🔧 Dashboard tools list requested for server: {}", serverId);
        
        try {
            // Mock response for now - in real implementation, this would query actual MCP servers
            List<Map<String, Object>> tools = List.of(
                Map.of(
                    "name", "calculate",
                    "description", "Perform mathematical calculations",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "expression", Map.of("type", "string", "description", "Mathematical expression"),
                            "precision", Map.of("type", "integer", "description", "Decimal precision")
                        ),
                        "required", List.of("expression")
                    )
                ),
                Map.of(
                    "name", "get_weather",
                    "description", "Get current weather information",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "location", Map.of("type", "string", "description", "City or location name")
                        ),
                        "required", List.of("location")
                    )
                ),
                Map.of(
                    "name", "echo",
                    "description", "Echo back the input message",
                    "inputSchema", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "message", Map.of("type", "string", "description", "Message to echo")
                        ),
                        "required", List.of("message")
                    )
                )
            );
            
            Map<String, Object> response = Map.of(
                "tools", tools,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list tools", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to list tools: " + e.getMessage()));
        }
    }
    
    /**
     * Lists all available resources from MCP servers.
     * 
     * @param serverId Optional server ID to list resources from specific server
     * @return List of available resources with server information
     */
    @GetMapping("/examples/resources")
    public ResponseEntity<Map<String, Object>> listResourcesForDashboard(@RequestParam(required = false) String serverId) {
        logger.debug("📚 Dashboard resources list requested for server: {}", serverId);
        
        try {
            // Mock response for now - in real implementation, this would query actual MCP servers
            List<Map<String, Object>> resources = List.of(
                Map.of(
                    "uri", "demo://server/status",
                    "name", "Server Status",
                    "description", "Current server status and information",
                    "mimeType", "application/json"
                ),
                Map.of(
                    "uri", "demo://examples/help",
                    "name", "Help Documentation",
                    "description", "Help and usage examples",
                    "mimeType", "text/markdown"
                ),
                Map.of(
                    "uri", "file://example.txt",
                    "name", "Example File",
                    "description", "An example text file resource",
                    "mimeType", "text/plain"
                )
            );
            
            Map<String, Object> response = Map.of(
                "resources", resources,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to list resources", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to list resources: " + e.getMessage()));
        }
    }
    
    /**
     * Executes a tool call for the dashboard interface.
     * 
     * @param request The tool call request with tool name, arguments, and optional server ID
     * @return Tool execution result with server information
     */
    @PostMapping("/examples/dashboard/tool-call")
    public ResponseEntity<Map<String, Object>> executeToolForDashboard(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("toolName");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) request.getOrDefault("arguments", Map.of());
        String serverId = (String) request.get("serverId");
        
        logger.info("🔧 Dashboard tool execution: {} on server: {}", toolName, serverId);
        
        try {
            // Mock tool execution - in real implementation, this would call actual MCP servers
            String result = executeToolMock(toolName, arguments);
            
            Map<String, Object> response = Map.of(
                "toolName", toolName,
                "arguments", arguments,
                "result", result,
                "isError", false,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis(),
                "executionTime", "45ms"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Tool execution failed: {}", toolName, e);
            
            Map<String, Object> errorResponse = Map.of(
                "toolName", toolName,
                "arguments", arguments,
                "error", e.getMessage(),
                "isError", true,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    /**
     * Reads a resource for the dashboard interface.
     * 
     * @param request The resource read request with URI and optional server ID
     * @return Resource content with server information
     */
    @PostMapping("/examples/dashboard/resource-read")
    public ResponseEntity<Map<String, Object>> readResourceForDashboard(@RequestBody Map<String, Object> request) {
        String uri = (String) request.get("uri");
        String serverId = (String) request.get("serverId");
        
        logger.info("📖 Dashboard resource read: {} from server: {}", uri, serverId);
        
        try {
            // Mock resource reading - in real implementation, this would read from actual MCP servers
            String content = readResourceMock(uri);
            
            Map<String, Object> response = Map.of(
                "uri", uri,
                "content", content,
                "isError", false,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis(),
                "contentLength", content.length()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Resource read failed: {}", uri, e);
            
            Map<String, Object> errorResponse = Map.of(
                "uri", uri,
                "error", e.getMessage(),
                "isError", true,
                "serverId", serverId != null ? serverId : "demo-server",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(errorResponse);
        }
    }
    
    // Mock implementations for demonstration
    
    private String executeToolMock(String toolName, Map<String, Object> arguments) {
        return switch (toolName) {
            case "calculate" -> {
                String expression = (String) arguments.get("expression");
                if (expression == null) {
                    throw new IllegalArgumentException("Expression is required");
                }
                // Simple calculation mock
                if (expression.equals("2 + 3")) {
                    yield "Result: 5";
                } else if (expression.equals("10 * 5")) {
                    yield "Result: 50";
                } else {
                    yield "Result: " + expression + " = [calculated value]";
                }
            }
            case "get_weather" -> {
                String location = (String) arguments.get("location");
                if (location == null) {
                    throw new IllegalArgumentException("Location is required");
                }
                yield String.format("Weather in %s: 22°C, Sunny, Light breeze", location);
            }
            case "echo" -> {
                String message = (String) arguments.get("message");
                if (message == null) {
                    throw new IllegalArgumentException("Message is required");
                }
                yield "Echo: " + message;
            }
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }
    
    private String readResourceMock(String uri) {
        return switch (uri) {
            case "demo://server/status" -> """
                {
                  "server": "Demo MCP Server",
                  "status": "running",
                  "uptime": "2h 15m",
                  "connections": 1,
                  "tools": 3,
                  "resources": 3
                }
                """;
            case "demo://examples/help" -> """
                # MCP Server Help
                
                ## Available Tools
                - **calculate**: Perform mathematical calculations
                - **get_weather**: Get weather information
                - **echo**: Echo back messages
                
                ## Available Resources
                - **demo://server/status**: Server status information
                - **demo://examples/help**: This help document
                - **file://example.txt**: Example text file
                
                ## Usage
                Use the dashboard interface to interact with tools and resources.
                """;
            case "file://example.txt" -> """
                This is an example text file resource.
                
                It demonstrates how MCP servers can provide access to files
                and other resources through the Model Context Protocol.
                
                Content can be:
                - Plain text (like this file)
                - JSON data
                - Markdown documentation
                - Binary data (with appropriate encoding)
                
                The MCP protocol allows clients to discover and access
                these resources dynamically.
                """;
            default -> throw new IllegalArgumentException("Unknown resource: " + uri);
        };
    }
    
    // Helper methods
    
    private MCPClient getClientForTool(String toolName) {
        // In multi-server mode, use the tool router
        if (demonstrator.getCurrentMode() == MCPDemonstrator.DemonstrationMode.MULTI_SERVER) {
            // This would need access to the MultiMCPServerManager
            // For now, return the first available client
        }
        
        // Get first available client
        return demonstrator.getCurrentMode() == MCPDemonstrator.DemonstrationMode.SINGLE_SERVER ?
            getFirstAvailableClient() : getFirstAvailableClient();
    }
    
    private MCPClient getFirstAvailableClient() {
        // This is a simplified implementation
        // In a real scenario, you'd get this from the demonstrator
        return null;
    }
    
    // Request/Response DTOs
    
    /**
     * Request for switching demonstration mode.
     */
    public record ModeSwitchRequest(String mode) {}
    
    /**
     * Information about a demonstration mode.
     */
    public record ModeInfo(String name, String displayName, String description) {}
    
    /**
     * Request for executing a tool.
     */
    public record ToolCallRequest(String toolName, Map<String, Object> arguments) {}
    
    /**
     * Response from tool execution.
     */
    public record ToolExecutionResponse(boolean success, String message, String content, String error) {}
    
    /**
     * Information about an available tool.
     */
    public record ToolInfo(String name, String description, String inputType) {}
    
    /**
     * Information about an available resource.
     */
    public record ResourceInfo(String uri, String name, String description, String mimeType) {}
}