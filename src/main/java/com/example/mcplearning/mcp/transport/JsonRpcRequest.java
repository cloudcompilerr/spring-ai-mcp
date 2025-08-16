package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JSON-RPC 2.0 request message.
 * 
 * Represents a JSON-RPC request as defined in the JSON-RPC 2.0 specification.
 * MCP uses JSON-RPC as its underlying transport protocol.
 * 
 * @param jsonrpc The JSON-RPC version (always "2.0")
 * @param id The request identifier (used to match responses)
 * @param method The method name to call
 * @param params The method parameters (can be null)
 */
public record JsonRpcRequest(
    @JsonProperty("jsonrpc")
    @NotBlank(message = "JSON-RPC version is required")
    String jsonrpc,
    
    @JsonProperty("id")
    @NotBlank(message = "Request ID is required")
    String id,
    
    @JsonProperty("method")
    @NotBlank(message = "Method name is required")
    String method,
    
    @JsonProperty("params")
    Object params
) {
    
    /**
     * Creates a JSON-RPC 2.0 request with parameters.
     * 
     * @param id The request ID
     * @param method The method name
     * @param params The method parameters
     * @return A new JSON-RPC request
     */
    public static JsonRpcRequest create(String id, String method, Object params) {
        return new JsonRpcRequest("2.0", id, method, params);
    }
    
    /**
     * Creates a JSON-RPC 2.0 request without parameters.
     * 
     * @param id The request ID
     * @param method The method name
     * @return A new JSON-RPC request
     */
    public static JsonRpcRequest create(String id, String method) {
        return new JsonRpcRequest("2.0", id, method, null);
    }
    
    /**
     * Checks if this request has parameters.
     * 
     * @return true if parameters are present
     */
    public boolean hasParams() {
        return params != null;
    }
}