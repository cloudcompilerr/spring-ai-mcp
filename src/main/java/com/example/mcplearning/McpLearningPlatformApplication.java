package com.example.mcplearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main application class for the MCP Learning Platform.
 * 
 * This Spring Boot application serves as an educational platform for learning
 * Model Context Protocol (MCP) implementation patterns. The application demonstrates
 * both single and multi-server MCP scenarios with comprehensive logging and examples.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class McpLearningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpLearningPlatformApplication.class, args);
    }
}