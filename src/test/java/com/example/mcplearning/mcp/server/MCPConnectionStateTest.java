package com.example.mcplearning.mcp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MCPConnectionState enum.
 * 
 * These tests verify the behavior and classification methods of the
 * connection state enumeration. They serve as documentation of the
 * expected state behavior and transitions.
 */
class MCPConnectionStateTest {
    
    @Test
    void shouldIdentifyTransitionalStates() {
        // Transitional states
        assertTrue(MCPConnectionState.CONNECTING.isTransitional());
        assertTrue(MCPConnectionState.INITIALIZING.isTransitional());
        
        // Non-transitional states
        assertFalse(MCPConnectionState.DISCONNECTED.isTransitional());
        assertFalse(MCPConnectionState.CONNECTED.isTransitional());
        assertFalse(MCPConnectionState.READY.isTransitional());
        assertFalse(MCPConnectionState.ERROR.isTransitional());
    }
    
    @Test
    void shouldIdentifyStableStates() {
        // Stable states
        assertTrue(MCPConnectionState.DISCONNECTED.isStable());
        assertTrue(MCPConnectionState.READY.isStable());
        assertTrue(MCPConnectionState.ERROR.isStable());
        
        // Non-stable states
        assertFalse(MCPConnectionState.CONNECTING.isStable());
        assertFalse(MCPConnectionState.CONNECTED.isStable());
        assertFalse(MCPConnectionState.INITIALIZING.isStable());
    }
    
    @Test
    void shouldIdentifyOperationalStates() {
        // Only READY allows operations
        assertTrue(MCPConnectionState.READY.canPerformOperations());
        
        // All other states do not allow operations
        assertFalse(MCPConnectionState.DISCONNECTED.canPerformOperations());
        assertFalse(MCPConnectionState.CONNECTING.canPerformOperations());
        assertFalse(MCPConnectionState.CONNECTED.canPerformOperations());
        assertFalse(MCPConnectionState.INITIALIZING.canPerformOperations());
        assertFalse(MCPConnectionState.ERROR.canPerformOperations());
    }
    
    @Test
    void shouldIdentifyConnectedStates() {
        // Connected states (have some form of connection)
        assertTrue(MCPConnectionState.CONNECTED.isConnected());
        assertTrue(MCPConnectionState.INITIALIZING.isConnected());
        assertTrue(MCPConnectionState.READY.isConnected());
        
        // Disconnected states
        assertFalse(MCPConnectionState.DISCONNECTED.isConnected());
        assertFalse(MCPConnectionState.CONNECTING.isConnected());
        assertFalse(MCPConnectionState.ERROR.isConnected());
    }
    
    @Test
    void shouldProvideCorrectDescriptions() {
        assertEquals("Disconnected", MCPConnectionState.DISCONNECTED.getDescription());
        assertEquals("Connecting", MCPConnectionState.CONNECTING.getDescription());
        assertEquals("Connected", MCPConnectionState.CONNECTED.getDescription());
        assertEquals("Initializing", MCPConnectionState.INITIALIZING.getDescription());
        assertEquals("Ready", MCPConnectionState.READY.getDescription());
        assertEquals("Error", MCPConnectionState.ERROR.getDescription());
    }
    
    @Test
    void shouldHaveCorrectStateTransitionLogic() {
        // Test typical state progression
        
        // Start disconnected
        MCPConnectionState state = MCPConnectionState.DISCONNECTED;
        assertFalse(state.isConnected());
        assertFalse(state.canPerformOperations());
        assertTrue(state.isStable());
        
        // Move to connecting (transitional)
        state = MCPConnectionState.CONNECTING;
        assertFalse(state.isConnected());
        assertFalse(state.canPerformOperations());
        assertTrue(state.isTransitional());
        
        // Move to connected
        state = MCPConnectionState.CONNECTED;
        assertTrue(state.isConnected());
        assertFalse(state.canPerformOperations()); // Not ready yet
        assertFalse(state.isStable()); // Still transitioning
        
        // Move to initializing (still transitional)
        state = MCPConnectionState.INITIALIZING;
        assertTrue(state.isConnected());
        assertFalse(state.canPerformOperations());
        assertTrue(state.isTransitional());
        
        // Finally ready (operational)
        state = MCPConnectionState.READY;
        assertTrue(state.isConnected());
        assertTrue(state.canPerformOperations());
        assertTrue(state.isStable());
        
        // Error state (stable but not operational)
        state = MCPConnectionState.ERROR;
        assertFalse(state.isConnected());
        assertFalse(state.canPerformOperations());
        assertTrue(state.isStable());
    }
    
    @Test
    void shouldHaveAllExpectedStates() {
        // Verify all expected states exist
        MCPConnectionState[] states = MCPConnectionState.values();
        assertEquals(6, states.length);
        
        // Verify specific states exist
        assertNotNull(MCPConnectionState.valueOf("DISCONNECTED"));
        assertNotNull(MCPConnectionState.valueOf("CONNECTING"));
        assertNotNull(MCPConnectionState.valueOf("CONNECTED"));
        assertNotNull(MCPConnectionState.valueOf("INITIALIZING"));
        assertNotNull(MCPConnectionState.valueOf("READY"));
        assertNotNull(MCPConnectionState.valueOf("ERROR"));
    }
}