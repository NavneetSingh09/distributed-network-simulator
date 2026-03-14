package sim.service;

import org.springframework.stereotype.Service;
import sim.router.Router;
import sim.server.ServerNode;
import sim.client.TrafficSimulator;
import sim.config.Ports;

@Service
public class SimulatorService {

    private Router router = new Router();

    public void startRouter() {
        new Thread(() -> router.start()).start();
    }

    public void startServer1() {
        new Thread(() -> new ServerNode(Ports.SERVER1_PORT).start()).start();
    }

    public void startServer2() {
        new Thread(() -> new ServerNode(Ports.SERVER2_PORT).start()).start();
    }

    public void startTraffic() {
        new TrafficSimulator().start();
    }
}