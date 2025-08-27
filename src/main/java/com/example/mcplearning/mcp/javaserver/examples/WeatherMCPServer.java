package com.example.mcplearning.mcp.javaserver.examples;

import com.example.mcplearning.mcp.javaserver.MCPServer;
import com.example.mcplearning.mcp.javaserver.MCPServerTool;
import com.example.mcplearning.mcp.javaserver.MCPServerResource;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Weather MCP Server - Educational Example
 * 
 * EDUCATIONAL OVERVIEW:
 * This demonstrates a more complex MCP server that simulates weather services.
 * It shows how to build servers that provide real-world functionality while
 * maintaining educational value.
 * 
 * FEATURES:
 * - Current weather lookup (simulated)
 * - Weather forecasts
 * - Location-based services
 * - Weather alerts and warnings
 * - Historical weather data
 * - Weather statistics and trends
 * 
 * NOTE: This is a simulation for educational purposes.
 * In a real implementation, you would integrate with actual weather APIs.
 */
public class WeatherMCPServer extends MCPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherMCPServer.class);
    
    // Simulated weather data
    private final Map<String, WeatherData> weatherCache = new HashMap<>();
    private final List<WeatherQuery> queryHistory = new ArrayList<>();
    private final Random random = ThreadLocalRandom.current();
    
    public WeatherMCPServer() {
        super("weather-server", "1.0.0");
        logger.info("🌤️ Weather MCP Server initialized");
    }
    
    @Override
    protected void initializeServer() {
        logger.info("🔧 Initializing Weather MCP Server...");
        
        // Initialize some sample weather data
        initializeSampleData();
        
        // Register tools
        registerTool(new CurrentWeatherTool());
        registerTool(new WeatherForecastTool());
        registerTool(new WeatherAlertsTool());
        registerTool(new WeatherHistoryTool());
        
        // Register resources
        registerResource(new WeatherStationsResource());
        registerResource(new WeatherAlertsResource());
        registerResource(new WeatherStatsResource());
        
        logger.info("✅ Weather MCP Server initialization complete");
    }
    
    private void initializeSampleData() {
        // Add some sample cities with weather data
        weatherCache.put("london", new WeatherData("London", 15.5, 65, "Partly Cloudy", "SW", 12));
        weatherCache.put("new york", new WeatherData("New York", 22.1, 58, "Sunny", "NW", 8));
        weatherCache.put("tokyo", new WeatherData("Tokyo", 18.3, 72, "Rainy", "E", 15));
        weatherCache.put("sydney", new WeatherData("Sydney", 25.7, 45, "Clear", "S", 5));
        weatherCache.put("paris", new WeatherData("Paris", 12.8, 68, "Overcast", "W", 10));
    }
    
    /**
     * Weather data model
     */
    private static class WeatherData {
        final String location;
        final double temperature;
        final int humidity;
        final String condition;
        final String windDirection;
        final int windSpeed;
        final LocalDateTime lastUpdated;
        
        WeatherData(String location, double temperature, int humidity, String condition, String windDirection, int windSpeed) {
            this.location = location;
            this.temperature = temperature;
            this.humidity = humidity;
            this.condition = condition;
            this.windDirection = windDirection;
            this.windSpeed = windSpeed;
            this.lastUpdated = LocalDateTime.now();
        }
        
        @Override
        public String toString() {
            return String.format("%s: %.1f°C, %s, Humidity: %d%%, Wind: %s %d km/h", 
                location, temperature, condition, humidity, windDirection, windSpeed);
        }
    }
    
    /**
     * Weather query record for history
     */
    private static class WeatherQuery {
        final String location;
        final String queryType;
        final LocalDateTime timestamp;
        final boolean successful;
        
        WeatherQuery(String location, String queryType, boolean successful) {
            this.location = location;
            this.queryType = queryType;
            this.timestamp = LocalDateTime.now();
            this.successful = successful;
        }
    }
    
    /**
     * Current Weather Tool
     */
    private class CurrentWeatherTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "get_current_weather";
        }
        
        @Override
        public String getDescription() {
            return "Gets the current weather conditions for a specified location";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "location", Map.of(
                        "type", "string",
                        "description", "The city or location to get weather for"
                    ),
                    "units", Map.of(
                        "type", "string",
                        "description", "Temperature units (celsius or fahrenheit)",
                        "enum", List.of("celsius", "fahrenheit"),
                        "default", "celsius"
                    )
                ),
                "required", List.of("location")
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            String location = (String) arguments.get("location");
            String units = (String) arguments.getOrDefault("units", "celsius");
            
            if (location == null || location.trim().isEmpty()) {
                queryHistory.add(new WeatherQuery("", "current", false));
                return MCPToolResult.error("Location is required");
            }
            
            logger.info("🌤️ Getting current weather for: {}", location);
            
            // Simulate weather lookup
            WeatherData weather = getOrCreateWeatherData(location.toLowerCase().trim());
            queryHistory.add(new WeatherQuery(location, "current", true));
            
            double temp = weather.temperature;
            if ("fahrenheit".equalsIgnoreCase(units)) {
                temp = (temp * 9.0 / 5.0) + 32;
            }
            
            String response = String.format("""
                🌤️ Current Weather for %s
                
                Temperature: %.1f°%s
                Condition: %s
                Humidity: %d%%
                Wind: %s %d km/h
                Last Updated: %s
                
                💡 Educational Note: This is simulated weather data for demonstration purposes.
                In a real implementation, this would connect to weather APIs like OpenWeatherMap.
                """, 
                weather.location, 
                temp, 
                "fahrenheit".equalsIgnoreCase(units) ? "F" : "C",
                weather.condition,
                weather.humidity,
                weather.windDirection,
                weather.windSpeed,
                weather.lastUpdated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            
            return MCPToolResult.success(response, "text/plain");
        }
    }
    
    /**
     * Weather Forecast Tool
     */
    private class WeatherForecastTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "get_weather_forecast";
        }
        
        @Override
        public String getDescription() {
            return "Gets the weather forecast for a specified location";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "location", Map.of(
                        "type", "string",
                        "description", "The city or location to get forecast for"
                    ),
                    "days", Map.of(
                        "type", "integer",
                        "description", "Number of days to forecast (1-7)",
                        "minimum", 1,
                        "maximum", 7,
                        "default", 3
                    )
                ),
                "required", List.of("location")
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            String location = (String) arguments.get("location");
            Object daysObj = arguments.get("days");
            int days = daysObj != null ? ((Number) daysObj).intValue() : 3;
            
            if (location == null || location.trim().isEmpty()) {
                queryHistory.add(new WeatherQuery("", "forecast", false));
                return MCPToolResult.error("Location is required");
            }
            
            days = Math.max(1, Math.min(7, days)); // Clamp to valid range
            
            logger.info("📅 Getting {}-day forecast for: {}", days, location);
            
            WeatherData baseWeather = getOrCreateWeatherData(location.toLowerCase().trim());
            queryHistory.add(new WeatherQuery(location, "forecast", true));
            
            StringBuilder forecast = new StringBuilder();
            forecast.append(String.format("📅 %d-Day Weather Forecast for %s\n\n", days, baseWeather.location));
            
            String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Rainy", "Stormy", "Clear"};
            
            for (int i = 0; i < days; i++) {
                LocalDateTime date = LocalDateTime.now().plusDays(i);
                double temp = baseWeather.temperature + random.nextGaussian() * 3; // Vary temperature
                String condition = conditions[random.nextInt(conditions.length)];
                int humidity = Math.max(20, Math.min(90, baseWeather.humidity + random.nextInt(21) - 10));
                
                forecast.append(String.format("Day %d (%s):\n", i + 1, 
                    date.format(DateTimeFormatter.ofPattern("MMM dd"))));
                forecast.append(String.format("  Temperature: %.1f°C\n", temp));
                forecast.append(String.format("  Condition: %s\n", condition));
                forecast.append(String.format("  Humidity: %d%%\n", humidity));
                forecast.append("\n");
            }
            
            forecast.append("💡 Educational Note: This forecast is generated using random variations ");
            forecast.append("for demonstration. Real weather forecasts use complex meteorological models.");
            
            return MCPToolResult.success(forecast.toString(), "text/plain");
        }
    }
    
    /**
     * Weather Alerts Tool
     */
    private class WeatherAlertsTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "get_weather_alerts";
        }
        
        @Override
        public String getDescription() {
            return "Gets weather alerts and warnings for a specified location";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "location", Map.of(
                        "type", "string",
                        "description", "The city or location to get alerts for"
                    )
                ),
                "required", List.of("location")
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            String location = (String) arguments.get("location");
            
            if (location == null || location.trim().isEmpty()) {
                queryHistory.add(new WeatherQuery("", "alerts", false));
                return MCPToolResult.error("Location is required");
            }
            
            logger.info("⚠️ Getting weather alerts for: {}", location);
            
            WeatherData weather = getOrCreateWeatherData(location.toLowerCase().trim());
            queryHistory.add(new WeatherQuery(location, "alerts", true));
            
            // Simulate weather alerts based on conditions
            List<String> alerts = new ArrayList<>();
            
            if (weather.windSpeed > 20) {
                alerts.add("🌪️ High Wind Warning: Winds exceeding 20 km/h expected");
            }
            
            if (weather.humidity > 80) {
                alerts.add("🌧️ High Humidity Advisory: Humidity levels above 80%");
            }
            
            if (weather.temperature > 30) {
                alerts.add("🌡️ Heat Advisory: High temperatures expected");
            } else if (weather.temperature < 0) {
                alerts.add("❄️ Freeze Warning: Temperatures below freezing");
            }
            
            // Random chance of additional alerts for demonstration
            if (random.nextDouble() < 0.3) {
                String[] possibleAlerts = {
                    "⛈️ Thunderstorm Watch: Conditions favorable for thunderstorms",
                    "🌫️ Fog Advisory: Reduced visibility due to fog",
                    "🌨️ Snow Advisory: Light snow expected",
                    "☀️ UV Index High: Strong UV radiation levels"
                };
                alerts.add(possibleAlerts[random.nextInt(possibleAlerts.length)]);
            }
            
            StringBuilder response = new StringBuilder();
            response.append(String.format("⚠️ Weather Alerts for %s\n\n", weather.location));
            
            if (alerts.isEmpty()) {
                response.append("✅ No active weather alerts or warnings.\n");
                response.append("Current conditions are within normal ranges.\n");
            } else {
                response.append(String.format("📢 %d Active Alert(s):\n\n", alerts.size()));
                for (int i = 0; i < alerts.size(); i++) {
                    response.append(String.format("%d. %s\n", i + 1, alerts.get(i)));
                }
            }
            
            response.append("\n💡 Educational Note: These alerts are generated based on simulated ");
            response.append("weather conditions. Real weather services provide official alerts from meteorological agencies.");
            
            return MCPToolResult.success(response.toString(), "text/plain");
        }
    }
    
    /**
     * Weather History Tool
     */
    private class WeatherHistoryTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "get_query_history";
        }
        
        @Override
        public String getDescription() {
            return "Gets the history of weather queries made to this server";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "limit", Map.of(
                        "type", "integer",
                        "description", "Maximum number of history entries to return",
                        "default", 10
                    )
                )
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            Object limitObj = arguments.get("limit");
            int limit = limitObj != null ? ((Number) limitObj).intValue() : 10;
            
            logger.info("📜 Getting weather query history (limit: {})", limit);
            
            if (queryHistory.isEmpty()) {
                return MCPToolResult.success(
                    "📜 Weather Query History\n\nNo weather queries have been made yet.\n\n💡 Try using the weather tools to build up some history!",
                    "text/plain"
                );
            }
            
            StringBuilder history = new StringBuilder("📜 Weather Query History\n\n");
            
            int start = Math.max(0, queryHistory.size() - limit);
            for (int i = start; i < queryHistory.size(); i++) {
                WeatherQuery query = queryHistory.get(i);
                String status = query.successful ? "✅" : "❌";
                history.append(String.format("%d. %s [%s] %s query for '%s'\n", 
                    i + 1, 
                    status,
                    query.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    query.queryType,
                    query.location
                ));
            }
            
            long successCount = queryHistory.stream().mapToLong(q -> q.successful ? 1 : 0).sum();
            history.append(String.format("\n📊 Statistics: %d total queries, %d successful (%.1f%%)", 
                queryHistory.size(), successCount, 
                queryHistory.isEmpty() ? 0.0 : (successCount * 100.0 / queryHistory.size())));
            
            return MCPToolResult.success(history.toString(), "text/plain");
        }
    }
    
    // Resources
    
    private class WeatherStationsResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "weather://stations";
        }
        
        @Override
        public String getName() {
            return "Weather Stations";
        }
        
        @Override
        public String getDescription() {
            return "List of available weather monitoring stations";
        }
        
        @Override
        public String getMimeType() {
            return "application/json";
        }
        
        @Override
        public String read() throws Exception {
            StringBuilder json = new StringBuilder("{\n");
            json.append("  \"weatherStations\": [\n");
            
            int i = 0;
            for (WeatherData weather : weatherCache.values()) {
                json.append("    {\n");
                json.append("      \"location\": \"").append(weather.location).append("\",\n");
                json.append("      \"status\": \"active\",\n");
                json.append("      \"lastUpdate\": \"").append(weather.lastUpdated).append("\",\n");
                json.append("      \"coordinates\": {\n");
                json.append("        \"lat\": ").append(random.nextDouble() * 180 - 90).append(",\n");
                json.append("        \"lon\": ").append(random.nextDouble() * 360 - 180).append("\n");
                json.append("      }\n");
                json.append("    }");
                if (++i < weatherCache.size()) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ],\n");
            json.append("  \"totalStations\": ").append(weatherCache.size()).append(",\n");
            json.append("  \"lastUpdated\": \"").append(LocalDateTime.now()).append("\"\n");
            json.append("}");
            
            return json.toString();
        }
    }
    
    private class WeatherAlertsResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "weather://alerts";
        }
        
        @Override
        public String getName() {
            return "Active Weather Alerts";
        }
        
        @Override
        public String getDescription() {
            return "Current active weather alerts across all monitored locations";
        }
        
        @Override
        public String getMimeType() {
            return "text/plain";
        }
        
        @Override
        public String read() throws Exception {
            StringBuilder alerts = new StringBuilder("🌍 Global Weather Alerts Summary\n\n");
            
            int totalAlerts = 0;
            for (WeatherData weather : weatherCache.values()) {
                List<String> locationAlerts = new ArrayList<>();
                
                if (weather.windSpeed > 20) {
                    locationAlerts.add("High Wind Warning");
                }
                if (weather.humidity > 80) {
                    locationAlerts.add("High Humidity Advisory");
                }
                if (weather.temperature > 30) {
                    locationAlerts.add("Heat Advisory");
                } else if (weather.temperature < 0) {
                    locationAlerts.add("Freeze Warning");
                }
                
                if (!locationAlerts.isEmpty()) {
                    alerts.append(String.format("📍 %s (%d alert%s):\n", 
                        weather.location, locationAlerts.size(), 
                        locationAlerts.size() == 1 ? "" : "s"));
                    for (String alert : locationAlerts) {
                        alerts.append("  - ").append(alert).append("\n");
                    }
                    alerts.append("\n");
                    totalAlerts += locationAlerts.size();
                }
            }
            
            if (totalAlerts == 0) {
                alerts.append("✅ No active weather alerts at this time.\n");
                alerts.append("All monitored locations are experiencing normal conditions.\n");
            } else {
                alerts.insert(0, String.format("⚠️ %d active weather alert%s across %d location%s\n\n", 
                    totalAlerts, totalAlerts == 1 ? "" : "s",
                    weatherCache.size(), weatherCache.size() == 1 ? "" : "s"));
            }
            
            alerts.append("\n📅 Last Updated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            return alerts.toString();
        }
    }
    
    private class WeatherStatsResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "weather://stats";
        }
        
        @Override
        public String getName() {
            return "Weather Server Statistics";
        }
        
        @Override
        public String getDescription() {
            return "Server performance and usage statistics";
        }
        
        @Override
        public String getMimeType() {
            return "text/plain";
        }
        
        @Override
        public String read() throws Exception {
            long successfulQueries = queryHistory.stream().mapToLong(q -> q.successful ? 1 : 0).sum();
            long failedQueries = queryHistory.size() - successfulQueries;
            
            Map<String, Long> queryTypes = new HashMap<>();
            for (WeatherQuery query : queryHistory) {
                queryTypes.merge(query.queryType, 1L, Long::sum);
            }
            
            return String.format("""
                🌤️ Weather MCP Server Statistics
                
                Server Information:
                - Name: %s
                - Version: %s
                - Status: %s
                - Uptime: Running
                
                Query Statistics:
                - Total Queries: %d
                - Successful: %d
                - Failed: %d
                - Success Rate: %.1f%%
                
                Query Types:
                - Current Weather: %d
                - Forecasts: %d
                - Alerts: %d
                - History: %d
                
                Monitored Locations: %d
                Available Tools: 4
                Available Resources: 3
                
                🎓 Educational Value:
                This server demonstrates weather service patterns,
                data simulation, and comprehensive MCP server features.
                """,
                getServerName(),
                getServerVersion(),
                isRunning() ? "Running" : "Stopped",
                queryHistory.size(),
                successfulQueries,
                failedQueries,
                queryHistory.isEmpty() ? 0.0 : (successfulQueries * 100.0 / queryHistory.size()),
                queryTypes.getOrDefault("current", 0L),
                queryTypes.getOrDefault("forecast", 0L),
                queryTypes.getOrDefault("alerts", 0L),
                queryTypes.getOrDefault("history", 0L),
                weatherCache.size()
            );
        }
    }
    
    // Helper methods
    
    private WeatherData getOrCreateWeatherData(String location) {
        return weatherCache.computeIfAbsent(location, loc -> {
            // Generate random weather data for new locations
            double temp = 10 + random.nextGaussian() * 15; // Average around 10°C with variation
            int humidity = 30 + random.nextInt(50); // 30-80%
            String[] conditions = {"Sunny", "Partly Cloudy", "Cloudy", "Rainy", "Clear", "Overcast"};
            String condition = conditions[random.nextInt(conditions.length)];
            String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
            String windDir = directions[random.nextInt(directions.length)];
            int windSpeed = 5 + random.nextInt(20); // 5-25 km/h
            
            return new WeatherData(
                location.substring(0, 1).toUpperCase() + location.substring(1),
                temp, humidity, condition, windDir, windSpeed
            );
        });
    }
    
    /**
     * Main method to run the Weather MCP Server as a standalone process.
     */
    public static void main(String[] args) {
        WeatherMCPServer server = new WeatherMCPServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 Shutting down Weather MCP Server...");
            server.stop();
        }));
        
        // Start the server
        server.start();
    }
}