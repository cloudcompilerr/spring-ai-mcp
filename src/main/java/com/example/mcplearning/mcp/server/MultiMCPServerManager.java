package com.example.mcplearning.mcp.server;

import com.example.mcplearning.mcp.client.MCPClient;
import com.example.mcplearning.mcp.config.MCPConfiguration;
import com.example.mcplearning.mcp.config.MCPServerConfig;
import com.example.mcplearning.mcp.protocol.MCPTool;
import com.example.mcplearning.mcp.protocol.MCPResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Multi-server MCP Server Manager implementation.
 * 
 * This implementation extends the single server concept to manage multiple
 * concurrent MCP server connections with advanced features:
 * - Server pool management with load balancing
 * - Tool and resource routing across servers
 * - Conflict resolution for duplicate tool names
 * - Health monitoring and failover mechanisms
 * - Educational logging of multi-server coordination
 * 
 * Key educational concepts demonstrated:
 * - Server selection strategies
 * - Conflict resolution patterns
 * - Distributed system health monitoring
 * - Failover and recovery mechanisms
 */
public class MultiMCPServerManager implements MCPServerManager {
    
    private static final Logger logger = LoggerFactory.getLogger(MultiMCPServerManager.class);
    
    private final MCPConfiguration configuration;
    private final SingleMCPServerManager singleServerManager;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    // Multi-server specific state
    private final Map<String, Set<String>> serverTools = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> serverResources = new ConcurrentHashMap<>();
    private final Map<String, String> toolToServerMapping = new ConcurrentHashMap<>();
    private final Map<String, List<String>> conflictedTools = new ConcurrentHashMap<>();
    private final Map<String, ServerHealthMetrics> serverHealthMetrics = new ConcurrentHashMap<>();
    
    /**
     * Health metrics for individual servers in the pool.
     */
    private static class ServerHealthMetrics {
        volatile int successfulOperations = 0;
        volatile int failedOperations = 0;
        volatile Duration averageResponseTime = Duration.ZERO;
        volatile Instant lastSuccessfulOperation;
        volatile double healthScore = 1.0;
        
        void recordSuccess(Duration responseTime) {
            successfulOperations++;
            lastSuccessfulOperation = Instant.now();
            updateAverageResponseTime(responseTime);
            updateHealthScore();
        }
        
        void recordFailure() {
            failedOperations++;
            updateHealthScore();
        }
        
        private void updateAverageResponseTime(Duration newTime) {
            if (averageResponseTime.equals(Duration.ZERO)) {
                averageResponseTime = newTime;
            } else {
                long avgMillis = averageResponseTime.toMillis();
                long newMillis = newTime.toMillis();
                averageResponseTime = Duration.ofMillis((avgMillis + newMillis) / 2);
            }
        }
        
        private void updateHealthScore() {
            int totalOperations = successfulOperations + failedOperations;
            if (totalOperations == 0) {
                healthScore = 1.0;
            } else {
                healthScore = (double) successfulOperations / totalOperations;
            }
        }
    }
    
    public MultiMCPServerManager(MCPConfiguration configuration, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.singleServerManager = new SingleMCPServerManager(configuration, objectMapper);
        this.scheduler = Executors.newScheduledThreadPool(4);
        
        logger.info("MultiMCPServerManager initialized - multi-server support enabled");
    }
    
    @Override
    public CompletableFuture<Void> addServer(MCPServerConfig config) {
        if (config == null) {
            return CompletableFuture.failedFuture(new MCPServerException("Server configuration cannot be null"));
        }
        
        logger.info("Adding server to multi-server pool: {} ({})", config.name(), config.id());
        
        return singleServerManager.addServer(config)
            .thenCompose(v -> discoverServerCapabilities(config.id()))
            .thenAccept(v -> {
                serverHealthMetrics.put(config.id(), new ServerHealthMetrics());
                logger.info("Server {} successfully added to multi-server pool", config.id());
            })
            .exceptionally(throwable -> {
                logger.error("Failed to add server {} to multi-server pool: {}", 
                    config.id(), throwable.getMessage());
                return null;
            });
    }
    
