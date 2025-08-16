package com.example.mcplearning.mcp.server;

/**
 * Exception thrown when MCP server management operations fail.
 * 
 * This exception is used to indicate problems with server lifecycle
 * management, configuration issues, or connection failures that are
 * specific to server management rather than client operations.
 */
public class MCPServerException extends RuntimeException {
    
    private final String serverId;
    
    /**
     * Creates a new MCP server exception.
     * 
     * @param message The error message
     */
    public MCPServerException(String message) {
        super(message);
        this.serverId = null;
    }
    
    /**
     * Creates a new MCP server exception with a cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public MCPServerException(String message, Throwable cause) {
        super(message, cause);
        this.serverId = null;
    }
    
    /**
     * Creates a new MCP server exception for a specific server.
     * 
     * @param serverId The ID of the server that caused the error
     * @param message The error message
     */
    public MCPServerException(String serverId, String message) {
        super(message);
        this.serverId = serverId;
    }
    
    /**
     * Creates a new MCP server exception for a specific server with a cause.
     * 
     * @param serverId The ID of the server that caused the error
     * @param message The error message
     * @param cause The underlying cause
     */
    public MCPServerException(String serverId, String message, Throwable cause) {
        super(message, cause);
        this.serverId = serverId;
    }
    
    /**
     * Gets the ID of the server that caused this exception.
     * 
     * @return The server ID, or null if not server-specific
     */
    public String getServerId() {
        return serverId;
    }
    
    /**
     * Checks if this exception is associated with a specific server.
     * 
     * @return true if this exception has a server ID, false otherwise
     */
    public boolean hasServerId() {
        return serverId != null;
    }
    
    @Override
    public String getMessage() {
        if (serverId != null) {
            return String.format("[Server: %s] %s", serverId, super.getMessage());
        }
        return super.getMessage();
    }
}