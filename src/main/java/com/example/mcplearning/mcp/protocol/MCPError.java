package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MCP Error information.
 * 
 * Represents error details in MCP protocol responses, following
 * JSON-RPC 2.0 error object specification.
 * 
 * @param code The error code (numeric identifier)
 * @param message A human-readable error message
 * @param data Additional error data (optional)
 */
public record MCPError(
    @JsonProperty("code")
    @NotNull(message = "Error code is required")
    Integer code,
    
    @JsonProperty("message")
    @NotBlank(message = "Error message is required")
    String message,
    
    @JsonProperty("data")
    Object data
) {
    
    // Standard JSON-RPC 2.0 error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    // MCP-specific error codes
    public static final int SERVER_NOT_INITIALIZED = -32001;
    public static final int TOOL_NOT_FOUND = -32002;
    public static final int RESOURCE_NOT_FOUND = -32003;
    
    /**
     * Creates a parse error.
     * 
     * @param message The error message
     * @return A parse error
     */
    public static MCPError parseError(String message) {
        return new MCPError(PARSE_ERROR, message, null);
    }
    
    /**
     * Creates an invalid request error.
     * 
     * @param message The error message
     * @return An invalid request error
     */
    public static MCPError invalidRequest(String message) {
        return new MCPError(INVALID_REQUEST, message, null);
    }
    
    /**
     * Creates a method not found error.
     * 
     * @param methodName The method that was not found
     * @return A method not found error
     */
    public static MCPError methodNotFound(String methodName) {
        return new MCPError(METHOD_NOT_FOUND, "Method not found: " + methodName, null);
    }
    
    /**
     * Creates an invalid parameters error.
     * 
     * @param message The error message
     * @return An invalid parameters error
     */
    public static MCPError invalidParams(String message) {
        return new MCPError(INVALID_PARAMS, message, null);
    }
    
    /**
     * Creates an internal error.
     * 
     * @param message The error message
     * @return An internal error
     */
    public static MCPError internalError(String message) {
        return new MCPError(INTERNAL_ERROR, message, null);
    }
    
    /**
     * Creates a tool not found error.
     * 
     * @param toolName The tool that was not found
     * @return A tool not found error
     */
    public static MCPError toolNotFound(String toolName) {
        return new MCPError(TOOL_NOT_FOUND, "Tool not found: " + toolName, null);
    }
    
    /**
     * Creates a resource not found error.
     * 
     * @param resourceUri The resource that was not found
     * @return A resource not found error
     */
    public static MCPError resourceNotFound(String resourceUri) {
        return new MCPError(RESOURCE_NOT_FOUND, "Resource not found: " + resourceUri, null);
    }
}