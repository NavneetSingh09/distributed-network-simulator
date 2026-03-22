package sim.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sim.metrics.MetricsStore;
import sim.router.PacketQueue;

import java.util.Map;

/**
 * Pushes metrics to all connected WebSocket clients every second.
 * Frontend receives live updates without polling.
 */
@Service
public class MetricsBroadcaster {

    private final SimpMessagingTemplate messaging;
    private final MetricsStore          metricsStore;
    private final PacketQueue           packetQueue;

    public MetricsBroadcaster(SimpMessagingTemplate messaging,
                               MetricsStore metricsStore,
                               PacketQueue packetQueue) {
        this.messaging   = messaging;
        this.metricsStore = metricsStore;
        this.packetQueue  = packetQueue;
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastMetrics() {
        Map<String, Object> metrics = Map.of(
                "packetsSent",      metricsStore.getPacketsSent(),
                "packetsDropped",   metricsStore.getPacketsDropped(),
                "packetsProcessed", metricsStore.getPacketsProcessed(),
                "packetsQueued",    metricsStore.getPacketsQueued(),
                "packetsDequeued",  metricsStore.getPacketsDequeued(),
                "queueSize",        packetQueue.size(),
                "server1Load",      metricsStore.getServer1Load(),
                "server2Load",      metricsStore.getServer2Load(),
                "avgLatency",       metricsStore.avgLatency()
        );

        messaging.convertAndSend("/topic/metrics", metrics);
    }
}