    @Override
    public CompletableFuture<Void> removeServer(String serverId) {
        logger.info("Removing server from multi-server pool: {}", serverId);
        
        // Clean up multi-server state
        Set<String> tools = serverTools.remove(serverId);
        serverResources.remove(serverId);
        serverHealthMetrics.remove(serverId);
        
        // Remove tool mappings for this server
        if (tools != null) {
            tools.forEach(toolName -> {
                toolToServerMapping.remove(toolName, serverId);
                conflictedTools.remove(toolName);
            });
            
            // Rebuild tool mappings without this server
            rebuildToolMappings();
        }
        
        return singleServerManager.removeServer(serverId);
    }
    
    @Override
    public MCPClient getClient(String serverId) {
        return singleServerManager.getClient(serverId);
    }
    
    /**
     * Gets the best available client for a specific tool.
     * 
     * @param toolName The name of the tool
     * @return The client that can execute the tool, or null if not available
     */
    public MCPClient getClientForTool(String toolName) {
        String serverId = selectServerForTool(toolName);
        return serverId != null ? getClient(serverId) : null;
    }
    
    /**
     * Gets all available tools across all servers.
     * 
     * @return A map of tool names to server IDs that provide them
     */
    public Map<String, String> getAllAvailableTools() {
        return Map.copyOf(toolToServerMapping);
    }
    
    /**
     * Gets tools that are available on multiple servers (conflicts).
     * 
     * @return A map of tool names to lists of server IDs that provide them
     */
    public Map<String, List<String>> getConflictedTools() {
        return Map.copyOf(conflictedTools);
    }
    
    @Override
    public List<MCPServerStatus> getServerStatuses() {
        return singleServerManager.getServerStatuses();
    }
    
    @Override
    public MCPServerStatus getServerStatus(String serverId) {
        return singleServerManager.getServerStatus(serverId);
    }
    
    @Override
    public CompletableFuture<Void> healthCheck() {
        return singleServerManager.healthCheck()
            .thenRun(this::updateServerHealthMetrics);
    }
    
    @Override
    public CompletableFuture<Void> healthCheck(String serverId) {
        return singleServerManager.healthCheck(serverId)
            .thenRun(() -> updateServerHealthMetrics(serverId));
    }
    
    @Override
    public List<String> getServerIds() {
        return singleServerManager.getServerIds();
    }
    
    @Override
    public boolean isServerReady(String serverId) {
        return singleServerManager.isServerReady(serverId);
    }
    
    /**
     * Gets the list of ready servers, ordered by health score.
     * 
     * @return List of server IDs ordered by health (best first)
     */
    public List<String> getReadyServersByHealth() {
        return getServerIds().stream()
            .filter(this::isServerReady)
            .sorted((s1, s2) -> {
                double health1 = serverHealthMetrics.getOrDefault(s1, new ServerHealthMetrics()).healthScore;
                double health2 = serverHealthMetrics.getOrDefault(s2, new ServerHealthMetrics()).healthScore;
                return Double.compare(health2, health1); // Descending order
            })
            .toList();
    }
    
