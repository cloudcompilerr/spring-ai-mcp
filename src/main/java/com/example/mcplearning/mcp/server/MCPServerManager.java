package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.config.MCPServerConfig;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for managing MCP server connections and lifecycle.
 * 
 * The MCPServerManager is responsible for:
 * - Managing the lifecycle of MCP server connections (connect, initialize, disconnect)
 * - Providing access to MCP clients for server communication
 * - Monitoring server health and status
 * - Handling connection failures and recovery
 * 
 * This interface supports both single and multi-server scenarios, with
 * implementations handling the complexity of server coordination.
 */
public interface MCPServerManager {
    
    /**
     * Adds a new MCP server configuration and attempts to connect to it.
     * 
     * @param config The server configuration to add
     * @return A future that completes when the server is connected and initialized
     * @throws MCPServerException if the server configuration is invalid
     */
    CompletableFuture<Void> addServer(MCPServerConfig config);
    
    /**
     * Removes a server configuration and disconnects from it.
     * 
     * @param serverId The ID of the server to remove
     * @return A future that completes when the server is disconnected
     */
    CompletableFuture<Void> removeServer(String serverId);
    
    /**
     * Gets the MCP client for a specific server.
     * 
     * @param serverId The ID of the server
     * @return The MCP client for the server, or null if not connected
     */
    MCPClient getClient(String serverId);
    
    /**
     * Gets the status of all managed servers.
     * 
     * @return List of server status information
     */
    List<MCPServerStatus> getServerStatuses();
    
    /**
     * Gets the status of a specific server.
     * 
     * @param serverId The ID of the server
     * @return The server status, or null if server not found
     */
    MCPServerStatus getServerStatus(String serverId);
    
    /**
     * Performs a health check on all managed servers.
     * 
     * @return A future that completes when all health checks are done
     */
    CompletableFuture<Void> healthCheck();
    
    /**
     * Performs a health check on a specific server.
     * 
     * @param serverId The ID of the server to check
     * @return A future that completes when the health check is done
     */
    CompletableFuture<Void> healthCheck(String serverId);
    
    /**
     * Gets the list of all managed server IDs.
     * 
     * @return List of server IDs
     */
    List<String> getServerIds();
    
    /**
     * Checks if a server is currently connected and ready.
     * 
     * @param serverId The ID of the server to check
     * @return true if the server is connected and initialized, false otherwise
     */
    boolean isServerReady(String serverId);
    
    /**
     * Starts the server manager and connects to all configured servers.
     * 
     * @return A future that completes when all initial connections are attempted
     */
    CompletableFuture<Void> start();
    
    /**
     * Stops the server manager and disconnects from all servers.
     * 
     * @return A future that completes when all servers are disconnected
     */
    CompletableFuture<Void> stop();
    
    /**
     * Checks if the server manager is currently running.
     * 
     * @return true if running, false otherwise
     */
    boolean isRunning();
}