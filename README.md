# MCP Learning Platform

A comprehensive educational platform for learning and demonstrating the **Model Context Protocol (MCP)** with multi-server support. This Spring Boot application showcases how a single MCP client can interact with multiple different MCP servers simultaneously.

## 🎯 What This Application Does

This platform demonstrates **one MCP client connecting to multiple different MCP servers** (e.g., filesystem + git + memory + database servers), not distributed servers of the same type. Perfect for scenarios like:

- **Weather Service** + **Task Planner** + **File Manager**
- **Database Tools** + **Git Operations** + **Memory Storage**
- **Multiple Specialized Services** working together

## 🏗️ Architecture Overview

```
MCP Learning Platform (Client)
├── Filesystem MCP Server (file operations)
├── Git MCP Server (version control)
├── Memory MCP Server (in-memory storage)
└── SQLite MCP Server (database operations)
```

## 🚀 Quick Start

### Prerequisites

1. **Java 21+** installed
2. **Maven 3.8+** installed
3. **Python with uv/uvx** (for external MCP servers)

```bash
# Install uv (Python package manager for external MCP servers)
curl -LsSf https://astral.sh/uv/install.sh | sh
```

## 🔧 What `curl -LsSf https://astral.sh/uv/install.sh | sh` Does

**This installs `uv` and `uvx`** - which are Python package management tools, NOT the MCP servers themselves.

- **`uv`**: Fast Python package installer and environment manager
- **`uvx`**: Tool to run Python applications in isolated environments (like `npx` for Node.js)

## 📦 How MCP Servers Are Installed

The MCP servers are **downloaded and run on-demand** by `uvx`:

```bash
# When you run this:
uvx mcp-server-filesystem /tmp/demo

# uvx automatically:
# 1. Downloads the mcp-server-filesystem package from PyPI
# 2. Creates an isolated environment
# 3. Installs the package and its dependencies
# 4. Runs the server
```

**Available Ready-Made MCP Servers:**
- `mcp-server-filesystem` - File operations
- `mcp-server-git` - Git operations  
- `mcp-server-memory` - In-memory storage
- `mcp-server-sqlite` - Database operations
- `mcp-server-brave-search` - Web search
- `mcp-server-github` - GitHub integration
- And many more at: https://github.com/modelcontextprotocol/servers

## 🏗️ Does This Codebase Create Custom MCP Servers?

**Yes! This codebase now includes both MCP CLIENT and MCP SERVER implementations.**

### Built-in Java MCP Servers

This application includes two complete Java-based MCP servers:

#### 🧮 Calculator MCP Server
- **Location**: `src/main/java/.../examples/CalculatorMCPServer.java`
- **Features**: Basic arithmetic, advanced math functions, calculation history
- **Tools**: `basic_calculate`, `advanced_calculate`, `get_history`, `clear_history`
- **Resources**: `calc://history`, `calc://help`, `calc://stats`
- **Run Standalone**: `java -cp target/classes com.example.mcplearning.mcp.javaserver.examples.CalculatorMCPServer`

#### 🌤️ Weather MCP Server  
- **Location**: `src/main/java/.../examples/WeatherMCPServer.java`
- **Features**: Current weather, forecasts, alerts, query history (simulated data)
- **Tools**: `get_current_weather`, `get_weather_forecast`, `get_weather_alerts`, `get_query_history`
- **Resources**: `weather://stations`, `weather://alerts`, `weather://stats`
- **Run Standalone**: `java -cp target/classes com.example.mcplearning.mcp.javaserver.examples.WeatherMCPServer`

### What This Codebase IS:
- ✅ **MCP Client** - Connects to and communicates with MCP servers
- ✅ **Multi-Server Manager** - Manages multiple server connections
- ✅ **Educational Platform** - Teaches MCP concepts
- ✅ **Web Interface** - Dashboard for interacting with servers
- ✅ **Custom MCP Servers** - Built-in Java MCP server implementations
- ✅ **Server Framework** - Tools for building custom MCP servers in Java

## 🏗️ Java MCP Server Framework

This codebase includes a complete Java framework for building MCP servers:

### Framework Components
- **MCPServer** - Abstract base class for MCP servers
- **MCPServerTool** - Interface for implementing server tools
- **MCPServerResource** - Interface for implementing server resources
- **JSON-RPC Handling** - Complete protocol implementation
- **Educational Examples** - Calculator and Weather servers

### Creating Custom Java MCP Servers

