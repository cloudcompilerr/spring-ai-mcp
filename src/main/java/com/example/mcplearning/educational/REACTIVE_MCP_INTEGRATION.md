# Spring WebFlux MCP Integration Guide

This document explains the integration patterns and trade-offs when using Model Context Protocol (MCP) with Spring WebFlux reactive programming.

## Overview

The reactive MCP integration demonstrates how to combine MCP's asynchronous nature with Spring WebFlux's reactive streams for building non-blocking, scalable applications.

## Key Components

### 1. ReactiveMCPClient Interface

The `ReactiveMCPClient` interface provides reactive alternatives to the standard MCP operations:

```java
// Traditional approach
CompletableFuture<List<MCPTool>> listTools();

// Reactive approach
Mono<List<MCPTool>> listTools();
Flux<MCPTool> streamTools();
```

**Benefits:**
- Better integration with reactive web endpoints
- Composable operations using reactive operators
- Built-in backpressure handling
- Non-blocking I/O operations

### 2. Server-Sent Events (SSE) Integration

The `ReactiveMCPController` demonstrates real-time MCP updates using SSE:

```java
@GetMapping(value = "/tools/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<MCPTool> streamTools() {
    return reactiveMCPClient.streamTools()
            .delayElements(Duration.ofMillis(100));
}
```

**Use Cases:**
- Real-time tool discovery updates
- Live resource monitoring
- Connection status streaming
- Progressive data loading

### 3. Reactive Composition Patterns

The integration showcases various reactive composition patterns:

#### Parallel Execution
```java
Mono<Integer> toolCount = reactiveMCPClient.listTools().map(List::size);
Mono<Integer> resourceCount = reactiveMCPClient.listResources().map(List::size);

return Mono.zip(toolCount, resourceCount)
        .map(tuple -> new Summary(tuple.getT1(), tuple.getT2()));
```

#### Error Handling with Retries
```java
return reactiveMCPClient.callTool(toolName, arguments)
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
        .onErrorReturn("Fallback result");
```

#### Stream Processing
```java
return reactiveMCPClient.streamResources()
        .filter(resource -> resource.mimeType().startsWith("text/"))
        .flatMap(resource -> reactiveMCPClient.readResource(resource.uri()))
        .collectList();
```

## Integration Patterns

### 1. Wrapper Pattern

The `DefaultReactiveMCPClient` wraps existing `MCPClient` implementations:

**Advantages:**
- Reuses existing MCP client logic
- Gradual migration from synchronous to reactive
- Maintains compatibility with existing code

**Trade-offs:**
- Additional abstraction layer
- Potential performance overhead from CompletableFuture → Mono conversion

### 2. Native Reactive Implementation

For optimal performance, implement `ReactiveMCPClient` directly:

**Advantages:**
- True non-blocking operations
- Better resource utilization
- Optimal reactive stream performance

**Trade-offs:**
- More complex implementation
- Requires reactive transport layer

### 3. Hybrid Approach

Use reactive clients for web endpoints, synchronous for background tasks:

```java
@RestController
public class HybridController {
    private final ReactiveMCPClient reactiveMCPClient;
    private final MCPClient syncMCPClient;
    
    // Use reactive for web endpoints
    @GetMapping("/tools")
    public Flux<MCPTool> getTools() {
        return reactiveMCPClient.streamTools();
    }
    
    // Use synchronous for scheduled tasks
    @Scheduled(fixedRate = 60000)
    public void healthCheck() {
        syncMCPClient.listTools().join();
    }
}
```

## Performance Considerations

### Memory Usage

**Reactive Streams:**
- Lower memory footprint for large datasets
- Backpressure prevents memory overflow
- Streaming reduces peak memory usage

**Traditional Approach:**
- Loads entire datasets into memory
- Higher memory requirements for large responses
- Potential OutOfMemoryError with large datasets

### Throughput

**Reactive Benefits:**
- Non-blocking I/O increases throughput
- Better resource utilization
- Handles more concurrent requests

**Measurement Example:**
```java
// Reactive: Can handle 1000+ concurrent requests
@GetMapping("/tools/reactive")
public Flux<MCPTool> getToolsReactive() {
    return reactiveMCPClient.streamTools();
}

// Synchronous: Limited by thread pool size
@GetMapping("/tools/sync")
public List<MCPTool> getToolsSync() {
    return syncMCPClient.listTools().join();
}
```

### Latency

**First Response Time:**
- Reactive: Lower latency for first item in stream
- Synchronous: Must wait for complete response

**Total Response Time:**
- Depends on dataset size and processing requirements
- Reactive excels with large datasets and streaming scenarios

## Configuration

### Application Properties

```yaml
mcp:
  reactive:
    enabled: true
    buffer-size: 256
    timeout: 30s
    enable-server-sent-events: true
    sse-heartbeat-interval: 30s
```

### Bean Configuration

```java
@Configuration
public class ReactiveMCPConfiguration {
    
    @Bean
    @ConditionalOnProperty("mcp.reactive.enabled")
    public ReactiveMCPClient reactiveMCPClient(MCPClient mcpClient) {
        return new DefaultReactiveMCPClient(mcpClient);
    }
}
```

## Best Practices

### 1. Error Handling

Always implement proper error handling in reactive streams:

```java
return reactiveMCPClient.callTool(toolName, arguments)
        .doOnError(error -> logger.error("Tool execution failed", error))
        .onErrorReturn(MCPToolResult.error("Tool execution failed"));
```

### 2. Backpressure Management

Use appropriate operators to handle backpressure:

```java
return reactiveMCPClient.streamTools()
        .onBackpressureBuffer(1000)
        .delayElements(Duration.ofMillis(10));
```

### 3. Resource Management

Ensure proper resource cleanup:

```java
return reactiveMCPClient.callTool(toolName, arguments)
        .doFinally(signalType -> {
            // Cleanup resources regardless of completion type
            logger.debug("Tool execution completed with signal: {}", signalType);
        });
```

### 4. Testing

Use `reactor-test` for testing reactive streams:

```java
@Test
void shouldStreamTools() {
    StepVerifier.create(reactiveMCPClient.streamTools())
            .expectNextCount(2)
            .verifyComplete();
}
```

## Trade-offs Summary

| Aspect | Reactive | Synchronous |
|--------|----------|-------------|
| **Complexity** | Higher learning curve | Simpler to understand |
| **Performance** | Better for I/O intensive | Better for CPU intensive |
| **Memory Usage** | Lower with streaming | Higher with large datasets |
| **Debugging** | More complex stack traces | Simpler debugging |
| **Testing** | Requires reactive testing | Standard unit testing |
| **Integration** | Native WebFlux support | Traditional Spring MVC |

## When to Use Reactive MCP

**Choose Reactive When:**
- Building high-throughput web applications
- Handling large datasets or streams
- Need real-time updates (SSE/WebSocket)
- I/O intensive MCP operations
- Building microservices with reactive stack

**Choose Synchronous When:**
- Simple CRUD operations
- CPU-intensive processing
- Team lacks reactive experience
- Existing synchronous codebase
- Batch processing scenarios

## Conclusion

The reactive MCP integration provides powerful patterns for building scalable, non-blocking applications. While it introduces complexity, the benefits in terms of performance, resource utilization, and real-time capabilities make it valuable for modern web applications.

The key is to choose the right approach based on your specific requirements and team expertise.