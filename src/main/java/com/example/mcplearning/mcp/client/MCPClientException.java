package com.example.mcplearning.mcp.client;

/**
 * Exception thrown when MCP client operations fail.
 * 
 * This exception wraps various types of failures that can occur during
 * MCP client operations, including:
 * - Connection failures
 * - Protocol errors
 * - Server-side errors
 * - Serialization/deserialization errors
 */
public class MCPClientException extends RuntimeException {
    
    private final String errorCode;
    private final Object errorData;
    
    /**
     * Creates a new MCP client exception with a message.
     * 
     * @param message The error message
     */
    public MCPClientException(String message) {
        super(message);
        this.errorCode = null;
        this.errorData = null;
    }
    
    /**
     * Creates a new MCP client exception with a message and cause.
     * 
     * @param message The error message
     * @param cause The underlying cause
     */
    public MCPClientException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorData = null;
    }
    
    /**
     * Creates a new MCP client exception with detailed error information.
     * 
     * @param message The error message
     * @param errorCode The MCP error code
     * @param errorData Additional error data
     */
    public MCPClientException(String message, String errorCode, Object errorData) {
        super(message);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }
    
    /**
     * Creates a new MCP client exception with detailed error information and cause.
     * 
     * @param message The error message
     * @param errorCode The MCP error code
     * @param errorData Additional error data
     * @param cause The underlying cause
     */
    public MCPClientException(String message, String errorCode, Object errorData, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorData = errorData;
    }
    
    /**
     * Gets the MCP error code if available.
     * 
     * @return The error code, or null if not available
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Gets additional error data if available.
     * 
     * @return The error data, or null if not available
     */
    public Object getErrorData() {
        return errorData;
    }
    
    /**
     * Checks if this exception has an MCP error code.
     * 
     * @return true if an error code is present
     */
    public boolean hasErrorCode() {
        return errorCode != null;
    }
    
    /**
     * Checks if this exception has additional error data.
     * 
     * @return true if error data is present
     */
    public boolean hasErrorData() {
        return errorData != null;
    }
}