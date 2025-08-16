package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.client.MCPClientException;
import com.example.mcplearning.mcp.protocol.MCPResource;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Educational demonstrator for MCP tool execution and resource access functionality.
 * 
 * This class provides interactive examples and demonstrations of how to:
 * - Discover available tools and resources from MCP servers
 * - Execute tools with various argument patterns
 * - Read resource content and handle different content types
 * - Handle errors and edge cases gracefully
 * 
 * The demonstrations include detailed logging to help developers understand
 * the MCP protocol interactions and best practices.
 */
@Component
public class MCPToolAndResourceDemonstrator {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPToolAndResourceDemonstrator.class);
    
    /**
     * Demonstrates the complete tool discovery and execution workflow.
     * 
     * This method shows how to:
     * 1. Discover available tools from an MCP server
     * 2. Examine tool schemas and capabilities
     * 3. Execute tools with appropriate arguments
     * 4. Handle tool results and errors
     * 
     * @param client The initialized MCP client
     * @return A demonstration result containing the outcomes
     */
    public CompletableFuture<MCPExampleResult> demonstrateToolWorkflow(MCPClient client) {
        logger.info("🎓 Starting MCP Tool Workflow Demonstration");
        
        return client.listTools()
            .thenCompose(tools -> {
                logger.info("📋 Discovered {} tools from MCP server", tools.size());
                
                if (tools.isEmpty()) {
                    logger.warn("⚠️ No tools available for demonstration");
                    return CompletableFuture.completedFuture(
                        MCPExampleResult.quickSuccess("Tool workflow completed - no tools available"));
                }
                
                // Demonstrate tool examination
                examineToolCapabilities(tools);
                
                // Execute the first available tool as an example
                MCPTool firstTool = tools.get(0);
                return demonstrateToolExecution(client, firstTool)
                    .thenApply(result -> MCPExampleResult.success("Tool workflow demonstration completed", result));
            })
            .thenApply(result -> {
                logger.info("✅ Tool workflow demonstration completed successfully");
                return result;
            })
            .exceptionally(throwable -> {
                logger.error("❌ Tool workflow demonstration failed", throwable);
                return MCPExampleResult.error("Tool workflow failed", throwable.getMessage());
            });
    }
    
    /**
     * Demonstrates the complete resource discovery and reading workflow.
     * 
     * This method shows how to:
     * 1. Discover available resources from an MCP server
     * 2. Examine resource metadata and types
     * 3. Read resource content
     * 4. Handle different content types and errors
     * 
     * @param client The initialized MCP client
     * @return A demonstration result containing the outcomes
     */
    public CompletableFuture<MCPExampleResult> demonstrateResourceWorkflow(MCPClient client) {
        logger.info("🎓 Starting MCP Resource Workflow Demonstration");
        
        return client.listResources()
            .thenCompose(resources -> {
                logger.info("📚 Discovered {} resources from MCP server", resources.size());
                
                if (resources.isEmpty()) {
                    logger.warn("⚠️ No resources available for demonstration");
                    return CompletableFuture.completedFuture(
                        MCPExampleResult.quickSuccess("Resource workflow completed - no resources available"));
                }
                
                // Demonstrate resource examination
                examineResourceCapabilities(resources);
                
                // Read the first available resource as an example
                MCPResource firstResource = resources.get(0);
                return demonstrateResourceReading(client, firstResource)
                    .thenApply(result -> MCPExampleResult.success("Resource workflow demonstration completed", result));
            })
            .thenApply(result -> {
                logger.info("✅ Resource workflow demonstration completed successfully");
                return result;
            })
            .exceptionally(throwable -> {
                logger.error("❌ Resource workflow demonstration failed", throwable);
                return MCPExampleResult.error("Resource workflow failed", throwable.getMessage());
            });
    }
    
    /**
     * Demonstrates advanced tool execution patterns with various argument types.
     * 
     * @param client The initialized MCP client
     * @param toolName The name of the tool to execute
     * @param scenarios Different argument scenarios to test
     * @return A demonstration result
     */
    public CompletableFuture<MCPExampleResult> demonstrateAdvancedToolExecution(
            MCPClient client, String toolName, List<Map<String, Object>> scenarios) {
        
        logger.info("🎓 Starting Advanced Tool Execution Demonstration for '{}'", toolName);
        
        CompletableFuture<String> results = CompletableFuture.completedFuture("");
        
        for (int i = 0; i < scenarios.size(); i++) {
            Map<String, Object> scenario = scenarios.get(i);
            final int scenarioIndex = i + 1;
            
            results = results.thenCompose(previousResults -> {
                logger.info("🧪 Executing scenario {} of {} for tool '{}'", 
                           scenarioIndex, scenarios.size(), toolName);
                
                return client.callTool(toolName, scenario)
                    .thenApply(result -> {
                        String scenarioResult = String.format(
                            "Scenario %d: %s (Error: %s)", 
                            scenarioIndex, 
                            result.content() != null ? result.content().substring(0, Math.min(50, result.content().length())) : "null",
                            result.isError()
                        );
                        
                        logger.info("📊 Scenario {} result: {}", scenarioIndex, 
                                   result.isError() ? "ERROR" : "SUCCESS");
                        
                        return previousResults + "\n" + scenarioResult;
                    })
                    .exceptionally(throwable -> {
                        logger.warn("⚠️ Scenario {} failed: {}", scenarioIndex, throwable.getMessage());
                        return previousResults + "\nScenario " + scenarioIndex + ": FAILED - " + throwable.getMessage();
                    });
            });
        }
        
        return results.thenApply(allResults -> {
            logger.info("✅ Advanced tool execution demonstration completed");
            return MCPExampleResult.success("Advanced execution results", allResults);
        });
    }
    
    /**
     * Demonstrates error handling patterns for tool and resource operations.
     * 
     * @param client The initialized MCP client
     * @return A demonstration result showing error handling
     */
    public CompletableFuture<MCPExampleResult> demonstrateErrorHandling(MCPClient client) {
        logger.info("🎓 Starting MCP Error Handling Demonstration");
        
        StringBuilder errorResults = new StringBuilder();
        
        // Test 1: Non-existent tool
        CompletableFuture<String> toolErrorTest = client.callTool("nonexistent_tool", Map.of())
            .thenApply(result -> "Unexpected success for non-existent tool")
            .exceptionally(throwable -> {
                logger.info("✅ Correctly handled non-existent tool error: {}", throwable.getMessage());
                return "Non-existent tool error handled correctly";
            });
        
        // Test 2: Non-existent resource
        CompletableFuture<String> resourceErrorTest = client.readResource("file:///nonexistent/file.txt")
            .thenApply(result -> "Unexpected success for non-existent resource")
            .exceptionally(throwable -> {
                logger.info("✅ Correctly handled non-existent resource error: {}", throwable.getMessage());
                return "Non-existent resource error handled correctly";
            });
        
        return CompletableFuture.allOf(toolErrorTest, resourceErrorTest)
            .thenApply(v -> {
                try {
                    String toolResult = toolErrorTest.get();
                    String resourceResult = resourceErrorTest.get();
                    
                    String combinedResults = String.format(
                        "Error Handling Results:\n- Tool Error: %s\n- Resource Error: %s",
                        toolResult, resourceResult
                    );
                    
                    logger.info("✅ Error handling demonstration completed successfully");
                    return MCPExampleResult.success("Error handling demonstration completed", combinedResults);
                } catch (Exception e) {
                    logger.error("❌ Error handling demonstration failed", e);
                    return MCPExampleResult.error("Error handling demonstration failed", e.getMessage());
                }
            });
    }
    
    /**
     * Examines and logs the capabilities of discovered tools for educational purposes.
     * 
     * @param tools The list of tools to examine
     */
    private void examineToolCapabilities(List<MCPTool> tools) {
        logger.info("🔍 Examining Tool Capabilities:");
        
        for (int i = 0; i < tools.size(); i++) {
            MCPTool tool = tools.get(i);
            logger.info("  {}. Tool: '{}'", i + 1, tool.name());
            logger.info("     Description: {}", tool.description());
            
            if (tool.inputSchema() != null) {
                logger.info("     Input Schema Type: {}", tool.inputSchema().type());
                if (tool.inputSchema().properties() != null) {
                    logger.info("     Parameters: {}", tool.inputSchema().properties().keySet());
                }
            }
        }
    }
    
    /**
     * Examines and logs the capabilities of discovered resources for educational purposes.
     * 
     * @param resources The list of resources to examine
     */
    private void examineResourceCapabilities(List<MCPResource> resources) {
        logger.info("🔍 Examining Resource Capabilities:");
        
        for (int i = 0; i < resources.size(); i++) {
            MCPResource resource = resources.get(i);
            logger.info("  {}. Resource: '{}'", i + 1, resource.name());
            logger.info("     URI: {}", resource.uri());
            logger.info("     Description: {}", resource.description());
            logger.info("     MIME Type: {}", resource.mimeType());
        }
    }
    
    /**
     * Demonstrates tool execution with the given tool.
     * 
     * @param client The MCP client
     * @param tool The tool to execute
     * @return The execution result
     */
    private CompletableFuture<String> demonstrateToolExecution(MCPClient client, MCPTool tool) {
        logger.info("🚀 Demonstrating execution of tool: '{}'", tool.name());
        
        // Create simple arguments based on the tool's schema
        Map<String, Object> arguments = createSampleArguments(tool);
        
        return client.callTool(tool.name(), arguments)
            .thenApply(result -> {
                String resultSummary = String.format(
                    "Tool '%s' executed - Success: %s, Content Length: %d",
                    tool.name(),
                    !result.isError(),
                    result.content() != null ? result.content().length() : 0
                );
                
                logger.info("📊 Tool execution result: {}", resultSummary);
                return resultSummary;
            });
    }
    
    /**
     * Demonstrates resource reading with the given resource.
     * 
     * @param client The MCP client
     * @param resource The resource to read
     * @return The reading result
     */
    private CompletableFuture<String> demonstrateResourceReading(MCPClient client, MCPResource resource) {
        logger.info("📖 Demonstrating reading of resource: '{}'", resource.name());
        
        return client.readResource(resource.uri())
            .thenApply(content -> {
                String resultSummary = String.format(
                    "Resource '%s' read successfully - Content Length: %d, Type: %s",
                    resource.name(),
                    content.length(),
                    resource.mimeType()
                );
                
                logger.info("📊 Resource reading result: {}", resultSummary);
                return resultSummary;
            });
    }
    
    /**
     * Creates sample arguments for a tool based on its input schema.
     * This is a simplified implementation for demonstration purposes.
     * 
     * @param tool The tool to create arguments for
     * @return Sample arguments map
     */
    private Map<String, Object> createSampleArguments(MCPTool tool) {
        // For demonstration, we'll create minimal valid arguments
        // In a real implementation, this would be more sophisticated
        
        if (tool.inputSchema() != null && tool.inputSchema().properties() != null) {
            Map<String, Object> properties = tool.inputSchema().properties();
            
            // Create sample values for common parameter types
            Map<String, Object> arguments = new java.util.HashMap<>();
            
            properties.forEach((key, value) -> {
                if (value instanceof Map<?, ?> propDef) {
                    String type = (String) propDef.get("type");
                    switch (type != null ? type : "string") {
                        case "string" -> arguments.put(key, "sample_value");
                        case "number", "integer" -> arguments.put(key, 42);
                        case "boolean" -> arguments.put(key, true);
                        default -> arguments.put(key, "sample_value");
                    }
                }
            });
            
            logger.debug("📝 Created sample arguments for '{}': {}", tool.name(), arguments);
            return arguments;
        }
        
        return Map.of();
    }
}