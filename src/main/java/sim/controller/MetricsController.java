package sim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sim.metrics.LogStore;
import sim.metrics.PacketFlowStore;

import java.util.List;

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
}