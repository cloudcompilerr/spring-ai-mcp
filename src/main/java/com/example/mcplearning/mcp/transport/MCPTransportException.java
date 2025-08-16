package com.example.mcplearning.mcp.transport;

/**
 * Exception thrown when MCP transport operations fail.
 * 
 * This exception wraps various types of transport-level failures that can
 * occur during MCP communication, including:
 * - Connection failures
 * - Network timeouts
 * - Serialization/deserialization errors
 * - Protocol violations
 */
public class MCPTransportException extends RuntimeException {
    
    /**
     * Creates a new MCP transport exception with a message.
     * 
     * @param message The error message
     */
    public MCPTransportException(String message) {
        super(message);
    }
    
    /**
     * Creates a new MCP transport exception with a message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public MCPTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}