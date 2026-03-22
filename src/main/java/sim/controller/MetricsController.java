package sim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sim.metrics.LogStore;
import sim.metrics.MetricsStore;
import sim.metrics.PacketFlowStore;
import sim.router.PacketQueue;

import java.util.List;
import java.util.Map;

@RestController
public class MetricsController {

    private final MetricsStore metricsStore;
    private final PacketQueue  packetQueue;

    public MetricsController(MetricsStore metricsStore, PacketQueue packetQueue) {
        this.metricsStore = metricsStore;
        this.packetQueue  = packetQueue;
    }

    @GetMapping("/api/logs")
    public List<String> getLogs() {
        return LogStore.getLogs();
    }

    @GetMapping("/api/flows")
    public List<String> flows() {
        return PacketFlowStore.getFlows();
    }

    @GetMapping("/api/metrics")
    public Map<String, Object> metrics() {
        return Map.of(
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
    }
}