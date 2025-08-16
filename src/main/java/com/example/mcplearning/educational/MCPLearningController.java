package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import com.example.mcplearning.mcp.protocol.MCPResource;
import com.example.mcplearning.mcp.server.MultiMCPServerManager;
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
     * Lists all available tools across all servers.
     * 
     * @return List of available tools
     */
    @GetMapping("/tools")
    public CompletableFuture<ResponseEntity<List<ToolInfo>>> listTools() {
        logger.debug("🔧 Tools list requested");
        
        // This is a simplified implementation - in a real scenario, you'd aggregate from all servers
        return CompletableFuture.completedFuture(
            ResponseEntity.ok(List.of(
                new ToolInfo("example_tool", "An example tool for demonstration", "string")
            ))
        );
    }
    
    /**
     * Lists all available resources across all servers.
     * 
     * @return List of available resources
     */
    @GetMapping("/resources")
    public CompletableFuture<ResponseEntity<List<ResourceInfo>>> listResources() {
        logger.debug("📚 Resources list requested");
        
        // This is a simplified implementation - in a real scenario, you'd aggregate from all servers
        return CompletableFuture.completedFuture(
            ResponseEntity.ok(List.of(
                new ResourceInfo("file:///example.txt", "Example Resource", "An example resource for demonstration", "text/plain")
            ))
        );
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