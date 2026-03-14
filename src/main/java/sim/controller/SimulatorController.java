package sim.controller;

import org.springframework.web.bind.annotation.*;
import sim.service.SimulatorService;

@RestController
@RequestMapping("/api")
public class SimulatorController {

    private final SimulatorService simulatorService;

    public SimulatorController(SimulatorService simulatorService) {
        this.simulatorService = simulatorService;
    }

    @PostMapping("/router/start")
    public String startRouter() {
        simulatorService.startRouter();
        return "Router started";
    }

    @PostMapping("/server/start/1")
    public String startServer1() {
        simulatorService.startServer1();
        return "Server1 started";
    }

    @PostMapping("/server/start/2")
    public String startServer2() {
        simulatorService.startServer2();
        return "Server2 started";
    }

    @PostMapping("/traffic/start")
    public String startTraffic() {
        simulatorService.startTraffic();
        return "Traffic simulation started";
    }
}