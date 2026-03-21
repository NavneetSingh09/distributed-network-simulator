package sim.metrics;

import org.springframework.stereotype.Component;

/**
 * Thread-safe in-memory metrics store.
 * Injected as a Spring bean — no static state, fully testable.
 */
@Component
public class MetricsStore {

    private int  packetsSent      = 0;
    private int  packetsDropped   = 0;
    private int  packetsProcessed = 0;

    private int  server1Load      = 0;
    private int  server2Load      = 0;

    private long totalLatency     = 0;
    private int  latencySamples   = 0;

    public synchronized void packetSent()      { packetsSent++; }
    public synchronized void packetDropped()   { packetsDropped++; }
    public synchronized void packetProcessed() { packetsProcessed++; }

    public synchronized void server1Handled()  { server1Load++; }
    public synchronized void server2Handled()  { server2Load++; }

    public synchronized void recordLatency(long latency) {
        totalLatency += latency;
        latencySamples++;
    }

    public synchronized double avgLatency() {
        return latencySamples == 0 ? 0 : totalLatency / (double) latencySamples;
    }

    public synchronized int getPacketsSent()      { return packetsSent; }
    public synchronized int getPacketsDropped()   { return packetsDropped; }
    public synchronized int getPacketsProcessed() { return packetsProcessed; }
    public synchronized int getServer1Load()      { return server1Load; }
    public synchronized int getServer2Load()      { return server2Load; }
}
