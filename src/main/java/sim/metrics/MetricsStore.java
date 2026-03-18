package sim.metrics;

public class MetricsStore {

    public static int packetsSent = 0;
    public static int packetsDropped = 0;
    public static int packetsProcessed = 0;

    public static int server1Load = 0;
    public static int server2Load = 0;

    public static long totalLatency = 0;
    public static int latencySamples = 0;

    public static synchronized void packetSent() {
        packetsSent++;
    }

    public static synchronized void packetDropped() {
        packetsDropped++;
    }

    public static synchronized void packetProcessed() {
        packetsProcessed++;
    }

    public static synchronized void server1Handled() {
        server1Load++;
    }

    public static synchronized void server2Handled() {
        server2Load++;
    }

    public static synchronized void recordLatency(long latency) {
        totalLatency += latency;
        latencySamples++;
    }

    public static synchronized double avgLatency() {
        if (latencySamples == 0) return 0;
        return totalLatency / (double) latencySamples;
    }

    
    public static synchronized int getServer1Load() {
        return server1Load;
    }

    public static synchronized int getServer2Load() {
        return server2Load;
    }
}