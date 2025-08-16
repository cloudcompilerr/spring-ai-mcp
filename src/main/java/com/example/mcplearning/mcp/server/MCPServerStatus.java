package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.config.MCPServerConfig;

import java.time.Instant;
import java.time.Duration;

/**
 * Represents the current status and health information of an MCP server.
 * 
 * This record contains comprehensive information about a server's state,
 * including connection status, health metrics, and error information.
 * It's designed to provide educational insights into server management.
 */
public record MCPServerStatus(
    String serverId,
    String serverName,
    MCPConnectionState state,
    Instant lastHealthCheck,
    String errorMessage,
    Duration responseTime,
    int connectionAttempts,
    Instant lastConnectionAttempt,
    MCPServerConfig config
) {
    
    /**
     * Creates a status for a disconnected server.
     * 
     * @param config The server configuration
     * @return A disconnected server status
     */
    public static MCPServerStatus disconnected(MCPServerConfig config) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.DISCONNECTED,
            null,
            null,
            null,
            0,
            null,
            config
        );
    }
    
    /**
     * Creates a status for a connecting server.
     * 
     * @param config The server configuration
     * @param attempts Number of connection attempts
     * @return A connecting server status
     */
    public static MCPServerStatus connecting(MCPServerConfig config, int attempts) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.CONNECTING,
            null,
            null,
            null,
            attempts,
            Instant.now(),
            config
        );
    }
    
    /**
     * Creates a status for a connected server.
     * 
     * @param config The server configuration
     * @param responseTime The connection response time
     * @return A connected server status
     */
    public static MCPServerStatus connected(MCPServerConfig config, Duration responseTime) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.CONNECTED,
            Instant.now(),
            null,
            responseTime,
            0,
            null,
            config
        );
    }
    
    /**
     * Creates a status for an initializing server.
     * 
     * @param config The server configuration
     * @return An initializing server status
     */
    public static MCPServerStatus initializing(MCPServerConfig config) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.INITIALIZING,
            null,
            null,
            null,
            0,
            null,
            config
        );
    }
    
    /**
     * Creates a status for a ready server.
     * 
     * @param config The server configuration
     * @param responseTime The last response time
     * @return A ready server status
     */
    public static MCPServerStatus ready(MCPServerConfig config, Duration responseTime) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.READY,
            Instant.now(),
            null,
            responseTime,
            0,
            null,
            config
        );
    }
    
    /**
     * Creates a status for an error state server.
     * 
     * @param config The server configuration
     * @param errorMessage The error message
     * @param attempts Number of failed attempts
     * @return An error server status
     */
    public static MCPServerStatus error(MCPServerConfig config, String errorMessage, int attempts) {
        return new MCPServerStatus(
            config.id(),
            config.name(),
            MCPConnectionState.ERROR,
            Instant.now(),
            errorMessage,
            null,
            attempts,
            Instant.now(),
            config
        );
    }
    
    /**
     * Creates a new status with updated health check information.
     * 
     * @param responseTime The response time from the health check
     * @return A new status with updated health information
     */
    public MCPServerStatus withHealthCheck(Duration responseTime) {
        return new MCPServerStatus(
            serverId,
            serverName,
            state,
            Instant.now(),
            errorMessage,
            responseTime,
            connectionAttempts,
            lastConnectionAttempt,
            config
        );
    }
    
    /**
     * Creates a new status with an error.
     * 
     * @param errorMessage The error message
     * @return A new status with error information
     */
    public MCPServerStatus withError(String errorMessage) {
        return new MCPServerStatus(
            serverId,
            serverName,
            MCPConnectionState.ERROR,
            Instant.now(),
            errorMessage,
            responseTime,
            connectionAttempts + 1,
            Instant.now(),
            config
        );
    }
    
    /**
     * Creates a new status with updated connection state.
     * 
     * @param newState The new connection state
     * @return A new status with updated state
     */
    public MCPServerStatus withState(MCPConnectionState newState) {
        return new MCPServerStatus(
            serverId,
            serverName,
            newState,
            lastHealthCheck,
            errorMessage,
            responseTime,
            connectionAttempts,
            lastConnectionAttempt,
            config
        );
    }
    
    /**
     * Checks if the server is in a healthy state.
     * 
     * @return true if the server is ready or connected, false otherwise
     */
    public boolean isHealthy() {
        return state == MCPConnectionState.READY || state == MCPConnectionState.CONNECTED;
    }
    
    /**
     * Checks if the server is in an error state.
     * 
     * @return true if the server is in error state, false otherwise
     */
    public boolean isError() {
        return state == MCPConnectionState.ERROR;
    }
    
    /**
     * Gets a human-readable description of the server status.
     * 
     * @return A descriptive status string
     */
    public String getDescription() {
        return switch (state) {
            case DISCONNECTED -> "Server is disconnected";
            case CONNECTING -> "Connecting to server (attempt " + connectionAttempts + ")";
            case CONNECTED -> "Connected to server";
            case INITIALIZING -> "Initializing MCP protocol";
            case READY -> "Server is ready and operational";
            case ERROR -> "Server error: " + (errorMessage != null ? errorMessage : "Unknown error");
        };
    }
}