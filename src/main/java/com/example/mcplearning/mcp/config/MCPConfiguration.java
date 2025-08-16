package com.example.mcplearning.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.util.List;

/**
 * MCP Configuration Properties.
 * 
 * Main configuration class for the MCP Learning Platform, binding to
 * application properties with the "mcp" prefix. This class centralizes
 * all MCP-related configuration settings.
 */
@ConfigurationProperties(prefix = "mcp")
@Validated
public class MCPConfiguration {
    
    /**
     * List of MCP server configurations.
     */
    @NotNull(message = "Servers list is required (can be empty)")
    @Valid
    private List<MCPServerConfig> servers = List.of();
    
    /**
     * Whether to enable multi-server support.
     * When false, only the first enabled server will be used.
     */
    private boolean enableMultiServer = false;
    
    /**
     * Connection timeout for MCP server connections.
     */
    @NotNull(message = "Connection timeout is required")
    private Duration connectionTimeout = Duration.ofSeconds(30);
    
    /**
     * Maximum number of connection retry attempts.
     */
    @Min(value = 0, message = "Max retries must be non-negative")
    private int maxRetries = 3;
    
    /**
     * Whether to enable verbose logging for educational purposes.
     * When true, all MCP protocol messages will be logged.
     */
    private boolean verboseLogging = true;
    
    /**
     * Whether to enable interactive examples and demonstrations.
     */
    private boolean enableExamples = true;
    
    /**
     * Retry delay between connection attempts.
     */
    @NotNull(message = "Retry delay is required")
    private Duration retryDelay = Duration.ofSeconds(5);
    
    /**
     * Health check interval for server monitoring.
     */
    @NotNull(message = "Health check interval is required")
    private Duration healthCheckInterval = Duration.ofMinutes(1);
    
    /**
     * Reactive configuration settings.
     */
    @Valid
    private ReactiveConfig reactive = new ReactiveConfig();
    
    // Getters and setters
    
    public List<MCPServerConfig> getServers() {
        return servers;
    }
    
    public void setServers(List<MCPServerConfig> servers) {
        this.servers = servers != null ? servers : List.of();
    }
    
    public boolean isEnableMultiServer() {
        return enableMultiServer;
    }
    
    public void setEnableMultiServer(boolean enableMultiServer) {
        this.enableMultiServer = enableMultiServer;
    }
    
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }
    
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public boolean isVerboseLogging() {
        return verboseLogging;
    }
    
    public void setVerboseLogging(boolean verboseLogging) {
        this.verboseLogging = verboseLogging;
    }
    
    public boolean isEnableExamples() {
        return enableExamples;
    }
    
    public void setEnableExamples(boolean enableExamples) {
        this.enableExamples = enableExamples;
    }
    
    public Duration getRetryDelay() {
        return retryDelay;
    }
    
    public void setRetryDelay(Duration retryDelay) {
        this.retryDelay = retryDelay;
    }
    
    public Duration getHealthCheckInterval() {
        return healthCheckInterval;
    }
    
    public void setHealthCheckInterval(Duration healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }
    
    public ReactiveConfig getReactive() {
        return reactive;
    }
    
    public void setReactive(ReactiveConfig reactive) {
        this.reactive = reactive;
    }
    
    /**
     * Gets only the enabled server configurations.
     * 
     * @return List of enabled servers
     */
    public List<MCPServerConfig> getEnabledServers() {
        return servers.stream()
                .filter(MCPServerConfig::enabled)
                .toList();
    }
    
    /**
     * Gets the first enabled server configuration.
     * 
     * @return The first enabled server, or null if none are enabled
     */
    public MCPServerConfig getFirstEnabledServer() {
        return getEnabledServers().stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Checks if any servers are configured and enabled.
     * 
     * @return true if there are enabled servers, false otherwise
     */
    public boolean hasEnabledServers() {
        return !getEnabledServers().isEmpty();
    }
    
    /**
     * Gets the effective server list based on multi-server setting.
     * 
     * @return All enabled servers if multi-server is enabled, 
     *         or just the first enabled server otherwise
     */
    public List<MCPServerConfig> getEffectiveServers() {
        var enabledServers = getEnabledServers();
        if (enabledServers.isEmpty()) {
            return List.of();
        }
        
        if (enableMultiServer) {
            return enabledServers;
        } else {
            return List.of(enabledServers.get(0));
        }
    }
    
    /**
     * Reactive configuration settings for WebFlux integration.
     */
    public static class ReactiveConfig {
        
        /**
         * Whether to enable reactive MCP features.
         */
        private boolean enabled = true;
        
        /**
         * Buffer size for reactive streams.
         */
        @Min(value = 1, message = "Buffer size must be positive")
        private int bufferSize = 256;
        
        /**
         * Timeout for reactive operations.
         */
        @NotNull(message = "Reactive timeout is required")
        private Duration timeout = Duration.ofSeconds(30);
        
        /**
         * Whether to enable server-sent events.
         */
        private boolean enableServerSentEvents = true;
        
        /**
         * Interval for server-sent event heartbeats.
         */
        @NotNull(message = "SSE heartbeat interval is required")
        private Duration sseHeartbeatInterval = Duration.ofSeconds(30);
        
        // Getters and setters
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getBufferSize() {
            return bufferSize;
        }
        
        public void setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
        }
        
        public Duration getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
        
        public boolean isEnableServerSentEvents() {
            return enableServerSentEvents;
        }
        
        public void setEnableServerSentEvents(boolean enableServerSentEvents) {
            this.enableServerSentEvents = enableServerSentEvents;
        }
        
        public Duration getSseHeartbeatInterval() {
            return sseHeartbeatInterval;
        }
        
        public void setSseHeartbeatInterval(Duration sseHeartbeatInterval) {
            this.sseHeartbeatInterval = sseHeartbeatInterval;
        }
    }
}