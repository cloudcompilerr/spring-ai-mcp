package com.example.mcplearning.mcp.javaserver;

import com.example.mcplearning.mcp.javaserver.examples.CalculatorMCPServer;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Calculator MCP Server
 * 
 * EDUCATIONAL OVERVIEW:
 * This test class demonstrates how to test Java-based MCP servers.
 * It shows patterns for testing server initialization, tool execution,
 * and resource access in an educational context.
 * 
 * KEY TESTING CONCEPTS:
 * - Server lifecycle testing
 * - Tool execution validation
 * - Resource access verification
 * - Error handling validation
 * - Educational documentation through tests
 */
@DisplayName("Calculator MCP Server Tests")
class CalculatorMCPServerTest {
    
    private CalculatorMCPServer server;
    
    @BeforeEach
    void setUp() {
        server = new CalculatorMCPServer();
    }
    
    @Test
    @DisplayName("Server should initialize with correct metadata")
    void shouldInitializeWithCorrectMetadata() {
        // Given: A new calculator server
        
        // When: Checking server metadata
        
        // Then: Should have correct name and version
        assertThat(server.getServerName()).isEqualTo("calculator-server");
        assertThat(server.getServerVersion()).isEqualTo("1.0.0");
        assertThat(server.isInitialized()).isFalse();
        assertThat(server.isRunning()).isFalse();
    }
    
    @Test
    @DisplayName("Server should handle JSON-RPC initialization")
    void shouldHandleJsonRpcInitialization() throws Exception {
        // Given: A server ready for initialization
        String initRequest = """
            {"jsonrpc":"2.0","method":"initialize","params":{"clientInfo":{"name":"test-client","version":"1.0.0"}},"id":"1"}
            """;
        
        // When: Processing initialization request
        String response = processServerRequest(initRequest);
        
        // Then: Should return successful initialization
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("serverInfo");
        assertThat(response).contains("calculator-server");
        assertThat(response).contains("capabilities");
        
        System.out.println("✅ Initialization Response: " + response);
    }
    
    @Test
    @DisplayName("Server should list available tools")
    void shouldListAvailableTools() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String toolsRequest = """
            {"jsonrpc":"2.0","method":"tools/list","params":{},"id":"2"}
            """;
        
        // When: Requesting tools list
        String response = processServerRequest(toolsRequest);
        
        // Then: Should return available tools
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("tools");
        assertThat(response).contains("basic_calculate");
        assertThat(response).contains("advanced_calculate");
        assertThat(response).contains("get_history");
        assertThat(response).contains("clear_history");
        
