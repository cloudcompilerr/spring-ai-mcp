package com.example.mcplearning.educational;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * MCP Example Result.
 * 
 * Represents the expected result of running an MCP example,
 * including whether it should succeed or fail, and what
 * output or error message should be produced.
 * 
 * @param success Whether the example should succeed
 * @param message The main result message
 * @param details Additional details about the result
 * @param executionTimeMs Expected execution time in milliseconds (optional)
 */
public record MCPExampleResult(
    @JsonProperty("success")
    @NotNull(message = "Success status is required")
    Boolean success,
    
    @JsonProperty("message")
    @NotBlank(message = "Result message is required")
    String message,
    
    @JsonProperty("details")
    String details,
    
    @JsonProperty("executionTimeMs")
    Long executionTimeMs
) {
    
    /**
     * Creates a successful result.
     * 
     * @param message The success message
     * @param details Additional details about the success
     * @return A successful example result
     */
    public static MCPExampleResult success(String message, String details) {
        return new MCPExampleResult(true, message, details, null);
    }
    
    /**
     * Creates a successful result with execution time.
     * 
     * @param message The success message
     * @param details Additional details about the success
     * @param executionTimeMs The execution time in milliseconds
     * @return A successful example result with timing
     */
    public static MCPExampleResult success(String message, String details, long executionTimeMs) {
        return new MCPExampleResult(true, message, details, executionTimeMs);
    }
    
    /**
     * Creates an error result.
     * 
     * @param message The error message
     * @param details Additional details about the error
     * @return An error example result
     */
    public static MCPExampleResult error(String message, String details) {
        return new MCPExampleResult(false, message, details, null);
    }
    
    /**
     * Creates an error result with execution time.
     * 
     * @param message The error message
     * @param details Additional details about the error
     * @param executionTimeMs The execution time in milliseconds
     * @return An error example result with timing
     */
    public static MCPExampleResult error(String message, String details, long executionTimeMs) {
        return new MCPExampleResult(false, message, details, executionTimeMs);
    }
    
    /**
     * Creates a quick success result with minimal information.
     * 
     * @param message The success message
     * @return A simple successful result
     */
    public static MCPExampleResult quickSuccess(String message) {
        return success(message, null);
    }
    
    /**
     * Creates a quick error result with minimal information.
     * 
     * @param message The error message
     * @return A simple error result
     */
    public static MCPExampleResult quickError(String message) {
        return error(message, null);
    }
    
    /**
     * Checks if this result represents a successful operation.
     * 
     * @return true if the result is successful, false otherwise
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Checks if this result represents an error.
     * 
     * @return true if the result is an error, false otherwise
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isError() {
        return !success;
    }
    
    /**
     * Checks if execution time information is available.
     * 
     * @return true if execution time is specified, false otherwise
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean hasExecutionTime() {
        return executionTimeMs != null;
    }
    
    /**
     * Gets a formatted string representation of the result.
     * 
     * @return A formatted result string
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getFormattedResult() {
        var result = new StringBuilder();
        result.append(success ? "✓ SUCCESS" : "✗ ERROR");
        result.append(": ").append(message);
        
        if (details != null && !details.isBlank()) {
            result.append("\nDetails: ").append(details);
        }
        
        if (hasExecutionTime()) {
            result.append("\nExecution time: ").append(executionTimeMs).append("ms");
        }
        
        return result.toString();
    }
}