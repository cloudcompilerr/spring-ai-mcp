package com.example.mcplearning.mcp.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for MCP configuration information.
 * 
 * This controller provides endpoints to view and validate MCP configuration,
 * serving as both a utility for administrators and an educational tool for
 * understanding configuration structure.
 */
@RestController
@RequestMapping("/api/mcp/config")
public class MCPConfigurationController {
    
    private final MCPConfiguration mcpConfiguration;
    private final MCPEducationalConfiguration educationalConfiguration;
    private final MCPConfigurationValidator configurationValidator;
    private final MCPConfigurationDocumentationGenerator documentationGenerator;
    
    public MCPConfigurationController(
            MCPConfiguration mcpConfiguration,
            MCPEducationalConfiguration educationalConfiguration,
            MCPConfigurationValidator configurationValidator,
            MCPConfigurationDocumentationGenerator documentationGenerator) {
        this.mcpConfiguration = mcpConfiguration;
        this.educationalConfiguration = educationalConfiguration;
        this.configurationValidator = configurationValidator;
        this.documentationGenerator = documentationGenerator;
    }
    
    /**
     * Gets the current MCP configuration.
     * 
     * @return Current MCP configuration
     */
    @GetMapping
    public ResponseEntity<MCPConfiguration> getConfiguration() {
        return ResponseEntity.ok(mcpConfiguration);
    }
    
    /**
     * Gets the current educational configuration.
     * 
     * @return Current educational configuration
     */
    @GetMapping("/educational")
    public ResponseEntity<MCPEducationalConfiguration> getEducationalConfiguration() {
        return ResponseEntity.ok(educationalConfiguration);
    }
    
    /**
     * Validates the current configuration.
     * 
     * @return Configuration validation result
     */
    @GetMapping("/validate")
    public ResponseEntity<MCPConfigurationValidator.ValidationResult> validateConfiguration() {
        MCPConfigurationValidator.ValidationResult result = 
            configurationValidator.validateConfigurationProgrammatically();
        return ResponseEntity.ok(result);
    }
    
    /**
     * Gets configuration summary information.
     * 
     * @return Configuration summary
     */
    @GetMapping("/summary")
    public ResponseEntity<ConfigurationSummary> getConfigurationSummary() {
        ConfigurationSummary summary = new ConfigurationSummary(
            mcpConfiguration.getServers().size(),
            mcpConfiguration.getEnabledServers().size(),
            mcpConfiguration.isEnableMultiServer(),
            mcpConfiguration.isVerboseLogging(),
            mcpConfiguration.isEnableExamples(),
            mcpConfiguration.getReactive().isEnabled(),
            educationalConfiguration.isEnableDemonstrations(),
            educationalConfiguration.isEnableInteractiveExamples()
        );
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Gets effective server configurations based on current settings.
     * 
     * @return Effective server configurations
     */
    @GetMapping("/servers/effective")
    public ResponseEntity<Map<String, Object>> getEffectiveServers() {
        var effectiveServers = mcpConfiguration.getEffectiveServers();
        var response = Map.of(
            "multiServerEnabled", mcpConfiguration.isEnableMultiServer(),
            "totalServers", mcpConfiguration.getServers().size(),
            "enabledServers", mcpConfiguration.getEnabledServers().size(),
            "effectiveServers", effectiveServers.size(),
            "servers", effectiveServers
        );
        return ResponseEntity.ok(response);
    }
    
    /**
     * Gets configuration documentation in markdown format.
     * 
     * @return Configuration documentation
     */
    @GetMapping("/documentation")
    public ResponseEntity<String> getConfigurationDocumentation() {
        String documentation = documentationGenerator.generateConfigurationDocumentation();
        return ResponseEntity.ok(documentation);
    }
    
    /**
     * Gets example configuration in YAML format.
     * 
     * @return Example configuration
     */
    @GetMapping("/example")
    public ResponseEntity<String> getExampleConfiguration() {
        String example = documentationGenerator.generateExampleConfiguration();
        return ResponseEntity.ok(example);
    }
    
    /**
     * Configuration summary record.
     */
    public record ConfigurationSummary(
        int totalServers,
        int enabledServers,
        boolean multiServerEnabled,
        boolean verboseLogging,
        boolean examplesEnabled,
        boolean reactiveEnabled,
        boolean demonstrationsEnabled,
        boolean interactiveExamplesEnabled
    ) {}
}