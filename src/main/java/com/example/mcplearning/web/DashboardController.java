package com.example.mcplearning.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.mcplearning.mcp.config.MCPConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Web dashboard controller for the MCP Learning Platform.
 * 
 * Provides a simple web interface for exploring MCP functionality
 * and accessing educational resources.
 */
@Controller
public class DashboardController {
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private MCPConfiguration mcpConfiguration;
    
    /**
     * Main dashboard page - provides HTML interface
     */
    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("title", "MCP Learning Platform");
        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("multiServerEnabled", mcpConfiguration.isEnableMultiServer());
        model.addAttribute("serverCount", mcpConfiguration.getEffectiveServers().size());
        model.addAttribute("verboseLogging", mcpConfiguration.isVerboseLogging());
        model.addAttribute("examplesEnabled", mcpConfiguration.isEnableExamples());
        
        return "dashboard";
    }
    
    /**
     * API endpoint for dashboard data
     */
    @GetMapping("/api/dashboard")
    @ResponseBody
    public Map<String, Object> dashboardData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("applicationName", "MCP Learning Platform");
        data.put("timestamp", LocalDateTime.now());
        data.put("status", "running");
        
        // Configuration information
        Map<String, Object> config = new HashMap<>();
        config.put("multiServerEnabled", mcpConfiguration.isEnableMultiServer());
        config.put("serverCount", mcpConfiguration.getEffectiveServers().size());
        config.put("verboseLogging", mcpConfiguration.isVerboseLogging());
        config.put("examplesEnabled", mcpConfiguration.isEnableExamples());
        config.put("connectionTimeout", mcpConfiguration.getConnectionTimeout().toString());
        config.put("maxRetries", mcpConfiguration.getMaxRetries());
        data.put("configuration", config);
        
        // Server information
        data.put("servers", mcpConfiguration.getEffectiveServers().stream()
            .map(server -> Map.of(
                "id", server.id(),
                "name", server.name(),
                "command", server.command(),
                "enabled", server.enabled()
            ))
            .toList());
        
        // Quick links
        Map<String, String> links = new HashMap<>();
        links.put("examples", "/api/mcp/examples");
        links.put("status", "/api/mcp/status");
        links.put("config", "/api/mcp/config");
        links.put("health", "/actuator/health");
        links.put("documentation", "/api/mcp/config/documentation");
        data.put("links", links);
        
        return data;
    }
}