package sim.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sim.dpi.DpiStore;
import sim.metrics.MetricsStore;
import sim.router.PacketQueue;

import java.util.HashMap;
import java.util.Map;

@Service
public class MetricsBroadcaster {

    private final SimpMessagingTemplate messaging;
    private final MetricsStore          metricsStore;
    private final PacketQueue           packetQueue;
    private final DpiStore              dpiStore;

    public MetricsBroadcaster(SimpMessagingTemplate messaging,
                               MetricsStore metricsStore,
                               PacketQueue packetQueue,
                               DpiStore dpiStore) {
        this.messaging    = messaging;
        this.metricsStore = metricsStore;
        this.packetQueue  = packetQueue;
        this.dpiStore     = dpiStore;
    }

    @Scheduled(fixedRate = 1000)
    public void broadcastMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("packetsSent",      metricsStore.getPacketsSent());
        metrics.put("packetsDropped",   metricsStore.getPacketsDropped());
        metrics.put("packetsProcessed", metricsStore.getPacketsProcessed());
        metrics.put("packetsQueued",    metricsStore.getPacketsQueued());
        metrics.put("packetsDequeued",  metricsStore.getPacketsDequeued());
        metrics.put("queueSize",        packetQueue.size());
        metrics.put("server1Load",      metricsStore.getServer1Load());
        metrics.put("server2Load",      metricsStore.getServer2Load());
        metrics.put("avgLatency",       metricsStore.avgLatency());
        metrics.put("protocols",        dpiStore.getProtocolCounts());
        metrics.put("threatsBlocked",   dpiStore.getProtocolCounts().getOrDefault("SUSPICIOUS", 0));

        messaging.convertAndSend("/topic/metrics", metrics);
    }
}