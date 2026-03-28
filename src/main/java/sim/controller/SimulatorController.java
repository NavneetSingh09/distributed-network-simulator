package sim.controller;

import org.springframework.web.bind.annotation.*;

import sim.circuitbreaker.CircuitBreakerRegistry;
import sim.config.RoutingConfig;
import sim.config.ServerStatusConfig;
import sim.config.SimulationConfig;
import sim.service.SimulatorService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SimulatorController {

    private final SimulatorService   simulatorService;
    private final SimulationConfig   simulationConfig;
    private final RoutingConfig      routingConfig;
    private final ServerStatusConfig serverStatusConfig;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    public SimulatorController(SimulatorService simulatorService,
                           SimulationConfig simulationConfig,
                           RoutingConfig routingConfig,
                           ServerStatusConfig serverStatusConfig,
                           CircuitBreakerRegistry circuitBreakerRegistry) {
    this.simulatorService       = simulatorService;
    this.simulationConfig       = simulationConfig;
    this.routingConfig          = routingConfig;
    this.serverStatusConfig     = serverStatusConfig;
    this.circuitBreakerRegistry = circuitBreakerRegistry;
}

    /* ================= ROUTER ================= */

    @PostMapping("/router/start")
    public String startRouter() {
        simulatorService.startRouter();
        return "Router started";
    }

    /* ================= SERVERS ================= */

    @PostMapping("/server/start/1")
    public String startServer1() {
        try {
            simulatorService.startServer1();
            serverStatusConfig.setServer1Up(true);
            System.out.println("Server1 STARTED");
            return "Server1 started";
        } catch (Exception e) {
            serverStatusConfig.setServer1Up(false);
            return "Server1 failed to start";
        }
    }

    @PostMapping("/server/start/2")
    public String startServer2() {
        try {
            simulatorService.startServer2();
            serverStatusConfig.setServer2Up(true);
            System.out.println("Server2 STARTED");
            return "Server2 started";
        } catch (Exception e) {
            serverStatusConfig.setServer2Up(false);
            return "Server2 failed to start";
        }
    }

    /* ================= TOGGLE ================= */

  @PostMapping("/server/toggle")
public String toggleServer(@RequestParam(name = "id") int id, 
                           @RequestParam(name = "up") String up) {
    boolean isUp = "true".equalsIgnoreCase(up);
    if (id == 1) {
        serverStatusConfig.setServer1Up(isUp);
        if (!isUp) {
            // Trip the circuit breaker immediately when server manually downed
            circuitBreakerRegistry.forServer1().recordFailure();
            circuitBreakerRegistry.forServer1().recordFailure();
            circuitBreakerRegistry.forServer1().recordFailure();
        } else {
            // Reset circuit breaker when server brought back up
            circuitBreakerRegistry.forServer1().recordSuccess();
        }
    } else if (id == 2) {
        serverStatusConfig.setServer2Up(isUp);
        if (!isUp) {
            circuitBreakerRegistry.forServer2().recordFailure();
            circuitBreakerRegistry.forServer2().recordFailure();
            circuitBreakerRegistry.forServer2().recordFailure();
        } else {
            circuitBreakerRegistry.forServer2().recordSuccess();
        }
    } else {
        return "Invalid server id";
    }
    return "Server " + id + (isUp ? " UP" : " DOWN");
}

    /* ================= TRAFFIC ================= */

    @PostMapping("/traffic/start")
    public String startTraffic() {
        simulatorService.startTraffic();
        return "Traffic started";
    }

    @PostMapping("/traffic/stop")
    public String stopTraffic() {
        simulatorService.stopTraffic();
        return "Traffic stopped";
    }

    /* ================= CONFIG ================= */

    @PostMapping("/config")
    public String updateConfig(@RequestParam(name = "dropRate") double dropRate,
                           @RequestParam(name = "minLatency") int minLatency,
                           @RequestParam(name = "maxLatency") int maxLatency) {
        simulationConfig.setDropRate(dropRate);
        simulationConfig.setMinLatency(minLatency);
        simulationConfig.setMaxLatency(maxLatency);
        System.out.println("Config updated → Drop:" + dropRate +
                " MinLatency:" + minLatency + " MaxLatency:" + maxLatency);
        return "Simulation updated";
    }

    /* ================= ROUTING ================= */

    @PostMapping("/routing")
    public String setRouting(@RequestParam(name = "algo") String algo) {
        try {
            routingConfig.setAlgorithm(algo);
            return "Routing set to " + algo;
        } catch (IllegalArgumentException e) {
            return "Invalid algorithm. Use ROUND_ROBIN or LEAST_LOAD";
        }
    }

    /* ================= STATUS ================= */

    @GetMapping("/server/status")
    public Map<String, Boolean> getStatus() {
        return Map.of(
                "server1", serverStatusConfig.isServer1Up(),
                "server2", serverStatusConfig.isServer2Up()
        );
    }
}
