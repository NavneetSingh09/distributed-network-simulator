package sim.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import sim.config.RoutingConfig;
import sim.config.ServerStatusConfig;
import sim.config.SimulationConfig;
import sim.service.SimulatorService;

@RestController
@RequestMapping("/api")
public class SimulatorController {

    private final SimulatorService simulatorService;

    public SimulatorController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
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
            ServerStatusConfig.SERVER1_UP = true;

            System.out.println("Server1 STARTED");

            return "Server1 started";

        } catch (Exception e) {

            ServerStatusConfig.SERVER1_UP = false;

            System.out.println("Server1 FAILED");

            return "Server1 failed to start";
        }
    }

    @PostMapping("/server/start/2")
    public String startServer2() {
        try {
            simulatorService.startServer2();
            ServerStatusConfig.SERVER2_UP = true;

            System.out.println("Server2 STARTED");

            return "Server2 started";

        } catch (Exception e) {

            ServerStatusConfig.SERVER2_UP = false;

            System.out.println("Server2 FAILED");

            return "Server2 failed to start";
        }
    }

    /* ================= TOGGLE (STOP / START) ================= */

  @PostMapping("/server/toggle")
public String toggleServer(
        @RequestParam(name = "id") int id,
        @RequestParam(name = "up") String up
) {
System.out.println("RAW up value = " + up);
    boolean isUp = "true".equalsIgnoreCase(up);

    System.out.println("Toggle called → id=" + id + " up=" + isUp);

    if (id == 1) {
        ServerStatusConfig.SERVER1_UP = isUp;
    } else if (id == 2) {
        ServerStatusConfig.SERVER2_UP = isUp;
    } else {
        return "Invalid server id";
    }

    return "Server " + id + (isUp ? " UP" : " DOWN");
}

    /* ================= TRAFFIC ================= */

    @PostMapping("/traffic/start")
    public String startTraffic() {

        System.out.println("Traffic START requested");

        simulatorService.startTraffic();

        return "Traffic started";
    }

    @PostMapping("/traffic/stop")
    public String stopTraffic() {

        System.out.println("Traffic STOP requested");

        simulatorService.stopTraffic();

        return "Traffic stopped";
    }

    /* ================= CONFIG ================= */

    @PostMapping("/config")
    public String updateConfig(
            @RequestParam double dropRate,
            @RequestParam int minLatency,
            @RequestParam int maxLatency
    ) {
        SimulationConfig.DROP_RATE = dropRate;
        SimulationConfig.MIN_LATENCY = minLatency;
        SimulationConfig.MAX_LATENCY = maxLatency;

        System.out.println("Config updated → Drop:" + dropRate +
                " MinLatency:" + minLatency +
                " MaxLatency:" + maxLatency);

        return "Simulation updated";
    }

    /* ================= ROUTING ================= */

    @PostMapping("/routing")
    public String setRouting(@RequestParam String algo) {

        RoutingConfig.ALGORITHM = algo;

        System.out.println("Routing algorithm set to: " + algo);

        return "Routing set to " + algo;
    }

    /* ================= STATUS ================= */

    @GetMapping("/server/status")
    public Map<String, Boolean> getStatus() {

        Map<String, Boolean> map = new HashMap<>();

        map.put("server1", ServerStatusConfig.SERVER1_UP);
        map.put("server2", ServerStatusConfig.SERVER2_UP);

        return map;
    }
}