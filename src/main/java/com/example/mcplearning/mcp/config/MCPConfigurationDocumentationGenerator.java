package com.example.mcplearning.mcp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates documentation for MCP configuration properties.
 * 
 * This component can generate markdown documentation for all configuration
 * properties, making it easier to maintain up-to-date configuration guides.
 */
@Component
public class MCPConfigurationDocumentationGenerator {
    
    /**
     * Generates markdown documentation for MCP configuration properties.
     * 
     * @return Markdown documentation string
     */
    public String generateConfigurationDocumentation() {
        StringBuilder doc = new StringBuilder();
        
        doc.append("# MCP Configuration Properties Reference\n\n");
        doc.append("This document is auto-generated from the configuration classes.\n\n");
        
        // Generate documentation for main MCP configuration
        doc.append("## Core MCP Configuration (`mcp.*`)\n\n");
        doc.append(generateClassDocumentation(MCPConfiguration.class, "mcp"));
        
        // Generate documentation for educational configuration
        doc.append("\n## Educational Configuration (`mcp.educational.*`)\n\n");
        doc.append(generateClassDocumentation(MCPEducationalConfiguration.class, "mcp.educational"));
        
        // Generate documentation for server configuration
        doc.append("\n## Server Configuration (`mcp.servers[]`)\n\n");
        doc.append(generateClassDocumentation(MCPServerConfig.class, "mcp.servers[]"));
        
        return doc.toString();
    }
    
    /**
     * Generates documentation for a specific configuration class.
     */
    private String generateClassDocumentation(Class<?> configClass, String prefix) {
        StringBuilder doc = new StringBuilder();
        
        // Get configuration properties annotation
        ConfigurationProperties annotation = configClass.getAnnotation(ConfigurationProperties.class);
        if (annotation != null) {
            doc.append("**Configuration Prefix:** `").append(annotation.prefix()).append("`\n\n");
        }
        
        // Generate table header
        doc.append("| Property | Type | Default | Description |\n");
        doc.append("|----------|------|---------|-------------|\n");
        
        // Process fields
        List<PropertyInfo> properties = extractProperties(configClass, prefix);
        for (PropertyInfo property : properties) {
            doc.append("| `").append(property.name()).append("` ");
            doc.append("| ").append(property.type()).append(" ");
            doc.append("| ").append(property.defaultValue()).append(" ");
            doc.append("| ").append(property.description()).append(" |\n");
        }
        
        return doc.toString();
    }
    
    /**
     * Extracts property information from a configuration class.
     */
    private List<PropertyInfo> extractProperties(Class<?> configClass, String prefix) {
        List<PropertyInfo> properties = new ArrayList<>();
        
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            if (isConfigurationProperty(field)) {
                PropertyInfo property = createPropertyInfo(field, prefix);
                properties.add(property);
            }
        }
        
