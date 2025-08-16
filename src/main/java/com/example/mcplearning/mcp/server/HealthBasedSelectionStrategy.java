package com.example.mcplearning.mcp.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Health-based server selection strategy.
 * 
 * This strategy selects servers based on their health metrics, prioritizing
 * servers with better performance and reliability. It demonstrates how to
 * implement intelligent load balancing in distributed systems.
 * 
 * Selection criteria (in order of priority):
 * 1. Server must be ready and operational
 * 2. Prefer servers with recent successful operations
 * 3. Prefer servers with faster response times
 * 4. Prefer servers with higher success rates
 * 
 * Educational concepts demonstrated:
 * - Health-based load balancing
 * - Multi-criteria decision making
 * - Performance-aware server selection
 */
@Component
public class HealthBasedSelectionStrategy implements ServerSelectionStrategy {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthBasedSelectionStrategy.class);
    
    @Override
    public String selectServer(List<String> availableServers, MCPServerManager serverManager) {
        if (availableServers == null || availableServers.isEmpty()) {
            return null;
        }
        
        logger.debug("Selecting server using health-based strategy from {} candidates", availableServers.size());
        
        String bestServer = null;
        double bestScore = -1.0;
        
        for (String serverId : availableServers) {
            if (!serverManager.isServerReady(serverId)) {
                logger.debug("Skipping non-ready server: {}", serverId);
                continue;
            }
            
            double score = calculateHealthScore(serverId, serverManager);
            logger.debug("Server {} health score: {:.3f}", serverId, score);
            
            if (score > bestScore) {
                bestScore = score;
                bestServer = serverId;
            }
        }
        
        if (bestServer != null) {
            logger.debug("Selected server {} with health score: {:.3f}", bestServer, bestScore);
        } else {
            logger.warn("No suitable server found from {} candidates", availableServers.size());
        }
        
        return bestServer;
    }
    
    @Override
    public String selectServerForTool(String toolName, List<String> availableServers, MCPServerManager serverManager) {
        logger.debug("Selecting server for tool '{}' using health-based strategy", toolName);
        return selectServer(availableServers, serverManager);
    }
    
    @Override
    public String getStrategyName() {
        return "Health-Based Selection";
    }
    
    @Override
    public String getDescription() {
        return "Selects servers based on health metrics including response time, success rate, and recent activity";
    }
    
    /**
     * Calculates a comprehensive health score for a server.
     * 
     * The score is a weighted combination of multiple factors:
     * - Response time (faster is better)
     * - Recent activity (more recent is better)
     * - Connection state (ready servers get bonus)
     * 
     * @param serverId The server to evaluate
     * @param serverManager The server manager for accessing server information
     * @return A health score between 0.0 and 1.0 (higher is better)
     */
    private double calculateHealthScore(String serverId, MCPServerManager serverManager) {
        MCPServerStatus status = serverManager.getServerStatus(serverId);
        if (status == null || !status.isHealthy()) {
            return 0.0;
        }
        
        double score = 0.0;
        
        // Base score for being ready
        if (status.state() == MCPConnectionState.READY) {
            score += 0.4;
        } else if (status.state() == MCPConnectionState.CONNECTED) {
            score += 0.2;
        }
        
        // Response time factor (up to 0.3 points)
        if (status.responseTime() != null) {
            double responseTimeScore = calculateResponseTimeScore(status.responseTime());
            score += responseTimeScore * 0.3;
        }
        
        // Recent activity factor (up to 0.3 points)
        if (status.lastHealthCheck() != null) {
            double activityScore = calculateActivityScore(status.lastHealthCheck());
            score += activityScore * 0.3;
        }
        
        return Math.min(score, 1.0);
    }
    
    /**
     * Calculates a score based on response time (faster is better).
     * 
     * @param responseTime The server's response time
     * @return A score between 0.0 and 1.0
     */
    private double calculateResponseTimeScore(Duration responseTime) {
        long millis = responseTime.toMillis();
        
        if (millis <= 50) {
            return 1.0; // Excellent response time
        } else if (millis <= 100) {
            return 0.8; // Good response time
        } else if (millis <= 500) {
            return 0.6; // Acceptable response time
        } else if (millis <= 1000) {
            return 0.4; // Slow response time
        } else {
            return 0.2; // Very slow response time
        }
    }
    
    /**
     * Calculates a score based on recent activity (more recent is better).
     * 
     * @param lastActivity The timestamp of the last activity
     * @return A score between 0.0 and 1.0
     */
    private double calculateActivityScore(Instant lastActivity) {
        Duration timeSinceActivity = Duration.between(lastActivity, Instant.now());
        long minutes = timeSinceActivity.toMinutes();
        
        if (minutes <= 1) {
            return 1.0; // Very recent activity
        } else if (minutes <= 5) {
            return 0.8; // Recent activity
        } else if (minutes <= 15) {
            return 0.6; // Somewhat recent activity
        } else if (minutes <= 60) {
            return 0.4; // Old activity
        } else {
            return 0.2; // Very old activity
        }
    }
}