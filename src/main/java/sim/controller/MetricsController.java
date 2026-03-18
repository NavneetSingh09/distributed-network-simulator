package sim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sim.metrics.LogStore;
import sim.metrics.MetricsStore;
import sim.metrics.PacketFlowStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MetricsController {

    @GetMapping("/api/logs")
    public List<String> getLogs() {
        return LogStore.getLogs();
    }
    
    @GetMapping("/api/flows")
public List<String> flows(){
    return PacketFlowStore.getFlows();
}

@GetMapping("/api/metrics")
public Map<String,Object> metrics(){

    Map<String,Object> data = new HashMap<>();

    data.put("packetsSent", MetricsStore.packetsSent);
    data.put("packetsDropped", MetricsStore.packetsDropped);
    data.put("packetsProcessed", MetricsStore.packetsProcessed);

    data.put("server1Load", MetricsStore.server1Load);
    data.put("server2Load", MetricsStore.server2Load);

    data.put("avgLatency", MetricsStore.avgLatency());

    return data;
}
}