```java
public class MyCustomServer extends MCPServer {
    public MyCustomServer() {
        super("my-server", "1.0.0");
    }
    
    @Override
    protected void initializeServer() {
        // Register your tools
        registerTool(new MyCustomTool());
        registerResource(new MyCustomResource());
    }
}

// Implement a tool
public class MyCustomTool implements MCPServerTool {
    @Override
    public String getName() { return "my_tool"; }
    
    @Override
    public String getDescription() { return "My custom tool"; }
    
    @Override
    public Map<String, Object> getInputSchema() {
        return Map.of("type", "object", "properties", Map.of(
            "input", Map.of("type", "string", "description", "Input parameter")
        ));
    }
    
    @Override
    public MCPToolResult execute(Map<String, Object> arguments) {
        String input = (String) arguments.get("input");
        return MCPToolResult.success("Processed: " + input, "text/plain");
    }
}
```

### Running Java MCP Servers

```bash
# Compile the application
mvn clean compile

# Run a Java MCP server directly
java -cp target/classes com.example.mcplearning.mcp.javaserver.examples.CalculatorMCPServer

# Or configure it in application.yml and use the multi-server manager
```

### Installation & Running

```bash
# 1. Clone and build
git clone <repository-url>
cd mcp-learning-platform
mvn clean compile

# 2. Easy startup with script (recommended)
./start.sh                    # Basic mode
./start.sh multi             # Multi-server mode  
./start.sh dev               # Development mode
./start.sh test              # Run tests only

# 3. Or run manually
mvn spring-boot:run                                    # Basic mode
mvn spring-boot:run -Dspring.profiles.active=multiserver  # Multi-server mode

# 4. Access the application
open http://localhost:8080
```

### First Time Setup

```bash
# Install uv/uvx for MCP servers
curl -LsSf https://astral.sh/uv/install.sh | sh


# Make startup script executable
chmod +x start.sh

# Test the application
./start.sh test
```

## 📋 Supported MCP Servers

The application is configured to work with these MCP servers:

### 🗂️ Filesystem Server
- **Purpose**: File and directory operations
- **Command**: `uvx mcp-server-filesystem /tmp/mcp-demo`
- **Tools**: read_file, write_file, list_directory, create_directory, delete_file
- **Resources**: Local file system access, file metadata
- **Status**: ✅ Enabled in multi-server mode

### 🔧 Git Server  
- **Purpose**: Version control operations
- **Command**: `uvx mcp-server-git --repository .`
- **Tools**: git_status, git_add, git_commit, git_log, git_diff
- **Resources**: Repository information, commit history, branch info
- **Status**: ✅ Enabled in multi-server mode

### 💾 Memory Server
- **Purpose**: In-memory key-value storage
- **Command**: `uvx mcp-server-memory`
- **Tools**: memory_set, memory_get, memory_delete, memory_list, memory_clear
- **Resources**: Stored data inspection, memory usage stats
- **Status**: ✅ Enabled in multi-server mode

### 🗄️ SQLite Server (Optional)
- **Purpose**: Database operations
- **Command**: `uvx mcp-server-sqlite --db-path /tmp/mcp-demo.db`
- **Tools**: sql_query, sql_execute, table_info, create_table
- **Resources**: Database schema, table data, query results
- **Status**: ❌ Disabled by default (enable in config)

### 🧮 Calculator Server (Java)
- **Purpose**: Mathematical calculations and functions
- **Command**: `java -cp target/classes com.example.mcplearning.mcp.javaserver.examples.CalculatorMCPServer`
- **Tools**: basic_calculate, advanced_calculate, get_history, clear_history
- **Resources**: calc://history, calc://help, calc://stats
- **Status**: ✅ Built-in Java implementation

### 🌤️ Weather Server (Java)
- **Purpose**: Weather information and forecasts (simulated)
- **Command**: `java -cp target/classes com.example.mcplearning.mcp.javaserver.examples.WeatherMCPServer`
- **Tools**: get_current_weather, get_weather_forecast, get_weather_alerts, get_query_history
- **Resources**: weather://stations, weather://alerts, weather://stats
- **Status**: ✅ Built-in Java implementation

> **Note**: External servers are disabled by default in basic mode. Java servers are included and can be enabled in multi-server mode. This demonstrates both client and server MCP implementations in the same codebase.

## ⚙️ Configuration

### Basic Configuration (`application.yml`)
```yaml
mcp:
  enable-multi-server: false  # Single server mode for learning
  verbose-logging: true
  enable-examples: true
  
  servers:
    - id: filesystem-server
      name: File System MCP Server
      command: uvx
      args: [mcp-server-filesystem, /tmp/mcp-demo]
      enabled: false  # Enable when ready
```

### Multi-Server Configuration (`application-multiserver.yml`)
```yaml
mcp:
  enable-multi-server: true  # Enable multiple servers
  
  servers:
    - id: filesystem-server
      enabled: true
    - id: git-server  
      enabled: true
    - id: memory-server
      enabled: true
```

