package com.example.mcplearning.mcp.transport;

import java.util.concurrent.CompletableFuture;

/**
 * Transport layer interface for MCP communication.
 * 
 * This interface abstracts the underlying transport mechanism used to
 * communicate with MCP servers. It handles the low-level details of
 * sending and receiving JSON-RPC messages.
 * 
 * Implementations might use different transport mechanisms such as:
 * - Process stdin/stdout (for local MCP servers)
 * - WebSockets (for remote MCP servers)
 * - HTTP (for stateless MCP interactions)
 */
public interface MCPTransport {
    
    /**
     * Connects to the MCP server using the configured transport mechanism.
     * 
     * @return A future that completes when the connection is established
     * @throws MCPTransportException if connection fails
     */
    CompletableFuture<Void> connect();
    
    /**
     * Sends a JSON-RPC request to the MCP server and waits for a response.
     * 
     * @param request The JSON-RPC request to send
     * @return A future that completes with the server's response
     * @throws MCPTransportException if sending fails or times out
     */
    CompletableFuture<JsonRpcResponse> sendRequest(JsonRpcRequest request);
    
    /**
     * Sends a JSON-RPC notification to the MCP server (no response expected).
     * 
     * @param request The JSON-RPC request to send as a notification
     * @return A future that completes when the notification is sent
     * @throws MCPTransportException if sending fails
     */
    CompletableFuture<Void> sendNotification(JsonRpcRequest request);
    
    /**
     * Checks if the transport is currently connected.
     * 
     * @return true if connected and ready for communication
     */
    boolean isConnected();
    
    /**
     * Closes the transport connection and releases all resources.
     * 
     * After calling this method, the transport cannot be used for further
     * communication. A new transport instance must be created for new connections.
     */
    void close();
}