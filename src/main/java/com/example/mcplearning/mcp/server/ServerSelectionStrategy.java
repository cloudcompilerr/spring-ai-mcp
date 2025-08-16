package com.example.mcplearning.mcp.server;

import java.util.List;

/**
 * Strategy interface for selecting servers in multi-server scenarios.
 * 
 * This interface demonstrates the Strategy pattern for server selection,
 * allowing different algorithms for choosing which server should handle
 * a particular operation. This is a key educational concept in distributed
 * system design.
 * 
 * Different strategies can optimize for:
 * - Load balancing (round-robin, least connections)
 * - Performance (fastest response time, highest success rate)
 * - Availability (failover, redundancy)
 * - Specialization (server capabilities, tool affinity)
 */
public interface ServerSelectionStrategy {
    
    /**
     * Selects the best server from a list of candidates for a general operation.
     * 
     * @param availableServers List of server IDs that are available
     * @param serverManager The server manager for accessing server information
     * @return The selected server ID, or null if no suitable server found
     */
    String selectServer(List<String> availableServers, MCPServerManager serverManager);
    
    /**
     * Selects the best server for executing a specific tool.
     * 
     * @param toolName The name of the tool to execute
     * @param availableServers List of server IDs that provide this tool
     * @param serverManager The server manager for accessing server information
     * @return The selected server ID, or null if no suitable server found
     */
    default String selectServerForTool(String toolName, List<String> availableServers, MCPServerManager serverManager) {
        return selectServer(availableServers, serverManager);
    }
    
    /**
     * Selects the best server for accessing a specific resource.
     * 
     * @param resourceUri The URI of the resource to access
     * @param availableServers List of server IDs that provide this resource
     * @param serverManager The server manager for accessing server information
     * @return The selected server ID, or null if no suitable server found
     */
    default String selectServerForResource(String resourceUri, List<String> availableServers, MCPServerManager serverManager) {
        return selectServer(availableServers, serverManager);
    }
    
    /**
     * Gets a human-readable name for this strategy.
     * 
     * @return The strategy name
     */
    String getStrategyName();
    
    /**
     * Gets a description of how this strategy works.
     * 
     * @return A description of the selection algorithm
     */
    String getDescription();
}