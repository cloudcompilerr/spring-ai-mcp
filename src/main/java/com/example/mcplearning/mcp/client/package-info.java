/**
 * MCP client implementation package.
 * 
 * This package contains the core MCP client interface and implementations
 * for communicating with Model Context Protocol servers. The client provides
 * a high-level API for MCP operations while abstracting the underlying
 * transport and protocol details.
 * 
 * Key components:
 * - {@link com.example.mcplearning.mcp.client.MCPClient} - Core client interface
 * - {@link com.example.mcplearning.mcp.client.DefaultMCPClient} - Default implementation
 * - {@link com.example.mcplearning.mcp.client.MCPClientException} - Client-specific exceptions
 * 
 * The client supports all standard MCP operations:
 * - Server initialization and capability negotiation
 * - Tool discovery and execution
 * - Resource listing and access
 * - Connection lifecycle management
 * 
 * All operations are asynchronous and return CompletableFuture instances
 * to support non-blocking I/O and better performance.
 */
package com.example.mcplearning.mcp.client;