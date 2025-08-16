/**
 * MCP transport layer package.
 * 
 * This package contains the transport layer implementations for MCP communication.
 * The transport layer handles the low-level details of sending and receiving
 * JSON-RPC messages between MCP clients and servers.
 * 
 * Key components:
 * - {@link com.example.mcplearning.mcp.transport.MCPTransport} - Transport interface
 * - {@link com.example.mcplearning.mcp.transport.ProcessMCPTransport} - Process-based transport
 * - JSON-RPC message structures for protocol communication
 * - Transport-specific exceptions and error handling
 * 
 * The transport layer is designed to be pluggable, allowing different
 * transport mechanisms such as:
 * - Process stdin/stdout (most common for MCP servers)
 * - WebSockets (for remote MCP servers)
 * - HTTP (for stateless MCP interactions)
 * 
 * All transport operations are asynchronous and handle connection management,
 * message serialization, and error recovery.
 */
package com.example.mcplearning.mcp.transport;