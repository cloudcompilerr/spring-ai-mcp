package com.example.mcplearning.mcp.javaserver;

import com.example.mcplearning.mcp.protocol.MCPToolResult;

import java.util.Map;

/**
 * Interface for MCP Server Tools
 * 
 * EDUCATIONAL OVERVIEW:
 * This interface defines how tools are implemented in a Java MCP server.
 * Tools are the primary way MCP servers expose functionality to clients.
 * 
 * KEY CONCEPTS:
 * - Tool Metadata: Name, description, and input schema
 * - Tool Execution: Processing client requests and returning results
 * - Input Validation: Ensuring parameters match the expected schema
 * - Error Handling: Graceful handling of execution failures
 * 
 * IMPLEMENTATION PATTERN:
 * Each tool should validate its inputs, perform the requested operation,
 * and return a properly formatted result or error.
 */
public interface MCPServerTool {
    
    /**
     * Gets the name of the tool.
     * This is used by clients to identify and call the tool.
     * 
     * @return The tool name (must be unique within the server)
     */
    String getName();
    
    /**
     * Gets the description of the tool.
     * This helps clients understand what the tool does.
     * 
     * @return A human-readable description of the tool's functionality
     */
    String getDescription();
    
    /**
     * Gets the input schema for the tool.
     * This defines what parameters the tool accepts and their types.
     * 
     * @return A JSON Schema object describing the tool's input parameters
     */
    Map<String, Object> getInputSchema();
    
    /**
     * Executes the tool with the provided arguments.
     * 
     * IMPLEMENTATION GUIDELINES:
     * - Validate input arguments against the schema
     * - Perform the tool's functionality
     * - Return success results or error information
     * - Handle exceptions gracefully
     * - Log important operations for debugging
     * 
     * @param arguments The input arguments provided by the client
     * @return The result of the tool execution
     * @throws Exception If the tool execution fails
     */
    MCPToolResult execute(Map<String, Object> arguments) throws Exception;
    
    /**
     * Validates the input arguments against the tool's schema.
     * Default implementation performs basic null checks.
     * 
     * @param arguments The arguments to validate
     * @throws IllegalArgumentException If validation fails
     */
    default void validateArguments(Map<String, Object> arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        // Subclasses can override this for more specific validation
    }
}