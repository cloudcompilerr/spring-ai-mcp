/**
 * MCP Server Management Components.
 * 
 * This package contains classes responsible for managing MCP server connections
 * and lifecycle. It provides both single and multi-server management capabilities
 * with comprehensive error handling, health monitoring, and educational logging.
 * 
 * Key Components:
 * - {@link com.example.mcplearning.mcp.server.MCPServerManager} - Main interface for server management
 * - {@link com.example.mcplearning.mcp.server.SingleMCPServerManager} - Single server implementation
 * - {@link com.example.mcplearning.mcp.server.MCPServerStatus} - Server status and health information
 * - {@link com.example.mcplearning.mcp.server.MCPConnectionState} - Connection state enumeration
 * - {@link com.example.mcplearning.mcp.server.MCPServerException} - Server management exceptions
 * 
 * The server management layer handles:
 * - Server lifecycle (connect, initialize, disconnect)
 * - Health monitoring and status reporting
 * - Error handling and recovery
 * - Connection retry logic with exponential backoff
 * - Educational logging for learning purposes
 * 
 * This package is designed to demonstrate proper MCP server management patterns
 * and serve as a foundation for understanding multi-server scenarios.
 */
package com.example.mcplearning.mcp.server;