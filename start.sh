#!/bin/bash

# MCP Learning Platform Startup Script
# This script provides easy ways to start the application in different modes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Function to check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 21 or higher."
        exit 1
    fi
    
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$java_version" -lt 21 ]; then
        print_error "Java 21 or higher is required. Current version: $java_version"
        exit 1
    fi
    print_success "Java $java_version found"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install Maven 3.8 or higher."
        exit 1
    fi
    print_success "Maven found"
    
    # Check if uvx is available (for MCP servers)
    if ! command -v uvx &> /dev/null; then
        print_warning "uvx not found. MCP servers won't be available."
        print_info "To install uvx: curl -LsSf https://astral.sh/uv/install.sh | sh"
    else
        print_success "uvx found - MCP servers can be used"
    fi
}

# Function to show usage
show_usage() {
    echo "MCP Learning Platform Startup Script"
    echo ""
    echo "Usage: $0 [MODE]"
    echo ""
    echo "Modes:"
    echo "  basic       - Start with basic configuration (default)"
    echo "  multi       - Start with multi-server configuration"
    echo "  dev         - Start in development mode"
    echo "  test        - Run tests only"
    echo "  build       - Build the application only"
    echo "  clean       - Clean and build"
    echo "  help        - Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0              # Start in basic mode"
    echo "  $0 multi        # Start with multiple MCP servers"
    echo "  $0 dev          # Start in development mode"
    echo "  $0 test         # Run all tests"
    echo ""
}

# Function to create demo directory
setup_demo_environment() {
    print_info "Setting up demo environment..."
    
    # Create demo directory
    mkdir -p /tmp/mcp-demo
    
    # Create sample files
    cat > /tmp/mcp-demo/README.md << 'EOF'
# MCP Demo Directory

This directory is used by the MCP Learning Platform for demonstration purposes.

## Files
- `sample.txt` - A sample text file
- `data.json` - Sample JSON data
- `config.yml` - Sample configuration

## Usage
The filesystem MCP server will use this directory to demonstrate file operations.
EOF

    cat > /tmp/mcp-demo/sample.txt << 'EOF'
Hello from the MCP Learning Platform!

This is a sample text file used to demonstrate MCP file operations.
You can read, write, and manipulate files through the MCP protocol.

Features demonstrated:
- File reading and writing
- Directory listing
- File metadata access
- Content manipulation
EOF

    cat > /tmp/mcp-demo/data.json << 'EOF'
{
  "application": "MCP Learning Platform",
  "version": "1.0.0",
  "features": [
    "Multi-server support",
    "Educational examples",
    "Interactive demonstrations",
    "Comprehensive testing"
  ],
  "servers": {
    "filesystem": {
      "enabled": true,
      "description": "File system operations"
    },
    "git": {
      "enabled": true,
      "description": "Version control operations"
    },
    "memory": {
      "enabled": true,
      "description": "In-memory storage"
    }
  }
}
EOF

    cat > /tmp/mcp-demo/config.yml << 'EOF'
# MCP Demo Configuration
demo:
  name: "MCP Learning Platform Demo"
  version: "1.0.0"
  
servers:
  - name: "filesystem"
    type: "file-operations"
    enabled: true
  - name: "git"
    type: "version-control"
    enabled: true
  - name: "memory"
    type: "key-value-store"
    enabled: true

settings:
  verbose_logging: true
  enable_examples: true
  connection_timeout: 30
EOF

    print_success "Demo environment created at /tmp/mcp-demo"
}

# Function to start the application
start_application() {
    local mode=$1
    
    print_info "Starting MCP Learning Platform in $mode mode..."
    
    case $mode in
        "basic")
            mvn spring-boot:run
            ;;
        "multi")
            setup_demo_environment
            mvn spring-boot:run -Dspring.profiles.active=multiserver
            ;;
        "dev")
            setup_demo_environment
            mvn spring-boot:run -Dspring.profiles.active=development
            ;;
        *)
            print_error "Unknown mode: $mode"
            show_usage
            exit 1
            ;;
    esac
}

# Function to run tests
run_tests() {
    print_info "Running tests..."
    mvn test
    print_success "Tests completed"
}

# Function to build application
build_application() {
    print_info "Building application..."
    mvn clean compile
    print_success "Build completed"
}

# Function to clean and build
clean_build() {
    print_info "Cleaning and building application..."
    mvn clean compile
    print_success "Clean build completed"
}

# Main script logic
main() {
    local mode=${1:-"basic"}
    
    case $mode in
        "help"|"-h"|"--help")
            show_usage
            exit 0
            ;;
        "test")
            check_prerequisites
            run_tests
            exit 0
            ;;
        "build")
            check_prerequisites
            build_application
            exit 0
            ;;
        "clean")
            check_prerequisites
            clean_build
            exit 0
            ;;
        "basic"|"multi"|"dev")
            check_prerequisites
            start_application $mode
            ;;
        *)
            print_error "Unknown command: $mode"
            show_usage
            exit 1
            ;;
    esac
}

# Print banner
echo ""
echo "🚀 MCP Learning Platform"
echo "📚 Educational Multi-Server MCP Implementation"
echo ""

# Run main function
main "$@"