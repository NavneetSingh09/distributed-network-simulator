package sim.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationConfig validation logic.
 */
class SimulationConfigTest {

    private SimulationConfig config;

    @BeforeEach
    void setUp() {
        config = new SimulationConfig();
    }

    @Test
    void defaultValues_areCorrect() {
        assertEquals(0.10, config.getDropRate());
        assertEquals(50,   config.getMinLatency());
        assertEquals(300,  config.getMaxLatency());
    }

    @Test
    void setDropRate_validValue_succeeds() {
        config.setDropRate(0.25);
        assertEquals(0.25, config.getDropRate());
    }

    @Test
    void setDropRate_negative_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> config.setDropRate(-0.1));
    }

    @Test
    void setDropRate_greaterThanOne_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> config.setDropRate(1.5));
    }

    @Test
    void setMinLatency_negative_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> config.setMinLatency(-10));
    }

    @Test
    void setMaxLatency_lessThanMin_throwsException() {
        config.setMinLatency(100);
        assertThrows(IllegalArgumentException.class,
                () -> config.setMaxLatency(50));
    }

    @Test
    void setMaxLatency_greaterThanMin_succeeds() {
        config.setMinLatency(50);
        config.setMaxLatency(500);
        assertEquals(500, config.getMaxLatency());
    }
}