package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JSON-RPC message structures.
 * 
 * These tests verify the serialization, deserialization, and validation
 * of JSON-RPC messages used in MCP communication.
 */
class JsonRpcMessageTest {
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }
    
    @Test
    void shouldCreateJsonRpcRequestWithParameters() {
        // Given
        String id = "test-123";
        String method = "tools/list";
        Map<String, Object> params = Map.of("filter", "active");
        
        // When
        JsonRpcRequest request = JsonRpcRequest.create(id, method, params);
        
        // Then
        assertEquals("2.0", request.jsonrpc());
        assertEquals(id, request.id());
        assertEquals(method, request.method());
        assertEquals(params, request.params());
        assertTrue(request.hasParams());
    }
    
    @Test
    void shouldCreateJsonRpcRequestWithoutParameters() {
        // Given
        String id = "test-456";
        String method = "resources/list";
        
        // When
        JsonRpcRequest request = JsonRpcRequest.create(id, method);
        
        // Then
        assertEquals("2.0", request.jsonrpc());
        assertEquals(id, request.id());
        assertEquals(method, request.method());
        assertNull(request.params());
        assertFalse(request.hasParams());
    }
    
    @Test
    void shouldSerializeJsonRpcRequestToJson() throws Exception {
        // Given
        JsonRpcRequest request = JsonRpcRequest.create("1", "initialize", 
            Map.of("protocolVersion", "2024-11-05"));
        
        // When
        String json = objectMapper.writeValueAsString(request);
        
        // Then
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":\"1\""));
        assertTrue(json.contains("\"method\":\"initialize\""));
        assertTrue(json.contains("\"protocolVersion\":\"2024-11-05\""));
    }
    
    @Test
    void shouldDeserializeJsonRpcRequestFromJson() throws Exception {
        // Given
        String json = """
            {
                "jsonrpc": "2.0",
                "id": "test-789",
                "method": "tools/call",
                "params": {
                    "name": "echo",
                    "arguments": {"message": "hello"}
                }
            }
            """;
        
        // When
        JsonRpcRequest request = objectMapper.readValue(json, JsonRpcRequest.class);
        
        // Then
        assertEquals("2.0", request.jsonrpc());
        assertEquals("test-789", request.id());
        assertEquals("tools/call", request.method());
        assertTrue(request.hasParams());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.params();
        assertEquals("echo", params.get("name"));
    }
    
    @Test
    void shouldCreateSuccessfulJsonRpcResponse() {
        // Given
        String id = "response-123";
        Map<String, Object> result = Map.of("status", "success", "data", "test");
        
        // When
        JsonRpcResponse response = JsonRpcResponse.success(id, result);
        
        // Then
        assertEquals("2.0", response.jsonrpc());
        assertEquals(id, response.id());
        assertEquals(result, response.result());
        assertNull(response.error());
        assertTrue(response.isSuccess());
        assertFalse(response.isError());
    }
    
    @Test
    void shouldCreateErrorJsonRpcResponse() {
        // Given
        String id = "error-456";
        JsonRpcError error = JsonRpcError.methodNotFound("unknown_method");
        
        // When
        JsonRpcResponse response = JsonRpcResponse.error(id, error);
        
        // Then
        assertEquals("2.0", response.jsonrpc());
        assertEquals(id, response.id());
        assertNull(response.result());
        assertEquals(error, response.error());
        assertFalse(response.isSuccess());
        assertTrue(response.isError());
    }
    
    @Test
    void shouldSerializeJsonRpcResponseToJson() throws Exception {
        // Given
        JsonRpcResponse response = JsonRpcResponse.success("1", Map.of("tools", "[]"));
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":\"1\""));
        assertTrue(json.contains("\"result\""));
        assertTrue(json.contains("\"error\":null") || !json.contains("\"error\""));
    }
    
    @Test
    void shouldDeserializeJsonRpcResponseFromJson() throws Exception {
        // Given
        String json = """
            {
                "jsonrpc": "2.0",
                "id": "test-response",
                "result": {
                    "tools": [
                        {"name": "echo", "description": "Echo tool"}
                    ]
                }
            }
            """;
        
        // When
        JsonRpcResponse response = objectMapper.readValue(json, JsonRpcResponse.class);
        
        // Then
        assertEquals("2.0", response.jsonrpc());
        assertEquals("test-response", response.id());
        assertNotNull(response.result());
        assertNull(response.error());
        assertTrue(response.isSuccess());
    }
    
    @Test
    void shouldCreateStandardJsonRpcErrors() {
        // Test parse error
        JsonRpcError parseError = JsonRpcError.parseError("Invalid JSON");
        assertEquals(JsonRpcError.PARSE_ERROR, parseError.code());
        assertEquals("Invalid JSON", parseError.message());
        assertTrue(parseError.isStandardError());
        
        // Test invalid request
        JsonRpcError invalidRequest = JsonRpcError.invalidRequest("Missing method");
        assertEquals(JsonRpcError.INVALID_REQUEST, invalidRequest.code());
        assertEquals("Missing method", invalidRequest.message());
        
        // Test method not found
        JsonRpcError methodNotFound = JsonRpcError.methodNotFound("unknown");
        assertEquals(JsonRpcError.METHOD_NOT_FOUND, methodNotFound.code());
        assertTrue(methodNotFound.message().contains("unknown"));
        
        // Test invalid params
        JsonRpcError invalidParams = JsonRpcError.invalidParams("Wrong type");
        assertEquals(JsonRpcError.INVALID_PARAMS, invalidParams.code());
        assertEquals("Wrong type", invalidParams.message());
        
        // Test internal error
        JsonRpcError internalError = JsonRpcError.internalError("Server error");
        assertEquals(JsonRpcError.INTERNAL_ERROR, internalError.code());
        assertEquals("Server error", internalError.message());
    }
    
    @Test
    void shouldCreateJsonRpcErrorWithData() {
        // Given
        Map<String, Object> errorData = Map.of("details", "Additional info");
        
        // When
        JsonRpcError error = JsonRpcError.create(-1000, "Custom error", errorData);
        
        // Then
        assertEquals(-1000, error.code());
        assertEquals("Custom error", error.message());
        assertEquals(errorData, error.data());
        assertTrue(error.hasData());
        assertFalse(error.isStandardError());
    }
    
    @Test
    void shouldSerializeJsonRpcErrorToJson() throws Exception {
        // Given
        JsonRpcError error = JsonRpcError.methodNotFound("test_method");
        JsonRpcResponse response = JsonRpcResponse.error("1", error);
        
        // When
        String json = objectMapper.writeValueAsString(response);
        
        // Then
        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("\"code\":" + JsonRpcError.METHOD_NOT_FOUND));
        assertTrue(json.contains("test_method"));
        assertTrue(json.contains("\"result\":null") || !json.contains("\"result\""));
    }
    
    @Test
    void shouldDeserializeJsonRpcErrorFromJson() throws Exception {
        // Given
        String json = """
            {
                "jsonrpc": "2.0",
                "id": "error-test",
                "error": {
                    "code": -32601,
                    "message": "Method not found: test_method",
                    "data": {"method": "test_method"}
                }
            }
            """;
        
        // When
        JsonRpcResponse response = objectMapper.readValue(json, JsonRpcResponse.class);
        
        // Then
        assertEquals("2.0", response.jsonrpc());
        assertEquals("error-test", response.id());
        assertNull(response.result());
        assertNotNull(response.error());
        assertTrue(response.isError());
        
        JsonRpcError error = response.error();
        assertEquals(JsonRpcError.METHOD_NOT_FOUND, error.code());
        assertTrue(error.message().contains("test_method"));
        assertTrue(error.hasData());
    }
}