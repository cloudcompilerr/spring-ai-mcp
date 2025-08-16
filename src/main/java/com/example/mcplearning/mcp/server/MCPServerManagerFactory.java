package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Factory configuration for creating the appropriate MCP Server Manager
 * based on the application configuration.
 * 
 * This factory demonstrates the educational concept of conditional bean
 * creation in Spring Boot, automatically selecting between single and
 * multi-server implementations based on configuration properties.
 * 
 * Key educational concepts:
 * - Conditional bean creation with @ConditionalOnProperty
 * - Factory pattern implementation in Spring
 * - Configuration-driven architecture selection
 */
@Configuration
public class MCPServerManagerFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(MCPServerManagerFactory.class);
    
    /**
     * Creates the primary MCP Server Manager bean.
     * 
     * This method provides a fallback single-server manager when multi-server
     * support is not enabled. The @ConditionalOnMissingBean ensures this is
     * only created if no other MCPServerManager bean exists.
     * 
     * @param configuration The MCP configuration
     * @param objectMapper The JSON object mapper
     * @return A single-server manager instance
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(MCPServerManager.class)
    public MCPServerManager mcpServerManager(MCPConfiguration configuration, ObjectMapper objectMapper) {
        if (configuration.isEnableMultiServer()) {
            logger.info("Creating MultiMCPServerManager (multi-server support enabled)");
            return new MultiMCPServerManager(configuration, objectMapper);
        } else {
            logger.info("Creating SingleMCPServerManager (single-server mode)");
            return new SingleMCPServerManager(configuration, objectMapper);
        }
    }
    
    /**
     * Creates a dedicated single-server manager for educational purposes.
     * 
     * This bean is always available for demonstration and testing purposes,
     * regardless of the multi-server configuration setting.
     * 
     * @param configuration The MCP configuration
     * @param objectMapper The JSON object mapper
     * @return A single-server manager instance
     */
    @Bean("singleMCPServerManager")
    public SingleMCPServerManager singleMCPServerManager(MCPConfiguration configuration, ObjectMapper objectMapper) {
        return new SingleMCPServerManager(configuration, objectMapper);
    }
}