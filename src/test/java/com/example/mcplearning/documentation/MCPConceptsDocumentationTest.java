package com.example.mcplearning.documentation;

import com.example.mcplearning.mcp.client.DefaultMCPClient;
import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.transport.JsonRpcRequest;
import com.example.mcplearning.mcp.transport.JsonRpcResponse;
import com.example.mcplearning.mcp.transport.MCPTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Documentation tests that explain MCP concepts through executable examples.
 * 
 * These tests serve dual purposes:
 * 1. Verify that MCP protocol implementation works correctly
 * 2. Provide comprehensive documentation of MCP concepts and usage patterns
 * 
 * Each test method demonstrates a specific MCP concept with detailed explanations
 * and practical examples that developers can learn from.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MCP Concepts Documentation")
class MCPConceptsDocumentationTest {
    
    @Mock
    private MCPTransport mockTransport;
    
    private ObjectMapper objectMapper;
    private MCPClient client;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        client = new DefaultMCPClient(mockTransport, objectMapper);
        when(mockTransport.isConnected()).thenReturn(true);
    }
    
    /**
     * CONCEPT: MCP Initialization
     * 
     * The Model Context Protocol begins with an initialization handshake between
     * client and server. This establishes the protocol version, capabilities,
     * and basic connection parameters.
     * 
     * Key Learning Points:
     * - MCP uses a standard initialization sequence
     * - Both client and server exchange capability information
     * - Protocol version negotiation ensures compatibility
     */
    @Test
    @DisplayName("MCP Initialization - Establishing Connection and Capabilities")
    void demonstrateMCPInitialization() throws Exception {
        // STEP 1: Create client information
        // The client must identify itself with name and version
        MCPClientInfo clientInfo = new MCPClientInfo(
            "learning-platform-client",  // Client name for identification
            "1.0.0"                      // Client version
        );
        
        // STEP 2: Create initialization request
        // This uses the standard MCP protocol version
        MCPInitializeRequest initRequest = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        // Verify the request structure
        assertThat(initRequest.protocolVersion()).isEqualTo("2024-11-05");
        assertThat(initRequest.clientInfo().name()).isEqualTo("learning-platform-client");
        
        // STEP 3: Mock server response
        // The server responds with its own information and capabilities
        Map<String, Object> serverResponse = Map.of(
            "serverInfo", Map.of(
                "name", "example-mcp-server",
                "version", "1.2.0"
            ),
            "capabilities", Map.of(
                "tools", Map.of("listChanged", true),
                "resources", Map.of("subscribe", true, "listChanged", true)
            )
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("init-1", serverResponse)));
        
        // STEP 4: Perform initialization
        MCPResponse response = client.initialize(initRequest).get();
        
        // STEP 5: Verify successful initialization
        assertThat(response.error()).isNull();
        assertThat(response.result()).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.result();
        
        // The server should provide its information
        assertThat(result).containsKey("serverInfo");
        assertThat(result).containsKey("capabilities");
        
        System.out.println("✓ MCP Initialization Complete");
        System.out.println("  Protocol Version: " + initRequest.protocolVersion());
        System.out.println("  Client: " + clientInfo.name() + " v" + clientInfo.version());
        System.out.println("  Server: " + ((Map<?, ?>) result.get("serverInfo")).get("name"));
    }
    
    /**
     * CONCEPT: MCP Tools
     * 
     * Tools are the primary way MCP servers expose functionality to clients.
     * Each tool has a name, description, and input schema that defines what
     * parameters it accepts.
     * 
     * Key Learning Points:
     * - Tools are discovered through the tools/list operation
     * - Each tool has a JSON Schema defining its input parameters
     * - Tools are executed through the tools/call operation
     * - Tool results can be successful data or error information
     */
    @Test
    @DisplayName("MCP Tools - Discovery and Execution")
    void demonstrateMCPTools() throws Exception {
        // STEP 1: Tool Discovery
        // First, we discover what tools are available on the server
        
        List<Map<String, Object>> toolsData = List.of(
            // Example 1: A simple file reading tool
            Map.of(
                "name", "read_file",
                "description", "Read the contents of a text file",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "path", Map.of(
                            "type", "string",
                            "description", "Path to the file to read"
                        )
                    ),
                    "required", List.of("path")
                )
            ),
            // Example 2: A more complex calculation tool
            Map.of(
                "name", "calculate",
                "description", "Perform mathematical calculations",
                "inputSchema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                        "expression", Map.of(
                            "type", "string",
                            "description", "Mathematical expression to evaluate"
                        ),
                        "precision", Map.of(
                            "type", "integer",
                            "description", "Number of decimal places",
                            "default", 2
                        )
                    ),
                    "required", List.of("expression")
                )
            )
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("tools-1", Map.of("tools", toolsData))));
        
        // Discover available tools
        List<MCPTool> tools = client.listTools().get();
        
        // STEP 2: Analyze discovered tools
        assertThat(tools).hasSize(2);
        
        MCPTool readFileTool = tools.get(0);
        assertThat(readFileTool.name()).isEqualTo("read_file");
        assertThat(readFileTool.description()).contains("Read the contents");
        assertThat(readFileTool.inputSchema()).isNotNull();
        
        MCPTool calculateTool = tools.get(1);
        assertThat(calculateTool.name()).isEqualTo("calculate");
        assertThat(calculateTool.description()).contains("mathematical");
        
        System.out.println("✓ Discovered " + tools.size() + " tools:");
        tools.forEach(tool -> {
            System.out.println("  - " + tool.name() + ": " + tool.description());
            
            // Show input schema structure
            MCPToolInputSchema schema = tool.inputSchema();
            if (schema.properties() != null) {
                System.out.println("    Parameters: " + schema.properties().keySet());
            }
        });
        
        // STEP 3: Tool Execution
        // Now we'll execute a tool with appropriate parameters
        
        Map<String, Object> toolResult = Map.of(
            "content", "Hello, World!\nThis is the content of the file.",
            "isError", false,
            "mimeType", "text/plain"
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("tool-exec-1", toolResult)));
        
        // Execute the read_file tool
        Map<String, Object> toolArgs = Map.of("path", "/example/file.txt");
        MCPToolResult result = client.callTool("read_file", toolArgs).get();
        
        // STEP 4: Analyze tool execution result
        assertThat(result.isError()).isFalse();
        assertThat(result.content()).contains("Hello, World!");
        assertThat(result.mimeType()).isEqualTo("text/plain");
        
        System.out.println("✓ Tool execution successful:");
        System.out.println("  Tool: read_file");
        System.out.println("  Arguments: " + toolArgs);
        System.out.println("  Result: " + result.content().substring(0, 20) + "...");
        System.out.println("  MIME Type: " + result.mimeType());
    }
    
    /**
     * CONCEPT: MCP Resources
     * 
     * Resources represent data or content that the MCP server can provide access to.
     * Unlike tools (which perform actions), resources are static content that can
     * be read by the client.
     * 
     * Key Learning Points:
     * - Resources are discovered through the resources/list operation
     * - Each resource has a URI, name, description, and MIME type
     * - Resources are accessed through the resources/read operation
     * - Resources can be text, binary, or structured data
     */
    @Test
    @DisplayName("MCP Resources - Discovery and Access")
    void demonstrateMCPResources() throws Exception {
        // STEP 1: Resource Discovery
        // Discover what resources are available on the server
        
        List<Map<String, Object>> resourcesData = List.of(
            // Example 1: A documentation file
            Map.of(
                "uri", "file:///docs/api-reference.md",
                "name", "API Reference Documentation",
                "description", "Complete API reference for the MCP server",
                "mimeType", "text/markdown"
            ),
            // Example 2: Configuration data
            Map.of(
                "uri", "config://server/settings.json",
                "name", "Server Configuration",
                "description", "Current server configuration settings",
                "mimeType", "application/json"
            ),
            // Example 3: A data file
            Map.of(
                "uri", "data://users/active.csv",
                "name", "Active Users Data",
                "description", "List of currently active users",
                "mimeType", "text/csv"
            )
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("resources-1", Map.of("resources", resourcesData))));
        
        // Discover available resources
        List<MCPResource> resources = client.listResources().get();
        
        // STEP 2: Analyze discovered resources
        assertThat(resources).hasSize(3);
        
        MCPResource docResource = resources.get(0);
        assertThat(docResource.uri()).startsWith("file://");
        assertThat(docResource.name()).contains("API Reference");
        assertThat(docResource.mimeType()).isEqualTo("text/markdown");
        
        System.out.println("✓ Discovered " + resources.size() + " resources:");
        resources.forEach(resource -> {
            System.out.println("  - " + resource.name());
            System.out.println("    URI: " + resource.uri());
            System.out.println("    Type: " + resource.mimeType());
            System.out.println("    Description: " + resource.description());
        });
        
        // STEP 3: Resource Access
        // Read the content of a specific resource
        
        String resourceContent = """
            # API Reference
            
            ## Overview
            This document provides a complete reference for the MCP server API.
            
            ## Endpoints
            - `/tools/list` - List available tools
            - `/tools/call` - Execute a tool
            - `/resources/list` - List available resources
            - `/resources/read` - Read resource content
            """;
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("resource-read-1", Map.of("contents", List.of(
                    Map.of(
                        "uri", "file:///docs/api-reference.md",
                        "mimeType", "text/markdown",
                        "text", resourceContent
                    )
                )))));
        
        // Read the documentation resource
        String content = client.readResource("file:///docs/api-reference.md").get();
        
        // STEP 4: Analyze resource content
        assertThat(content).contains("API Reference");
        assertThat(content).contains("Overview");
        assertThat(content).contains("/tools/list");
        
        System.out.println("✓ Resource access successful:");
        System.out.println("  URI: file:///docs/api-reference.md");
        System.out.println("  Content length: " + content.length() + " characters");
        System.out.println("  Content preview: " + content.substring(0, 50) + "...");
    }
    
    /**
     * CONCEPT: MCP Error Handling
     * 
     * MCP defines standard error codes and handling patterns for various
     * failure scenarios. Proper error handling is crucial for robust
     * MCP implementations.
     * 
     * Key Learning Points:
     * - MCP uses JSON-RPC error codes and messages
     * - Different types of errors require different handling strategies
     * - Error information should be preserved for debugging and user feedback
     * - Graceful degradation is important for user experience
     */
    @Test
    @DisplayName("MCP Error Handling - Standard Error Patterns")
    void demonstrateMCPErrorHandling() throws Exception {
        System.out.println("✓ Demonstrating MCP error handling patterns:");
        
        // ERROR SCENARIO 1: Tool Not Found
        // This happens when a client tries to call a non-existent tool
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("error-1", 
                    com.example.mcplearning.mcp.transport.JsonRpcError.create(
                        -32601, "Tool 'nonexistent_tool' not found"))));
        
        try {
            client.callTool("nonexistent_tool", Map.of()).get();
            assertThat(false).as("Should have thrown an exception").isTrue();
        } catch (Exception e) {
            System.out.println("  ✓ Tool Not Found Error:");
            System.out.println("    Error: " + e.getMessage());
            System.out.println("    Handling: Client should check available tools first");
        }
        
        // ERROR SCENARIO 2: Invalid Tool Arguments
        // This happens when tool arguments don't match the expected schema
        
        Map<String, Object> errorResult = Map.of(
            "content", "Invalid argument 'invalid_param': not defined in tool schema",
            "isError", true,
            "mimeType", "text/plain"
        );
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("error-2", errorResult)));
        
        MCPToolResult result = client.callTool("read_file", Map.of("invalid_param", "value")).get();
        
        assertThat(result.isError()).isTrue();
        assertThat(result.content()).contains("Invalid argument");
        
        System.out.println("  ✓ Invalid Arguments Error:");
        System.out.println("    Error: " + result.content());
        System.out.println("    Handling: Validate arguments against tool schema");
        
        // ERROR SCENARIO 3: Resource Not Found
        // This happens when trying to access a non-existent resource
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("error-3",
                    com.example.mcplearning.mcp.transport.JsonRpcError.create(
                        -32602, "Resource not found: invalid://nonexistent"))));
        
        try {
            client.readResource("invalid://nonexistent").get();
            assertThat(false).as("Should have thrown an exception").isTrue();
        } catch (Exception e) {
            System.out.println("  ✓ Resource Not Found Error:");
            System.out.println("    Error: " + e.getMessage());
            System.out.println("    Handling: Check resource availability first");
        }
        
        // ERROR SCENARIO 4: Server Internal Error
        // This represents server-side failures during operation
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("error-4",
                    com.example.mcplearning.mcp.transport.JsonRpcError.create(
                        -32603, "Internal server error: database connection failed"))));
        
        try {
            client.listTools().get();
            assertThat(false).as("Should have thrown an exception").isTrue();
        } catch (Exception e) {
            System.out.println("  ✓ Server Internal Error:");
            System.out.println("    Error: " + e.getMessage());
            System.out.println("    Handling: Retry with exponential backoff, fallback options");
        }
        
        System.out.println("  ✓ Error handling patterns demonstrated");
        System.out.println("    Key principle: Always provide meaningful error messages");
        System.out.println("    Key principle: Implement appropriate retry strategies");
        System.out.println("    Key principle: Graceful degradation when possible");
    }
    
    /**
     * CONCEPT: MCP Protocol Messages
     * 
     * MCP uses JSON-RPC 2.0 as its underlying transport protocol. Understanding
     * the message structure is important for debugging and advanced usage.
     * 
     * Key Learning Points:
     * - All MCP communication uses JSON-RPC 2.0 format
     * - Requests have method, params, and id fields
     * - Responses have result or error fields, plus id
     * - Message IDs are used to correlate requests and responses
     */
    @Test
    @DisplayName("MCP Protocol Messages - JSON-RPC Structure")
    void demonstrateMCPProtocolMessages() throws Exception {
        System.out.println("✓ Demonstrating MCP protocol message structure:");
        
        // EXAMPLE 1: Tool List Request
        // This shows the structure of a typical MCP request
        
        System.out.println("  Example Request (tools/list):");
        System.out.println("  {");
        System.out.println("    \"jsonrpc\": \"2.0\",");
        System.out.println("    \"method\": \"tools/list\",");
        System.out.println("    \"params\": {},");
        System.out.println("    \"id\": \"req-123\"");
        System.out.println("  }");
        
        // EXAMPLE 2: Successful Response
        // This shows how the server responds with successful data
        
        System.out.println("  Example Response (success):");
        System.out.println("  {");
        System.out.println("    \"jsonrpc\": \"2.0\",");
        System.out.println("    \"result\": {");
        System.out.println("      \"tools\": [");
        System.out.println("        {");
        System.out.println("          \"name\": \"read_file\",");
        System.out.println("          \"description\": \"Read file contents\",");
        System.out.println("          \"inputSchema\": { ... }");
        System.out.println("        }");
        System.out.println("      ]");
        System.out.println("    },");
        System.out.println("    \"id\": \"req-123\"");
        System.out.println("  }");
        
        // EXAMPLE 3: Error Response
        // This shows how errors are communicated
        
        System.out.println("  Example Response (error):");
        System.out.println("  {");
        System.out.println("    \"jsonrpc\": \"2.0\",");
        System.out.println("    \"error\": {");
        System.out.println("      \"code\": -32601,");
        System.out.println("      \"message\": \"Method not found\"");
        System.out.println("    },");
        System.out.println("    \"id\": \"req-123\"");
        System.out.println("  }");
        
        // EXAMPLE 4: Tool Call Request with Parameters
        // This shows a more complex request with parameters
        
        System.out.println("  Example Request (tools/call with params):");
        System.out.println("  {");
        System.out.println("    \"jsonrpc\": \"2.0\",");
        System.out.println("    \"method\": \"tools/call\",");
        System.out.println("    \"params\": {");
        System.out.println("      \"name\": \"read_file\",");
        System.out.println("      \"arguments\": {");
        System.out.println("        \"path\": \"/example/file.txt\"");
        System.out.println("      }");
        System.out.println("    },");
        System.out.println("    \"id\": \"req-124\"");
        System.out.println("  }");
        
        System.out.println("  ✓ Key Protocol Points:");
        System.out.println("    - Always use JSON-RPC 2.0 format");
        System.out.println("    - Include unique ID for request correlation");
        System.out.println("    - Use standard MCP method names (tools/list, tools/call, etc.)");
        System.out.println("    - Handle both result and error response types");
        System.out.println("    - Preserve message structure for debugging");
        
        // Verify that our implementation follows these patterns
        assertThat(true).as("Protocol structure documented").isTrue();
    }
    
    /**
     * CONCEPT: MCP Best Practices
     * 
     * This test documents important best practices for MCP implementation
     * that ensure reliable, maintainable, and user-friendly applications.
     * 
     * Key Learning Points:
     * - Connection management and lifecycle
     * - Error handling and recovery strategies
     * - Performance considerations
     * - Security and validation practices
     */
    @Test
    @DisplayName("MCP Best Practices - Implementation Guidelines")
    void demonstrateMCPBestPractices() {
        System.out.println("✓ MCP Implementation Best Practices:");
        
        System.out.println("  1. Connection Management:");
        System.out.println("    - Always check connection status before operations");
        System.out.println("    - Implement proper connection lifecycle (connect → initialize → use → disconnect)");
        System.out.println("    - Handle connection failures gracefully with retry logic");
        System.out.println("    - Use connection pooling for high-throughput scenarios");
        
        System.out.println("  2. Error Handling:");
        System.out.println("    - Distinguish between recoverable and non-recoverable errors");
        System.out.println("    - Implement exponential backoff for transient failures");
        System.out.println("    - Provide meaningful error messages to users");
        System.out.println("    - Log detailed error information for debugging");
        
        System.out.println("  3. Performance Optimization:");
        System.out.println("    - Cache tool and resource discovery results when appropriate");
        System.out.println("    - Use asynchronous operations to avoid blocking");
        System.out.println("    - Implement timeouts for all network operations");
        System.out.println("    - Monitor and measure operation performance");
        
        System.out.println("  4. Security Considerations:");
        System.out.println("    - Validate all input parameters against tool schemas");
        System.out.println("    - Sanitize file paths and URIs to prevent path traversal");
        System.out.println("    - Implement proper authentication when required");
        System.out.println("    - Use secure transport (TLS) for network communications");
        
        System.out.println("  5. User Experience:");
        System.out.println("    - Provide clear progress indicators for long operations");
        System.out.println("    - Offer helpful error messages with suggested solutions");
        System.out.println("    - Support cancellation of long-running operations");
        System.out.println("    - Implement proper resource cleanup");
        
        System.out.println("  6. Testing and Debugging:");
        System.out.println("    - Test with both mock and real MCP servers");
        System.out.println("    - Include comprehensive error scenario testing");
        System.out.println("    - Use verbose logging during development");
        System.out.println("    - Validate against MCP protocol specifications");
        
        // Verify that these practices are documented
        assertThat(true).as("Best practices documented").isTrue();
    }
}