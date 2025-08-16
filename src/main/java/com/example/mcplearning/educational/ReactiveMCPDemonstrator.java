package com.example.mcplearning.educational;

import com.example.mcplearning.mcp.client.ReactiveMCPClient;
import com.example.mcplearning.mcp.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * Demonstrates reactive MCP patterns and integration techniques.
 * 
 * This class showcases various reactive programming patterns when working
 * with MCP, including error handling, retries, composition, and backpressure.
 */
@Component
public class ReactiveMCPDemonstrator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReactiveMCPDemonstrator.class);
    
    private final ReactiveMCPClient reactiveMCPClient;
    
    public ReactiveMCPDemonstrator(ReactiveMCPClient reactiveMCPClient) {
        this.reactiveMCPClient = reactiveMCPClient;
        logger.info("Reactive MCP Demonstrator initialized");
    }
    
    /**
     * Demonstrates basic reactive MCP operations with error handling.
     * 
     * This method shows how to chain reactive operations and handle
     * errors gracefully using reactive patterns.
     * 
     * @return A Mono that completes when the demonstration is finished
     */
    public Mono<String> demonstrateBasicReactiveOperations() {
        logger.info("Starting basic reactive MCP operations demonstration");
        
        return reactiveMCPClient.isConnected()
                .filter(connected -> connected)
                .switchIfEmpty(Mono.error(new RuntimeException("MCP client not connected")))
                .flatMap(connected -> reactiveMCPClient.listTools())
                .flatMap(tools -> {
                    logger.info("Found {} tools available", tools.size());
                    if (tools.isEmpty()) {
                        return Mono.just("No tools available for demonstration");
                    }
                    
                    // Demonstrate calling the first available tool
                    MCPTool firstTool = tools.get(0);
                    logger.info("Demonstrating tool: {}", firstTool.name());
                    
                    return reactiveMCPClient.callTool(firstTool.name(), Map.of())
                            .map(result -> String.format("Successfully executed tool '%s': %s", 
                                    firstTool.name(), result.content()))
                            .onErrorReturn(String.format("Failed to execute tool '%s'", firstTool.name()));
                })
                .doOnSuccess(result -> logger.info("Basic reactive demonstration completed: {}", result))
                .doOnError(error -> logger.error("Basic reactive demonstration failed", error));
    }
    
    /**
     * Demonstrates reactive composition and parallel processing.
     * 
     * This method shows how to compose multiple reactive streams and
     * process them in parallel for better performance.
     * 
     * @return A Mono containing the demonstration results
     */
    public Mono<ReactiveCompositionResult> demonstrateReactiveComposition() {
        logger.info("Starting reactive composition demonstration");
        
        // Parallel execution of tools and resources listing
        Mono<Integer> toolCountMono = reactiveMCPClient.listTools()
                .map(tools -> tools.size())
                .doOnNext(count -> logger.info("Tool count: {}", count));
        
        Mono<Integer> resourceCountMono = reactiveMCPClient.listResources()
                .map(resources -> resources.size())
                .doOnNext(count -> logger.info("Resource count: {}", count));
        
        // Combine results using zip operator
        return Mono.zip(toolCountMono, resourceCountMono)
                .map(tuple -> new ReactiveCompositionResult(
                        tuple.getT1(),
                        tuple.getT2(),
                        "Reactive composition completed successfully"
                ))
                .doOnSuccess(result -> logger.info("Reactive composition demonstration completed: {}", result))
                .doOnError(error -> logger.error("Reactive composition demonstration failed", error));
    }
    
    /**
     * Demonstrates reactive streaming with backpressure handling.
     * 
     * This method shows how to handle large datasets using reactive
     * streams with proper backpressure management.
     * 
     * @return A Flux of processed tool information
     */
    public Flux<ToolProcessingResult> demonstrateReactiveStreaming() {
        logger.info("Starting reactive streaming demonstration");
        
        return reactiveMCPClient.streamTools()
                .doOnNext(tool -> logger.debug("Processing tool: {}", tool.name()))
                .map(tool -> new ToolProcessingResult(
                        tool.name(),
                        tool.description(),
                        "Processed successfully"
                ))
                .delayElements(Duration.ofMillis(100)) // Simulate processing time
                .doOnComplete(() -> logger.info("Reactive streaming demonstration completed"))
                .doOnError(error -> logger.error("Reactive streaming demonstration failed", error));
    }
    
    /**
     * Demonstrates error handling and retry patterns in reactive MCP operations.
     * 
     * This method shows various error handling strategies including
     * retries, fallbacks, and circuit breaker patterns.
     * 
     * @param toolName The name of the tool to execute with error handling
     * @return A Mono containing the execution result or error information
     */
    public Mono<String> demonstrateErrorHandlingAndRetry(String toolName) {
        logger.info("Starting error handling and retry demonstration for tool: {}", toolName);
        
        return reactiveMCPClient.callTool(toolName, Map.of())
                .map(result -> String.format("Tool '%s' executed successfully: %s", toolName, result.content()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> 
                                logger.warn("Retrying tool execution, attempt: {}", retrySignal.totalRetries() + 1)))
                .onErrorReturn(String.format("Tool '%s' execution failed after retries", toolName))
                .doOnSuccess(result -> logger.info("Error handling demonstration completed: {}", result))
                .doOnError(error -> logger.error("Error handling demonstration failed", error));
    }
    
    /**
     * Demonstrates reactive resource processing with filtering and transformation.
     * 
     * This method shows how to process resources reactively with
     * filtering, transformation, and aggregation operations.
     * 
     * @return A Mono containing the processing summary
     */
    public Mono<ResourceProcessingSummary> demonstrateResourceProcessing() {
        logger.info("Starting reactive resource processing demonstration");
        
        return reactiveMCPClient.streamResources()
                .filter(resource -> resource.mimeType() != null && resource.mimeType().startsWith("text/"))
                .flatMap(resource -> reactiveMCPClient.readResource(resource.uri())
                        .map(content -> new ProcessedResource(
                                resource.uri(),
                                resource.name(),
                                content.length()
                        ))
                        .onErrorReturn(new ProcessedResource(
                                resource.uri(),
                                resource.name(),
                                -1 // Indicate error
                        )))
                .collectList()
                .map(processedResources -> new ResourceProcessingSummary(
                        processedResources.size(),
                        processedResources.stream()
                                .mapToInt(ProcessedResource::contentLength)
                                .filter(length -> length > 0)
                                .sum(),
                        "Resource processing completed"
                ))
                .doOnSuccess(summary -> logger.info("Resource processing demonstration completed: {}", summary))
                .doOnError(error -> logger.error("Resource processing demonstration failed", error));
    }
    
    /**
     * Record representing the result of reactive composition demonstration.
     */
    public record ReactiveCompositionResult(
            int toolCount,
            int resourceCount,
            String message
    ) {}
    
    /**
     * Record representing the result of tool processing.
     */
    public record ToolProcessingResult(
            String toolName,
            String description,
            String status
    ) {}
    
    /**
     * Record representing a processed resource.
     */
    public record ProcessedResource(
            String uri,
            String name,
            int contentLength
    ) {}
    
    /**
     * Record representing the summary of resource processing.
     */
    public record ResourceProcessingSummary(
            int processedCount,
            int totalContentLength,
            String message
    ) {}
}