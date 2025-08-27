package com.example.mcplearning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPEducationalConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Main application class for the MCP Learning Platform.
 * 
 * EDUCATIONAL OVERVIEW:
 * This Spring Boot application serves as a comprehensive educational platform for learning
 * Model Context Protocol (MCP) implementation patterns. The application demonstrates:
 * 
 * - Single and multi-server MCP scenarios
 * - Tool discovery and execution patterns
 * - Resource access and management
 * - Error handling and recovery strategies
 * - Real-time monitoring and health checks
 * - Interactive learning modules
 * 
 * ARCHITECTURE:
 * The application uses a layered architecture with clear separation of concerns:
 * - Web Layer: REST controllers for API access
 * - Service Layer: Business logic and MCP operations
 * - Transport Layer: MCP protocol communication
 * - Configuration Layer: Externalized configuration management
 * 
 * LEARNING OBJECTIVES:
 * - Understand MCP protocol fundamentals
 * - Learn multi-server coordination patterns
 * - Master error handling and resilience
 * - Explore reactive programming with MCP
 * - Practice configuration management
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties({MCPConfiguration.class, MCPEducationalConfiguration.class})
public class McpLearningPlatformApplication {

    private static final Logger logger = LoggerFactory.getLogger(McpLearningPlatformApplication.class);

    public static void main(String[] args) {
        try {
            // Configure system properties for better performance
            System.setProperty("spring.output.ansi.enabled", "always");
            System.setProperty("logging.pattern.console", 
                "%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} " +
                "%clr([%15.15t]){faint} " +
                "%clr(%-5level) " +
                "%clr([%logger{36}]){cyan} " +
                "%clr(-){faint} " +
                "%m%n%wEx");

            logger.info("🚀 Starting MCP Learning Platform...");
            logger.info("📚 Educational MCP implementation with multi-server support");
            
            SpringApplication app = new SpringApplication(McpLearningPlatformApplication.class);
            
            // Add custom banner
            app.setBanner((environment, sourceClass, out) -> {
                out.println();
                out.println("  __  __  ____  ____    _                          _             ");
                out.println(" |  \\/  |/ ___||  _ \\  | |    ___  __ _ _ __ _ __ (_)_ __   __ _ ");
                out.println(" | |\\/| | |    | |_) | | |   / _ \\/ _` | '__| '_ \\| | '_ \\ / _` |");
                out.println(" | |  | | |___ |  __/  | |__|  __/ (_| | |  | | | | | | | | (_| |");
                out.println(" |_|  |_|\\____||_|     |_____\\___|\\__,_|_|  |_| |_|_|_| |_|\\__, |");
                out.println("                                                          |___/ ");
                out.println("  ____  _       _    __                      ");
                out.println(" |  _ \\| | __ _| |_ / _| ___  _ __ _ __ ___   ");
                out.println(" | |_) | |/ _` | __| |_ / _ \\| '__| '_ ` _ \\  ");
                out.println(" |  __/| | (_| | |_|  _| (_) | |  | | | | | | ");
                out.println(" |_|   |_|\\__,_|\\__|_|  \\___/|_|  |_| |_| |_| ");
                out.println();
                out.println(" :: Model Context Protocol Learning Platform :: ");
                out.println(" :: Educational Multi-Server MCP Implementation :: ");
                out.println();
            });
            
            app.run(args);
            
        } catch (Exception e) {
            logger.error("❌ Failed to start MCP Learning Platform", e);
            System.exit(1);
        }
    }

    /**
     * Application startup event listener that provides educational information
     * and validates the application configuration.
     */
    @Component
    public static class ApplicationStartupListener {
        
        private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
        
        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationReady(ApplicationReadyEvent event) {
            Environment env = event.getApplicationContext().getEnvironment();
            
            try {
                String serverPort = env.getProperty("server.port", "8080");
                String hostAddress = InetAddress.getLocalHost().getHostAddress();
                String hostName = InetAddress.getLocalHost().getHostName();
                
                logger.info("🎉 MCP Learning Platform started successfully!");
                logger.info("🌐 Application is running at:");
                logger.info("   Local:    http://localhost:{}", serverPort);
                logger.info("   Network:  http://{}:{}", hostAddress, serverPort);
                logger.info("   Host:     http://{}:{}", hostName, serverPort);
                logger.info("");
                logger.info("📋 Available Endpoints:");
                logger.info("   Dashboard:     http://localhost:{}/", serverPort);
                logger.info("   API Docs:      http://localhost:{}/api/mcp", serverPort);
                logger.info("   Health Check:  http://localhost:{}/actuator/health", serverPort);
                logger.info("   Examples:      http://localhost:{}/api/mcp/examples", serverPort);
                logger.info("   Configuration: http://localhost:{}/api/mcp/config", serverPort);
                logger.info("");
                
                // Display configuration information
                boolean multiServerEnabled = env.getProperty("mcp.enable-multi-server", Boolean.class, false);
                boolean verboseLogging = env.getProperty("mcp.verbose-logging", Boolean.class, true);
                boolean examplesEnabled = env.getProperty("mcp.enable-examples", Boolean.class, true);
                
                logger.info("⚙️ Current Configuration:");
                logger.info("   Multi-Server Mode: {}", multiServerEnabled ? "✅ Enabled" : "❌ Disabled");
                logger.info("   Verbose Logging:   {}", verboseLogging ? "✅ Enabled" : "❌ Disabled");
                logger.info("   Examples:          {}", examplesEnabled ? "✅ Enabled" : "❌ Disabled");
                logger.info("   Profile:           {}", String.join(", ", env.getActiveProfiles()));
                logger.info("");
                
                if (!multiServerEnabled) {
                    logger.info("💡 To enable multi-server mode, restart with:");
                    logger.info("   mvn spring-boot:run -Dspring.profiles.active=multiserver");
                    logger.info("");
                }
                
                logger.info("📚 Learning Resources:");
                logger.info("   README:            ./README.md");
                logger.info("   Configuration:     ./src/main/resources/application.yml");
                logger.info("   Examples:          ./src/test/java/.../documentation/");
                logger.info("   API Documentation: http://localhost:{}/api/mcp/config/documentation", serverPort);
                logger.info("");
                
                logger.info("🚀 Ready for MCP learning and experimentation!");
                
            } catch (UnknownHostException e) {
                logger.warn("Could not determine host information", e);
            }
        }
    }
}