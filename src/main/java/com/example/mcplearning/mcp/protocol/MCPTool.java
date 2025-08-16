package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MCP Tool definition.
 * 
 * Represents a tool available through an MCP server. Tools are functions
 * that can be called by clients to perform specific operations.
 * 
 * @param name The unique name of the tool
 * @param description A human-readable description of what the tool does
 * @param inputSchema The JSON schema defining the tool's input parameters
 */
public record MCPTool(
    @JsonProperty("name")
    @NotBlank(message = "Tool name is required")
    String name,
    
    @JsonProperty("description")
    @NotBlank(message = "Tool description is required")
    String description,
    
    @JsonProperty("inputSchema")
    @NotNull(message = "Tool input schema is required")
    @Valid
    MCPToolInputSchema inputSchema
) {
    
    /**
     * Creates a simple tool with no parameters.
     * 
     * @param name The tool name
     * @param description The tool description
     * @return A tool with empty input schema
     */
    public static MCPTool withNoParameters(String name, String description) {
        return new MCPTool(name, description, MCPToolInputSchema.empty());
    }
    
    /**
     * Checks if this tool requires parameters.
     * 
     * @return true if the tool has required parameters, false otherwise
     */
    public boolean hasRequiredParameters() {
        return inputSchema.hasRequiredProperties();
    }
}