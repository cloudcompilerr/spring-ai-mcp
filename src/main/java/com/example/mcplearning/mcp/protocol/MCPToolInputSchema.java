package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Input Schema.
 * 
 * Defines the JSON schema for a tool's input parameters, following
 * JSON Schema specification. This allows clients to understand
 * what parameters a tool expects and validate inputs before calling.
 * 
 * @param type The schema type (typically "object" for tool parameters)
 * @param properties The properties definition for the schema
 * @param required List of required property names
 */
public record MCPToolInputSchema(
    @JsonProperty("type")
    @NotBlank(message = "Schema type is required")
    String type,
    
    @JsonProperty("properties")
    @NotNull(message = "Schema properties are required")
    Map<String, Object> properties,
    
    @JsonProperty("required")
    List<String> required
) {
    
    /**
     * Creates an empty schema for tools with no parameters.
     * 
     * @return An empty object schema
     */
    public static MCPToolInputSchema empty() {
        return new MCPToolInputSchema("object", Map.of(), List.of());
    }
    
    /**
     * Creates a schema with string parameters.
     * 
     * @param requiredParams Map of required parameter names to descriptions
     * @param optionalParams Map of optional parameter names to descriptions
     * @return A schema with string parameters
     */
    public static MCPToolInputSchema withStringParameters(
            Map<String, String> requiredParams, 
            Map<String, String> optionalParams) {
        
        var properties = Map.<String, Object>of();
        var propertiesBuilder = new java.util.HashMap<String, Object>();
        
        // Add required parameters
        requiredParams.forEach((name, description) -> 
            propertiesBuilder.put(name, Map.of(
                "type", "string",
                "description", description
            ))
        );
        
        // Add optional parameters
        optionalParams.forEach((name, description) -> 
            propertiesBuilder.put(name, Map.of(
                "type", "string", 
                "description", description
            ))
        );
        
        return new MCPToolInputSchema(
            "object", 
            propertiesBuilder, 
            List.copyOf(requiredParams.keySet())
        );
    }
    
    /**
     * Checks if this schema has required properties.
     * 
     * @return true if there are required properties, false otherwise
     */
    public boolean hasRequiredProperties() {
        return required != null && !required.isEmpty();
    }
    
    /**
     * Gets the number of properties defined in this schema.
     * 
     * @return The number of properties
     */
    public int getPropertyCount() {
        return properties.size();
    }
}