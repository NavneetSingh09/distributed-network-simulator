package sim.circuitbreaker;

import org.springframework.stereotype.Component;

/**
 * Holds one CircuitBreaker per server.
 * Injected as a Spring bean so Router and MetricsController can share it.
 */
@Component
public class CircuitBreakerRegistry {

    private final CircuitBreaker server1 = new CircuitBreaker("SERVER1");
    private final CircuitBreaker server2 = new CircuitBreaker("SERVER2");

    public CircuitBreaker forServer1() { return server1; }
    public CircuitBreaker forServer2() { return server2; }

    public CircuitBreaker forPort(int port) {
        return port == 6001 ? server1 : server2;
    }
}