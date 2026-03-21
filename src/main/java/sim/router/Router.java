package sim.router;

import org.springframework.stereotype.Component;
import sim.config.Ports;
import sim.config.RoutingConfig;
import sim.config.ServerStatusConfig;
import sim.config.SimulationConfig;
import sim.metrics.MetricsStore;
import sim.metrics.PacketFlowStore;
import sim.model.Packet;
import sim.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listens for incoming packets from clients and forwards them to servers.
 * Uses a thread pool so multiple packets can be handled concurrently.
 * All dependencies are injected — no static references.
 */
@Component
public class Router {

    private final SimulationConfig  simulationConfig;
    private final RoutingConfig     routingConfig;
    private final ServerStatusConfig serverStatusConfig;
    private final MetricsStore      metricsStore;

    // Thread pool: handles multiple incoming connections concurrently
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private volatile int currentServer = 0;
    private final Random random = new Random();

    public Router(SimulationConfig simulationConfig,
                  RoutingConfig routingConfig,
                  ServerStatusConfig serverStatusConfig,
                  MetricsStore metricsStore) {
        this.simulationConfig   = simulationConfig;
        this.routingConfig      = routingConfig;
        this.serverStatusConfig = serverStatusConfig;
        this.metricsStore       = metricsStore;
    }

    public void start() {
        try (ServerSocket routerSocket = new ServerSocket(Ports.ROUTER_PORT)) {

            Log.info("ROUTER", "Router started on port " + Ports.ROUTER_PORT);

            while (true) {
                Socket clientSocket = routerSocket.accept();
                // Hand off to thread pool — router is no longer single-threaded
                executor.submit(() -> handleConnection(clientSocket));
            }

        } catch (Exception e) {
            Log.error("ROUTER", "Router failure: " + e.getMessage());
        }
    }

    private void handleConnection(Socket clientSocket) {
        try (clientSocket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            String data = reader.readLine();
            if (data == null) return;

            Packet packet = Packet.deserialize(data);
            Log.info("ROUTER", "Packet received: " + packet.getPacketId());
            PacketFlowStore.add("CLIENT_ROUTER");

            // Simulate packet drop
            if (random.nextDouble() < simulationConfig.getDropRate()) {
                Log.warn("ROUTER", "Packet dropped: " + packet.getPacketId());
                metricsStore.packetDropped();
                return;
            }

            int serverPort = getNextServer();
            if (serverPort == -1) {
                Log.warn("ROUTER", "Dropping packet — no servers available");
                metricsStore.packetDropped();
                return;
            }

            long latency = simulateLatency();
            metricsStore.recordLatency(latency);

            Log.info("ROUTER", "Forwarding packet to port " + serverPort);
            if (serverPort == Ports.SERVER1_PORT)
                PacketFlowStore.add("ROUTER_SERVER1");
            else
                PacketFlowStore.add("ROUTER_SERVER2");

            forwardToServer(packet, serverPort);
            metricsStore.packetProcessed();

        } catch (Exception e) {
            Log.error("ROUTER", "Error handling connection: " + e.getMessage());
        }
    }

    private synchronized int getNextServer() {
        boolean s1Up = serverStatusConfig.isServer1Up();
        boolean s2Up = serverStatusConfig.isServer2Up();

        if (!s1Up && !s2Up) {
            Log.error("ROUTER", "All servers are DOWN!");
            return -1;
        }
        if (s1Up && !s2Up) return Ports.SERVER1_PORT;
        if (!s1Up)         return Ports.SERVER2_PORT;

        // Both up — apply routing algorithm
        if (routingConfig.getAlgorithm() == RoutingConfig.Algorithm.LEAST_LOAD) {
            return (metricsStore.getServer1Load() <= metricsStore.getServer2Load())
                    ? Ports.SERVER1_PORT
                    : Ports.SERVER2_PORT;
        }

        // Round Robin (default)
        currentServer = (currentServer + 1) % 2;
        return (currentServer == 0) ? Ports.SERVER1_PORT : Ports.SERVER2_PORT;
    }

    private long simulateLatency() {
        try {
            int range = simulationConfig.getMaxLatency() - simulationConfig.getMinLatency();
            int delay = simulationConfig.getMinLatency() + (range > 0 ? random.nextInt(range) : 0);
            Log.info("ROUTER", "Simulating latency: " + delay + "ms");
            Thread.sleep(delay);
            return delay;
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

    private void forwardToServer(Packet packet, int port) {
        try (Socket serverSocket = new Socket("localhost", port);
             PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true)) {
            writer.println(packet.serialize());
        } catch (Exception e) {
            Log.error("ROUTER", "Failed to forward packet: " + packet.getPacketId());
        }
    }
}
