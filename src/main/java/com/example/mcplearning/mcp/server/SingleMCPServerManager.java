package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.DefaultMCPClient;
import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.client.MCPClientException;
import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.example.mcplearning.mcp.protocol.MCPClientInfo;
import com.example.mcplearning.mcp.protocol.MCPInitializeRequest;
import com.example.mcplearning.mcp.transport.MCPTransport;
import com.example.mcplearning.mcp.transport.MCPTransportException;
import com.example.mcplearning.mcp.transport.ProcessMCPTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Single MCP Server Manager implementation.
 * 
 * This implementation focuses on managing a single MCP server connection
 * with comprehensive lifecycle management, error handling, and educational
 * logging. It serves as the foundation for understanding MCP server
 * management before moving to multi-server scenarios.
 * 
 * Key features:
 * - Complete server lifecycle management (connect, initialize, disconnect)
 * - Comprehensive error handling and recovery
 * - Educational logging of all MCP operations
 * - Health monitoring and status reporting
 * - Configurable retry logic with exponential backoff
 */
@Component
public class SingleMCPServerManager implements MCPServerManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SingleMCPServerManager.class);
    
    private final MCPConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // Server state management
    private final Map<String, MCPServerState> serverStates = new ConcurrentHashMap<>();
    
    /**
     * Internal state holder for a managed server.
     */
    private static class MCPServerState {
        final MCPServerConfig config;
        volatile MCPClient client;
        volatile MCPTransport transport;
        volatile MCPServerStatus status;
        volatile int connectionAttempts;
        volatile Instant lastConnectionAttempt;
        
        MCPServerState(MCPServerConfig config) {
            this.config = config;
            this.status = MCPServerStatus.disconnected(config);
            this.connectionAttempts = 0;
        }
    }
    
    public SingleMCPServerManager(MCPConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        logger.info("SingleMCPServerManager initialized with configuration: enableMultiServer={}, maxRetries={}, connectionTimeout={}",
            configuration.isEnableMultiServer(), configuration.getMaxRetries(), configuration.getConnectionTimeout());
    }
    
    @Override
    public CompletableFuture<Void> addServer(MCPServerConfig config) {
        if (config == null) {
            return CompletableFuture.failedFuture(new MCPServerException("Server configuration cannot be null"));
        }
        
        if (!config.enabled()) {
            logger.info("Skipping disabled server: {} ({})", config.name(), config.id());
            return CompletableFuture.completedFuture(null);
        }
        
        logger.info("Adding MCP server: {} ({})", config.name(), config.id());
        
        MCPServerState state = new MCPServerState(config);
        serverStates.put(config.id(), state);
        
        return connectToServer(state);
    }
    
    @Override
    public CompletableFuture<Void> removeServer(String serverId) {
        logger.info("Removing MCP server: {}", serverId);
        
        MCPServerState state = serverStates.remove(serverId);
        if (state == null) {
            logger.warn("Attempted to remove non-existent server: {}", serverId);
            return CompletableFuture.completedFuture(null);
        }
        
        return disconnectFromServer(state);
    }
    
    @Override
    public MCPClient getClient(String serverId) {
        MCPServerState state = serverStates.get(serverId);
        return state != null ? state.client : null;
    }
    
    @Override
    public List<MCPServerStatus> getServerStatuses() {
        return serverStates.values().stream()
            .map(state -> state.status)
            .toList();
    }
    
    @Override
    public MCPServerStatus getServerStatus(String serverId) {
        MCPServerState state = serverStates.get(serverId);
        return state != null ? state.status : null;
    }
    
    @Override
    public CompletableFuture<Void> healthCheck() {
        List<CompletableFuture<Void>> healthChecks = serverStates.keySet().stream()
            .map(this::healthCheck)
            .toList();
        
        return CompletableFuture.allOf(healthChecks.toArray(new CompletableFuture[0]));
    }
    
    @Override
    public CompletableFuture<Void> healthCheck(String serverId) {
        MCPServerState state = serverStates.get(serverId);
        if (state == null) {
            return CompletableFuture.failedFuture(new MCPServerException(serverId, "Server not found"));
        }
        
        if (state.client == null || !state.client.isConnected()) {
            logger.debug("Health check failed for {}: client not connected", serverId);
            updateServerStatus(state, state.status.withError("Client not connected"));
            return CompletableFuture.completedFuture(null);
        }
        
        Instant start = Instant.now();
        
        // Perform a simple health check by listing tools
        return state.client.listTools()
            .thenAccept(tools -> {
                Duration responseTime = Duration.between(start, Instant.now());
                logger.debug("Health check successful for {}: {} tools available, response time: {}ms", 
                    serverId, tools.size(), responseTime.toMillis());
                updateServerStatus(state, state.status.withHealthCheck(responseTime));
            })
            .exceptionally(throwable -> {
                logger.warn("Health check failed for {}: {}", serverId, throwable.getMessage());
                updateServerStatus(state, state.status.withError("Health check failed: " + throwable.getMessage()));
                return null;
            });
    }
    
    @Override
    public List<String> getServerIds() {
        return List.copyOf(serverStates.keySet());
    }
    
    @Override
    public boolean isServerReady(String serverId) {
        MCPServerState state = serverStates.get(serverId);
        return state != null && state.status.state() == MCPConnectionState.READY;
    }
    
    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting SingleMCPServerManager");
            
            // Start health check scheduler
            scheduler.scheduleWithFixedDelay(
                this::performScheduledHealthCheck,
                configuration.getHealthCheckInterval().toSeconds(),
                configuration.getHealthCheckInterval().toSeconds(),
                TimeUnit.SECONDS
            );
            
            // Connect to configured servers
            List<CompletableFuture<Void>> connections = configuration.getEffectiveServers().stream()
                .map(this::addServer)
                .toList();
            
            return CompletableFuture.allOf(connections.toArray(new CompletableFuture[0]))
                .thenRun(() -> logger.info("SingleMCPServerManager started successfully"));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping SingleMCPServerManager");
            
            scheduler.shutdown();
            
            List<CompletableFuture<Void>> disconnections = serverStates.keySet().stream()
                .map(this::removeServer)
                .toList();
            
            return CompletableFuture.allOf(disconnections.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    try {
                        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    logger.info("SingleMCPServerManager stopped");
                });
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Connects to a server with retry logic and comprehensive error handling.
     */
    private CompletableFuture<Void> connectToServer(MCPServerState state) {
        return connectToServerWithRetry(state, 0);
    }
    
    /**
     * Recursive method to handle connection retries with exponential backoff.
     */
    private CompletableFuture<Void> connectToServerWithRetry(MCPServerState state, int attempt) {
        if (attempt >= configuration.getMaxRetries()) {
            String errorMsg = String.format("Failed to connect after %d attempts", configuration.getMaxRetries());
            logger.error("Connection failed for server {} ({}): {}", state.config.name(), state.config.id(), errorMsg);
            updateServerStatus(state, MCPServerStatus.error(state.config, errorMsg, attempt));
            return CompletableFuture.failedFuture(new MCPServerException(state.config.id(), errorMsg));
        }
        
        logger.info("Connecting to MCP server {} ({}) - attempt {} of {}", 
            state.config.name(), state.config.id(), attempt + 1, configuration.getMaxRetries());
        
        updateServerStatus(state, MCPServerStatus.connecting(state.config, attempt + 1));
        
        return establishConnection(state)
            .thenCompose(this::initializeConnection)
            .thenAccept(client -> {
                state.client = client;
                updateServerStatus(state, MCPServerStatus.ready(state.config, Duration.ofMillis(100)));
                logger.info("Successfully connected and initialized server {} ({})", 
                    state.config.name(), state.config.id());
            })
            .exceptionally(throwable -> {
                logger.warn("Connection attempt {} failed for server {} ({}): {}", 
                    attempt + 1, state.config.name(), state.config.id(), throwable.getMessage());
                
                if (configuration.isVerboseLogging()) {
                    logger.debug("Connection failure details:", throwable);
                }
                
                // Calculate delay for next attempt (exponential backoff)
                long delaySeconds = Math.min(configuration.getRetryDelay().toSeconds() * (1L << attempt), 60);
                
                if (attempt + 1 < configuration.getMaxRetries()) {
                    logger.info("Retrying connection to {} ({}) in {} seconds", 
                        state.config.name(), state.config.id(), delaySeconds);
                    
                    // Schedule retry
                    scheduler.schedule(
                        () -> connectToServerWithRetry(state, attempt + 1),
                        delaySeconds,
                        TimeUnit.SECONDS
                    );
                } else {
                    updateServerStatus(state, MCPServerStatus.error(state.config, throwable.getMessage(), attempt + 1));
                }
                
                return null;
            });
    }
    
    /**
     * Establishes the transport connection to the server.
     */
    private CompletableFuture<MCPTransport> establishConnection(MCPServerState state) {
        logger.debug("Creating transport for server {} ({}): {}", 
            state.config.name(), state.config.id(), state.config.getFullCommandLine());
        
        MCPTransport transport = new ProcessMCPTransport(
            state.config.command(),
            state.config.args(),
            state.config.env(),
            objectMapper,
            configuration.getConnectionTimeout().toMillis()
        );
        
        return transport.connect()
            .thenApply(v -> {
                state.transport = transport;
                updateServerStatus(state, MCPServerStatus.connected(state.config, Duration.ofMillis(50)));
                
                logger.debug("Transport connection established for server {} ({})", 
                    state.config.name(), state.config.id());
                
                return transport;
            })
            .exceptionally(throwable -> {
                throw new MCPTransportException("Failed to establish transport connection", throwable);
            });
    }
    
    /**
     * Initializes the MCP protocol with the server.
     */
    private CompletableFuture<MCPClient> initializeConnection(MCPTransport transport) {
        logger.debug("Initializing MCP client");
        
        MCPClient client = new DefaultMCPClient(transport, objectMapper);
        
        // Create initialization request
        MCPClientInfo clientInfo = new MCPClientInfo(
            "MCP Learning Platform",
            "1.0.0"
        );
        
        MCPInitializeRequest initRequest = new MCPInitializeRequest(
            "2024-11-05",
            clientInfo
        );
        
        // Perform initialization
        return client.initialize(initRequest)
            .thenApply(response -> {
                logger.debug("MCP client initialized successfully");
                return client;
            })
            .exceptionally(throwable -> {
                throw new MCPClientException("Failed to initialize MCP client", throwable);
            });
    }
    
    /**
     * Disconnects from a server and cleans up resources.
     */
    private CompletableFuture<Void> disconnectFromServer(MCPServerState state) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (state.client != null) {
                    logger.debug("Closing MCP client for server {} ({})", state.config.name(), state.config.id());
                    state.client.close();
                    state.client = null;
                }
                
                if (state.transport != null) {
                    logger.debug("Closing transport for server {} ({})", state.config.name(), state.config.id());
                    state.transport.close();
                    state.transport = null;
                }
                
                updateServerStatus(state, MCPServerStatus.disconnected(state.config));
                
                logger.info("Successfully disconnected from server {} ({})", state.config.name(), state.config.id());
                
            } catch (Exception e) {
                logger.warn("Error during disconnection from server {} ({}): {}", 
                    state.config.name(), state.config.id(), e.getMessage());
            }
        });
    }
    
    /**
     * Updates the status of a server and logs the change if verbose logging is enabled.
     */
    private void updateServerStatus(MCPServerState state, MCPServerStatus newStatus) {
        MCPServerStatus oldStatus = state.status;
        state.status = newStatus;
        
        if (configuration.isVerboseLogging() && oldStatus.state() != newStatus.state()) {
            logger.info("Server {} ({}) state changed: {} -> {}", 
                state.config.name(), state.config.id(), 
                oldStatus.state().getDescription(), newStatus.state().getDescription());
        }
    }
    
    /**
     * Performs scheduled health checks on all servers.
     */
    private void performScheduledHealthCheck() {
        if (!running.get()) {
            return;
        }
        
        logger.debug("Performing scheduled health check on {} servers", serverStates.size());
        
        healthCheck().exceptionally(throwable -> {
            logger.warn("Scheduled health check failed: {}", throwable.getMessage());
            return null;
        });
    }
}