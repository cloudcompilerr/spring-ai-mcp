package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * MCP Response message.
 * 
 * Represents a response from an MCP server to a client request.
 * Following JSON-RPC 2.0 specification, a response contains either
 * a result or an error, but never both.
 * 
 * @param id The request identifier that this response corresponds to
 * @param result The successful result (null if error occurred)
 * @param error The error information (null if successful)
 */
public record MCPResponse(
    @JsonProperty("id")
    @NotBlank(message = "Response ID is required")
    String id,
    
    @JsonProperty("result")
    Object result,
    
    @JsonProperty("error")
    @Valid
    MCPError error
) {
    
    /**
     * Creates a successful response with a result.
     * 
     * @param id The request ID
     * @param result The result object
     * @return A successful MCP response
     */
    public static MCPResponse success(String id, Object result) {
        return new MCPResponse(id, result, null);
    }
    
    /**
     * Creates an error response.
     * 
     * @param id The request ID
     * @param error The error information
     * @return An error MCP response
     */
    public static MCPResponse error(String id, MCPError error) {
        return new MCPResponse(id, null, error);
    }
    
    /**
     * Checks if this response represents a successful operation.
     * 
     * @return true if the response contains a result, false if it contains an error
     */
    public boolean isSuccess() {
        return error == null;
    }
    
    /**
     * Checks if this response represents an error.
     * 
     * @return true if the response contains an error, false if it contains a result
     */
    public boolean isError() {
        return error != null;
    }
}