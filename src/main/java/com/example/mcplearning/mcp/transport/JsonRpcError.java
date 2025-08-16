package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JSON-RPC 2.0 error object.
 * 
 * Represents an error in a JSON-RPC response as defined in the
 * JSON-RPC 2.0 specification.
 * 
 * @param code The error code (integer)
 * @param message A short description of the error
 * @param data Additional error data (optional)
 */
public record JsonRpcError(
    @JsonProperty("code")
    @NotNull(message = "Error code is required")
    Integer code,
    
    @JsonProperty("message")
    @NotBlank(message = "Error message is required")
    String message,
    
    @JsonProperty("data")
    Object data
) {
    
    // Standard JSON-RPC error codes
    public static final int PARSE_ERROR = -32700;
    public static final int INVALID_REQUEST = -32600;
    public static final int METHOD_NOT_FOUND = -32601;
    public static final int INVALID_PARAMS = -32602;
    public static final int INTERNAL_ERROR = -32603;
    
    /**
     * Creates a JSON-RPC error with code and message.
     * 
     * @param code The error code
     * @param message The error message
     * @return A new JSON-RPC error
     */
    public static JsonRpcError create(int code, String message) {
        return new JsonRpcError(code, message, null);
    }
    
    /**
     * Creates a JSON-RPC error with code, message, and additional data.
     * 
     * @param code The error code
     * @param message The error message
     * @param data Additional error data
     * @return A new JSON-RPC error
     */
    public static JsonRpcError create(int code, String message, Object data) {
        return new JsonRpcError(code, message, data);
    }
    
    /**
     * Creates a parse error.
     * 
     * @param message The error message
     * @return A parse error
     */
    public static JsonRpcError parseError(String message) {
        return create(PARSE_ERROR, message);
    }
    
    /**
     * Creates an invalid request error.
     * 
     * @param message The error message
     * @return An invalid request error
     */
    public static JsonRpcError invalidRequest(String message) {
        return create(INVALID_REQUEST, message);
    }
    
    /**
     * Creates a method not found error.
     * 
     * @param method The method name that was not found
     * @return A method not found error
     */
    public static JsonRpcError methodNotFound(String method) {
        return create(METHOD_NOT_FOUND, "Method not found: " + method);
    }
    
    /**
     * Creates an invalid parameters error.
     * 
     * @param message The error message
     * @return An invalid parameters error
     */
    public static JsonRpcError invalidParams(String message) {
        return create(INVALID_PARAMS, message);
    }
    
    /**
     * Creates an internal error.
     * 
     * @param message The error message
     * @return An internal error
     */
    public static JsonRpcError internalError(String message) {
        return create(INTERNAL_ERROR, message);
    }
    
    /**
     * Checks if this error has additional data.
     * 
     * @return true if error data is present
     */
    public boolean hasData() {
        return data != null;
    }
    
    /**
     * Checks if this is a standard JSON-RPC error code.
     * 
     * @return true if the error code is in the standard range
     */
    public boolean isStandardError() {
        return code >= -32768 && code <= -32000;
    }
}