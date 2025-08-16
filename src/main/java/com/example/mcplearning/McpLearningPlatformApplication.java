package com.example.mcplearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPEducationalConfiguration;

/**
 * Main application class for the MCP Learning Platform.
 * 
 * This Spring Boot application serves as an educational platform for learning
 * Model Context Protocol (MCP) implementation patterns. The application demonstrates
 * both single and multi-server MCP scenarios with comprehensive logging and examples.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties({MCPConfiguration.class, MCPEducationalConfiguration.class})
public class McpLearningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpLearningPlatformApplication.class, args);
    }
}