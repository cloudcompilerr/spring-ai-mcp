package com.example.mcplearning.mcp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * MCP Server Configuration.
 * 
 * Defines the configuration for connecting to an MCP server, including
 * the command to execute, arguments, environment variables, and other
 * connection settings.
 * 
 * @param id Unique identifier for this server configuration
 * @param name Human-readable name for the server
 * @param command The command to execute to start the MCP server
 * @param args List of command-line arguments for the server
 * @param env Environment variables to set for the server process
 * @param enabled Whether this server configuration is enabled
 */
public record MCPServerConfig(
    @JsonProperty("id")
    @NotBlank(message = "Server ID is required")
    String id,
    
    @JsonProperty("name")
    @NotBlank(message = "Server name is required")
    String name,
    
    @JsonProperty("command")
    @NotBlank(message = "Server command is required")
    String command,
    
    @JsonProperty("args")
    @NotNull(message = "Server args list is required (can be empty)")
    List<String> args,
    
    @JsonProperty("env")
    @NotNull(message = "Server env map is required (can be empty)")
    Map<String, String> env,
    
    @JsonProperty("enabled")
    boolean enabled
) {
    
    /**
     * Creates a simple server configuration with minimal settings.
     * 
     * @param id The server ID
     * @param name The server name
     * @param command The command to execute
     * @return A basic server configuration
     */
    public static MCPServerConfig simple(String id, String name, String command) {
        return new MCPServerConfig(id, name, command, List.of(), Map.of(), true);
    }
    
    /**
     * Creates a server configuration with command arguments.
     * 
     * @param id The server ID
     * @param name The server name
     * @param command The command to execute
     * @param args The command arguments
     * @return A server configuration with arguments
     */
    public static MCPServerConfig withArgs(String id, String name, String command, List<String> args) {
        return new MCPServerConfig(id, name, command, args, Map.of(), true);
    }
    
    /**
     * Creates a disabled server configuration.
     * 
     * @param id The server ID
     * @param name The server name
     * @param command The command to execute
     * @return A disabled server configuration
     */
    public static MCPServerConfig disabled(String id, String name, String command) {
        return new MCPServerConfig(id, name, command, List.of(), Map.of(), false);
    }
    
    /**
     * Creates a copy of this configuration with enabled status changed.
     * 
     * @param enabled The new enabled status
     * @return A new configuration with updated enabled status
     */
    public MCPServerConfig withEnabled(boolean enabled) {
        return new MCPServerConfig(id, name, command, args, env, enabled);
    }
    
    /**
     * Creates a copy of this configuration with additional environment variables.
     * 
     * @param additionalEnv Additional environment variables to add
     * @return A new configuration with merged environment variables
     */
    public MCPServerConfig withAdditionalEnv(Map<String, String> additionalEnv) {
        var mergedEnv = new java.util.HashMap<>(env);
        mergedEnv.putAll(additionalEnv);
        return new MCPServerConfig(id, name, command, args, mergedEnv, enabled);
    }
    
    /**
     * Gets the full command line as a string for logging purposes.
     * 
     * @return The command and arguments as a single string
     */
    public String getFullCommandLine() {
        if (args.isEmpty()) {
            return command;
        }
        return command + " " + String.join(" ", args);
    }
}