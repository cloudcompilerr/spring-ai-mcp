package com.example.mcplearning.mcp.transport;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Process-based MCP transport implementation.
 * 
 * EDUCATIONAL OVERVIEW:
 * This transport demonstrates how MCP clients communicate with servers that run
 * as separate processes. This is the most common MCP deployment pattern, where
 * the server is a standalone executable that communicates via stdin/stdout.
 * 
 * KEY MCP CONCEPTS DEMONSTRATED:
 * - JSON-RPC 2.0 protocol over stdio streams
 * - Asynchronous request/response correlation using message IDs
 * - Process lifecycle management (start, monitor, cleanup)
 * - Error handling for network-like failures in process communication
 * - Timeout handling for unresponsive servers
 * 
 * ARCHITECTURE PATTERN:
 * Client Process ←→ stdin/stdout ←→ MCP Server Process
 * 
 * This transport handles:
 * - Process lifecycle management (starting, monitoring, terminating)
 * - Asynchronous message sending and receiving
 * - Request/response correlation using JSON-RPC message IDs
 * - Timeout handling for requests that don't receive responses
 * - Error handling and resource cleanup
 * - Thread management for concurrent operations
 */
public class ProcessMCPTransport implements MCPTransport {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessMCPTransport.class);
    
    private final String command;
    private final List<String> args;
    private final Map<String, String> environment;
    private final ObjectMapper objectMapper;
    private final long timeoutMs;
    
    private volatile Process process;
    private volatile BufferedWriter processInput;
    private volatile BufferedReader processOutput;
    private volatile boolean connected = false;
    
    private final ConcurrentHashMap<String, CompletableFuture<JsonRpcResponse>> pendingRequests = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread thread = new Thread(r, "mcp-transport");
        thread.setDaemon(true);
        return thread;
    });
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, r -> {
        Thread thread = new Thread(r, "mcp-transport-scheduler");
        thread.setDaemon(true);
        return thread;
    });
    
    /**
     * Creates a new process-based MCP transport.
     * 
     * @param command The command to execute
     * @param args The command arguments
     * @param environment Environment variables for the process
     * @param objectMapper JSON object mapper for serialization
     * @param timeoutMs Request timeout in milliseconds
     */
    public ProcessMCPTransport(String command, List<String> args, Map<String, String> environment, 
                              ObjectMapper objectMapper, long timeoutMs) {
        this.command = command;
        this.args = args;
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.timeoutMs = timeoutMs;
    }
    
    @Override
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("🚀 MCP Transport: Starting server process - {} {}", command, args);
                
                // EDUCATIONAL NOTE: ProcessBuilder is Java's way to start external processes
                // This is how we launch MCP servers that are separate executables
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command().add(command);
                processBuilder.command().addAll(args);
                
                // EDUCATIONAL NOTE: Environment variables can be used to configure MCP servers
                // Common examples: API keys, configuration paths, debug flags
                if (environment != null && !environment.isEmpty()) {
                    processBuilder.environment().putAll(environment);
                    logger.debug("🔧 MCP Transport: Added {} environment variables", environment.size());
                }
                
                // EDUCATIONAL NOTE: We keep stderr separate for debugging
                // MCP protocol uses stdout for JSON-RPC, stderr for logs/errors
                processBuilder.redirectErrorStream(false);
                
                // EDUCATIONAL NOTE: This starts the actual MCP server process
                process = processBuilder.start();
                logger.debug("📡 MCP Transport: Process started with PID {}", process.pid());
                
                // EDUCATIONAL NOTE: Set up communication channels
                // stdin -> send JSON-RPC requests to server
                // stdout -> receive JSON-RPC responses from server
                processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                
                // EDUCATIONAL NOTE: Start background thread to read server responses
                // This enables asynchronous communication - we can send requests
                // without blocking while waiting for responses
                startResponseReader();
                
                connected = true;
                logger.info("✅ MCP Transport: Server process connected and ready");
                
            } catch (IOException e) {
                logger.error("❌ MCP Transport: Failed to start server process", e);
                throw new MCPTransportException("Failed to start MCP server process", e);
            }
        }, executorService);
    }
    
    @Override
    public CompletableFuture<JsonRpcResponse> sendRequest(JsonRpcRequest request) {
        if (!connected) {
            return CompletableFuture.failedFuture(
                new MCPTransportException("Transport is not connected"));
        }
        
        logger.debug("📤 MCP Transport: Sending request [{}] method='{}'", 
                    request.id(), request.method());
        
        // EDUCATIONAL NOTE: Request/Response Correlation Pattern
        // Since JSON-RPC is asynchronous, we need to match responses to requests
        // We use the request ID as a correlation key and store a Future for each request
        CompletableFuture<JsonRpcResponse> responseFuture = new CompletableFuture<>();
        pendingRequests.put(request.id(), responseFuture);
        
        // EDUCATIONAL NOTE: Timeout Handling
        // MCP servers might not respond to requests (network issues, server hang, etc.)
        // We set up a timeout to prevent clients from waiting indefinitely
        scheduledExecutorService.schedule(() -> {
            CompletableFuture<JsonRpcResponse> removed = pendingRequests.remove(request.id());
            if (removed != null && !removed.isDone()) {
                logger.warn("⏰ MCP Transport: Request [{}] timed out after {}ms", 
                          request.id(), timeoutMs);
                removed.completeExceptionally(
                    new MCPTransportException("Request timed out after " + timeoutMs + "ms"));
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        // EDUCATIONAL NOTE: Asynchronous Message Flow
        // 1. Send the message to the server process
        // 2. Return immediately with a Future (non-blocking)
        // 3. The response reader thread will complete the Future when response arrives
        return sendMessage(request)
            .thenCompose(v -> responseFuture)
            .exceptionally(throwable -> {
                pendingRequests.remove(request.id());
                logger.error("❌ MCP Transport: Request [{}] failed", request.id(), throwable);
                throw new MCPTransportException("Failed to send request", throwable);
            });
    }
    
    @Override
    public CompletableFuture<Void> sendNotification(JsonRpcRequest request) {
        if (!connected) {
            return CompletableFuture.failedFuture(
                new MCPTransportException("Transport is not connected"));
        }
        
        return sendMessage(request);
    }
    
    @Override
    public boolean isConnected() {
        return connected && process != null && process.isAlive();
    }
    
    @Override
    public void close() {
        logger.debug("Closing MCP transport");
        
        connected = false;
        
        // Complete any pending requests with an error
        pendingRequests.values().forEach(future -> {
            if (!future.isDone()) {
                future.completeExceptionally(
                    new MCPTransportException("Transport closed"));
            }
        });
        pendingRequests.clear();
        
        // Close streams
        try {
            if (processInput != null) {
                processInput.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing process input stream", e);
        }
        
        try {
            if (processOutput != null) {
                processOutput.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing process output stream", e);
        }
        
        // Terminate process
        if (process != null) {
            process.destroyForcibly();
            try {
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        executorService.shutdown();
        scheduledExecutorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
            scheduledExecutorService.shutdownNow();
        }
    }
    
    /**
     * Sends a JSON-RPC message to the process.
     * 
     * @param message The message to send
     * @return A future that completes when the message is sent
     */
    private CompletableFuture<Void> sendMessage(Object message) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = objectMapper.writeValueAsString(message);
                logger.debug("Sending message: {}", json);
                
                synchronized (processInput) {
                    processInput.write(json);
                    processInput.newLine();
                    processInput.flush();
                }
            } catch (IOException e) {
                throw new MCPTransportException("Failed to send message", e);
            }
        }, executorService);
    }
    
    /**
     * Starts the background thread that reads responses from the process.
     */
    private void startResponseReader() {
        executorService.submit(() -> {
            try {
                String line;
                while (connected && (line = processOutput.readLine()) != null) {
                    logger.debug("Received message: {}", line);
                    
                    try {
                        JsonRpcResponse response = objectMapper.readValue(line, JsonRpcResponse.class);
                        handleResponse(response);
                    } catch (Exception e) {
                        logger.error("Failed to parse response: {}", line, e);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    logger.error("Error reading from process output", e);
                }
            } finally {
                logger.debug("Response reader thread terminated");
            }
        });
    }
    
    /**
     * Handles a received JSON-RPC response by completing the corresponding future.
     * 
     * @param response The received response
     */
    private void handleResponse(JsonRpcResponse response) {
        CompletableFuture<JsonRpcResponse> future = pendingRequests.remove(response.id());
        if (future != null) {
            future.complete(response);
        } else {
            logger.warn("Received response for unknown request ID: {}", response.id());
        }
    }
}