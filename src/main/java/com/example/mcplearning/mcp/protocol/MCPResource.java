package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * MCP Resource definition.
 * 
 * Represents a resource available through an MCP server. Resources are
 * data sources that can be read by clients, such as files, database
 * records, or API endpoints.
 * 
 * @param uri The unique URI identifying this resource
 * @param name A human-readable name for the resource
 * @param description A description of what the resource contains
 * @param mimeType The MIME type of the resource content (optional)
 */
public record MCPResource(
    @JsonProperty("uri")
    @NotBlank(message = "Resource URI is required")
    String uri,
    
    @JsonProperty("name")
    @NotBlank(message = "Resource name is required")
    String name,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("mimeType")
    String mimeType
) {
    
    /**
     * Creates a text resource.
     * 
     * @param uri The resource URI
     * @param name The resource name
     * @param description The resource description
     * @return A text resource
     */
    public static MCPResource textResource(String uri, String name, String description) {
        return new MCPResource(uri, name, description, "text/plain");
    }
    
    /**
     * Creates a JSON resource.
     * 
     * @param uri The resource URI
     * @param name The resource name
     * @param description The resource description
     * @return A JSON resource
     */
    public static MCPResource jsonResource(String uri, String name, String description) {
        return new MCPResource(uri, name, description, "application/json");
    }
    
    /**
     * Creates a resource without a specific MIME type.
     * 
     * @param uri The resource URI
     * @param name The resource name
     * @param description The resource description
     * @return A resource without MIME type
     */
    public static MCPResource genericResource(String uri, String name, String description) {
        return new MCPResource(uri, name, description, null);
    }
    
    /**
     * Checks if this resource has a MIME type specified.
     * 
     * @return true if MIME type is specified, false otherwise
     */
    public boolean hasMimeType() {
        return mimeType != null && !mimeType.isBlank();
    }
    
    /**
     * Checks if this is a text-based resource.
     * 
     * @return true if the MIME type indicates text content
     */
    public boolean isTextResource() {
        return hasMimeType() && mimeType.startsWith("text/");
    }
    
    /**
     * Checks if this is a JSON resource.
     * 
     * @return true if the MIME type indicates JSON content
     */
    public boolean isJsonResource() {
        return hasMimeType() && 
               (mimeType.equals("application/json") || mimeType.endsWith("+json"));
    }
}