package sim.service;

import org.springframework.stereotype.Service;
import sim.client.TrafficSimulator;
import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.metrics.PacketFlowStore;
import sim.router.Router;
import sim.server.ServerNode;

@Service
public class SimulatorService {

    private final Router       router;
    private final MetricsStore metricsStore;

    private Thread          trafficThread;
    private volatile boolean running = false;

    public SimulatorService(Router router, MetricsStore metricsStore) {
        this.router       = router;
        this.metricsStore = metricsStore;
    }

    /* ================= ROUTER ================= */

    public void startRouter() {
        new Thread(router::start, "router-thread").start();
    }

    /* ================= SERVERS ================= */

    public void startServer1() {
        new Thread(() -> new ServerNode(Ports.SERVER1_PORT, metricsStore).start(),
                "server1-thread").start();
    }

    public void startServer2() {
        new Thread(() -> new ServerNode(Ports.SERVER2_PORT, metricsStore).start(),
                "server2-thread").start();
    }

    /* ================= TRAFFIC ================= */

    public void startTraffic() {
        if (running) {
            System.out.println("Traffic already running");
            return;
        }

        running = true;

        trafficThread = new Thread(() -> {
            TrafficSimulator simulator = new TrafficSimulator(metricsStore);
            try {
                while (running) {
                    simulator.sendPacket();
                    Thread.sleep(400);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "traffic-thread");

        trafficThread.start();
        System.out.println("Traffic STARTED");
    }

    public void stopTraffic() {
        running = false;
        if (trafficThread != null) trafficThread.interrupt();
        PacketFlowStore.clear(); // clears animation on stop
        System.out.println("Traffic STOPPED");
    }
}