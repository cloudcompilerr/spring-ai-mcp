package com.example.mcplearning.educational;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MCP educational models.
 * 
 * These tests verify that educational examples and results work correctly
 * and can be properly serialized for API responses.
 */
class MCPEducationalModelsTest {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    void testMCPExampleFactoryMethods() {
        // Test basic connection example
        var connectionExample = MCPExample.basicConnection();
        assertThat(connectionExample.title()).contains("Connection");
        assertThat(connectionExample.getCategory()).isEqualTo("Connection");
        assertThat(connectionExample.isErrorExample()).isFalse();
        
        // Test tool listing example
        var toolExample = MCPExample.toolListing();
        assertThat(toolExample.getCategory()).isEqualTo("Tools");
        assertThat(toolExample.isErrorExample()).isFalse();
        
        // Test error handling example
        var errorExample = MCPExample.errorHandling();
        assertThat(errorExample.getCategory()).isEqualTo("Error Handling");
        assertThat(errorExample.isErrorExample()).isTrue();
    }
    
    @Test
    void testMCPExampleResultFactoryMethods() {
        // Test success results
        var successResult = MCPExampleResult.success("Operation completed", "All tools loaded");
        assertThat(successResult.isSuccess()).isTrue();
        assertThat(successResult.isError()).isFalse();
        assertThat(successResult.hasExecutionTime()).isFalse();
        
        var successWithTime = MCPExampleResult.success("Operation completed", "All tools loaded", 150L);
        assertThat(successWithTime.hasExecutionTime()).isTrue();
        assertThat(successWithTime.executionTimeMs()).isEqualTo(150L);
        
        // Test error results
        var errorResult = MCPExampleResult.error("Connection failed", "Server unreachable");
        assertThat(errorResult.isError()).isTrue();
        assertThat(errorResult.isSuccess()).isFalse();
        
        // Test quick methods
        var quickSuccess = MCPExampleResult.quickSuccess("Done");
        assertThat(quickSuccess.details()).isNull();
        
        var quickError = MCPExampleResult.quickError("Failed");
        assertThat(quickError.details()).isNull();
    }
    
    @Test
    void testMCPExampleResultFormatting() {
        // Test success formatting
        var successResult = MCPExampleResult.success("Operation completed", "Details here", 100L);
        String formatted = successResult.getFormattedResult();
        
        assertThat(formatted).contains("✓ SUCCESS");
        assertThat(formatted).contains("Operation completed");
        assertThat(formatted).contains("Details: Details here");
        assertThat(formatted).contains("Execution time: 100ms");
        
        // Test error formatting
        var errorResult = MCPExampleResult.error("Operation failed", "Error details");
        String errorFormatted = errorResult.getFormattedResult();
        
        assertThat(errorFormatted).contains("✗ ERROR");
        assertThat(errorFormatted).contains("Operation failed");
        assertThat(errorFormatted).contains("Details: Error details");
    }
    
    @Test
    void testMCPExampleSerialization() throws Exception {
        // Given
        var example = MCPExample.basicConnection();
        
        // When
        String json = objectMapper.writeValueAsString(example);
        MCPExample deserialized = objectMapper.readValue(json, MCPExample.class);
        
        // Then
        assertThat(deserialized.title()).isEqualTo(example.title());
        assertThat(deserialized.description()).isEqualTo(example.description());
        assertThat(deserialized.code()).isEqualTo(example.code());
        assertThat(deserialized.expectedResult().isSuccess()).isEqualTo(example.expectedResult().isSuccess());
    }
    
    @Test
    void testMCPExampleResultSerialization() throws Exception {
        // Given
        var result = MCPExampleResult.success("Test message", "Test details", 250L);
        
        // When
        String json = objectMapper.writeValueAsString(result);
        MCPExampleResult deserialized = objectMapper.readValue(json, MCPExampleResult.class);
        
        // Then
        assertThat(deserialized.success()).isEqualTo(result.success());
        assertThat(deserialized.message()).isEqualTo(result.message());
        assertThat(deserialized.details()).isEqualTo(result.details());
        assertThat(deserialized.executionTimeMs()).isEqualTo(result.executionTimeMs());
    }
    
    @Test
    void testMCPExampleCategorization() {
        // Test different categories
        var connectionExample = new MCPExample("Basic Connection", "desc", "code", 
            MCPExampleResult.quickSuccess("ok"));
        assertThat(connectionExample.getCategory()).isEqualTo("Connection");
        
        var toolExample = new MCPExample("Tool Execution", "desc", "code", 
            MCPExampleResult.quickSuccess("ok"));
        assertThat(toolExample.getCategory()).isEqualTo("Tools");
        
        var resourceExample = new MCPExample("Resource Access", "desc", "code", 
            MCPExampleResult.quickSuccess("ok"));
        assertThat(resourceExample.getCategory()).isEqualTo("Resources");
        
        var errorExample = new MCPExample("Error Handling", "desc", "code", 
            MCPExampleResult.quickError("error"));
        assertThat(errorExample.getCategory()).isEqualTo("Error Handling");
        
        var generalExample = new MCPExample("Something Else", "desc", "code", 
            MCPExampleResult.quickSuccess("ok"));
        assertThat(generalExample.getCategory()).isEqualTo("General");
    }
}