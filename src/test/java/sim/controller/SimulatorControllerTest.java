package sim.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SimulatorController REST endpoints.
 * Spins up full Spring context with MockMvc.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SimulatorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getServerStatus_returnsOk() throws Exception {
        mockMvc.perform(get("/api/server/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.server1").exists())
                .andExpect(jsonPath("$.server2").exists());
    }

    @Test
    void toggleServer1Up_returnsOk() throws Exception {
        mockMvc.perform(post("/api/server/toggle")
                        .param("id", "1")
                        .param("up", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void toggleServer1Down_returnsOk() throws Exception {
        mockMvc.perform(post("/api/server/toggle")
                        .param("id", "1")
                        .param("up", "false"))
                .andExpect(status().isOk());
    }

    @Test
    void toggleInvalidServer_returnsInvalidMessage() throws Exception {
        mockMvc.perform(post("/api/server/toggle")
                        .param("id", "99")
                        .param("up", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid server id"));
    }

    @Test
    void updateConfig_validValues_returnsOk() throws Exception {
        mockMvc.perform(post("/api/config")
                        .param("dropRate", "0.2")
                        .param("minLatency", "50")
                        .param("maxLatency", "300"))
                .andExpect(status().isOk());
    }

    @Test
    void setRouting_roundRobin_returnsOk() throws Exception {
        mockMvc.perform(post("/api/routing")
                        .param("algo", "ROUND_ROBIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Routing set to ROUND_ROBIN"));
    }

    @Test
    void setRouting_leastLoad_returnsOk() throws Exception {
        mockMvc.perform(post("/api/routing")
                        .param("algo", "LEAST_LOAD"))
                .andExpect(status().isOk())
                .andExpect(content().string("Routing set to LEAST_LOAD"));
    }

    @Test
    void setRouting_invalidAlgo_returnsErrorMessage() throws Exception {
        mockMvc.perform(post("/api/routing")
                        .param("algo", "INVALID"))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid algorithm. Use ROUND_ROBIN or LEAST_LOAD"));
    }

    @Test
    void getMetrics_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packetsSent").exists())
                .andExpect(jsonPath("$.packetsDropped").exists())
                .andExpect(jsonPath("$.packetsProcessed").exists())
                .andExpect(jsonPath("$.server1Load").exists())
                .andExpect(jsonPath("$.server2Load").exists())
                .andExpect(jsonPath("$.avgLatency").exists())
                .andExpect(jsonPath("$.queueSize").exists());
    }
}