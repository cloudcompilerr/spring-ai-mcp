package com.example.mcplearning.mcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Round-robin server selection strategy.
 * 
 * This strategy distributes requests evenly across all available servers
 * by cycling through them in order. It's a simple but effective load
 * balancing strategy that ensures fair distribution of work.
 * 
 * Educational concepts demonstrated:
 * - Round-robin load balancing algorithm
 * - Thread-safe counter management
 * - Simple but effective distribution strategy
 * - Stateful selection strategy implementation
 */
@Component
public class RoundRobinSelectionStrategy implements ServerSelectionStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(RoundRobinSelectionStrategy.class);
    
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public String selectServer(List<String> availableServers, MCPServerManager serverManager) {
        if (availableServers == null || availableServers.isEmpty()) {
            return null;
        }
        
        // Filter to only ready servers
        List<String> readyServers = availableServers.stream()
            .filter(serverManager::isServerReady)
            .toList();
        
        if (readyServers.isEmpty()) {
            logger.warn("No ready servers available for round-robin selection");
            return null;
        }
        
        // Use round-robin to select from ready servers
        int index = counter.getAndIncrement() % readyServers.size();
        String selectedServer = readyServers.get(index);
        
        logger.debug("Round-robin selected server {} (index {} of {} ready servers)", 
            selectedServer, index, readyServers.size());
        
        return selectedServer;
    }
    
    @Override
    public String selectServerForTool(String toolName, List<String> availableServers, MCPServerManager serverManager) {
        logger.debug("Selecting server for tool '{}' using round-robin strategy", toolName);
        return selectServer(availableServers, serverManager);
    }
    
    @Override
    public String getStrategyName() {
        return "Round-Robin Selection";
    }
    
    @Override
    public String getDescription() {
        return "Distributes requests evenly across all available servers by cycling through them in order";
    }
    
    /**
     * Resets the round-robin counter.
     * Useful for testing or when server list changes significantly.
     */
    public void resetCounter() {
        counter.set(0);
        logger.debug("Round-robin counter reset");
    }
    
    /**
     * Gets the current counter value.
     * Useful for monitoring and debugging.
     * 
     * @return The current counter value
     */
    public int getCurrentCounter() {
        return counter.get();
    }
}