package sim.config;

import org.springframework.stereotype.Component;

/**
 * Tracks which servers are currently up.
 * Injected as a Spring bean — no static state, fully testable.
 */
@Component
public class ServerStatusConfig {

    private volatile boolean server1Up = false;
    private volatile boolean server2Up = false;

    public boolean isServer1Up() { return server1Up; }
    public boolean isServer2Up() { return server2Up; }

    public void setServer1Up(boolean up) { this.server1Up = up; }
    public void setServer2Up(boolean up) { this.server2Up = up; }
}
