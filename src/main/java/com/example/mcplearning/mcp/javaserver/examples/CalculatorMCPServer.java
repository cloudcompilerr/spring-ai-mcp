package com.example.mcplearning.mcp.javaserver.examples;

import com.example.mcplearning.mcp.javaserver.MCPServer;
import com.example.mcplearning.mcp.javaserver.MCPServerTool;
import com.example.mcplearning.mcp.javaserver.MCPServerResource;
import com.example.mcplearning.mcp.protocol.MCPToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Calculator MCP Server - Educational Example
 * 
 * EDUCATIONAL OVERVIEW:
 * This is a complete example of a Java-based MCP server that provides
 * mathematical calculation capabilities. It demonstrates:
 * 
 * - Tool implementation with input validation
 * - Resource management for calculation history
 * - Error handling and user-friendly responses
 * - Server lifecycle management
 * - Educational logging and documentation
 * 
 * FEATURES:
 * - Basic arithmetic operations
 * - Advanced mathematical functions
 * - Calculation history tracking
 * - Expression validation and safety
 * - Educational explanations
 * 
 * USAGE:
 * This server can be run as a standalone process and connected to
 * by any MCP client, including the learning platform itself.
 */
public class CalculatorMCPServer extends MCPServer {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculatorMCPServer.class);
    
    // Calculation history for educational purposes
    private final List<CalculationRecord> calculationHistory = new ArrayList<>();
    private final ScriptEngine scriptEngine;
    
    /**
     * Creates a new Calculator MCP Server.
     */
    public CalculatorMCPServer() {
        super("calculator-server", "1.0.0");
        
        // Initialize JavaScript engine for safe expression evaluation
        ScriptEngineManager manager = new ScriptEngineManager();
        scriptEngine = manager.getEngineByName("javascript");
        
        logger.info("🧮 Calculator MCP Server initialized");
    }
    
    @Override
    protected void initializeServer() {
        logger.info("🔧 Initializing Calculator MCP Server tools and resources...");
        
        // Register calculation tools
        registerTool(new BasicCalculatorTool());
        registerTool(new AdvancedCalculatorTool());
        registerTool(new HistoryTool());
        registerTool(new ClearHistoryTool());
        
        // Register resources
        registerResource(new CalculationHistoryResource());
        registerResource(new CalculatorHelpResource());
        registerResource(new CalculatorStatsResource());
        
        logger.info("✅ Calculator MCP Server initialization complete");
        logger.info("📋 Available tools: basic_calculate, advanced_calculate, get_history, clear_history");
        logger.info("📄 Available resources: calc://history, calc://help, calc://stats");
    }
    
    /**
     * Record of a calculation for history tracking.
     */
    private static class CalculationRecord {
        final String expression;
        final String result;
        final LocalDateTime timestamp;
        final boolean isError;
        
        CalculationRecord(String expression, String result, boolean isError) {
            this.expression = expression;
            this.result = result;
            this.timestamp = LocalDateTime.now();
            this.isError = isError;
        }
        
        @Override
        public String toString() {
            String status = isError ? "❌" : "✅";
            return String.format("%s [%s] %s = %s", 
                status, timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")), 
                expression, result);
        }
    }
    
    /**
     * Basic Calculator Tool - Simple arithmetic operations
     */
    private class BasicCalculatorTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "basic_calculate";
        }
        
        @Override
        public String getDescription() {
            return "Performs basic arithmetic calculations (addition, subtraction, multiplication, division)";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "expression", Map.of(
                        "type", "string",
                        "description", "Mathematical expression to evaluate (e.g., '2 + 3 * 4')"
                    )
                ),
                "required", List.of("expression")
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            validateArguments(arguments);
            
            String expression = (String) arguments.get("expression");
            if (expression == null || expression.trim().isEmpty()) {
                return createErrorResult("Expression cannot be empty");
            }
            
            logger.info("🧮 Calculating: {}", expression);
            
            try {
                // Validate expression for safety
                if (!isValidBasicExpression(expression)) {
                    return createErrorResult("Invalid expression. Only basic arithmetic operations are allowed.");
                }
                
                // Evaluate the expression
                Object result = scriptEngine.eval(expression);
                String resultStr = formatResult(result);
                
                // Record the calculation
                calculationHistory.add(new CalculationRecord(expression, resultStr, false));
                
                logger.info("✅ Result: {} = {}", expression, resultStr);
                
                return MCPToolResult.success(
                    String.format("Expression: %s\nResult: %s\n\n💡 Educational Note: This calculation used basic arithmetic operations.", 
                        expression, resultStr),
                    "text/plain"
                );
                
            } catch (Exception e) {
                String errorMsg = "Calculation error: " + e.getMessage();
                calculationHistory.add(new CalculationRecord(expression, errorMsg, true));
                logger.warn("❌ Calculation failed: {} - {}", expression, e.getMessage());
                return createErrorResult(errorMsg);
            }
        }
        
        private boolean isValidBasicExpression(String expression) {
            // Allow only numbers, basic operators, parentheses, and whitespace
            return expression.matches("[0-9+\\-*/().\\s]+");
        }
    }
    
    /**
     * Advanced Calculator Tool - Mathematical functions
     */
    private class AdvancedCalculatorTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "advanced_calculate";
        }
        
        @Override
        public String getDescription() {
            return "Performs advanced mathematical calculations including trigonometry, logarithms, and powers";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "function", Map.of(
                        "type", "string",
                        "description", "Mathematical function (sin, cos, tan, log, sqrt, pow)",
                        "enum", List.of("sin", "cos", "tan", "log", "sqrt", "pow", "abs")
                    ),
                    "value", Map.of(
                        "type", "number",
                        "description", "Input value for the function"
                    ),
                    "value2", Map.of(
                        "type", "number",
                        "description", "Second value (for functions like pow that need two inputs)"
                    )
                ),
                "required", List.of("function", "value")
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            validateArguments(arguments);
            
            String function = (String) arguments.get("function");
            Object valueObj = arguments.get("value");
            Object value2Obj = arguments.get("value2");
            
            if (function == null || valueObj == null) {
                return createErrorResult("Function and value are required");
            }
            
            double value = ((Number) valueObj).doubleValue();
            Double value2 = value2Obj != null ? ((Number) value2Obj).doubleValue() : null;
            
            logger.info("🧮 Advanced calculation: {}({}{})", function, value, 
                value2 != null ? ", " + value2 : "");
            
            try {
                double result = switch (function.toLowerCase()) {
                    case "sin" -> Math.sin(value);
                    case "cos" -> Math.cos(value);
                    case "tan" -> Math.tan(value);
                    case "log" -> Math.log(value);
                    case "sqrt" -> Math.sqrt(value);
                    case "abs" -> Math.abs(value);
                    case "pow" -> {
                        if (value2 == null) {
                            throw new IllegalArgumentException("pow function requires two values");
                        }
                        yield Math.pow(value, value2);
                    }
                    default -> throw new IllegalArgumentException("Unknown function: " + function);
                };
                
                String expression = value2 != null ? 
                    String.format("%s(%s, %s)", function, value, value2) :
                    String.format("%s(%s)", function, value);
                String resultStr = formatResult(result);
                
                // Record the calculation
                calculationHistory.add(new CalculationRecord(expression, resultStr, false));
                
                logger.info("✅ Advanced result: {} = {}", expression, resultStr);
                
                return MCPToolResult.success(
                    String.format("Function: %s\nResult: %s\n\n💡 Educational Note: This used the %s mathematical function.", 
                        expression, resultStr, function),
                    "text/plain"
                );
                
            } catch (Exception e) {
                String errorMsg = "Advanced calculation error: " + e.getMessage();
                String expression = String.format("%s(%s)", function, value);
                calculationHistory.add(new CalculationRecord(expression, errorMsg, true));
                logger.warn("❌ Advanced calculation failed: {} - {}", expression, e.getMessage());
                return createErrorResult(errorMsg);
            }
        }
    }
    
    /**
     * History Tool - Get calculation history
     */
    private class HistoryTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "get_history";
        }
        
        @Override
        public String getDescription() {
            return "Retrieves the calculation history";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of(
                "type", "object",
                "properties", Map.of(
                    "limit", Map.of(
                        "type", "integer",
                        "description", "Maximum number of history entries to return (default: 10)"
                    )
                )
            );
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            Object limitObj = arguments.get("limit");
            int limit = limitObj != null ? ((Number) limitObj).intValue() : 10;
            
            logger.info("📜 Retrieving calculation history (limit: {})", limit);
            
            if (calculationHistory.isEmpty()) {
                return MCPToolResult.success(
                    "📜 Calculation History\n\nNo calculations performed yet.\n\n💡 Try using basic_calculate or advanced_calculate tools!",
                    "text/plain"
                );
            }
            
            StringBuilder history = new StringBuilder("📜 Calculation History\n\n");
            
            int start = Math.max(0, calculationHistory.size() - limit);
            for (int i = start; i < calculationHistory.size(); i++) {
                CalculationRecord record = calculationHistory.get(i);
                history.append(String.format("%d. %s\n", i + 1, record.toString()));
            }
            
            history.append(String.format("\n📊 Total calculations: %d", calculationHistory.size()));
            
            return MCPToolResult.success(history.toString(), "text/plain");
        }
    }
    
    /**
     * Clear History Tool - Clear calculation history
     */
    private class ClearHistoryTool implements MCPServerTool {
        
        @Override
        public String getName() {
            return "clear_history";
        }
        
        @Override
        public String getDescription() {
            return "Clears the calculation history";
        }
        
        @Override
        public Map<String, Object> getInputSchema() {
            return Map.of("type", "object", "properties", Map.of());
        }
        
        @Override
        public MCPToolResult execute(Map<String, Object> arguments) throws Exception {
            int clearedCount = calculationHistory.size();
            calculationHistory.clear();
            
            logger.info("🗑️ Cleared {} calculation history entries", clearedCount);
            
            return MCPToolResult.success(
                String.format("✅ Cleared %d calculation history entries.\n\n💡 History is now empty and ready for new calculations!", 
                    clearedCount),
                "text/plain"
            );
        }
    }
    
    /**
     * Calculation History Resource
     */
    private class CalculationHistoryResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "calc://history";
        }
        
        @Override
        public String getName() {
            return "Calculation History";
        }
        
        @Override
        public String getDescription() {
            return "Complete calculation history in JSON format";
        }
        
        @Override
        public String getMimeType() {
            return "application/json";
        }
        
        @Override
        public String read() throws Exception {
            StringBuilder json = new StringBuilder("{\n");
            json.append("  \"server\": \"Calculator MCP Server\",\n");
            json.append("  \"timestamp\": \"").append(LocalDateTime.now()).append("\",\n");
            json.append("  \"totalCalculations\": ").append(calculationHistory.size()).append(",\n");
            json.append("  \"history\": [\n");
            
            for (int i = 0; i < calculationHistory.size(); i++) {
                CalculationRecord record = calculationHistory.get(i);
                json.append("    {\n");
                json.append("      \"expression\": \"").append(record.expression).append("\",\n");
                json.append("      \"result\": \"").append(record.result).append("\",\n");
                json.append("      \"timestamp\": \"").append(record.timestamp).append("\",\n");
                json.append("      \"isError\": ").append(record.isError).append("\n");
                json.append("    }");
                if (i < calculationHistory.size() - 1) {
                    json.append(",");
                }
                json.append("\n");
            }
            
            json.append("  ]\n");
            json.append("}");
            
            return json.toString();
        }
    }
    
    /**
     * Calculator Help Resource
     */
    private class CalculatorHelpResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "calc://help";
        }
        
        @Override
        public String getName() {
            return "Calculator Help";
        }
        
        @Override
        public String getDescription() {
            return "Help documentation for the Calculator MCP Server";
        }
        
        @Override
        public String getMimeType() {
            return "text/markdown";
        }
        
        @Override
        public String read() throws Exception {
            return """
                # Calculator MCP Server Help
                
                ## Overview
                The Calculator MCP Server provides mathematical calculation capabilities through MCP tools.
                
                ## Available Tools
                
                ### basic_calculate
                Performs basic arithmetic operations.
                - **Input**: `expression` (string) - Mathematical expression
                - **Example**: `"2 + 3 * 4"`
                - **Supported**: +, -, *, /, (), numbers
                
                ### advanced_calculate
                Performs advanced mathematical functions.
                - **Input**: 
                  - `function` (string) - Function name (sin, cos, tan, log, sqrt, pow, abs)
                  - `value` (number) - Input value
                  - `value2` (number, optional) - Second value for functions like pow
                - **Example**: `{"function": "sin", "value": 1.57}`
                
                ### get_history
                Retrieves calculation history.
                - **Input**: `limit` (integer, optional) - Max entries to return (default: 10)
                
                ### clear_history
                Clears the calculation history.
                - **Input**: None
                
                ## Available Resources
                
                ### calc://history
                Complete calculation history in JSON format.
                
                ### calc://help
                This help documentation.
                
                ### calc://stats
                Server statistics and information.
                
                ## Educational Notes
                This server demonstrates:
                - MCP tool implementation patterns
                - Input validation and error handling
                - Resource management
                - History tracking and state management
                """;
        }
    }
    
    /**
     * Calculator Statistics Resource
     */
    private class CalculatorStatsResource implements MCPServerResource {
        
        @Override
        public String getUri() {
            return "calc://stats";
        }
        
        @Override
        public String getName() {
            return "Calculator Statistics";
        }
        
        @Override
        public String getDescription() {
            return "Server statistics and performance information";
        }
        
        @Override
        public String getMimeType() {
            return "text/plain";
        }
        
        @Override
        public String read() throws Exception {
            long successCount = calculationHistory.stream()
                .mapToLong(r -> r.isError ? 0 : 1)
                .sum();
            long errorCount = calculationHistory.size() - successCount;
            
            return String.format("""
                📊 Calculator MCP Server Statistics
                
                Server Information:
                - Name: %s
                - Version: %s
                - Status: %s
                - Initialized: %s
                
                Calculation Statistics:
                - Total Calculations: %d
                - Successful: %d
                - Errors: %d
                - Success Rate: %.1f%%
                
                Tools Available: 4
                Resources Available: 3
                
                🎓 Educational Value:
                This server demonstrates a complete MCP server implementation
                with tools, resources, state management, and error handling.
                """, 
                getServerName(), 
                getServerVersion(),
                isRunning() ? "Running" : "Stopped",
                isInitialized() ? "Yes" : "No",
                calculationHistory.size(),
                successCount,
                errorCount,
                calculationHistory.isEmpty() ? 0.0 : (successCount * 100.0 / calculationHistory.size())
            );
        }
    }
    
    // Helper methods
    
    private MCPToolResult createErrorResult(String message) {
        return MCPToolResult.error(message);
    }
    
    private String formatResult(Object result) {
        if (result instanceof Double d) {
            // Format doubles nicely
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf(d.longValue());
            } else {
                return String.format("%.6f", d).replaceAll("0+$", "").replaceAll("\\.$", "");
            }
        }
        return result.toString();
    }
    
    /**
     * Main method to run the Calculator MCP Server as a standalone process.
     */
    public static void main(String[] args) {
        CalculatorMCPServer server = new CalculatorMCPServer();
        
        // Add shutdown hook for graceful termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("🛑 Shutting down Calculator MCP Server...");
            server.stop();
        }));
        
        // Start the server (this blocks until the server stops)
        server.start();
    }
}