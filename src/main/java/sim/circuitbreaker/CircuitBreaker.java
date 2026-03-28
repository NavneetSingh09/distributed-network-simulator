package sim.circuitbreaker;

import sim.util.Log;

/**
 * Circuit Breaker pattern implementation.
 * Prevents the router from repeatedly trying to forward
 * packets to a server that is known to be failing.
 *
 * States:
 *   CLOSED    — normal operation, packets flow through
 *   OPEN      — server failing, packets rejected immediately
 *   HALF_OPEN — testing if server recovered, one packet allowed
 */
public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private static final int  FAILURE_THRESHOLD   = 3;
    private static final long OPEN_TIMEOUT_MS      = 30_000; // 30 seconds

    private final String name;
    private State  state          = State.CLOSED;
    private int    failureCount   = 0;
    private long   openedAt       = 0;

    public CircuitBreaker(String name) {
        this.name = name;
    }

    
    public synchronized boolean allowRequest() {
        switch (state) {
            case CLOSED:
                return true;

            case OPEN:
                // Check if timeout has elapsed — transition to HALF_OPEN
                if (System.currentTimeMillis() - openedAt >= OPEN_TIMEOUT_MS) {
                    state = State.HALF_OPEN;
                    Log.info("CIRCUIT", "[" + name + "] OPEN → HALF_OPEN — testing recovery");
                    return true;
                }
                return false;

            case HALF_OPEN:
                // Only allow one request through to test
                return true;

            default:
                return false;
        }
    }

    
    public synchronized void recordSuccess() {
        if (state == State.HALF_OPEN) {
            state        = State.CLOSED;
            failureCount = 0;
            Log.info("CIRCUIT", "[" + name + "] HALF_OPEN → CLOSED — server recovered ✅");
        } else if (state == State.CLOSED) {
            failureCount = 0;
        }
    }

    
    public synchronized void recordFailure() {
        failureCount++;
        Log.warn("CIRCUIT", "[" + name + "] Failure " + failureCount + "/" + FAILURE_THRESHOLD);

        if (state == State.HALF_OPEN || failureCount >= FAILURE_THRESHOLD) {
            state    = State.OPEN;
            openedAt = System.currentTimeMillis();
            Log.error("CIRCUIT", "[" + name + "] CLOSED → OPEN — server circuit tripped 🔴");
        }
    }

    public synchronized State  getState()        { return state; }
    public synchronized int    getFailureCount()  { return failureCount; }
    public String              getName()          { return name; }
}