package sim.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoutingConfig enum switching.
 */
class RoutingConfigTest {

    private RoutingConfig config;

    @BeforeEach
    void setUp() {
        config = new RoutingConfig();
    }

    @Test
    void defaultAlgorithm_isRoundRobin() {
        assertEquals(RoutingConfig.Algorithm.ROUND_ROBIN, config.getAlgorithm());
    }

    @Test
    void setAlgorithm_leastLoad_succeeds() {
        config.setAlgorithm("LEAST_LOAD");
        assertEquals(RoutingConfig.Algorithm.LEAST_LOAD, config.getAlgorithm());
    }

    @Test
    void setAlgorithm_roundRobin_succeeds() {
        config.setAlgorithm("LEAST_LOAD");
        config.setAlgorithm("ROUND_ROBIN");
        assertEquals(RoutingConfig.Algorithm.ROUND_ROBIN, config.getAlgorithm());
    }

    @Test
    void setAlgorithm_invalidValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> config.setAlgorithm("INVALID_ALGO"));
    }

    @Test
    void setAlgorithm_caseInsensitive_succeeds() {
        config.setAlgorithm("least_load");
        assertEquals(RoutingConfig.Algorithm.LEAST_LOAD, config.getAlgorithm());
    }
}