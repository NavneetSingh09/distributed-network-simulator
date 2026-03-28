package sim.dpi;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Stores DPI statistics — protocol distribution and threat log.
 */
@Component
public class DpiStore {

    private final Map<String, AtomicInteger> protocolCounts = new ConcurrentHashMap<>();
    private final List<String> threatLog = new ArrayList<>();

    public DpiStore() {
        // Initialize all protocol counters
        for (PacketClassifier.Protocol p : PacketClassifier.Protocol.values()) {
            protocolCounts.put(p.name(), new AtomicInteger(0));
        }
    }

    public void recordProtocol(PacketClassifier.Protocol protocol) {
        protocolCounts.get(protocol.name()).incrementAndGet();
    }

    public synchronized void recordThreat(String packetId, String payload) {
        String entry = "[THREAT] Packet " + packetId +
                       " — suspicious payload detected: " +
                       payload.substring(0, Math.min(payload.length(), 50));
        threatLog.add(entry);
        if (threatLog.size() > 100) threatLog.remove(0);
    }

    public Map<String, Integer> getProtocolCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        protocolCounts.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }

    public synchronized List<String> getThreatLog() {
        return new ArrayList<>(threatLog);
    }

    public synchronized void reset() {
        protocolCounts.values().forEach(c -> c.set(0));
        threatLog.clear();
    }
}