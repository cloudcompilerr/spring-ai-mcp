# MCP Learning Platform

A comprehensive Spring Boot Java 21 application designed as a self-illustrative learning platform for Model Context Protocol (MCP) implementation.

## Overview

This repository serves as both a functional MCP implementation and an educational resource, demonstrating core MCP concepts through progressively complex examples. The focus is on clean, well-engineered code that prioritizes clarity and understanding over unnecessary complexity.

## Features

- **Educational MCP Implementation**: Learn MCP concepts through working code examples
- **Single Server Management**: Understand basic MCP server connection and operations
- **Multi-Server Support**: Explore advanced scenarios with multiple concurrent MCP servers
- **Framework Integration**: See how MCP integrates with Spring Boot and other Java frameworks
- **Comprehensive Documentation**: Inline comments and examples explain MCP concepts
- **Interactive Examples**: REST endpoints for exploring MCP functionality

## Requirements

- Java 21 or higher
- Maven 3.6 or higher
- MCP servers for testing (optional, examples provided)

## Getting Started

### 1. Clone and Build

```bash
git clone <repository-url>
cd mcp-learning-platform
mvn clean install
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 3. Explore the Examples

- Check the logs for educational output about MCP concepts
- Visit the REST endpoints (to be implemented in later tasks)
- Examine the code structure to understand MCP implementation patterns

## Project Structure

```
src/main/java/com/example/mcplearning/
├── config/          # Spring configuration classes
├── mcp/             # Core MCP protocol implementation
├── server/          # MCP server management components
└── educational/     # Learning and demonstration components
```

## Configuration

The application uses `application.yml` for configuration. Key settings include:

- `mcp.verbose-logging`: Enable detailed logging for educational purposes
- `mcp.enable-examples`: Enable interactive examples and demonstrations
- `mcp.servers`: Configuration for MCP servers (examples provided)

## Learning Path

This application is designed to teach MCP concepts progressively:

1. **Foundation**: Basic Spring Boot setup and MCP concepts
2. **Single Server**: Simple MCP server connection and operations
3. **Multi-Server**: Advanced server management and coordination
4. **Integration**: Framework integration patterns and best practices

## Contributing

This is an educational project. Contributions that improve clarity, add educational value, or demonstrate additional MCP concepts are welcome.

## License

[Add your license here]