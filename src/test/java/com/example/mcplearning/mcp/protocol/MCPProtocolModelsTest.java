package com.example.mcplearning.mcp.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MCP protocol models.
 * 
 * These tests verify that the MCP protocol records can be properly
 * serialized to/from JSON and that their factory methods work correctly.
 */
class MCPProtocolModelsTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testMCPInitializeRequestSerialization() throws Exception {
        // Given
        var clientInfo = MCPClientInfo.forLearningPlatform();
        var request = MCPInitializeRequest.withStandardVersion(clientInfo);
        
        // When
        String json = objectMapper.writeValueAsString(request);
        MCPInitializeRequest deserialized = objectMapper.readValue(json, MCPInitializeRequest.class);
        
        // Then
        assertThat(deserialized.protocolVersion()).isEqualTo("2024-11-05");
        assertThat(deserialized.clientInfo().name()).isEqualTo("MCP Learning Platform");
        assertThat(deserialized.clientInfo().version()).isEqualTo("1.0.0");
    }
    
    @Test
    void testMCPResponseFactoryMethods() {
        // Test success response
        var successResponse = MCPResponse.success("test-id", "result-data");
        assertThat(successResponse.isSuccess()).isTrue();
        assertThat(successResponse.isError()).isFalse();
        assertThat(successResponse.result()).isEqualTo("result-data");
        assertThat(successResponse.error()).isNull();
        
        // Test error response
        var error = MCPError.toolNotFound("test-tool");
        var errorResponse = MCPResponse.error("test-id", error);
        assertThat(errorResponse.isSuccess()).isFalse();
        assertThat(errorResponse.isError()).isTrue();
        assertThat(errorResponse.result()).isNull();
        assertThat(errorResponse.error()).isEqualTo(error);
    }
    
    @Test
    void testMCPErrorFactoryMethods() {
        // Test standard error codes
        var parseError = MCPError.parseError("Invalid JSON");
        assertThat(parseError.code()).isEqualTo(MCPError.PARSE_ERROR);
        assertThat(parseError.message()).isEqualTo("Invalid JSON");
        
        var toolNotFound = MCPError.toolNotFound("missing-tool");
        assertThat(toolNotFound.code()).isEqualTo(MCPError.TOOL_NOT_FOUND);
        assertThat(toolNotFound.message()).contains("missing-tool");
    }
    
    @Test
    void testMCPToolWithSchema() {
        // Given
        var schema = MCPToolInputSchema.withStringParameters(
            Map.of("path", "File path to read"),
            Map.of("encoding", "File encoding (optional)")
        );
        var tool = new MCPTool("file_read", "Read a file", schema);
        
        // Then
        assertThat(tool.hasRequiredParameters()).isTrue();
        assertThat(schema.hasRequiredProperties()).isTrue();
        assertThat(schema.getPropertyCount()).isEqualTo(2);
    }
    
    @Test
    void testMCPResourceFactoryMethods() {
        // Test different resource types
        var textResource = MCPResource.textResource("file:///tmp/test.txt", "Test File", "A test file");
        assertThat(textResource.isTextResource()).isTrue();
        assertThat(textResource.isJsonResource()).isFalse();
        assertThat(textResource.hasMimeType()).isTrue();
        
        var jsonResource = MCPResource.jsonResource("data://config.json", "Config", "Configuration data");
        assertThat(jsonResource.isJsonResource()).isTrue();
        assertThat(jsonResource.isTextResource()).isFalse();
        
        var genericResource = MCPResource.genericResource("custom://resource", "Custom", "Custom resource");
        assertThat(genericResource.hasMimeType()).isFalse();
    }
    
    @Test
    void testMCPToolResultTypes() {
        // Test success result
        var successResult = MCPToolResult.success("File content here");
        assertThat(successResult.isSuccess()).isTrue();
        assertThat(successResult.isError()).isFalse();
        assertThat(successResult.isTextResult()).isTrue();
        
        // Test JSON result
        var jsonResult = MCPToolResult.json("{\"key\": \"value\"}");
        assertThat(jsonResult.isJsonResult()).isTrue();
        assertThat(jsonResult.isTextResult()).isFalse();
        
        // Test error result
        var errorResult = MCPToolResult.error("File not found");
        assertThat(errorResult.isError()).isTrue();
        assertThat(errorResult.isSuccess()).isFalse();
    }
}