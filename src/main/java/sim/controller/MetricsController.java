package sim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sim.circuitbreaker.CircuitBreakerRegistry;
import sim.dpi.DpiStore;
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
    private final DpiStore dpiStore;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

  public MetricsController(MetricsStore metricsStore,
                          PacketQueue packetQueue,
                          DpiStore dpiStore,
                          CircuitBreakerRegistry circuitBreakerRegistry) {
    this.metricsStore           = metricsStore;
    this.packetQueue            = packetQueue;
    this.dpiStore               = dpiStore;
    this.circuitBreakerRegistry = circuitBreakerRegistry;
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

    @PostMapping("/api/metrics/server-handled")
public void serverHandled(@RequestParam(name = "serverId") int serverId) {
    if (serverId == 1)
        metricsStore.server1Handled();
    else
        metricsStore.server2Handled();
}

@GetMapping("/api/health")
public Map<String, Object> health() {
    return Map.of(
        "status",    "UP",
        "timestamp", java.time.Instant.now().toString(),
        "version",   "2.0"
    );
}

@PostMapping("/api/metrics/reset")
public String resetMetrics() {
    metricsStore.reset();
    PacketFlowStore.clear();
    dpiStore.reset();
    return "Metrics reset";
}

@GetMapping("/api/dpi/protocols")
public Map<String, Integer> getProtocolStats() {
    return dpiStore.getProtocolCounts();
}

@GetMapping("/api/dpi/threats")
public List<String> getThreats() {
    return dpiStore.getThreatLog();
}

@GetMapping("/api/circuit")
public Map<String, String> getCircuitStatus() {
    return Map.of(
        "server1", circuitBreakerRegistry.forServer1().getState().name(),
        "server2", circuitBreakerRegistry.forServer2().getState().name()
    );
}

}