package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import com.example.mcplearning.mcp.protocol.MCPResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Tool and resource routing service for multi-server MCP environments.
 * 
 * This service provides intelligent routing of tool calls and resource
 * access across multiple MCP servers. It demonstrates key concepts in
 * distributed system design including:
 * - Request routing and load balancing
 * - Failover and retry mechanisms
 * - Aggregation of distributed resources
 * - Conflict resolution strategies
 * 
 * Educational concepts:
 * - Service mesh patterns
 * - Distributed system routing
 * - Fault tolerance and resilience
 * - Resource aggregation patterns
 */
@Component
public class MCPToolRouter {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPToolRouter.class);
    
    private final MCPServerManager serverManager;
    private final ServerSelectionStrategy selectionStrategy;
    
    public MCPToolRouter(MCPServerManager serverManager, ServerSelectionStrategy selectionStrategy) {
        this.serverManager = serverManager;
        this.selectionStrategy = selectionStrategy;
        
        logger.info("MCPToolRouter initialized with strategy: {}", selectionStrategy.getStrategyName());
    }
    
    /**
     * Routes a tool call to the appropriate server.
     * 
     * @param toolName The name of the tool to execute
     * @param arguments The arguments for the tool
     * @return A future containing the tool execution result
     */
    public CompletableFuture<MCPToolResult> callTool(String toolName, Map<String, Object> arguments) {
        logger.debug("Routing tool call: {} with {} arguments", toolName, arguments.size());
        
        // Find servers that provide this tool
        List<String> candidateServers = findServersWithTool(toolName);
        
        if (candidateServers.isEmpty()) {
            logger.warn("No servers found that provide tool: {}", toolName);
            return CompletableFuture.failedFuture(
                new MCPServerException("Tool not found: " + toolName));
        }
        
        // Select the best server using the configured strategy
        String selectedServer = selectionStrategy.selectServerForTool(toolName, candidateServers, serverManager);
        
        if (selectedServer == null) {
            logger.warn("No suitable server selected for tool: {}", toolName);
            return CompletableFuture.failedFuture(
                new MCPServerException("No available server for tool: " + toolName));
        }
        
        logger.info("Routing tool '{}' to server: {}", toolName, selectedServer);
        
        // Execute the tool with failover
        return executeToolWithFailover(toolName, arguments, selectedServer, candidateServers);
    }
    
    /**
     * Reads a resource from the appropriate server.
     * 
     * @param resourceUri The URI of the resource to read
     * @return A future containing the resource content
     */
    public CompletableFuture<String> readResource(String resourceUri) {
        logger.debug("Routing resource read: {}", resourceUri);
        
        // Find servers that provide this resource
        List<String> candidateServers = findServersWithResource(resourceUri);
        
        if (candidateServers.isEmpty()) {
            logger.warn("No servers found that provide resource: {}", resourceUri);
            return CompletableFuture.failedFuture(
                new MCPServerException("Resource not found: " + resourceUri));
        }
        
        // Select the best server using the configured strategy
        String selectedServer = selectionStrategy.selectServerForResource(resourceUri, candidateServers, serverManager);
        
        if (selectedServer == null) {
            logger.warn("No suitable server selected for resource: {}", resourceUri);
            return CompletableFuture.failedFuture(
                new MCPServerException("No available server for resource: " + resourceUri));
        }
        
        logger.info("Routing resource '{}' to server: {}", resourceUri, selectedServer);
        
        // Read the resource with failover
        return readResourceWithFailover(resourceUri, selectedServer, candidateServers);
    }
    
    /**
     * Lists all available tools across all servers.
     * 
     * @return A future containing the aggregated list of tools
     */
    public CompletableFuture<List<MCPTool>> listAllTools() {
        logger.debug("Listing all tools across {} servers", serverManager.getServerIds().size());
        
        List<CompletableFuture<List<MCPTool>>> toolListFutures = serverManager.getServerIds().stream()
            .filter(serverManager::isServerReady)
            .map(serverId -> {
                MCPClient client = serverManager.getClient(serverId);
                return client != null ? client.listTools() : CompletableFuture.<List<MCPTool>>completedFuture(List.of());
            })
            .toList();
        
        return CompletableFuture.allOf(toolListFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                // Aggregate and deduplicate tools
                Map<String, MCPTool> uniqueTools = toolListFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(
                        MCPTool::name,
                        tool -> tool,
                        (existing, replacement) -> existing // Keep first occurrence
                    ));
                
                List<MCPTool> result = List.copyOf(uniqueTools.values());
                logger.info("Aggregated {} unique tools from {} servers", result.size(), toolListFutures.size());
                return result;
            });
    }
    
    /**
     * Lists all available resources across all servers.
     * 
     * @return A future containing the aggregated list of resources
     */
    public CompletableFuture<List<MCPResource>> listAllResources() {
        logger.debug("Listing all resources across {} servers", serverManager.getServerIds().size());
        
        List<CompletableFuture<List<MCPResource>>> resourceListFutures = serverManager.getServerIds().stream()
            .filter(serverManager::isServerReady)
            .map(serverId -> {
                MCPClient client = serverManager.getClient(serverId);
                return client != null ? client.listResources() : CompletableFuture.<List<MCPResource>>completedFuture(List.of());
            })
            .toList();
        
        return CompletableFuture.allOf(resourceListFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> {
                // Aggregate and deduplicate resources
                Map<String, MCPResource> uniqueResources = resourceListFutures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toMap(
                        MCPResource::uri,
                        resource -> resource,
                        (existing, replacement) -> existing // Keep first occurrence
                    ));
                
                List<MCPResource> result = List.copyOf(uniqueResources.values());
                logger.info("Aggregated {} unique resources from {} servers", result.size(), resourceListFutures.size());
                return result;
            });
    }
    
    /**
     * Finds all servers that provide a specific tool.
     */
    private List<String> findServersWithTool(String toolName) {
        return serverManager.getServerIds().stream()
            .filter(serverManager::isServerReady)
            .filter(serverId -> serverHasTool(serverId, toolName))
            .toList();
    }
    
    /**
     * Finds all servers that provide a specific resource.
     */
    private List<String> findServersWithResource(String resourceUri) {
        return serverManager.getServerIds().stream()
            .filter(serverManager::isServerReady)
            .filter(serverId -> serverHasResource(serverId, resourceUri))
            .toList();
    }
    
    /**
     * Checks if a server provides a specific tool.
     */
    private boolean serverHasTool(String serverId, String toolName) {
        MCPClient client = serverManager.getClient(serverId);
        if (client == null) {
            return false;
        }
        
        try {
            List<MCPTool> tools = client.listTools().get();
            return tools.stream().anyMatch(tool -> tool.name().equals(toolName));
        } catch (Exception e) {
            logger.debug("Failed to check tools for server {}: {}", serverId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a server provides a specific resource.
     */
    private boolean serverHasResource(String serverId, String resourceUri) {
        MCPClient client = serverManager.getClient(serverId);
        if (client == null) {
            return false;
        }
        
        try {
            List<MCPResource> resources = client.listResources().get();
            return resources.stream().anyMatch(resource -> resource.uri().equals(resourceUri));
        } catch (Exception e) {
            logger.debug("Failed to check resources for server {}: {}", serverId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Executes a tool with automatic failover to alternative servers.
     */
    private CompletableFuture<MCPToolResult> executeToolWithFailover(
            String toolName, Map<String, Object> arguments, String primaryServer, List<String> alternativeServers) {
        
        MCPClient client = serverManager.getClient(primaryServer);
        if (client == null) {
            return tryAlternativeServersForTool(toolName, arguments, alternativeServers, primaryServer);
        }
        
        return client.callTool(toolName, arguments)
            .exceptionally(throwable -> {
                logger.warn("Tool execution failed on primary server {}: {}", primaryServer, throwable.getMessage());
                
                // Try alternative servers
                return tryAlternativeServersForTool(toolName, arguments, alternativeServers, primaryServer)
                    .exceptionally(altThrowable -> {
                        logger.error("Tool execution failed on all servers for tool: {}", toolName);
                        throw new RuntimeException("Tool execution failed on all available servers", altThrowable);
                    })
                    .join();
            });
    }
    
    /**
     * Tries alternative servers for tool execution.
     */
    private CompletableFuture<MCPToolResult> tryAlternativeServersForTool(
            String toolName, Map<String, Object> arguments, List<String> servers, String excludeServer) {
        
        List<String> alternatives = servers.stream()
            .filter(serverId -> !serverId.equals(excludeServer))
            .filter(serverManager::isServerReady)
            .toList();
        
        if (alternatives.isEmpty()) {
            return CompletableFuture.failedFuture(
                new MCPServerException("No alternative servers available for tool: " + toolName));
        }
        
        String nextServer = alternatives.get(0);
        logger.info("Trying alternative server {} for tool: {}", nextServer, toolName);
        
        MCPClient client = serverManager.getClient(nextServer);
        if (client == null) {
            return tryAlternativeServersForTool(toolName, arguments, alternatives, nextServer);
        }
        
        return client.callTool(toolName, arguments)
            .exceptionally(throwable -> {
                logger.warn("Tool execution failed on alternative server {}: {}", nextServer, throwable.getMessage());
                return tryAlternativeServersForTool(toolName, arguments, alternatives, nextServer).join();
            });
    }
    
    /**
     * Reads a resource with automatic failover to alternative servers.
     */
    private CompletableFuture<String> readResourceWithFailover(
            String resourceUri, String primaryServer, List<String> alternativeServers) {
        
        MCPClient client = serverManager.getClient(primaryServer);
        if (client == null) {
            return tryAlternativeServersForResource(resourceUri, alternativeServers, primaryServer);
        }
        
        return client.readResource(resourceUri)
            .exceptionally(throwable -> {
                logger.warn("Resource read failed on primary server {}: {}", primaryServer, throwable.getMessage());
                
                // Try alternative servers
                return tryAlternativeServersForResource(resourceUri, alternativeServers, primaryServer)
                    .exceptionally(altThrowable -> {
                        logger.error("Resource read failed on all servers for resource: {}", resourceUri);
                        throw new RuntimeException("Resource read failed on all available servers", altThrowable);
                    })
                    .join();
            });
    }
    
    /**
     * Tries alternative servers for resource reading.
     */
    private CompletableFuture<String> tryAlternativeServersForResource(
            String resourceUri, List<String> servers, String excludeServer) {
        
        List<String> alternatives = servers.stream()
            .filter(serverId -> !serverId.equals(excludeServer))
            .filter(serverManager::isServerReady)
            .toList();
        
        if (alternatives.isEmpty()) {
            return CompletableFuture.failedFuture(
                new MCPServerException("No alternative servers available for resource: " + resourceUri));
        }
        
        String nextServer = alternatives.get(0);
        logger.info("Trying alternative server {} for resource: {}", nextServer, resourceUri);
        
        MCPClient client = serverManager.getClient(nextServer);
        if (client == null) {
            return tryAlternativeServersForResource(resourceUri, alternatives, nextServer);
        }
        
        return client.readResource(resourceUri)
            .exceptionally(throwable -> {
                logger.warn("Resource read failed on alternative server {}: {}", nextServer, throwable.getMessage());
                return tryAlternativeServersForResource(resourceUri, alternatives, nextServer).join();
            });
    }
}