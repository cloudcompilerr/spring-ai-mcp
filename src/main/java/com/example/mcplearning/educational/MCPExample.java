package com.example.mcplearning.educational;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MCP Educational Example.
 * 
 * Represents an educational example that demonstrates MCP concepts
 * and functionality. Each example includes a title, description,
 * code snippet, and expected result for learning purposes.
 * 
 * @param title The title of the example
 * @param description A detailed description of what the example demonstrates
 * @param code The code snippet or configuration being demonstrated
 * @param expectedResult The expected result when running this example
 */
public record MCPExample(
    @JsonProperty("title")
    @NotBlank(message = "Example title is required")
    String title,
    
    @JsonProperty("description")
    @NotBlank(message = "Example description is required")
    String description,
    
    @JsonProperty("code")
    @NotBlank(message = "Example code is required")
    String code,
    
    @JsonProperty("expectedResult")
    @NotNull(message = "Expected result is required")
    @Valid
    MCPExampleResult expectedResult
) {
    
    /**
     * Creates a basic connection example.
     * 
     * @return An example demonstrating MCP server connection
     */
    public static MCPExample basicConnection() {
        return new MCPExample(
            "Basic MCP Server Connection",
            "Demonstrates how to establish a connection to an MCP server and perform initialization.",
            """
            // Connect to MCP server
            MCPClient client = mcpClientFactory.createClient(serverConfig);
            MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(
                MCPClientInfo.forLearningPlatform()
            );
            MCPResponse response = client.initialize(request).get();
            """,
            MCPExampleResult.success("Connection established successfully", 
                "Server initialized with protocol version 2024-11-05")
        );
    }
    
    /**
     * Creates a tool listing example.
     * 
     * @return An example demonstrating tool discovery
     */
    public static MCPExample toolListing() {
        return new MCPExample(
            "List Available Tools",
            "Shows how to discover and list all tools available from an MCP server.",
            """
            // List all available tools
            List<MCPTool> tools = client.listTools().get();
            tools.forEach(tool -> {
                System.out.println("Tool: " + tool.name());
                System.out.println("Description: " + tool.description());
            });
            """,
            MCPExampleResult.success("Tools listed successfully", 
                "Found 3 tools: file_read, file_write, directory_list")
        );
    }
    
    /**
     * Creates a tool execution example.
     * 
     * @return An example demonstrating tool execution
     */
    public static MCPExample toolExecution() {
        return new MCPExample(
            "Execute MCP Tool",
            "Demonstrates how to call an MCP tool with parameters and handle the response.",
            """
            // Execute a tool with parameters
            Map<String, Object> params = Map.of("path", "/tmp/example.txt");
            MCPToolResult result = client.callTool("file_read", params).get();
            
            if (result.isSuccess()) {
                System.out.println("File content: " + result.content());
            }
            """,
            MCPExampleResult.success("Tool executed successfully", 
                "File content retrieved: 'Hello, MCP World!'")
        );
    }
    
    /**
     * Creates an error handling example.
     * 
     * @return An example demonstrating error handling
     */
    public static MCPExample errorHandling() {
        return new MCPExample(
            "MCP Error Handling",
            "Shows how to properly handle errors in MCP operations.",
            """
            try {
                MCPToolResult result = client.callTool("nonexistent_tool", Map.of()).get();
            } catch (MCPException e) {
                if (e.getError().code() == MCPError.TOOL_NOT_FOUND) {
                    System.out.println("Tool not found: " + e.getMessage());
                }
            }
            """,
            MCPExampleResult.error("Tool not found", 
                "The requested tool 'nonexistent_tool' is not available on this server")
        );
    }
    
    /**
     * Gets the category of this example based on its title.
     * 
     * @return The example category
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCategory() {
        if (title.toLowerCase().contains("connection")) {
            return "Connection";
        } else if (title.toLowerCase().contains("tool")) {
            return "Tools";
        } else if (title.toLowerCase().contains("resource")) {
            return "Resources";
        } else if (title.toLowerCase().contains("error")) {
            return "Error Handling";
        } else {
            return "General";
        }
    }
    
    /**
     * Checks if this example demonstrates an error scenario.
     * 
     * @return true if this is an error example, false otherwise
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isErrorExample() {
        return expectedResult.isError();
    }
}