## 🎓 Educational Features

### Interactive Learning Modes

1. **Single Server Mode**: Learn MCP basics with one server
2. **Multi-Server Mode**: Advanced scenarios with multiple servers
3. **Reactive Mode**: Asynchronous operations and streaming
4. **Error Simulation**: Learn error handling patterns

### REST API Endpoints

```bash
# Educational demonstrations
GET  /api/mcp/examples                    # List available examples
GET  /api/mcp/examples/{id}               # Run specific example
GET  /api/mcp/status                      # Current system status

# Server management
GET  /api/mcp/servers                     # List configured servers
GET  /api/mcp/servers/{id}/status         # Server health status
POST /api/mcp/servers/{id}/connect        # Connect to server
POST /api/mcp/servers/{id}/disconnect     # Disconnect from server

# Tool operations
GET  /api/mcp/tools                       # List all available tools
POST /api/mcp/tools/{name}/execute        # Execute a specific tool

# Resource operations  
GET  /api/mcp/resources                   # List all available resources
GET  /api/mcp/resources/{uri}             # Read specific resource

# Configuration
GET  /api/mcp/config                      # Current configuration
POST /api/mcp/config/validate             # Validate configuration
GET  /api/mcp/config/documentation        # Generate config docs
```

### Web Interface Features

- **Dashboard**: Real-time server status and metrics
- **Tool Explorer**: Interactive tool discovery and execution
- **Resource Browser**: Browse and access server resources
- **Configuration Manager**: Dynamic server configuration
- **Learning Modules**: Step-by-step MCP tutorials

## 🧪 Testing & Development

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test categories
mvn test -Dtest=MCPConceptsDocumentationTest     # Educational tests
mvn test -Dtest=MCPEducationalErrorHandlingTest # Error scenarios
mvn test -Dtest=MCPComprehensiveTestSuite       # Full test suite

# Run integration tests (requires MCP servers)
MCP_INTEGRATION_TESTS=true mvn test -Dtest=*IntegrationTest
```

### Development Profiles

```bash
# Development mode (verbose logging, auto-reload)
mvn spring-boot:run -Dspring.profiles.active=development

# Testing mode (mock servers, fast startup)  
mvn spring-boot:run -Dspring.profiles.active=testing

# Production mode (optimized, minimal logging)
mvn spring-boot:run -Dspring.profiles.active=production
```

## 📚 Learning Path

### 1. **Beginner**: Start with Single Server
```bash
# Enable one server and explore basics
mvn spring-boot:run
# Visit http://localhost:8080/api/mcp/examples
```

### 2. **Intermediate**: Multi-Server Operations
```bash
# Enable multiple servers
mvn spring-boot:run -Dspring.profiles.active=multiserver
# Explore tool routing and server selection
```

### 3. **Advanced**: Custom Configurations
```bash
# Create custom server configurations
# Implement custom selection strategies
# Build reactive applications
```

## 🔧 Troubleshooting

### Common Issues

**MCP Servers Not Starting**
```bash
# Check if uvx is installed
uvx --version

# Test server manually
uvx mcp-server-filesystem /tmp/test

# Check logs
tail -f logs/mcp-learning-platform.log
```

**Connection Timeouts**
```yaml
# Increase timeouts in application.yml
mcp:
  connection-timeout: 60s
  max-retries: 5
```

**Port Conflicts**
```yaml
# Change server port
server:
  port: 8081
```

### Debug Mode
```bash
# Enable debug logging
mvn spring-boot:run -Dlogging.level.com.example.mcplearning=DEBUG
```

## 🏗️ Architecture Details

### Core Components

- **MCPClient**: Protocol implementation for server communication
- **MultiMCPServerManager**: Manages multiple server connections
- **MCPToolRouter**: Routes tool calls to appropriate servers
- **ServerSelectionStrategy**: Load balancing and failover logic
- **Educational Components**: Interactive learning modules

### Design Patterns

- **Strategy Pattern**: Server selection algorithms
- **Observer Pattern**: Health monitoring and events
- **Factory Pattern**: Server manager creation
- **Adapter Pattern**: Protocol abstraction
- **Circuit Breaker**: Fault tolerance

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🔗 Related Resources

- [MCP Specification](https://spec.modelcontextprotocol.io/)
- [MCP Python SDK](https://github.com/modelcontextprotocol/python-sdk)
- [Available MCP Servers](https://github.com/modelcontextprotocol/servers)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## 📞 Support

- **Issues**: GitHub Issues
- **Discussions**: GitHub Discussions  
- **Documentation**: `/api/mcp/config/documentation`
- **Health Check**: `/actuator/health`

---

**Happy Learning with MCP! 🚀**