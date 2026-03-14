package sim.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sim.metrics.LogStore;

import java.util.List;

@RestController
public class MetricsController {

    @GetMapping("/api/logs")
    public List<String> getLogs() {
        return LogStore.getLogs();
    }
    
}