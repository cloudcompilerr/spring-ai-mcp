package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.example.mcplearning.mcp.server.MCPServerManager;
import com.example.mcplearning.mcp.server.MultiMCPServerManager;
import com.example.mcplearning.mcp.server.SingleMCPServerManager;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Educational MCP Demonstrator.
 * 
 * This component provides interactive examples and demonstrations of MCP concepts,
 * including mode switching between single and multi-server scenarios. It serves
 * as the primary educational interface for learning MCP implementation patterns.
 * 
 * Key features:
 * - Interactive examples for all MCP concepts
 * - Mode switching between single and multi-server demonstrations
 * - Success and failure scenario demonstrations
 * - Comprehensive logging for educational purposes
 * - Real-time MCP protocol interaction examples
 */
@Component
public class MCPDemonstrator {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPDemonstrator.class);
    
    private final MCPConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final MCPToolAndResourceDemonstrator toolDemonstrator;
    
    // Server managers for different modes
    private SingleMCPServerManager singleServerManager;
    private MultiMCPServerManager multiServerManager;
    private MCPServerManager currentManager;
    private DemonstrationMode currentMode = DemonstrationMode.SINGLE_SERVER;
    
    /**
     * Demonstration modes available in the learning platform.
     */
    public enum DemonstrationMode {
        SINGLE_SERVER("Single Server Mode", "Demonstrates basic MCP operations with a single server connection"),
        MULTI_SERVER("Multi Server Mode", "Demonstrates advanced MCP operations with multiple concurrent servers");
        
        private final String displayName;
        private final String description;
        
        DemonstrationMode(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public MCPDemonstrator(MCPConfiguration configuration, ObjectMapper objectMapper, 
                          MCPToolAndResourceDemonstrator toolDemonstrator) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.toolDemonstrator = toolDemonstrator;
        
        initializeServerManagers();
        
        logger.info("MCPDemonstrator initialized - ready for interactive demonstrations");
    }
    
    /**
     * Switches between single and multi-server demonstration modes.
     * 
     * @param mode The demonstration mode to switch to
     * @return A future that completes when the mode switch is complete
     */
    public CompletableFuture<MCPExampleResult> switchMode(DemonstrationMode mode) {
        if (mode == currentMode) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.quickSuccess("Already in " + mode.getDisplayName())
            );
        }
        
        logger.info("🔄 Switching demonstration mode: {} -> {}", 
            currentMode.getDisplayName(), mode.getDisplayName());
        
        return stopCurrentMode()
            .thenCompose(v -> startNewMode(mode))
            .thenApply(v -> {
                currentMode = mode;
                String result = String.format("Successfully switched to %s", mode.getDisplayName());
                logger.info("✅ Mode switch completed: {}", result);
                return MCPExampleResult.success(result, mode.getDescription());
            })
            .exceptionally(throwable -> {
                logger.error("❌ Mode switch failed: {}", throwable.getMessage());
                return MCPExampleResult.error("Mode switch failed", throwable.getMessage());
            });
    }
    
    /**
     * Demonstrates single server MCP operations.
     * 
     * @return A demonstration result showing single server capabilities
     */
    public CompletableFuture<MCPExampleResult> demonstrateSingleServer() {
        logger.info("🎓 Starting Single Server MCP Demonstration");
        
        return ensureMode(DemonstrationMode.SINGLE_SERVER)
            .thenCompose(v -> performSingleServerDemo())
            .thenApply(result -> {
                logger.info("✅ Single server demonstration completed");
                return MCPExampleResult.success("Single server demonstration completed", result);
            })
            .exceptionally(throwable -> {
                logger.error("❌ Single server demonstration failed", throwable);
                return MCPExampleResult.error("Single server demonstration failed", throwable.getMessage());
            });
    }
    
    /**
     * Demonstrates multi-server MCP operations.
     * 
     * @return A demonstration result showing multi-server capabilities
     */
    public CompletableFuture<MCPExampleResult> demonstrateMultiServer() {
        logger.info("🎓 Starting Multi-Server MCP Demonstration");
        
        return ensureMode(DemonstrationMode.MULTI_SERVER)
            .thenCompose(v -> performMultiServerDemo())
            .thenApply(result -> {
                logger.info("✅ Multi-server demonstration completed");
                return MCPExampleResult.success("Multi-server demonstration completed", result);
            })
            .exceptionally(throwable -> {
                logger.error("❌ Multi-server demonstration failed", throwable);
                return MCPExampleResult.error("Multi-server demonstration failed", throwable.getMessage());
            });
    }
    
    /**
     * Demonstrates tool execution patterns across different scenarios.
     * 
     * @return A demonstration result showing tool execution examples
     */
    public CompletableFuture<MCPExampleResult> demonstrateToolExecution() {
        logger.info("🎓 Starting Tool Execution Demonstration");
        
        if (currentManager == null || !currentManager.isRunning()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No active server manager", "Please start a demonstration mode first")
            );
        }
        
        List<String> serverIds = currentManager.getServerIds();
        if (serverIds.isEmpty()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No servers available", "No MCP servers are currently connected")
            );
        }
        
        String serverId = serverIds.get(0);
        MCPClient client = currentManager.getClient(serverId);
        
        if (client == null) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("Client not available", "MCP client is not ready for server: " + serverId)
            );
        }
        
        return toolDemonstrator.demonstrateToolWorkflow(client)
            .thenApply(result -> {
                logger.info("✅ Tool execution demonstration completed");
                return result;
            });
    }
    
    /**
     * Demonstrates error handling patterns in MCP operations.
     * 
     * @return A demonstration result showing error handling examples
     */
    public CompletableFuture<MCPExampleResult> demonstrateErrorHandling() {
        logger.info("🎓 Starting Error Handling Demonstration");
        
        if (currentManager == null || !currentManager.isRunning()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No active server manager", "Please start a demonstration mode first")
            );
        }
        
        List<String> serverIds = currentManager.getServerIds();
        if (serverIds.isEmpty()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No servers available", "No MCP servers are currently connected")
            );
        }
        
        String serverId = serverIds.get(0);
        MCPClient client = currentManager.getClient(serverId);
        
        if (client == null) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("Client not available", "MCP client is not ready for server: " + serverId)
            );
        }
        
        return toolDemonstrator.demonstrateErrorHandling(client)
            .thenApply(result -> {
                logger.info("✅ Error handling demonstration completed");
                return result;
            });
    }
    
    /**
     * Demonstrates resource access patterns.
     * 
     * @return A demonstration result showing resource access examples
     */
    public CompletableFuture<MCPExampleResult> demonstrateResourceAccess() {
        logger.info("🎓 Starting Resource Access Demonstration");
        
        if (currentManager == null || !currentManager.isRunning()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No active server manager", "Please start a demonstration mode first")
            );
        }
        
        List<String> serverIds = currentManager.getServerIds();
        if (serverIds.isEmpty()) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("No servers available", "No MCP servers are currently connected")
            );
        }
        
        String serverId = serverIds.get(0);
        MCPClient client = currentManager.getClient(serverId);
        
        if (client == null) {
            return CompletableFuture.completedFuture(
                MCPExampleResult.error("Client not available", "MCP client is not ready for server: " + serverId)
            );
        }
        
        return toolDemonstrator.demonstrateResourceWorkflow(client)
            .thenApply(result -> {
                logger.info("✅ Resource access demonstration completed");
                return result;
            });
    }
    
    /**
     * Gets the current demonstration mode.
     * 
     * @return The current demonstration mode
     */
    public DemonstrationMode getCurrentMode() {
        return currentMode;
    }
    
    /**
     * Gets the status of the current demonstration setup.
     * 
     * @return A status summary of the current demonstration
     */
    public MCPExampleResult getDemonstrationStatus() {
        if (currentManager == null) {
            return MCPExampleResult.error("No manager initialized", "Demonstration system not ready");
        }
        
        boolean isRunning = currentManager.isRunning();
        List<String> serverIds = currentManager.getServerIds();
        long readyServers = serverIds.stream()
            .mapToLong(id -> currentManager.isServerReady(id) ? 1 : 0)
            .sum();
        
        String statusDetails = String.format(
            "Mode: %s, Running: %s, Servers: %d/%d ready",
            currentMode.getDisplayName(), isRunning, readyServers, serverIds.size()
        );
        
        if (isRunning && readyServers > 0) {
            return MCPExampleResult.success("Demonstration system ready", statusDetails);
        } else {
            return MCPExampleResult.error("Demonstration system not ready", statusDetails);
        }
    }
    
    /**
     * Gets all available demonstration examples.
     * 
     * @return A list of available examples
     */
    public List<MCPExample> getAvailableExamples() {
        List<MCPExample> examples = new ArrayList<>();
        
        // Add basic examples
        examples.add(MCPExample.basicConnection());
        examples.add(MCPExample.toolListing());
        examples.add(MCPExample.toolExecution());
        examples.add(MCPExample.errorHandling());
        
        // Add mode-specific examples
        if (currentMode == DemonstrationMode.MULTI_SERVER) {
            examples.add(createMultiServerExample());
            examples.add(createConflictResolutionExample());
        }
        
        return examples;
    }
    
    // Private helper methods
    
    private void initializeServerManagers() {
        singleServerManager = new SingleMCPServerManager(configuration, objectMapper);
        multiServerManager = new MultiMCPServerManager(configuration, objectMapper);
        currentManager = singleServerManager;
    }
    
    private CompletableFuture<Void> ensureMode(DemonstrationMode requiredMode) {
        if (currentMode == requiredMode && currentManager != null && currentManager.isRunning()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return switchMode(requiredMode).thenApply(result -> null);
    }
    
    private CompletableFuture<Void> stopCurrentMode() {
        if (currentManager != null && currentManager.isRunning()) {
            logger.debug("Stopping current demonstration mode: {}", currentMode.getDisplayName());
            return currentManager.stop();
        }
        return CompletableFuture.completedFuture(null);
    }
    
    private CompletableFuture<Void> startNewMode(DemonstrationMode mode) {
        logger.debug("Starting new demonstration mode: {}", mode.getDisplayName());
        
        switch (mode) {
            case SINGLE_SERVER -> {
                currentManager = singleServerManager;
                return currentManager.start();
            }
            case MULTI_SERVER -> {
                currentManager = multiServerManager;
                return currentManager.start();
            }
            default -> {
                return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Unknown demonstration mode: " + mode)
                );
            }
        }
    }
    
    private CompletableFuture<String> performSingleServerDemo() {
        logger.info("📋 Performing single server demonstration");
        
        List<String> serverIds = currentManager.getServerIds();
        if (serverIds.isEmpty()) {
            return CompletableFuture.completedFuture("No servers configured for single server demonstration");
        }
        
        String serverId = serverIds.get(0);
        MCPClient client = currentManager.getClient(serverId);
        
        if (client == null) {
            return CompletableFuture.completedFuture("Server client not available: " + serverId);
        }
        
        // Demonstrate basic operations
        return client.listTools()
            .thenCompose(tools -> {
                logger.info("📊 Single server provides {} tools", tools.size());
                return client.listResources();
            })
            .thenApply(resources -> {
                logger.info("📊 Single server provides {} resources", resources.size());
                return String.format("Single server demonstration: 1 server connected, tools and resources discovered");
            });
    }
    
    private CompletableFuture<String> performMultiServerDemo() {
        logger.info("📋 Performing multi-server demonstration");
        
        if (!(currentManager instanceof MultiMCPServerManager multiManager)) {
            return CompletableFuture.completedFuture("Multi-server manager not available");
        }
        
        List<String> serverIds = currentManager.getServerIds();
        long readyServers = serverIds.stream()
            .mapToLong(id -> currentManager.isServerReady(id) ? 1 : 0)
            .sum();
        
        Map<String, String> allTools = multiManager.getAllAvailableTools();
        Map<String, List<String>> conflicts = multiManager.getConflictedTools();
        
        logger.info("📊 Multi-server summary: {} servers ready, {} tools available, {} conflicts",
            readyServers, allTools.size(), conflicts.size());
        
        return CompletableFuture.completedFuture(
            String.format("Multi-server demonstration: %d servers ready, %d tools available, %d conflicts resolved",
                readyServers, allTools.size(), conflicts.size())
        );
    }
    
    private MCPExample createMultiServerExample() {
        return new MCPExample(
            "Multi-Server Tool Routing",
            "Demonstrates how tools are routed across multiple MCP servers with conflict resolution.",
            """
            // Get the multi-server manager
            MultiMCPServerManager multiManager = (MultiMCPServerManager) serverManager;
            
            // Get all available tools across servers
            Map<String, String> allTools = multiManager.getAllAvailableTools();
            
            // Check for conflicts
            Map<String, List<String>> conflicts = multiManager.getConflictedTools();
            
            // Execute a tool (automatically routed to best server)
            MCPClient client = multiManager.getClientForTool("file_read");
            MCPToolResult result = client.callTool("file_read", params).get();
            """,
            MCPExampleResult.success("Multi-server routing demonstrated", 
                "Tools successfully routed across multiple servers with conflict resolution")
        );
    }
    
    private MCPExample createConflictResolutionExample() {
        return new MCPExample(
            "Tool Conflict Resolution",
            "Shows how the system handles multiple servers providing the same tool name.",
            """
            // When multiple servers provide the same tool
            List<String> providers = conflicts.get("duplicate_tool");
            
            // System automatically selects best server based on:
            // 1. Server health score
            // 2. Response time
            // 3. Success rate
            
            String selectedServer = toolToServerMapping.get("duplicate_tool");
            logger.info("Tool 'duplicate_tool' routed to server: {}", selectedServer);
            """,
            MCPExampleResult.success("Conflict resolution demonstrated", 
                "System successfully resolved tool conflicts using health-based selection")
        );
    }
}