package com.example.mcplearning.mcp.server;

/**
 * Enumeration of possible MCP server connection states.
 * 
 * These states represent the lifecycle of an MCP server connection,
 * from initial disconnection through successful operation or error states.
 * The states are designed to provide clear educational insight into
 * the MCP connection process.
 */
public enum MCPConnectionState {
    
    /**
     * Server is not connected. This is the initial state and the state
     * after a clean disconnection.
     */
    DISCONNECTED,
    
    /**
     * Attempting to establish a connection to the server.
     * This includes starting the server process and establishing
     * the transport layer connection.
     */
    CONNECTING,
    
    /**
     * Transport connection is established but MCP protocol
     * initialization has not yet completed.
     */
    CONNECTED,
    
    /**
     * MCP protocol initialization is in progress.
     * The client is negotiating capabilities with the server.
     */
    INITIALIZING,
    
    /**
     * Server is fully connected, initialized, and ready for operations.
     * This is the desired operational state.
     */
    READY,
    
    /**
     * An error has occurred that prevents normal operation.
     * The server may need to be restarted or reconfigured.
     */
    ERROR;
    
    /**
     * Checks if this state represents a transitional state.
     * 
     * @return true if the state is transitional (connecting or initializing)
     */
    public boolean isTransitional() {
        return this == CONNECTING || this == INITIALIZING;
    }
    
    /**
     * Checks if this state represents a stable state.
     * 
     * @return true if the state is stable (disconnected, ready, or error)
     */
    public boolean isStable() {
        return !isTransitional() && this != CONNECTED;
    }
    
    /**
     * Checks if this state allows MCP operations.
     * 
     * @return true if MCP operations can be performed in this state
     */
    public boolean canPerformOperations() {
        return this == READY;
    }
    
    /**
     * Checks if this state indicates a successful connection.
     * 
     * @return true if connected or ready
     */
    public boolean isConnected() {
        return this == CONNECTED || this == INITIALIZING || this == READY;
    }
    
    /**
     * Gets a human-readable description of the state.
     * 
     * @return A descriptive string for the state
     */
    public String getDescription() {
        return switch (this) {
            case DISCONNECTED -> "Disconnected";
            case CONNECTING -> "Connecting";
            case CONNECTED -> "Connected";
            case INITIALIZING -> "Initializing";
            case READY -> "Ready";
            case ERROR -> "Error";
        };
    }
}