package com.example.mcplearning.mcp.javaserver;

/**
 * Interface for MCP Server Resources
 * 
 * EDUCATIONAL OVERVIEW:
 * This interface defines how resources are implemented in a Java MCP server.
 * Resources represent data or content that the server can provide to clients.
 * Unlike tools (which perform actions), resources are static content.
 * 
 * KEY CONCEPTS:
 * - Resource Metadata: URI, name, description, and MIME type
 * - Resource Access: Reading and providing content to clients
 * - Content Types: Text, binary, structured data, etc.
 * - URI Schemes: Different ways to identify and access resources
 * 
 * COMMON RESOURCE TYPES:
 * - Files: file:///path/to/file.txt
 * - Configuration: config://app/settings.json
 * - Data: data://users/active.csv
 * - Documentation: docs://api/reference.md
 */
public interface MCPServerResource {
    
    /**
     * Gets the URI of the resource.
     * This is used by clients to identify and request the resource.
     * 
     * @return The resource URI (must be unique within the server)
     */
    String getUri();
    
    /**
     * Gets the human-readable name of the resource.
     * 
     * @return A descriptive name for the resource
     */
    String getName();
    
    /**
     * Gets the description of the resource.
     * This helps clients understand what the resource contains.
     * 
     * @return A human-readable description of the resource content
     */
    String getDescription();
    
    /**
     * Gets the MIME type of the resource.
     * This indicates the format of the resource content.
     * 
     * Common MIME types:
     * - text/plain - Plain text
     * - text/markdown - Markdown content
     * - application/json - JSON data
     * - text/csv - CSV data
     * - application/xml - XML content
     * 
     * @return The MIME type of the resource content
     */
    String getMimeType();
    
    /**
     * Reads and returns the content of the resource.
     * 
     * IMPLEMENTATION GUIDELINES:
     * - Return the current content of the resource
     * - Handle file I/O or data access operations
     * - Throw exceptions for access errors or missing resources
     * - Log access operations for debugging
     * - Consider caching for frequently accessed resources
     * 
     * @return The content of the resource as a string
     * @throws Exception If the resource cannot be read
     */
    String read() throws Exception;
    
    /**
     * Checks if the resource exists and is accessible.
     * Default implementation tries to read the resource.
     * 
     * @return true if the resource exists and can be read, false otherwise
     */
    default boolean exists() {
        try {
            read();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Gets the size of the resource content in bytes.
     * Default implementation reads the content and measures its length.
     * 
     * @return The size of the resource content in bytes, or -1 if unknown
     */
    default long getSize() {
        try {
            return read().getBytes().length;
        } catch (Exception e) {
            return -1;
        }
    }
}