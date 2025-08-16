package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MCP Initialize Request message.
 * 
 * This record represents the initialization request sent to an MCP server
 * to establish a connection and negotiate protocol capabilities.
 * 
 * @param protocolVersion The MCP protocol version (e.g., "2024-11-05")
 * @param clientInfo Information about the MCP client
 */
public record MCPInitializeRequest(
    @JsonProperty("protocolVersion")
    @NotBlank(message = "Protocol version is required")
    String protocolVersion,
    
    @JsonProperty("clientInfo")
    @NotNull(message = "Client info is required")
    @Valid
    MCPClientInfo clientInfo
) {
    
    /**
     * Creates an initialize request with the standard MCP protocol version.
     * 
     * @param clientInfo The client information
     * @return A new initialize request
     */
    public static MCPInitializeRequest withStandardVersion(MCPClientInfo clientInfo) {
        return new MCPInitializeRequest("2024-11-05", clientInfo);
    }
}