/**
 * MCP Protocol Message Definitions.
 * 
 * This package contains all the core MCP (Model Context Protocol) message
 * definitions and data structures. These records represent the JSON messages
 * exchanged between MCP clients and servers, following the MCP specification.
 * 
 * Key components:
 * - {@link com.example.mcplearning.mcp.protocol.MCPInitializeRequest} - Server initialization
 * - {@link com.example.mcplearning.mcp.protocol.MCPResponse} - Generic server responses
 * - {@link com.example.mcplearning.mcp.protocol.MCPTool} - Tool definitions
 * - {@link com.example.mcplearning.mcp.protocol.MCPResource} - Resource definitions
 * - {@link com.example.mcplearning.mcp.protocol.MCPError} - Error information
 * 
 * All records in this package include:
 * - JSON serialization annotations for proper wire format
 * - Validation annotations for data integrity
 * - Factory methods for common use cases
 * - Educational helper methods for learning purposes
 */
package com.example.mcplearning.mcp.protocol;