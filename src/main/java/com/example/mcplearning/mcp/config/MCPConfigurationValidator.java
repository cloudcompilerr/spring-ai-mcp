package com.example.mcplearning.mcp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration validator for MCP settings.
 * 
 * This component validates MCP configuration at application startup and provides
 * detailed error reporting for common configuration issues. It serves as an
 * educational tool to help users understand proper MCP configuration.
 */
@Component
public class MCPConfigurationValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPConfigurationValidator.class);
    
    private final MCPConfiguration mcpConfiguration;
    
    public MCPConfigurationValidator(MCPConfiguration mcpConfiguration) {
        this.mcpConfiguration = mcpConfiguration;
    }
    
    /**
     * Validates MCP configuration after application startup.
     * 
     * This method is called automatically when the application is ready,
     * providing immediate feedback about configuration issues.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void validateConfiguration() {
        logger.info("Validating MCP configuration...");
        
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        validateBasicSettings(warnings, errors);
        validateServerConfigurations(warnings, errors);
        validateReactiveSettings(warnings, errors);
        validateMultiServerSettings(warnings, errors);
        
        reportValidationResults(warnings, errors);
    }
    
    /**
     * Validates basic MCP settings.
     */
    private void validateBasicSettings(List<String> warnings, List<String> errors) {
        // Validate connection timeout
        if (mcpConfiguration.getConnectionTimeout().isNegative() || mcpConfiguration.getConnectionTimeout().isZero()) {
            errors.add("Connection timeout must be positive");
        } else if (mcpConfiguration.getConnectionTimeout().toSeconds() > 300) {
            warnings.add("Connection timeout is very long (" + mcpConfiguration.getConnectionTimeout().toSeconds() + "s). Consider reducing it for better user experience.");
        }
        
        // Validate retry settings
        if (mcpConfiguration.getMaxRetries() < 0) {
            errors.add("Max retries cannot be negative");
        } else if (mcpConfiguration.getMaxRetries() > 10) {
            warnings.add("Max retries is very high (" + mcpConfiguration.getMaxRetries() + "). This may cause long delays on connection failures.");
        }
        
        // Validate retry delay
        if (mcpConfiguration.getRetryDelay().isNegative()) {
            errors.add("Retry delay cannot be negative");
        }
        
        // Validate health check interval
        if (mcpConfiguration.getHealthCheckInterval().isNegative() || mcpConfiguration.getHealthCheckInterval().isZero()) {
            errors.add("Health check interval must be positive");
        } else if (mcpConfiguration.getHealthCheckInterval().toSeconds() < 10) {
            warnings.add("Health check interval is very short (" + mcpConfiguration.getHealthCheckInterval().toSeconds() + "s). This may cause excessive resource usage.");
        }
    }
    
    /**
     * Validates server configurations.
     */
    private void validateServerConfigurations(List<String> warnings, List<String> errors) {
        List<MCPServerConfig> servers = mcpConfiguration.getServers();
        
        if (servers == null || servers.isEmpty()) {
            warnings.add("No MCP servers configured. The application will run in demo mode only.");
            return;
        }
        
        // Check for duplicate server IDs
        Set<String> serverIds = servers.stream()
                .map(MCPServerConfig::id)
                .collect(Collectors.toSet());
        
        if (serverIds.size() != servers.size()) {
            errors.add("Duplicate server IDs found. Each server must have a unique ID.");
        }
        
        // Validate individual server configurations
        for (MCPServerConfig server : servers) {
            validateServerConfig(server, warnings, errors);
        }
        
        // Check if any servers are enabled
        long enabledCount = servers.stream()
                .filter(MCPServerConfig::enabled)
                .count();
        
        if (enabledCount == 0) {
            warnings.add("No MCP servers are enabled. Enable at least one server for full functionality.");
        }
    }
    
    /**
     * Validates an individual server configuration.
     */
    private void validateServerConfig(MCPServerConfig server, List<String> warnings, List<String> errors) {
        String serverContext = "Server '" + server.id() + "'";
        
        // Validate command exists (basic check)
        if (server.command().trim().isEmpty()) {
            errors.add(serverContext + ": Command cannot be empty");
        } else {
            // Check if command looks like a path and if it exists
            if (server.command().contains("/") || server.command().contains("\\")) {
                Path commandPath = Paths.get(server.command());
                if (!Files.exists(commandPath)) {
                    warnings.add(serverContext + ": Command path '" + server.command() + "' does not exist. Ensure the MCP server is installed.");
                } else if (!Files.isExecutable(commandPath)) {
                    warnings.add(serverContext + ": Command '" + server.command() + "' may not be executable.");
                }
            }
        }
        
        // Validate arguments
        if (server.args() == null) {
            errors.add(serverContext + ": Arguments list cannot be null");
        } else {
            for (String arg : server.args()) {
                if (arg == null || arg.trim().isEmpty()) {
                    warnings.add(serverContext + ": Empty or null argument found in args list");
                }
            }
        }
        
        // Validate environment variables
        if (server.env() == null) {
            errors.add(serverContext + ": Environment variables map cannot be null");
        } else {
            for (var entry : server.env().entrySet()) {
                if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                    warnings.add(serverContext + ": Empty environment variable name found");
                }
                if (entry.getValue() == null) {
                    warnings.add(serverContext + ": Null value for environment variable '" + entry.getKey() + "'");
                }
            }
        }
    }
    
    /**
     * Validates reactive settings.
     */
    private void validateReactiveSettings(List<String> warnings, List<String> errors) {
        MCPConfiguration.ReactiveConfig reactive = mcpConfiguration.getReactive();
        
        if (reactive == null) {
            errors.add("Reactive configuration cannot be null");
            return;
        }
        
        // Validate buffer size
        if (reactive.getBufferSize() <= 0) {
            errors.add("Reactive buffer size must be positive");
        } else if (reactive.getBufferSize() > 10000) {
            warnings.add("Reactive buffer size is very large (" + reactive.getBufferSize() + "). This may cause high memory usage.");
        }
        
        // Validate timeout
        if (reactive.getTimeout().isNegative() || reactive.getTimeout().isZero()) {
            errors.add("Reactive timeout must be positive");
        }
        
        // Validate SSE heartbeat interval
        if (reactive.getSseHeartbeatInterval().isNegative() || reactive.getSseHeartbeatInterval().isZero()) {
            errors.add("SSE heartbeat interval must be positive");
        } else if (reactive.getSseHeartbeatInterval().toSeconds() < 5) {
            warnings.add("SSE heartbeat interval is very short (" + reactive.getSseHeartbeatInterval().toSeconds() + "s). This may cause excessive network traffic.");
        }
    }
    
    /**
     * Validates multi-server specific settings.
     */
    private void validateMultiServerSettings(List<String> warnings, List<String> errors) {
        if (mcpConfiguration.isEnableMultiServer()) {
            long enabledServerCount = mcpConfiguration.getEnabledServers().size();
            
            if (enabledServerCount < 2) {
                warnings.add("Multi-server mode is enabled but only " + enabledServerCount + " server(s) are enabled. Consider enabling more servers or disabling multi-server mode.");
            }
            
            if (enabledServerCount > 10) {
                warnings.add("Very high number of enabled servers (" + enabledServerCount + ") in multi-server mode. This may impact performance.");
            }
        }
    }
    
    /**
     * Reports validation results to the user.
     */
    private void reportValidationResults(List<String> warnings, List<String> errors) {
        if (errors.isEmpty() && warnings.isEmpty()) {
            logger.info("✅ MCP configuration validation passed with no issues");
            return;
        }
        
        if (!errors.isEmpty()) {
            logger.error("❌ MCP configuration validation failed with {} error(s):", errors.size());
            for (int i = 0; i < errors.size(); i++) {
                logger.error("  {}. {}", i + 1, errors.get(i));
            }
        }
        
        if (!warnings.isEmpty()) {
            logger.warn("⚠️  MCP configuration validation found {} warning(s):", warnings.size());
            for (int i = 0; i < warnings.size(); i++) {
                logger.warn("  {}. {}", i + 1, warnings.get(i));
            }
        }
        
        if (!errors.isEmpty()) {
            logger.error("Please fix the configuration errors before proceeding. See the MCP Learning Platform documentation for configuration examples.");
        } else {
            logger.info("MCP configuration validation completed with warnings. The application should work correctly, but consider addressing the warnings for optimal performance.");
        }
    }
    
    /**
     * Validates configuration programmatically (for testing purposes).
     * 
     * @return ValidationResult containing errors and warnings
     */
    public ValidationResult validateConfigurationProgrammatically() {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        validateBasicSettings(warnings, errors);
        validateServerConfigurations(warnings, errors);
        validateReactiveSettings(warnings, errors);
        validateMultiServerSettings(warnings, errors);
        
        return new ValidationResult(errors, warnings);
    }
    
    /**
     * Result of configuration validation.
     */
    public record ValidationResult(List<String> errors, List<String> warnings) {
        
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        public boolean isValid() {
            return !hasErrors();
        }
    }
}