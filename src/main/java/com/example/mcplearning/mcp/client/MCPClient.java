package com.example.mcplearning.mcp.client;

import com.example.mcplearning.mcp.protocol.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Core MCP client interface defining the essential operations for communicating
 * with Model Context Protocol servers.
 * 
 * This interface provides the fundamental MCP operations:
 * - Server initialization and capability negotiation
 * - Tool discovery and execution
 * - Resource listing and access
 * - Connection lifecycle management
 * 
 * All operations are asynchronous to support non-blocking I/O and better
 * performance when dealing with multiple servers or long-running operations.
 */
public interface MCPClient {
    
    /**
     * Initializes the connection with the MCP server.
     * 
     * This is the first operation that must be performed after establishing
     * a connection. It negotiates the protocol version and exchanges
     * capability information between client and server.
     * 
     * @param request The initialization request containing protocol version and client info
     * @return A future that completes with the server's initialization response
     * @throws MCPClientException if initialization fails
     */
    CompletableFuture<MCPResponse> initialize(MCPInitializeRequest request);
    
    /**
     * Lists all tools available on the connected MCP server.
     * 
     * Tools are functions that can be called by the client to perform
     * specific operations. Each tool has a name, description, and input schema.
     * 
     * @return A future that completes with the list of available tools
     * @throws MCPClientException if the operation fails
     */
    CompletableFuture<List<MCPTool>> listTools();
    
    /**
     * Executes a tool on the MCP server with the provided arguments.
     * 
     * The arguments must conform to the tool's input schema as returned
     * by the listTools() operation.
     * 
     * @param toolName The name of the tool to execute
     * @param arguments The arguments to pass to the tool (must match input schema)
     * @return A future that completes with the tool execution result
     * @throws MCPClientException if the tool doesn't exist or execution fails
     */
    CompletableFuture<MCPToolResult> callTool(String toolName, Map<String, Object> arguments);
    
    /**
     * Lists all resources available on the connected MCP server.
     * 
     * Resources are data sources that can be read by clients, such as
     * files, database records, or API endpoints.
     * 
     * @return A future that completes with the list of available resources
     * @throws MCPClientException if the operation fails
     */
    CompletableFuture<List<MCPResource>> listResources();
    
    /**
     * Reads the content of a specific resource from the MCP server.
     * 
     * @param uri The URI of the resource to read
     * @return A future that completes with the resource content
     * @throws MCPClientException if the resource doesn't exist or reading fails
     */
    CompletableFuture<String> readResource(String uri);
    
    /**
     * Checks if the client is currently connected to an MCP server.
     * 
     * @return true if connected and ready for operations, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the server information after successful initialization.
     * 
     * @return The server information, or null if not initialized
     */
    MCPClientInfo getServerInfo();
    
    /**
     * Closes the connection to the MCP server and releases all resources.
     * 
     * After calling this method, the client cannot be used for further
     * operations. A new client instance must be created for new connections.
     */
    void close();
}