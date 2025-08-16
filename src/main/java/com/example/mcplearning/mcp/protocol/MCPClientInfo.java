package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

/**
 * MCP Client Information.
 * 
 * Contains metadata about the MCP client making the connection request.
 * This information helps servers understand the client's capabilities
 * and provide appropriate responses.
 * 
 * @param name The name of the client application
 * @param version The version of the client application
 */
public record MCPClientInfo(
    @JsonProperty("name")
    @NotBlank(message = "Client name is required")
    String name,
    
    @JsonProperty("version")
    @NotBlank(message = "Client version is required")
    String version
) {
    
    /**
     * Creates client info for the MCP Learning Platform.
     * 
     * @return Client info with platform details
     */
    public static MCPClientInfo forLearningPlatform() {
        return new MCPClientInfo("MCP Learning Platform", "1.0.0");
    }
}