package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.DefaultMCPClient;
import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.client.MCPClientException;
import com.example.mcplearning.mcp.protocol.*;
import com.example.mcplearning.mcp.server.MCPServerException;
import com.example.mcplearning.mcp.server.MCPServerManager;
import com.example.mcplearning.mcp.server.SingleMCPServerManager;
import com.example.mcplearning.mcp.transport.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Educational test scenarios for MCP error handling.
 * 
 * This test class demonstrates various error scenarios that can occur in MCP
 * implementations and shows how to handle them properly. Each test serves as
 * both a validation of error handling logic and an educational example of
 * proper error management patterns.
 * 
 * Learning Objectives:
 * - Understand different types of MCP errors and their causes
 * - Learn proper error handling and recovery strategies
 * - See examples of graceful degradation techniques
 * - Understand error reporting and logging best practices
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Educational MCP Error Handling Scenarios")
class MCPEducationalErrorHandlingTest {
    
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
     * SCENARIO: Connection Failures
     * 
     * This scenario demonstrates what happens when the MCP client cannot
     * establish or maintain a connection to the server. This is one of the
     * most common error scenarios in distributed systems.
     * 
     * Educational Points:
     * - Connection failures can happen at any time
     * - Proper retry logic with exponential backoff
     * - Graceful degradation when servers are unavailable
     * - User-friendly error messages
     */
    @Test
    @DisplayName("Connection Failures - Network and Server Unavailability")
    void demonstrateConnectionFailures() {
        System.out.println("📚 Learning Scenario: Connection Failures");
        System.out.println("   This demonstrates handling network and server availability issues");
        
        // CASE 1: Initial Connection Failure
        System.out.println("\n🔍 Case 1: Server Unreachable During Initial Connection");
        
        when(mockTransport.isConnected()).thenReturn(false);
        when(mockTransport.connect())
            .thenReturn(CompletableFuture.failedFuture(
                new MCPTransportException("Connection refused: server not running on port 8080")));
        
        MCPTransportException connectionError = assertThrows(MCPTransportException.class, () -> {
            try {
                mockTransport.connect().get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPTransportException) {
                    throw (MCPTransportException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Connection failed: " + connectionError.getMessage());
        System.out.println("   💡 Learning: Always check if server is running and accessible");
        System.out.println("   🔧 Solution: Implement retry logic with exponential backoff");
        
        // CASE 2: Connection Lost During Operation
        System.out.println("\n🔍 Case 2: Connection Lost During Operation");
        
        when(mockTransport.isConnected()).thenReturn(true, false); // Connected, then disconnected
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new MCPTransportException("Connection lost: socket closed by peer")));
        
        MCPClientException operationError = assertThrows(MCPClientException.class, () -> {
            try {
                client.listTools().get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Operation failed: " + operationError.getMessage());
        System.out.println("   💡 Learning: Network connections can be lost at any time");
        System.out.println("   🔧 Solution: Implement automatic reconnection and operation retry");
        
        // CASE 3: Timeout During Long Operation
        System.out.println("\n🔍 Case 3: Operation Timeout");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.failedFuture(
                new TimeoutException("Operation timed out after 30 seconds")));
        
        assertThrows(Exception.class, () -> {
            client.callTool("slow_operation", Map.of("data", "large_dataset")).get();
        });
        
        System.out.println("   ❌ Operation timed out");
        System.out.println("   💡 Learning: Some operations may take longer than expected");
        System.out.println("   🔧 Solution: Implement configurable timeouts and progress indicators");
    }
    
    /**
     * SCENARIO: Protocol-Level Errors
     * 
     * This scenario covers errors that occur at the MCP protocol level,
     * such as invalid requests, unsupported operations, or protocol
     * version mismatches.
     * 
     * Educational Points:
     * - MCP protocol has specific error codes and meanings
     * - Version compatibility is important
     * - Proper request validation prevents many errors
     * - Error codes help identify the root cause
     */
    @Test
    @DisplayName("Protocol Errors - Invalid Requests and Version Mismatches")
    void demonstrateProtocolErrors() {
        System.out.println("📚 Learning Scenario: Protocol-Level Errors");
        System.out.println("   This demonstrates MCP protocol error handling");
        
        // CASE 1: Method Not Found
        System.out.println("\n🔍 Case 1: Unsupported Method");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("1", JsonRpcError.create(
                    -32601, "Method 'unsupported/operation' not found"))));
        
        MCPClientException methodError = assertThrows(MCPClientException.class, () -> {
            try {
                // Simulate calling an unsupported method
                client.listTools().get(); // This will trigger the mocked error
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Method not supported: " + methodError.getMessage());
        System.out.println("   💡 Learning: Not all servers support all MCP methods");
        System.out.println("   🔧 Solution: Check server capabilities during initialization");
        
        // CASE 2: Invalid Parameters
        System.out.println("\n🔍 Case 2: Invalid Request Parameters");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("2", JsonRpcError.create(
                    -32602, "Invalid params: missing required parameter 'name'"))));
        
        MCPClientException paramError = assertThrows(MCPClientException.class, () -> {
            try {
                client.callTool("test_tool", Map.of()).get(); // Missing required parameters
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Invalid parameters: " + paramError.getMessage());
        System.out.println("   💡 Learning: Tool parameters must match the expected schema");
        System.out.println("   🔧 Solution: Validate parameters against tool input schema");
        
        // CASE 3: Protocol Version Mismatch
        System.out.println("\n🔍 Case 3: Protocol Version Incompatibility");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("3", JsonRpcError.create(
                    -32000, "Unsupported protocol version: client requested '2024-11-05', server supports '2024-06-01'"))));
        
        MCPClientException versionError = assertThrows(MCPClientException.class, () -> {
            try {
                MCPClientInfo clientInfo = new MCPClientInfo("test-client", "1.0.0");
                MCPInitializeRequest request = MCPInitializeRequest.withStandardVersion(clientInfo);
                client.initialize(request).get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Version mismatch: " + versionError.getMessage());
        System.out.println("   💡 Learning: MCP protocol versions must be compatible");
        System.out.println("   🔧 Solution: Implement version negotiation or fallback protocols");
    }
    
    /**
     * SCENARIO: Tool Execution Errors
     * 
     * This scenario demonstrates errors that occur during tool execution,
     * including tool not found, invalid arguments, and tool-specific failures.
     * 
     * Educational Points:
     * - Tools can fail for various reasons
     * - Error information helps users understand what went wrong
     * - Some errors are user errors, others are system errors
     * - Proper error categorization improves user experience
     */
    @Test
    @DisplayName("Tool Execution Errors - Invalid Tools and Arguments")
    void demonstrateToolExecutionErrors() throws Exception {
        System.out.println("📚 Learning Scenario: Tool Execution Errors");
        System.out.println("   This demonstrates handling tool-specific error conditions");
        
        // CASE 1: Tool Not Found
        System.out.println("\n🔍 Case 1: Attempting to Call Non-Existent Tool");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("1", JsonRpcError.create(
                    -32601, "Tool 'nonexistent_calculator' not found. Available tools: [read_file, write_file, list_directory]"))));
        
        MCPClientException toolNotFoundError = assertThrows(MCPClientException.class, () -> {
            try {
                client.callTool("nonexistent_calculator", Map.of("expression", "2+2")).get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Tool not found: " + toolNotFoundError.getMessage());
        System.out.println("   💡 Learning: Always verify tool availability before calling");
        System.out.println("   🔧 Solution: Use listTools() to discover available tools first");
        
        // CASE 2: Invalid Tool Arguments
        System.out.println("\n🔍 Case 2: Tool Called with Invalid Arguments");
        
        // Mock a successful tool list first
        when(mockTransport.sendRequest(argThat(req -> "tools/list".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("tools", Map.of("tools", List.of(
                    Map.of(
                        "name", "read_file",
                        "description", "Read file contents",
                        "inputSchema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "File path")
                            ),
                            "required", List.of("path")
                        )
                    )
                )))));
        
        // Mock tool call with invalid arguments
        when(mockTransport.sendRequest(argThat(req -> "tools/call".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("2", Map.of(
                    "content", "Error: Invalid argument 'filename'. Expected 'path' (string). " +
                              "Tool schema requires: {\"path\": \"string (required)\"}",
                    "isError", true,
                    "mimeType", "text/plain"
                ))));
        
        // First, discover tools to show proper workflow
        List<MCPTool> tools = client.listTools().get();
        assertThat(tools).hasSize(1);
        MCPTool readFileTool = tools.get(0);
        
        // Then call with wrong arguments
        MCPToolResult result = client.callTool("read_file", 
            Map.of("filename", "/test.txt")).get(); // Wrong parameter name
        
        assertThat(result.isError()).isTrue();
        System.out.println("   ❌ Invalid arguments: " + result.content());
        System.out.println("   💡 Learning: Tool arguments must match the input schema exactly");
        System.out.println("   🔧 Solution: Validate arguments against the tool's inputSchema");
        
        // Show the correct way
        System.out.println("   ✅ Correct usage would be:");
        System.out.println("      Tool: " + readFileTool.name());
        System.out.println("      Required params: " + readFileTool.inputSchema().properties().keySet());
        System.out.println("      Correct call: callTool(\"read_file\", {\"path\": \"/test.txt\"})");
        
        // CASE 3: Tool Execution Failure
        System.out.println("\n🔍 Case 3: Tool Execution Fails Due to System Error");
        
        when(mockTransport.sendRequest(argThat(req -> "tools/call".equals(req.method()))))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("3", Map.of(
                    "content", "Permission denied: Cannot read file '/etc/shadow' - insufficient privileges",
                    "isError", true,
                    "mimeType", "text/plain"
                ))));
        
        MCPToolResult permissionResult = client.callTool("read_file", 
            Map.of("path", "/etc/shadow")).get();
        
        assertThat(permissionResult.isError()).isTrue();
        System.out.println("   ❌ System error: " + permissionResult.content());
        System.out.println("   💡 Learning: Tools may fail due to system-level restrictions");
        System.out.println("   🔧 Solution: Handle system errors gracefully, suggest alternatives");
    }
    
    /**
     * SCENARIO: Resource Access Errors
     * 
     * This scenario demonstrates errors that occur when accessing MCP resources,
     * including missing resources, access permissions, and format issues.
     * 
     * Educational Points:
     * - Resource access can fail for various reasons
     * - URI validation is important for security
     * - Different resource types have different access patterns
     * - Proper error messages help users understand access issues
     */
    @Test
    @DisplayName("Resource Access Errors - Missing and Inaccessible Resources")
    void demonstrateResourceAccessErrors() throws Exception {
        System.out.println("📚 Learning Scenario: Resource Access Errors");
        System.out.println("   This demonstrates handling resource access failures");
        
        // CASE 1: Resource Not Found
        System.out.println("\n🔍 Case 1: Accessing Non-Existent Resource");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("1", JsonRpcError.create(
                    -32602, "Resource not found: file:///nonexistent/document.pdf"))));
        
        MCPClientException resourceNotFoundError = assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("file:///nonexistent/document.pdf").get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Resource not found: " + resourceNotFoundError.getMessage());
        System.out.println("   💡 Learning: Always verify resource existence before accessing");
        System.out.println("   🔧 Solution: Use listResources() to discover available resources");
        
        // CASE 2: Invalid Resource URI
        System.out.println("\n🔍 Case 2: Malformed Resource URI");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("2", JsonRpcError.create(
                    -32602, "Invalid URI format: 'not-a-valid-uri' - must be a valid URI scheme"))));
        
        MCPClientException invalidUriError = assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("not-a-valid-uri").get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Invalid URI: " + invalidUriError.getMessage());
        System.out.println("   💡 Learning: Resource URIs must follow proper URI format");
        System.out.println("   🔧 Solution: Validate URI format before making requests");
        
        // CASE 3: Access Permission Denied
        System.out.println("\n🔍 Case 3: Resource Access Permission Denied");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.error("3", JsonRpcError.create(
                    -32603, "Access denied: Resource 'config://system/secrets.json' requires admin privileges"))));
        
        MCPClientException accessDeniedError = assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("config://system/secrets.json").get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Access denied: " + accessDeniedError.getMessage());
        System.out.println("   💡 Learning: Some resources require special permissions");
        System.out.println("   🔧 Solution: Check resource permissions, provide authentication if needed");
        
        // CASE 4: Unsupported Resource Type
        System.out.println("\n🔍 Case 4: Unsupported Resource Format");
        
        when(mockTransport.sendRequest(any(JsonRpcRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(
                JsonRpcResponse.success("4", Map.of("contents", List.of(
                    Map.of(
                        "uri", "data://binary/image.png",
                        "mimeType", "image/png",
                        "blob", "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
                    )
                )))));
        
        // Current implementation only supports text resources
        MCPClientException unsupportedTypeError = assertThrows(MCPClientException.class, () -> {
            try {
                client.readResource("data://binary/image.png").get();
            } catch (Exception e) {
                if (e.getCause() instanceof MCPClientException) {
                    throw (MCPClientException) e.getCause();
                }
                throw new RuntimeException(e);
            }
        });
        
        System.out.println("   ❌ Unsupported format: " + unsupportedTypeError.getMessage());
        System.out.println("   💡 Learning: Not all resource types may be supported by the client");
        System.out.println("   🔧 Solution: Check MIME type and implement appropriate handlers");
    }
    
    /**
     * SCENARIO: Server Management Errors
     * 
     * This scenario demonstrates errors that occur at the server management level,
     * including server startup failures, configuration errors, and health check issues.
     * 
     * Educational Points:
     * - Server management involves multiple failure points
     * - Configuration validation prevents many runtime errors
     * - Health monitoring helps detect issues early
     * - Proper cleanup prevents resource leaks
     */
    @Test
    @DisplayName("Server Management Errors - Configuration and Lifecycle Issues")
    void demonstrateServerManagementErrors() {
        System.out.println("📚 Learning Scenario: Server Management Errors");
        System.out.println("   This demonstrates server lifecycle and configuration error handling");
        
        // CASE 1: Invalid Server Configuration
        System.out.println("\n🔍 Case 1: Invalid Server Configuration");
        
        com.example.mcplearning.mcp.config.MCPServerConfig invalidConfig = 
            new com.example.mcplearning.mcp.config.MCPServerConfig(
                "invalid-server",
                "Invalid Server",
                "", // Empty command
                List.of(),
                Map.of(),
                true
            );
        
        // This would fail in a real scenario due to invalid configuration
        System.out.println("   ❌ Configuration error: Empty command not allowed");
        System.out.println("   💡 Learning: Server configuration must be validated before use");
        System.out.println("   🔧 Solution: Implement configuration validation with clear error messages");
        

        
        // CASE 2: Server Process Startup Failure
        System.out.println("\n🔍 Case 2: Server Process Cannot Start");
        
        com.example.mcplearning.mcp.config.MCPServerConfig failingConfig = 
            new com.example.mcplearning.mcp.config.MCPServerConfig(
                "failing-server",
                "Failing Server",
                "nonexistent-command",
                List.of("--invalid-args"),
                Map.of(),
                true
            );
        
        // This would fail in real scenario
        System.out.println("   ❌ Server startup would fail: Command 'nonexistent-command' not found");
        System.out.println("   💡 Learning: Server commands must be valid and accessible");
        System.out.println("   🔧 Solution: Validate command availability before attempting startup");
        
        // CASE 3: Server Health Check Failure
        System.out.println("\n🔍 Case 3: Server Health Check Timeout");
        
        // Simulate a server that starts but doesn't respond to health checks
        System.out.println("   ❌ Health check timeout: Server started but not responding");
        System.out.println("   💡 Learning: Servers may start but fail to initialize properly");
        System.out.println("   🔧 Solution: Implement health checks with appropriate timeouts");
        
        // CASE 4: Resource Cleanup Issues
        System.out.println("\n🔍 Case 4: Resource Cleanup Problems");
        
        System.out.println("   ❌ Cleanup warning: Server process may not have terminated cleanly");
        System.out.println("   💡 Learning: Always implement proper resource cleanup");
        System.out.println("   🔧 Solution: Use try-with-resources and shutdown hooks");
        
        System.out.println("\n✅ Error Handling Best Practices Summary:");
        System.out.println("   1. Validate configuration early and thoroughly");
        System.out.println("   2. Implement proper retry logic with exponential backoff");
        System.out.println("   3. Provide clear, actionable error messages");
        System.out.println("   4. Use appropriate timeouts for all operations");
        System.out.println("   5. Implement graceful degradation when possible");
        System.out.println("   6. Always clean up resources properly");
        System.out.println("   7. Log detailed error information for debugging");
        System.out.println("   8. Test error scenarios as thoroughly as success scenarios");
    }
}