package sim.service;

import org.springframework.stereotype.Service;
import sim.router.Router;
import sim.server.ServerNode;
import sim.client.TrafficSimulator;
import sim.config.Ports;

@Service
public class SimulatorService {

    private Router router = new Router();

    // ✅ traffic control
    private Thread trafficThread;
    private volatile boolean running = false;

    /* ================= ROUTER ================= */

    public void startRouter() {
        new Thread(() -> router.start()).start();
    }

    /* ================= SERVERS ================= */

    public void startServer1() {
        new Thread(() -> new ServerNode(Ports.SERVER1_PORT).start()).start();
    }

    public void startServer2() {
        new Thread(() -> new ServerNode(Ports.SERVER2_PORT).start()).start();
    }

    /* ================= TRAFFIC ================= */

    public void startTraffic() {

        if (running) {
            System.out.println("Traffic already running");
            return;
        }

        running = true;

        trafficThread = new Thread(() -> {

            TrafficSimulator simulator = new TrafficSimulator();

            try {

                while (running) {

                    simulator.sendPacket(); // 👈 IMPORTANT (see below)

                    Thread.sleep(400); // control speed

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        trafficThread.start();

        System.out.println("Traffic STARTED");
    }

    public void stopTraffic() {

        running = false;

        System.out.println("Traffic STOPPED");
    }
}