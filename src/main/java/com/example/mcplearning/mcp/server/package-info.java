/**
 * MCP Server Management Package.
 * 
 * This package provides comprehensive server management capabilities for the
 * Model Context Protocol (MCP) learning platform. It demonstrates both single
 * and multi-server scenarios with educational focus on distributed system concepts.
 * 
 * <h2>Key Components</h2>
 * 
 * <h3>Server Management</h3>
 * <ul>
 *   <li>{@link MCPServerManager} - Core interface for server lifecycle management</li>
 *   <li>{@link SingleMCPServerManager} - Single server implementation with comprehensive logging</li>
 *   <li>{@link MultiMCPServerManager} - Multi-server implementation with advanced features</li>
 *   <li>{@link MCPServerManagerFactory} - Factory for creating appropriate server managers</li>
 * </ul>
 * 
 * <h3>Server Selection and Routing</h3>
 * <ul>
 *   <li>{@link ServerSelectionStrategy} - Strategy interface for server selection algorithms</li>
 *   <li>{@link HealthBasedSelectionStrategy} - Health-aware server selection</li>
 *   <li>{@link RoundRobinSelectionStrategy} - Round-robin load balancing</li>
 *   <li>{@link MCPToolRouter} - Tool and resource routing across multiple servers</li>
 * </ul>
 * 
 * <h3>Status and Health Monitoring</h3>
 * <ul>
 *   <li>{@link MCPServerStatus} - Comprehensive server status information</li>
 *   <li>{@link MCPConnectionState} - Connection state enumeration</li>
 * </ul>
 * 
 * <h2>Educational Concepts Demonstrated</h2>
 * 
 * <h3>Single Server Management</h3>
 * <ul>
 *   <li>Connection lifecycle management</li>
 *   <li>Retry logic with exponential backoff</li>
 *   <li>Comprehensive error handling and recovery</li>
 *   <li>Health monitoring and status reporting</li>
 *   <li>Educational logging for protocol understanding</li>
 * </ul>
 * 
 * <h3>Multi-Server Management</h3>
 * <ul>
 *   <li>Concurrent server connection management</li>
 *   <li>Tool and resource discovery across servers</li>
 *   <li>Conflict resolution for duplicate tool names</li>
 *   <li>Server selection strategies and load balancing</li>
 *   <li>Health-based routing and failover mechanisms</li>
 *   <li>Distributed system coordination patterns</li>
 * </ul>
 * 
 * <h3>Advanced Features</h3>
 * <ul>
 *   <li>Strategy pattern for server selection algorithms</li>
 *   <li>Factory pattern for conditional bean creation</li>
 *   <li>Observer pattern for health monitoring</li>
 *   <li>Circuit breaker patterns for fault tolerance</li>
 *   <li>Service mesh concepts for request routing</li>
 * </ul>
 * 
 * <h2>Configuration</h2>
 * 
 * <p>The server management behavior is controlled through Spring Boot configuration:</p>
 * 
 * <pre>{@code
 * mcp:
 *   enable-multi-server: true  # Enable multi-server support
 *   connection-timeout: 30s
 *   max-retries: 3
 *   retry-delay: 5s
 *   health-check-interval: 1m
 *   verbose-logging: true
 *   servers:
 *     - id: server1
 *       name: "Example Server 1"
 *       command: uvx
 *       args: [mcp-server-example]
 *       enabled: true
 * }</pre>
 * 
 * <h2>Usage Examples</h2>
 * 
 * <h3>Single Server Usage</h3>
 * <pre>{@code
 * @Autowired
 * private MCPServerManager serverManager;
 * 
 * // Add a server
 * MCPServerConfig config = new MCPServerConfig("server1", "Example", "uvx", 
 *     List.of("mcp-server-example"), Map.of(), true);
 * serverManager.addServer(config).join();
 * 
 * // Get client and use it
 * MCPClient client = serverManager.getClient("server1");
 * List<MCPTool> tools = client.listTools().join();
 * }</pre>
 * 
 * <h3>Multi-Server Usage with Routing</h3>
 * <pre>{@code
 * @Autowired
 * private MCPToolRouter toolRouter;
 * 
 * // Route tool calls automatically
 * MCPToolResult result = toolRouter.callTool("example-tool", 
 *     Map.of("param", "value")).join();
 * 
 * // List all tools across servers
 * List<MCPTool> allTools = toolRouter.listAllTools().join();
 * }</pre>
 * 
 * <h2>Testing</h2>
 * 
 * <p>The package includes comprehensive tests demonstrating:</p>
 * <ul>
 *   <li>Unit tests for individual components</li>
 *   <li>Integration tests with mock servers</li>
 *   <li>Strategy pattern testing</li>
 *   <li>Error handling and recovery scenarios</li>
 *   <li>Multi-server coordination testing</li>
 * </ul>
 * 
 * @since 1.0
 * @author MCP Learning Platform
 */
package com.example.mcplearning.mcp.server;