    @Override
    public CompletableFuture<Void> start() {
        if (running.compareAndSet(false, true)) {
            logger.info("Starting MultiMCPServerManager");
            
            return singleServerManager.start()
                .thenCompose(v -> initializeMultiServerFeatures())
                .thenRun(() -> {
                    // Start multi-server specific monitoring
                    scheduler.scheduleWithFixedDelay(
                        this::performMultiServerHealthCheck,
                        configuration.getHealthCheckInterval().toSeconds(),
                        configuration.getHealthCheckInterval().toSeconds(),
                        TimeUnit.SECONDS
                    );
                    
                    // Start capability discovery refresh
                    scheduler.scheduleWithFixedDelay(
                        this::refreshServerCapabilities,
                        Duration.ofMinutes(5).toSeconds(),
                        Duration.ofMinutes(5).toSeconds(),
                        TimeUnit.SECONDS
                    );
                    
                    logger.info("MultiMCPServerManager started successfully");
                });
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public CompletableFuture<Void> stop() {
        if (running.compareAndSet(true, false)) {
            logger.info("Stopping MultiMCPServerManager");
            
            scheduler.shutdown();
            
            return singleServerManager.stop()
                .thenRun(() -> {
                    try {
                        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                            scheduler.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        scheduler.shutdownNow();
                        Thread.currentThread().interrupt();
                    }
                    
                    // Clear multi-server state
                    serverTools.clear();
                    serverResources.clear();
                    toolToServerMapping.clear();
                    conflictedTools.clear();
                    serverHealthMetrics.clear();
                    
                    logger.info("MultiMCPServerManager stopped");
                });
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Override
    public boolean isRunning() {
        return running.get() && singleServerManager.isRunning();
    }
    
    /**
     * Initializes multi-server specific features after basic startup.
     */
    private CompletableFuture<Void> initializeMultiServerFeatures() {
        logger.info("Initializing multi-server features");
        
        List<CompletableFuture<Void>> discoveries = getServerIds().stream()
            .map(this::discoverServerCapabilities)
            .toList();
        
        return CompletableFuture.allOf(discoveries.toArray(new CompletableFuture[0]))
            .thenRun(() -> {
                rebuildToolMappings();
                logMultiServerSummary();
            });
    }
    
    /**
     * Discovers the capabilities (tools and resources) of a specific server.
     */
    private CompletableFuture<Void> discoverServerCapabilities(String serverId) {
        MCPClient client = getClient(serverId);
        if (client == null || !isServerReady(serverId)) {
            logger.debug("Skipping capability discovery for non-ready server: {}", serverId);
            return CompletableFuture.completedFuture(null);
        }
        
        logger.debug("Discovering capabilities for server: {}", serverId);
        
        CompletableFuture<Void> toolDiscovery = client.listTools()
            .thenAccept(tools -> {
                Set<String> toolNames = tools.stream()
                    .map(MCPTool::name)
                    .collect(Collectors.toSet());
                serverTools.put(serverId, toolNames);
                
                if (configuration.isVerboseLogging()) {
                    logger.info("Server {} provides {} tools: {}", 
                        serverId, toolNames.size(), toolNames);
                }
            })
            .exceptionally(throwable -> {
                logger.warn("Failed to discover tools for server {}: {}", serverId, throwable.getMessage());
                return null;
            });
        
        CompletableFuture<Void> resourceDiscovery = client.listResources()
            .thenAccept(resources -> {
                Set<String> resourceUris = resources.stream()
                    .map(MCPResource::uri)
                    .collect(Collectors.toSet());
                serverResources.put(serverId, resourceUris);
                
                if (configuration.isVerboseLogging()) {
                    logger.info("Server {} provides {} resources: {}", 
                        serverId, resourceUris.size(), resourceUris);
                }
            })
            .exceptionally(throwable -> {
                logger.warn("Failed to discover resources for server {}: {}", serverId, throwable.getMessage());
                return null;
            });
        
        return CompletableFuture.allOf(toolDiscovery, resourceDiscovery);
    }
    
    /**
     * Rebuilds tool-to-server mappings and identifies conflicts.
     */
    private void rebuildToolMappings() {
        logger.debug("Rebuilding tool mappings for {} servers", serverTools.size());
        
        toolToServerMapping.clear();
        conflictedTools.clear();
        
        Map<String, List<String>> toolProviders = new HashMap<>();
        
        // Collect all tool providers
        serverTools.forEach((serverId, tools) -> {
            tools.forEach(toolName -> {
                toolProviders.computeIfAbsent(toolName, k -> new ArrayList<>()).add(serverId);
            });
        });
        
        // Process each tool
        toolProviders.forEach((toolName, providers) -> {
            if (providers.size() == 1) {
                // No conflict - single provider
                toolToServerMapping.put(toolName, providers.get(0));
            } else {
                // Conflict - multiple providers
                conflictedTools.put(toolName, new ArrayList<>(providers));
                
                // Apply conflict resolution strategy
                String selectedServer = resolveToolConflict(toolName, providers);
                if (selectedServer != null) {
                    toolToServerMapping.put(toolName, selectedServer);
                }
                
                if (configuration.isVerboseLogging()) {
                    logger.info("Tool conflict resolved for '{}': {} providers, selected server: {}", 
                        toolName, providers.size(), selectedServer);
                }
            }
        });
        
        logger.info("Tool mapping complete: {} tools mapped, {} conflicts resolved", 
            toolToServerMapping.size(), conflictedTools.size());
    }
    
    /**
     * Resolves conflicts when multiple servers provide the same tool.
     * 
     * Current strategy: Select the server with the best health score.
     * Future strategies could include: round-robin, server priority, load balancing.
     */
    private String resolveToolConflict(String toolName, List<String> providers) {
        // Strategy 1: Select server with best health score
        return providers.stream()
            .filter(this::isServerReady)
            .max((s1, s2) -> {
                double health1 = serverHealthMetrics.getOrDefault(s1, new ServerHealthMetrics()).healthScore;
                double health2 = serverHealthMetrics.getOrDefault(s2, new ServerHealthMetrics()).healthScore;
                return Double.compare(health1, health2);
            })
            .orElse(providers.get(0)); // Fallback to first provider
    }
    
    /**
     * Selects the best server for executing a specific tool.
     */
    private String selectServerForTool(String toolName) {
        // First check direct mapping
        String mappedServer = toolToServerMapping.get(toolName);
        if (mappedServer != null && isServerReady(mappedServer)) {
            return mappedServer;
        }
        
        // If mapped server is not ready, try alternatives from conflicts
        List<String> alternatives = conflictedTools.get(toolName);
        if (alternatives != null) {
            return alternatives.stream()
                .filter(this::isServerReady)
                .findFirst()
                .orElse(null);
        }
        
        return null;
    }
    
    /**
     * Updates health metrics for all servers.
     */
    private void updateServerHealthMetrics() {
        getServerIds().forEach(this::updateServerHealthMetrics);
    }
    
    /**
     * Updates health metrics for a specific server.
     */
    private void updateServerHealthMetrics(String serverId) {
        MCPServerStatus status = getServerStatus(serverId);
        if (status == null) {
            return;
        }
        
        ServerHealthMetrics metrics = serverHealthMetrics.computeIfAbsent(serverId, k -> new ServerHealthMetrics());
        
        if (status.isHealthy() && status.responseTime() != null) {
            metrics.recordSuccess(status.responseTime());
        } else if (status.isError()) {
            metrics.recordFailure();
        }
    }
    
    /**
     * Performs multi-server specific health checks.
     */
    private void performMultiServerHealthCheck() {
        if (!running.get()) {
            return;
        }
        
        logger.debug("Performing multi-server health check");
        
        // Check for failed servers and attempt recovery
        getServerIds().stream()
            .filter(serverId -> !isServerReady(serverId))
            .forEach(this::attemptServerRecovery);
        
        // Log health summary if verbose logging is enabled
        if (configuration.isVerboseLogging()) {
            logHealthSummary();
        }
    }
    
    /**
     * Attempts to recover a failed server.
     */
    private void attemptServerRecovery(String serverId) {
        MCPServerStatus status = getServerStatus(serverId);
        if (status == null || !status.isError()) {
            return;
        }
        
        logger.info("Attempting recovery for failed server: {}", serverId);
        
        // Simple recovery: remove and re-add the server
        removeServer(serverId)
            .thenCompose(v -> addServer(status.config()))
            .exceptionally(throwable -> {
                logger.warn("Server recovery failed for {}: {}", serverId, throwable.getMessage());
                return null;
            });
    }
    
    /**
     * Refreshes server capabilities periodically.
     */
    private void refreshServerCapabilities() {
        if (!running.get()) {
            return;
        }
        
        logger.debug("Refreshing server capabilities");
        
        List<CompletableFuture<Void>> refreshTasks = getServerIds().stream()
            .filter(this::isServerReady)
            .map(this::discoverServerCapabilities)
            .toList();
        
        CompletableFuture.allOf(refreshTasks.toArray(new CompletableFuture[0]))
            .thenRun(this::rebuildToolMappings)
            .exceptionally(throwable -> {
                logger.warn("Capability refresh failed: {}", throwable.getMessage());
                return null;
            });
    }
    
    /**
     * Logs a summary of the multi-server setup.
     */
    private void logMultiServerSummary() {
        int totalServers = getServerIds().size();
        int readyServers = (int) getServerIds().stream().filter(this::isServerReady).count();
        int totalTools = toolToServerMapping.size();
        int conflicts = conflictedTools.size();
        
        logger.info("Multi-server summary: {}/{} servers ready, {} tools available, {} conflicts resolved",
            readyServers, totalServers, totalTools, conflicts);
    }
    
    /**
     * Logs health summary for all servers.
     */
    private void logHealthSummary() {
        serverHealthMetrics.forEach((serverId, metrics) -> {
            logger.debug("Server {} health: score={:.2f}, success={}, failed={}, avgResponse={}ms",
                serverId, metrics.healthScore, metrics.successfulOperations, 
                metrics.failedOperations, metrics.averageResponseTime.toMillis());
        });
    }
}