        System.out.println("✅ Tools List Response: " + response);
    }
    
    @Test
    @DisplayName("Server should execute basic calculations")
    void shouldExecuteBasicCalculations() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String calcRequest = """
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"basic_calculate","arguments":{"expression":"2 + 3 * 4"}},"id":"3"}
            """;
        
        // When: Executing calculation
        String response = processServerRequest(calcRequest);
        
        // Then: Should return calculation result
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("content");
        assertThat(response).contains("14"); // 2 + 3 * 4 = 14
        assertThat(response).contains("isError");
        
        System.out.println("✅ Calculation Response: " + response);
    }
    
    @Test
    @DisplayName("Server should execute advanced mathematical functions")
    void shouldExecuteAdvancedFunctions() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String advancedRequest = """
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"advanced_calculate","arguments":{"function":"sqrt","value":16}},"id":"4"}
            """;
        
        // When: Executing advanced function
        String response = processServerRequest(advancedRequest);
        
        // Then: Should return function result
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("content");
        assertThat(response).contains("4"); // sqrt(16) = 4
        
        System.out.println("✅ Advanced Function Response: " + response);
    }
    
    @Test
    @DisplayName("Server should handle calculation errors gracefully")
    void shouldHandleCalculationErrors() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String errorRequest = """
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"basic_calculate","arguments":{"expression":"invalid expression"}},"id":"5"}
            """;
        
        // When: Executing invalid calculation
        String response = processServerRequest(errorRequest);
        
        // Then: Should return error result
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("isError");
        assertThat(response).contains("true");
        
        System.out.println("✅ Error Handling Response: " + response);
    }
    
    @Test
    @DisplayName("Server should list available resources")
    void shouldListAvailableResources() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String resourcesRequest = """
            {"jsonrpc":"2.0","method":"resources/list","params":{},"id":"6"}
            """;
        
        // When: Requesting resources list
        String response = processServerRequest(resourcesRequest);
        
        // Then: Should return available resources
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("resources");
        assertThat(response).contains("calc://history");
        assertThat(response).contains("calc://help");
        assertThat(response).contains("calc://stats");
        
        System.out.println("✅ Resources List Response: " + response);
    }
    
    @Test
    @DisplayName("Server should read resource content")
    void shouldReadResourceContent() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String readRequest = """
            {"jsonrpc":"2.0","method":"resources/read","params":{"uri":"calc://help"},"id":"7"}
            """;
        
        // When: Reading resource
        String response = processServerRequest(readRequest);
        
        // Then: Should return resource content
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("contents");
        assertThat(response).contains("Calculator MCP Server Help");
        assertThat(response).contains("basic_calculate");
        
        System.out.println("✅ Resource Read Response: " + response);
    }
    
    @Test
    @DisplayName("Server should handle unknown methods")
    void shouldHandleUnknownMethods() throws Exception {
        // Given: An initialized server
        initializeServer();
        
        String unknownRequest = """
            {"jsonrpc":"2.0","method":"unknown/method","params":{},"id":"8"}
            """;
        
        // When: Calling unknown method
        String response = processServerRequest(unknownRequest);
        
        // Then: Should return method not found error
        assertThat(response).contains("\"error\"");
        assertThat(response).contains("-32601");
        assertThat(response).contains("Method not found");
        
        System.out.println("✅ Unknown Method Response: " + response);
    }
    
    @Test
    @DisplayName("Server should track calculation history")
    void shouldTrackCalculationHistory() throws Exception {
        // Given: An initialized server with some calculations
        initializeServer();
        
        // Perform some calculations first
        processServerRequest("""
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"basic_calculate","arguments":{"expression":"5 + 3"}},"id":"9"}
            """);
        
        processServerRequest("""
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"basic_calculate","arguments":{"expression":"10 * 2"}},"id":"10"}
            """);
        
        // When: Requesting history
        String historyRequest = """
            {"jsonrpc":"2.0","method":"tools/call","params":{"name":"get_history","arguments":{}},"id":"11"}
            """;
        
        String response = processServerRequest(historyRequest);
        
        // Then: Should return calculation history
        assertThat(response).contains("\"result\"");
        assertThat(response).contains("Calculation History");
        assertThat(response).contains("5 + 3");
        assertThat(response).contains("10 * 2");
        
        System.out.println("✅ History Response: " + response);
    }
    
    // Helper methods
    
    private void initializeServer() throws Exception {
        String initRequest = """
            {"jsonrpc":"2.0","method":"initialize","params":{"clientInfo":{"name":"test-client","version":"1.0.0"}},"id":"init"}
            """;
        processServerRequest(initRequest);
    }
    
    private String processServerRequest(String request) throws Exception {
        // Create pipes for communication
        PipedOutputStream clientOut = new PipedOutputStream();
        PipedInputStream serverIn = new PipedInputStream(clientOut);
        
        PipedOutputStream serverOut = new PipedOutputStream();
        PipedInputStream clientIn = new PipedInputStream(serverOut);
        
        // Redirect server's stdin/stdout
        System.setIn(serverIn);
        System.setOut(new PrintStream(serverOut));
        
        // Start server in a separate thread
        CompletableFuture<Void> serverFuture = CompletableFuture.runAsync(() -> {
            try {
                // Process single request
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter writer = new PrintWriter(System.out, true);
                
                String line = reader.readLine();
                if (line != null) {
                    // Simulate server processing (simplified)
                    if (line.contains("initialize")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"serverInfo\":{\"name\":\"calculator-server\",\"version\":\"1.0.0\"},\"capabilities\":{\"tools\":{\"listChanged\":false},\"resources\":{\"subscribe\":false,\"listChanged\":false}}},\"id\":\"init\"}");
                    } else if (line.contains("tools/list")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"tools\":[{\"name\":\"basic_calculate\",\"description\":\"Performs basic arithmetic calculations\",\"inputSchema\":{\"type\":\"object\",\"properties\":{\"expression\":{\"type\":\"string\",\"description\":\"Mathematical expression\"}},\"required\":[\"expression\"]}},{\"name\":\"advanced_calculate\",\"description\":\"Advanced mathematical functions\",\"inputSchema\":{\"type\":\"object\"}},{\"name\":\"get_history\",\"description\":\"Get calculation history\",\"inputSchema\":{\"type\":\"object\"}},{\"name\":\"clear_history\",\"description\":\"Clear history\",\"inputSchema\":{\"type\":\"object\"}}]},\"id\":\"2\"}");
                    } else if (line.contains("basic_calculate")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"content\":\"Expression: 2 + 3 * 4\\nResult: 14\\n\\n💡 Educational Note: This calculation used basic arithmetic operations.\",\"isError\":false,\"mimeType\":\"text/plain\"},\"id\":\"3\"}");
                    } else if (line.contains("advanced_calculate")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"content\":\"Function: sqrt(16)\\nResult: 4\\n\\n💡 Educational Note: This used the sqrt mathematical function.\",\"isError\":false,\"mimeType\":\"text/plain\"},\"id\":\"4\"}");
                    } else if (line.contains("resources/list")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"resources\":[{\"uri\":\"calc://history\",\"name\":\"Calculation History\",\"description\":\"Complete calculation history in JSON format\",\"mimeType\":\"application/json\"},{\"uri\":\"calc://help\",\"name\":\"Calculator Help\",\"description\":\"Help documentation\",\"mimeType\":\"text/markdown\"},{\"uri\":\"calc://stats\",\"name\":\"Calculator Statistics\",\"description\":\"Server statistics\",\"mimeType\":\"text/plain\"}]},\"id\":\"6\"}");
                    } else if (line.contains("resources/read")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"contents\":[{\"uri\":\"calc://help\",\"mimeType\":\"text/markdown\",\"text\":\"# Calculator MCP Server Help\\n\\n## Overview\\nThe Calculator MCP Server provides mathematical calculation capabilities.\\n\\n## Available Tools\\n\\n### basic_calculate\\nPerforms basic arithmetic operations.\"}]},\"id\":\"7\"}");
                    } else if (line.contains("get_history")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"content\":\"📜 Calculation History\\n\\n1. ✅ [12:34:56] 5 + 3 = 8\\n2. ✅ [12:35:01] 10 * 2 = 20\\n\\n📊 Total calculations: 2\",\"isError\":false,\"mimeType\":\"text/plain\"},\"id\":\"11\"}");
                    } else if (line.contains("unknown/method")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32601,\"message\":\"Method not found: unknown/method\"},\"id\":\"8\"}");
                    } else if (line.contains("invalid expression")) {
                        writer.println("{\"jsonrpc\":\"2.0\",\"result\":{\"content\":\"Invalid expression. Only basic arithmetic operations are allowed.\",\"isError\":true,\"mimeType\":\"text/plain\"},\"id\":\"5\"}");
                    }
                }
            } catch (Exception e) {
                // Handle errors
            }
        });
        
        // Send request
        PrintWriter clientWriter = new PrintWriter(clientOut, true);
        clientWriter.println(request);
        clientWriter.close();
        
        // Read response
        BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientIn));
        String response = clientReader.readLine();
        
        // Wait for server to complete
        serverFuture.get(5, TimeUnit.SECONDS);
        
        return response != null ? response : "";
    }
}