package com.example.mcplearning.mcp.config;

import com.example.mcplearning.mcp.client.DefaultMCPClient;
import com.example.mcplearning.mcp.client.DefaultReactiveMCPClient;
import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.client.ReactiveMCPClient;
import com.example.mcplearning.mcp.transport.ProcessMCPTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for reactive MCP components.
 * 
 * This configuration demonstrates how to set up reactive MCP clients
 * alongside traditional synchronous clients, providing flexibility
 * in choosing the appropriate programming model for different use cases.
 */
@Configuration
public class ReactiveMCPConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactiveMCPConfiguration.class);
    
    /**
     * Creates a demo MCP client bean for educational purposes.
     * 
     * This bean provides a basic MCPClient implementation that can be used
     * for demonstrations and testing when no real MCP servers are available.
     * 
     * @param mcpConfiguration The MCP configuration properties
     * @return A configured MCPClient instance
     */
    @Bean
    @ConditionalOnProperty(name = "mcp.demo.enabled", havingValue = "true", matchIfMissing = true)
    public MCPClient demoMCPClient(MCPConfiguration mcpConfiguration) {
        logger.info("Creating demo MCP client with configuration: {}", mcpConfiguration);
        return createDemoMCPClient(mcpConfiguration);
    }
    
    /**
     * Creates a reactive MCP client bean when reactive features are enabled.
     * 
     * This bean wraps an existing MCPClient to provide reactive operations.
     * 
     * @param mcpClient The MCPClient to wrap
     * @return A configured ReactiveMCPClient instance
     */
    @Bean
    @ConditionalOnProperty(name = "mcp.reactive.enabled", havingValue = "true", matchIfMissing = true)
    public ReactiveMCPClient reactiveMCPClient(MCPClient mcpClient) {
        logger.info("Creating reactive MCP client wrapper");
        
        ReactiveMCPClient reactiveMCPClient = new DefaultReactiveMCPClient(mcpClient);
        logger.info("Reactive MCP client created successfully");
        
        return reactiveMCPClient;
    }
    
    /**
     * Creates a demo MCP client for reactive demonstrations.
     * 
     * This method creates a basic MCP client that can be used for
     * educational purposes and demonstrations.
     * 
     * @param mcpConfiguration The MCP configuration
     * @return A configured MCPClient instance
     */
    private MCPClient createDemoMCPClient(MCPConfiguration mcpConfiguration) {
        logger.debug("Creating demo MCP client for reactive demonstrations");
        
        // Create a transport for the first configured server (if any)
        if (mcpConfiguration.getServers() != null && !mcpConfiguration.getServers().isEmpty()) {
            MCPServerConfig firstServer = mcpConfiguration.getServers().get(0);
            ProcessMCPTransport transport = new ProcessMCPTransport(
                    firstServer.command(),
                    firstServer.args(),
                    firstServer.env(),
                    new com.fasterxml.jackson.databind.ObjectMapper(),
                    30000L // 30 second timeout
            );
            
            return new DefaultMCPClient(transport, new com.fasterxml.jackson.databind.ObjectMapper());
        }
        
        // Return a mock client if no servers are configured
        return new MockMCPClientForReactiveDemo();
    }
    
    /**
     * Mock MCP client for demonstration purposes when no real servers are configured.
     * 
     * This client provides dummy responses to allow the reactive demonstrations
     * to work even without actual MCP servers configured.
     */
    private static class MockMCPClientForReactiveDemo implements MCPClient {
        private static final Logger logger = LoggerFactory.getLogger(MockMCPClientForReactiveDemo.class);
        
        @Override
        public java.util.concurrent.CompletableFuture<com.example.mcplearning.mcp.protocol.MCPResponse> initialize(
                com.example.mcplearning.mcp.protocol.MCPInitializeRequest request) {
            logger.debug("Mock MCP client initialize called");
            return java.util.concurrent.CompletableFuture.completedFuture(
                    new com.example.mcplearning.mcp.protocol.MCPResponse(
                            "init-1",
                            new com.example.mcplearning.mcp.protocol.MCPClientInfo(
                                    "Mock MCP Server",
                                    "1.0.0"
                            ),
                            null
                    )
            );
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<java.util.List<com.example.mcplearning.mcp.protocol.MCPTool>> listTools() {
            logger.debug("Mock MCP client listTools called");
            return java.util.concurrent.CompletableFuture.completedFuture(
                    java.util.List.of(
                            new com.example.mcplearning.mcp.protocol.MCPTool(
                                    "demo-tool",
                                    "A demonstration tool for reactive examples",
                                    new com.example.mcplearning.mcp.protocol.MCPToolInputSchema(
                                            "object",
                                            java.util.Map.of("message", java.util.Map.of("type", "string")),
                                            java.util.List.of("message")
                                    )
                            ),
                            new com.example.mcplearning.mcp.protocol.MCPTool(
                                    "echo-tool",
                                    "Echoes back the input message",
                                    new com.example.mcplearning.mcp.protocol.MCPToolInputSchema(
                                            "object",
                                            java.util.Map.of("text", java.util.Map.of("type", "string")),
                                            java.util.List.of("text")
                                    )
                            )
                    )
            );
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<com.example.mcplearning.mcp.protocol.MCPToolResult> callTool(
                String toolName, java.util.Map<String, Object> arguments) {
            logger.debug("Mock MCP client callTool called: {} with {}", toolName, arguments);
            return java.util.concurrent.CompletableFuture.completedFuture(
                    new com.example.mcplearning.mcp.protocol.MCPToolResult(
                            String.format("Mock result from tool '%s' with arguments: %s", toolName, arguments),
                            false,
                            "text/plain"
                    )
            );
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<java.util.List<com.example.mcplearning.mcp.protocol.MCPResource>> listResources() {
            logger.debug("Mock MCP client listResources called");
            return java.util.concurrent.CompletableFuture.completedFuture(
                    java.util.List.of(
                            new com.example.mcplearning.mcp.protocol.MCPResource(
                                    "demo://resource1",
                                    "Demo Resource 1",
                                    "A demonstration resource",
                                    "text/plain"
                            ),
                            new com.example.mcplearning.mcp.protocol.MCPResource(
                                    "demo://resource2",
                                    "Demo Resource 2",
                                    "Another demonstration resource",
                                    "text/plain"
                            )
                    )
            );
        }
        
        @Override
        public java.util.concurrent.CompletableFuture<String> readResource(String uri) {
            logger.debug("Mock MCP client readResource called: {}", uri);
            return java.util.concurrent.CompletableFuture.completedFuture(
                    String.format("Mock content for resource: %s\nThis is demonstration content.", uri)
            );
        }
        
        @Override
        public boolean isConnected() {
            return true;
        }
        
        @Override
        public com.example.mcplearning.mcp.protocol.MCPClientInfo getServerInfo() {
            return new com.example.mcplearning.mcp.protocol.MCPClientInfo(
                    "Mock MCP Server",
                    "1.0.0"
            );
        }
        
        @Override
        public void close() {
            logger.debug("Mock MCP client close called");
        }
    }
}