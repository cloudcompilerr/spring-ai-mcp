package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * JSON-RPC 2.0 response message.
 * 
 * Represents a JSON-RPC response as defined in the JSON-RPC 2.0 specification.
 * A response contains either a result or an error, but never both.
 * 
 * @param jsonrpc The JSON-RPC version (always "2.0")
 * @param id The request identifier that this response corresponds to
 * @param result The successful result (null if error occurred)
 * @param error The error information (null if successful)
 */
public record JsonRpcResponse(
    @JsonProperty("jsonrpc")
    @NotBlank(message = "JSON-RPC version is required")
    String jsonrpc,
    
    @JsonProperty("id")
    @NotBlank(message = "Response ID is required")
    String id,
    
    @JsonProperty("result")
    Object result,
    
    @JsonProperty("error")
    JsonRpcError error
) {
    
    /**
     * Creates a successful JSON-RPC 2.0 response.
     * 
     * @param id The request ID
     * @param result The result object
     * @return A successful JSON-RPC response
     */
    public static JsonRpcResponse success(String id, Object result) {
        return new JsonRpcResponse("2.0", id, result, null);
    }
    
    /**
     * Creates an error JSON-RPC 2.0 response.
     * 
     * @param id The request ID
     * @param error The error information
     * @return An error JSON-RPC response
     */
    public static JsonRpcResponse error(String id, JsonRpcError error) {
        return new JsonRpcResponse("2.0", id, null, error);
    }
    
    /**
     * Checks if this response represents a successful operation.
     * 
     * @return true if the response contains a result
     */
    public boolean isSuccess() {
        return error == null;
    }
    
    /**
     * Checks if this response represents an error.
     * 
     * @return true if the response contains an error
     */
    public boolean isError() {
        return error != null;
    }
}