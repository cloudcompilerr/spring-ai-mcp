package com.example.mcplearning.educational;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for MCPLearningController.
 * 
 * This test class validates the REST API endpoints for the MCP learning platform,
 * ensuring proper request handling, response formatting, and error management.
 * It serves as both a test suite and documentation for the API behavior.
 */
@ExtendWith(MockitoExtension.class)
class MCPLearningControllerTest {
    
    @Mock
    private MCPDemonstrator mockDemonstrator;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MCPLearningController controller = new MCPLearningController(mockDemonstrator);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }
    
    @Test
    void shouldReturnStatusSuccessfully() throws Exception {
        // Given: A demonstrator with status
        MCPExampleResult status = MCPExampleResult.success("System ready", "All systems operational");
        when(mockDemonstrator.getDemonstrationStatus()).thenReturn(status);
        
        // When: Requesting status
        // Then: Should return status information
        mockMvc.perform(get("/api/mcp/status"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("System ready"))
            .andExpect(jsonPath("$.details").value("All systems operational"));
    }
    
    @Test
    void shouldReturnAvailableExamples() throws Exception {
        // Given: A demonstrator with examples
        List<MCPExample> examples = List.of(
            MCPExample.basicConnection(),
            MCPExample.toolListing()
        );
        when(mockDemonstrator.getAvailableExamples()).thenReturn(examples);
        
        // When: Requesting examples
        // Then: Should return example list
        mockMvc.perform(get("/api/mcp/examples"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Basic MCP Server Connection"))
            .andExpect(jsonPath("$[1].title").value("List Available Tools"));
    }
    
    @Test
    void shouldFilterExamplesByCategory() throws Exception {
        // Given: A demonstrator with categorized examples
        List<MCPExample> allExamples = List.of(
            MCPExample.basicConnection(),
            MCPExample.toolListing(),
            MCPExample.errorHandling()
        );
        when(mockDemonstrator.getAvailableExamples()).thenReturn(allExamples);
        
        // When: Requesting examples by category
        // Then: Should return filtered examples
        mockMvc.perform(get("/api/mcp/examples/category/Tools"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("List Available Tools"));
    }
    
    @Test
    void shouldHandleSingleServerDemonstration() throws Exception {
        // Given: A demonstrator that can perform single server demo
        MCPExampleResult result = MCPExampleResult.success("Demo completed", "Single server demo successful");
        when(mockDemonstrator.demonstrateSingleServer()).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting single server demonstration
        // Then: Should return demonstration result
        mockMvc.perform(post("/api/mcp/examples/single-server"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Demo completed"));
    }
    
    @Test
    void shouldHandleMultiServerDemonstration() throws Exception {
        // Given: A demonstrator that can perform multi-server demo
        MCPExampleResult result = MCPExampleResult.success("Multi-demo completed", "Multi-server demo successful");
        when(mockDemonstrator.demonstrateMultiServer()).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting multi-server demonstration
        // Then: Should return demonstration result
        mockMvc.perform(post("/api/mcp/examples/multi-server"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Multi-demo completed"));
    }
    
    @Test
    void shouldHandleToolExecutionDemonstration() throws Exception {
        // Given: A demonstrator that can perform tool execution demo
        MCPExampleResult result = MCPExampleResult.success("Tool demo completed", "Tool execution successful");
        when(mockDemonstrator.demonstrateToolExecution()).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting tool execution demonstration
        // Then: Should return demonstration result
        mockMvc.perform(post("/api/mcp/examples/tool-execution"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Tool demo completed"));
    }
    
    @Test
    void shouldHandleErrorHandlingDemonstration() throws Exception {
        // Given: A demonstrator that can perform error handling demo
        MCPExampleResult result = MCPExampleResult.success("Error demo completed", "Error handling demonstrated");
        when(mockDemonstrator.demonstrateErrorHandling()).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting error handling demonstration
        // Then: Should return demonstration result
        mockMvc.perform(post("/api/mcp/examples/error-handling"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Error demo completed"));
    }
    
    @Test
    void shouldHandleResourceAccessDemonstration() throws Exception {
        // Given: A demonstrator that can perform resource access demo
        MCPExampleResult result = MCPExampleResult.success("Resource demo completed", "Resource access demonstrated");
        when(mockDemonstrator.demonstrateResourceAccess()).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting resource access demonstration
        // Then: Should return demonstration result
        mockMvc.perform(post("/api/mcp/examples/resource-access"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Resource demo completed"));
    }
    
    @Test
    void shouldSwitchModeSuccessfully() throws Exception {
        // Given: A demonstrator that can switch modes
        MCPExampleResult result = MCPExampleResult.success("Mode switched", "Successfully switched to multi-server mode");
        when(mockDemonstrator.switchMode(any())).thenReturn(CompletableFuture.completedFuture(result));
        
        // When: Requesting mode switch
        String requestBody = objectMapper.writeValueAsString(
            new MCPLearningController.ModeSwitchRequest("MULTI_SERVER")
        );
        
        // Then: Should return mode switch result
        mockMvc.perform(post("/api/mcp/mode/switch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Mode switched"));
    }
    
    @Test
    void shouldRejectInvalidModeSwitch() throws Exception {
        // Given: An invalid mode request
        String requestBody = objectMapper.writeValueAsString(
            new MCPLearningController.ModeSwitchRequest("INVALID_MODE")
        );
        
        // When: Requesting invalid mode switch
        // Then: Should return bad request
        mockMvc.perform(post("/api/mcp/mode/switch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid mode"));
    }
    
    @Test
    void shouldReturnCurrentMode() throws Exception {
        // Given: A demonstrator with current mode
        when(mockDemonstrator.getCurrentMode()).thenReturn(MCPDemonstrator.DemonstrationMode.SINGLE_SERVER);
        
        // When: Requesting current mode
        // Then: Should return mode information
        mockMvc.perform(get("/api/mcp/mode/current"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name").value("SINGLE_SERVER"))
            .andExpect(jsonPath("$.displayName").value("Single Server Mode"))
            .andExpect(jsonPath("$.description").exists());
    }
    
    @Test
    void shouldReturnAvailableModes() throws Exception {
        // When: Requesting available modes
        // Then: Should return all available modes
        mockMvc.perform(get("/api/mcp/mode/available"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("SINGLE_SERVER"))
            .andExpect(jsonPath("$[1].name").value("MULTI_SERVER"));
    }
    
    @Test
    void shouldHandleToolCallRequest() throws Exception {
        // Given: A tool call request
        MCPLearningController.ToolCallRequest request = new MCPLearningController.ToolCallRequest(
            "test_tool", Map.of("param1", "value1")
        );
        String requestBody = objectMapper.writeValueAsString(request);
        
        // When: Requesting tool execution
        // Then: Should handle gracefully (may return error due to no client)
        mockMvc.perform(post("/api/mcp/examples/tool-call")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void shouldReturnToolsList() throws Exception {
        // When: Requesting tools list
        // Then: Should return tools information
        mockMvc.perform(get("/api/mcp/tools"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void shouldReturnResourcesList() throws Exception {
        // When: Requesting resources list
        // Then: Should return resources information
        mockMvc.perform(get("/api/mcp/resources"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }
    
    @Test
    void shouldHandleDemonstrationFailures() throws Exception {
        // Given: A demonstrator that fails
        CompletableFuture<MCPExampleResult> failedFuture = CompletableFuture.failedFuture(
            new RuntimeException("Demo failed")
        );
        when(mockDemonstrator.demonstrateSingleServer()).thenReturn(failedFuture);
        
        // When: Requesting demonstration that fails
        // Then: Should return internal server error
        mockMvc.perform(post("/api/mcp/examples/single-server"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Single server demonstration failed"));
    }
}