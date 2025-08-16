package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * MCP Tool Execution Result.
 * 
 * Represents the result of executing an MCP tool, containing
 * the output content and metadata about the execution.
 * 
 * @param content The content returned by the tool execution
 * @param isError Whether the execution resulted in an error
 * @param mimeType The MIME type of the content (optional)
 */
public record MCPToolResult(
    @JsonProperty("content")
    @NotBlank(message = "Tool result content is required")
    String content,
    
    @JsonProperty("isError")
    boolean isError,
    
    @JsonProperty("mimeType")
    String mimeType
) {
    
    /**
     * Creates a successful tool result.
     * 
     * @param content The result content
     * @return A successful tool result
     */
    public static MCPToolResult success(String content) {
        return new MCPToolResult(content, false, "text/plain");
    }
    
    /**
     * Creates a successful tool result with specific MIME type.
     * 
     * @param content The result content
     * @param mimeType The MIME type of the content
     * @return A successful tool result with MIME type
     */
    public static MCPToolResult success(String content, String mimeType) {
        return new MCPToolResult(content, false, mimeType);
    }
    
    /**
     * Creates an error tool result.
     * 
     * @param errorMessage The error message
     * @return An error tool result
     */
    public static MCPToolResult error(String errorMessage) {
        return new MCPToolResult(errorMessage, true, "text/plain");
    }
    
    /**
     * Creates a JSON tool result.
     * 
     * @param jsonContent The JSON content
     * @return A JSON tool result
     */
    public static MCPToolResult json(String jsonContent) {
        return success(jsonContent, "application/json");
    }
    
    /**
     * Checks if this result represents a successful execution.
     * 
     * @return true if successful, false if error
     */
    public boolean isSuccess() {
        return !isError;
    }
    
    /**
     * Checks if this result has a MIME type specified.
     * 
     * @return true if MIME type is specified, false otherwise
     */
    public boolean hasMimeType() {
        return mimeType != null && !mimeType.isBlank();
    }
    
    /**
     * Checks if this is a text-based result.
     * 
     * @return true if the MIME type indicates text content
     */
    public boolean isTextResult() {
        return hasMimeType() && mimeType.startsWith("text/");
    }
    
    /**
     * Checks if this is a JSON result.
     * 
     * @return true if the MIME type indicates JSON content
     */
    public boolean isJsonResult() {
        return hasMimeType() && 
               (mimeType.equals("application/json") || mimeType.endsWith("+json"));
    }
}