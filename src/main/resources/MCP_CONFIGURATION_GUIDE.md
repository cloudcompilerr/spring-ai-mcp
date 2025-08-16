# MCP Learning Platform Configuration Guide

This guide provides comprehensive documentation for configuring the MCP Learning Platform. The platform uses Spring Boot's configuration system with externalized configuration through YAML files.

## Table of Contents

1. [Configuration Overview](#configuration-overview)
2. [Core MCP Settings](#core-mcp-settings)
3. [Server Configuration](#server-configuration)
4. [Educational Settings](#educational-settings)
5. [Reactive Configuration](#reactive-configuration)
6. [Profile-Specific Configurations](#profile-specific-configurations)
7. [Configuration Validation](#configuration-validation)
8. [Common Configuration Patterns](#common-configuration-patterns)
9. [Troubleshooting](#troubleshooting)

## Configuration Overview

The MCP Learning Platform uses a hierarchical configuration system with the following precedence (highest to lowest):

1. Command line arguments
2. Environment variables
3. Profile-specific YAML files (`application-{profile}.yml`)
4. Main configuration file (`application.yml`)
5. Default values in `@ConfigurationProperties` classes

### Configuration Properties Classes

- `MCPConfiguration`: Core MCP settings
- `MCPEducationalConfiguration`: Educational and demonstration settings
- `MCPServerConfig`: Individual server configurations

## Core MCP Settings

All MCP-related settings are under the `mcp` prefix:

```yaml
mcp:
  # Basic connection settings
  connection-timeout: 30s          # Timeout for server connections
  max-retries: 3                   # Maximum connection retry attempts
  retry-delay: 5s                  # Delay between retry attempts
  health-check-interval: 1m        # Interval for server health checks
  
  # Feature toggles
  enable-multi-server: false       # Enable multi-server support
  verbose-logging: true            # Enable detailed MCP protocol logging
  enable-examples: true            # Enable interactive examples
```

### Connection Settings

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `connection-timeout` | Duration | 30s | Maximum time to wait for server connection |
| `max-retries` | int | 3 | Number of retry attempts for failed connections |
| `retry-delay` | Duration | 5s | Delay between connection retry attempts |
| `health-check-interval` | Duration | 1m | Frequency of server health checks |

### Feature Toggles

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enable-multi-server` | boolean | false | Enable concurrent multiple server support |
| `verbose-logging` | boolean | true | Log all MCP protocol messages for education |
| `enable-examples` | boolean | true | Enable interactive demonstration endpoints |

## Server Configuration

MCP servers are configured under `mcp.servers` as a list:

```yaml
mcp:
  servers:
    - id: filesystem-server           # Unique server identifier
      name: File System MCP Server    # Human-readable name
      command: uvx                    # Command to execute
      args:                          # Command arguments
        - mcp-server-filesystem
        - /tmp/mcp-demo
      env:                           # Environment variables
        MCP_LOG_LEVEL: INFO
      enabled: true                  # Whether server is enabled
```

### Server Configuration Properties

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `id` | string | Yes | Unique identifier for the server |
| `name` | string | Yes | Human-readable server name |
| `command` | string | Yes | Command to execute to start the server |
| `args` | list | Yes | Command-line arguments (can be empty) |
| `env` | map | Yes | Environment variables (can be empty) |
| `enabled` | boolean | Yes | Whether this server configuration is active |

### Example Server Configurations

#### File System Server
```yaml
- id: filesystem-server
  name: File System MCP Server
  command: uvx
  args: 
    - mcp-server-filesystem
    - /path/to/directory
  env:
    MCP_LOG_LEVEL: INFO
  enabled: true
```

#### Git Server
```yaml
- id: git-server
  name: Git MCP Server
  command: uvx
  args:
    - mcp-server-git
    - --repository
    - /path/to/repository
  env:
    MCP_LOG_LEVEL: INFO
    GIT_CONFIG_GLOBAL: /path/to/gitconfig
  enabled: true
```

#### SQLite Server
```yaml
- id: sqlite-server
  name: SQLite MCP Server
  command: uvx
  args:
    - mcp-server-sqlite
    - --db-path
    - /path/to/database.db
  env:
    MCP_LOG_LEVEL: INFO
  enabled: true
```

## Educational Settings

Educational features are configured under `mcp.educational`:

```yaml
mcp:
  educational:
    # Demonstration settings
    enable-demonstrations: true      # Enable educational demonstrations
    enable-interactive-examples: true # Enable interactive examples
    show-protocol-details: true     # Show MCP protocol details
    enable-step-by-step-mode: false # Enable step-by-step learning
    step-delay: 2s                  # Delay between demonstration steps
    max-examples: 10                # Maximum examples to show
    enable-error-simulation: true   # Enable error simulation for learning
    
    # Learning progression
    learning-progression:
      enforce-order: false          # Enforce learning order
      track-progress: true          # Track user progress
      show-hints: true              # Show learning hints
      adaptive-difficulty: false    # Enable adaptive difficulty
      minimum-example-time: 30s     # Minimum time per example
```

### Educational Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enable-demonstrations` | boolean | true | Enable educational demonstrations |
| `enable-interactive-examples` | boolean | true | Enable interactive examples |
| `show-protocol-details` | boolean | true | Show detailed MCP protocol messages |
| `enable-step-by-step-mode` | boolean | false | Enable step-by-step learning mode |
| `step-delay` | Duration | 2s | Delay between demonstration steps |
| `max-examples` | int | 10 | Maximum number of examples to show |
| `enable-error-simulation` | boolean | true | Enable error simulation for education |

## Reactive Configuration

Reactive features are configured under `mcp.reactive`:

```yaml
mcp:
  reactive:
    enabled: true                   # Enable reactive MCP features
    buffer-size: 256               # Buffer size for reactive streams
    timeout: 30s                   # Timeout for reactive operations
    enable-server-sent-events: true # Enable SSE for real-time updates
    sse-heartbeat-interval: 30s    # SSE heartbeat interval
```

### Reactive Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | true | Enable reactive MCP client features |
| `buffer-size` | int | 256 | Buffer size for reactive streams |
| `timeout` | Duration | 30s | Timeout for reactive operations |
| `enable-server-sent-events` | boolean | true | Enable server-sent events |
| `sse-heartbeat-interval` | Duration | 30s | Interval for SSE heartbeat messages |

## Profile-Specific Configurations

The platform includes several pre-configured profiles:

### Development Profile (`application-development.yml`)
- Enhanced logging and debugging
- Shorter timeouts for faster feedback
- Mock servers for development
- All educational features enabled

```bash
java -jar mcp-learning-platform.jar --spring.profiles.active=development
```

### Production Profile (`application-production.yml`)
- Optimized for production deployment
- Security-focused settings
- Performance optimizations
- Structured logging

```bash
java -jar mcp-learning-platform.jar --spring.profiles.active=production
```

### Testing Profile (`application-testing.yml`)
- Fast execution for automated tests
- Mock servers only
- Minimal logging
- Simplified configuration

```bash
mvn test -Dspring.profiles.active=testing
```

### Multi-Server Profile (`application-multiserver.yml`)
- Multiple concurrent MCP servers
- Enhanced server management
- Load balancing and failover
- Advanced monitoring

```bash
java -jar mcp-learning-platform.jar --spring.profiles.active=multiserver
```

## Configuration Validation

The platform includes automatic configuration validation that runs at startup:

### Validation Features
- ✅ **Connection Settings**: Validates timeouts and retry settings
- ✅ **Server Configurations**: Checks server definitions and commands
- ✅ **Reactive Settings**: Validates buffer sizes and timeouts
- ✅ **Multi-Server Settings**: Ensures proper multi-server configuration
- ✅ **Educational Settings**: Validates learning progression settings

### Validation Messages
- **Errors**: Configuration issues that prevent startup
- **Warnings**: Potential issues that may affect performance or functionality

### Example Validation Output
```
2024-01-15 10:30:00.123 INFO  [MCPConfigurationValidator] - ✅ MCP configuration validation passed with no issues
```

```
2024-01-15 10:30:00.123 WARN  [MCPConfigurationValidator] - ⚠️  MCP configuration validation found 2 warning(s):
  1. Connection timeout is very long (300s). Consider reducing it for better user experience.
  2. No MCP servers are enabled. Enable at least one server for full functionality.
```

## Common Configuration Patterns

### Single Server Setup
```yaml
mcp:
  enable-multi-server: false
  servers:
    - id: main-server
      name: Main MCP Server
      command: uvx
      args: [mcp-server-filesystem, /data]
      env: {}
      enabled: true
```

### Multi-Server Setup with Load Balancing
```yaml
mcp:
  enable-multi-server: true
  servers:
    - id: server-1
      name: Primary Server
      command: uvx
      args: [mcp-server-filesystem, /data/primary]
      env: {}
      enabled: true
    - id: server-2
      name: Secondary Server
      command: uvx
      args: [mcp-server-filesystem, /data/secondary]
      env: {}
      enabled: true
```

### Development with Mock Servers
```yaml
mcp:
  verbose-logging: true
  enable-examples: true
  servers:
    - id: mock-server
      name: Mock Development Server
      command: echo
      args: ["Mock server for development"]
      env:
        DEV_MODE: "true"
      enabled: true
```

### Production with Security
```yaml
mcp:
  verbose-logging: false
  connection-timeout: 60s
  max-retries: 5
  servers:
    - id: prod-server
      name: Production MCP Server
      command: /opt/mcp/bin/server
      args: [--config, /etc/mcp/server.conf]
      env:
        MCP_LOG_LEVEL: WARN
        PRODUCTION_MODE: "true"
      enabled: true
```

## Environment Variable Override

All configuration properties can be overridden using environment variables:

```bash
# Override connection timeout
export MCP_CONNECTION_TIMEOUT=45s

# Override server configuration
export MCP_SERVERS_0_ENABLED=false
export MCP_SERVERS_1_COMMAND=/custom/path/to/server

# Override educational settings
export MCP_EDUCATIONAL_ENABLE_DEMONSTRATIONS=false
export MCP_EDUCATIONAL_STEP_DELAY=5s
```

### Environment Variable Naming Convention
- Replace dots (.) with underscores (_)
- Convert to uppercase
- Use array indices for list items (e.g., `SERVERS_0_`, `SERVERS_1_`)

## Troubleshooting

### Common Configuration Issues

#### 1. Server Not Starting
**Problem**: MCP server fails to start
**Solution**: Check command path and arguments
```yaml
# Incorrect
command: mcp-server-filesystem  # Command not in PATH

# Correct
command: uvx  # Use uvx to run MCP servers
args: [mcp-server-filesystem, /path/to/data]
```

#### 2. Connection Timeouts
**Problem**: Frequent connection timeouts
**Solution**: Increase timeout values
```yaml
mcp:
  connection-timeout: 60s  # Increase from default 30s
  retry-delay: 10s         # Increase retry delay
```

#### 3. Multi-Server Conflicts
**Problem**: Tool name conflicts between servers
**Solution**: Use server-specific prefixes or disable conflicting servers
```yaml
mcp:
  enable-multi-server: true
  servers:
    - id: server-1
      # ... configuration
      enabled: true
    - id: server-2
      # ... configuration
      enabled: false  # Disable if conflicts occur
```

#### 4. Performance Issues
**Problem**: High memory usage or slow responses
**Solution**: Tune reactive settings
```yaml
mcp:
  reactive:
    buffer-size: 128      # Reduce buffer size
    timeout: 15s          # Reduce timeout
```

### Configuration Validation Errors

#### Missing Required Properties
```
Error: Server ID is required
Solution: Ensure all servers have unique IDs
```

#### Invalid Duration Format
```
Error: Invalid duration format
Solution: Use valid duration format (e.g., 30s, 2m, 1h)
```

#### Negative Values
```
Error: Max retries must be non-negative
Solution: Use non-negative values for retry counts
```

### Debugging Configuration

#### Enable Configuration Debug Logging
```yaml
logging:
  level:
    "[com.example.mcplearning.mcp.config]": DEBUG
```

#### View Effective Configuration
Access the `/actuator/configprops` endpoint to see the effective configuration:
```bash
curl http://localhost:8080/actuator/configprops
```

#### Validate Configuration Programmatically
The `MCPConfigurationValidator` provides programmatic validation:
```java
@Autowired
private MCPConfigurationValidator validator;

ValidationResult result = validator.validateConfigurationProgrammatically();
if (result.hasErrors()) {
    // Handle configuration errors
}
```

## Best Practices

1. **Use Profiles**: Separate configurations for different environments
2. **Validate Early**: Enable configuration validation in all environments
3. **Monitor Health**: Use health checks to monitor server status
4. **Secure Credentials**: Use environment variables for sensitive data
5. **Document Changes**: Keep configuration changes documented
6. **Test Configurations**: Test configuration changes in development first
7. **Use Defaults**: Rely on sensible defaults when possible
8. **Monitor Performance**: Tune settings based on actual usage patterns

## Additional Resources

- [Spring Boot Configuration Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [MCP Protocol Specification](https://modelcontextprotocol.io/docs)
- [Application Properties Reference](application.yml)
- [Multi-Server Configuration Example](application-multiserver.yml)