        return properties;
    }
    
    /**
     * Checks if a field is a configuration property.
     */
    private boolean isConfigurationProperty(Field field) {
        // Skip static fields and synthetic fields
        return !java.lang.reflect.Modifier.isStatic(field.getModifiers()) 
               && !field.isSynthetic()
               && !field.getName().startsWith("$");
    }
    
    /**
     * Creates property information for a field.
     */
    private PropertyInfo createPropertyInfo(Field field, String prefix) {
        String name = convertFieldNameToPropertyName(field.getName());
        String type = getTypeDescription(field.getType(), field.getGenericType());
        String defaultValue = getDefaultValue(field);
        String description = getFieldDescription(field);
        
        return new PropertyInfo(name, type, defaultValue, description);
    }
    
    /**
     * Converts Java field name to property name (camelCase to kebab-case).
     */
    private String convertFieldNameToPropertyName(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase();
    }
    
    /**
     * Gets a human-readable type description.
     */
    private String getTypeDescription(Class<?> type, Type genericType) {
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        } else if (type == int.class || type == Integer.class) {
            return "int";
        } else if (type == long.class || type == Long.class) {
            return "long";
        } else if (type == String.class) {
            return "string";
        } else if (type == Duration.class) {
            return "Duration";
        } else if (List.class.isAssignableFrom(type)) {
            if (genericType instanceof ParameterizedType paramType) {
                Type[] args = paramType.getActualTypeArguments();
                if (args.length > 0) {
                    return "List<" + getSimpleTypeName(args[0]) + ">";
                }
            }
            return "List";
        } else if (Map.class.isAssignableFrom(type)) {
            if (genericType instanceof ParameterizedType paramType) {
                Type[] args = paramType.getActualTypeArguments();
                if (args.length == 2) {
                    return "Map<" + getSimpleTypeName(args[0]) + ", " + getSimpleTypeName(args[1]) + ">";
                }
            }
            return "Map";
        } else {
            return type.getSimpleName();
        }
    }
    
    /**
     * Gets simple type name for generic parameters.
     */
    private String getSimpleTypeName(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz.getSimpleName();
        }
        return type.toString();
    }
    
    /**
     * Gets the default value for a field (best effort).
     */
    private String getDefaultValue(Field field) {
        // This is a simplified implementation
        // In a real application, you might want to instantiate the class
        // and read the actual default values
        
        Class<?> type = field.getType();
        
        if (type == boolean.class || type == Boolean.class) {
            return "false";
        } else if (type == int.class || type == Integer.class) {
            return "0";
        } else if (type == long.class || type == Long.class) {
            return "0";
        } else if (type == String.class) {
            return "null";
        } else if (type == Duration.class) {
            return "null";
        } else if (List.class.isAssignableFrom(type)) {
            return "[]";
        } else if (Map.class.isAssignableFrom(type)) {
            return "{}";
        } else {
            return "null";
        }
    }
    
    /**
     * Gets description for a field (from javadoc or field name).
     */
    private String getFieldDescription(Field field) {
        // This is a simplified implementation
        // In a real application, you might want to parse javadoc comments
        // or use annotations for descriptions
        
        String fieldName = field.getName();
        
        // Convert camelCase to human-readable description
        String description = fieldName.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
        description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
        
        // Add some context based on field name patterns
        if (fieldName.contains("timeout") || fieldName.contains("Timeout")) {
            description += " (timeout duration)";
        } else if (fieldName.contains("enable") || fieldName.contains("Enable")) {
            description += " (feature toggle)";
        } else if (fieldName.contains("max") || fieldName.contains("Max")) {
            description += " (maximum value)";
        } else if (fieldName.contains("min") || fieldName.contains("Min")) {
            description += " (minimum value)";
        } else if (fieldName.contains("interval") || fieldName.contains("Interval")) {
            description += " (time interval)";
        } else if (fieldName.contains("size") || fieldName.contains("Size")) {
            description += " (size limit)";
        }
        
        return description;
    }
    
    /**
     * Generates example configuration YAML.
     */
    public String generateExampleConfiguration() {
        StringBuilder yaml = new StringBuilder();
        
        yaml.append("# Example MCP Configuration\n");
        yaml.append("# This configuration demonstrates all available settings\n\n");
        
        yaml.append("mcp:\n");
        yaml.append("  # Core settings\n");
        yaml.append("  connection-timeout: 30s\n");
        yaml.append("  max-retries: 3\n");
        yaml.append("  retry-delay: 5s\n");
        yaml.append("  health-check-interval: 1m\n");
        yaml.append("  enable-multi-server: false\n");
        yaml.append("  verbose-logging: true\n");
        yaml.append("  enable-examples: true\n\n");
        
        yaml.append("  # Reactive configuration\n");
        yaml.append("  reactive:\n");
        yaml.append("    enabled: true\n");
        yaml.append("    buffer-size: 256\n");
        yaml.append("    timeout: 30s\n");
        yaml.append("    enable-server-sent-events: true\n");
        yaml.append("    sse-heartbeat-interval: 30s\n\n");
        
        yaml.append("  # Educational configuration\n");
        yaml.append("  educational:\n");
        yaml.append("    enable-demonstrations: true\n");
        yaml.append("    enable-interactive-examples: true\n");
        yaml.append("    show-protocol-details: true\n");
        yaml.append("    enable-step-by-step-mode: false\n");
        yaml.append("    step-delay: 2s\n");
        yaml.append("    max-examples: 10\n");
        yaml.append("    enable-error-simulation: true\n\n");
        
        yaml.append("  # Server configurations\n");
        yaml.append("  servers:\n");
        yaml.append("    - id: filesystem-server\n");
        yaml.append("      name: File System MCP Server\n");
        yaml.append("      command: uvx\n");
        yaml.append("      args:\n");
        yaml.append("        - mcp-server-filesystem\n");
        yaml.append("        - /tmp/mcp-demo\n");
        yaml.append("      env:\n");
        yaml.append("        MCP_LOG_LEVEL: INFO\n");
        yaml.append("      enabled: true\n");
        
        return yaml.toString();
    }
    
    /**
     * Property information record.
     */
    private record PropertyInfo(
        String name,
        String type,
        String defaultValue,
        String description
    ) {}
}