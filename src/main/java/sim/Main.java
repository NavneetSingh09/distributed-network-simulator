package sim;

import sim.client.ClientNode;
import sim.client.TrafficSimulator;
import sim.config.Ports;
import sim.config.RoutingConfig;
import sim.config.ServerStatusConfig;
import sim.config.SimulationConfig;
import sim.metrics.MetricsStore;
import sim.router.PacketQueue;
import sim.router.Router;
import sim.server.ServerNode;


public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Usage: router | server1 | server2 | client | traffic");
            return;
        }

        // Manual wiring — mirrors what Spring would do automatically
        SimulationConfig   simConfig    = new SimulationConfig();
        RoutingConfig      routingConfig = new RoutingConfig();
        ServerStatusConfig statusConfig  = new ServerStatusConfig();
        MetricsStore       metrics       = new MetricsStore();
        PacketQueue        packetQueue   = new PacketQueue();

        switch (args[0]) {

            case "router":
                new Router(simConfig, routingConfig, statusConfig, metrics, packetQueue).start();
                break;

            case "server1":
                new ServerNode(Ports.SERVER1_PORT, metrics).start();
                break;

            case "server2":
                new ServerNode(Ports.SERVER2_PORT, metrics).start();
                break;

            case "client":
                new ClientNode("10.0.0.1", "10.0.0.5", metrics).start();
                break;

            case "traffic":
                startTrafficManually(metrics);
                break;

            default:
                System.out.println("Invalid argument");
                System.out.println("Usage: router | server1 | server2 | client | traffic");
        }
    }

    private static void startTrafficManually(MetricsStore metrics) {
        System.out.println("Manual Traffic Mode Started...");
        TrafficSimulator simulator = new TrafficSimulator(metrics);
        try {
            while (true) {
                simulator.sendPacket();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println("Traffic stopped");
        }
    }
}