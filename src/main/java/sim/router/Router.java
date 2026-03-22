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


@Component
public class Router {

    private static final int MAX_RETRIES  = 3;
    private static final int RETRY_DELAY_MS = 200;

    private final SimulationConfig   simulationConfig;
    private final RoutingConfig      routingConfig;
    private final ServerStatusConfig serverStatusConfig;
    private final MetricsStore       metricsStore;
    private final PacketQueue        packetQueue;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile int currentServer = 0;
    private final Random random = new Random();

    public Router(SimulationConfig simulationConfig,
                  RoutingConfig routingConfig,
                  ServerStatusConfig serverStatusConfig,
                  MetricsStore metricsStore,
                  PacketQueue packetQueue) {
        this.simulationConfig   = simulationConfig;
        this.routingConfig      = routingConfig;
        this.serverStatusConfig = serverStatusConfig;
        this.metricsStore       = metricsStore;
        this.packetQueue        = packetQueue;
    }

    public void start() {
        // Start the dispatcher thread that drains the queue
        Thread dispatcher = new Thread(this::dispatchLoop, "router-dispatcher");
        dispatcher.setDaemon(true);
        dispatcher.start();

        try (ServerSocket routerSocket = new ServerSocket(Ports.ROUTER_PORT)) {
            Log.info("ROUTER", "Router started on port " + Ports.ROUTER_PORT);

            while (true) {
                Socket clientSocket = routerSocket.accept();
                executor.submit(() -> receivePacket(clientSocket));
            }

        } catch (Exception e) {
            Log.error("ROUTER", "Router failure: " + e.getMessage());
        }
    }

    
    private void receivePacket(Socket clientSocket) {
        try (clientSocket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()))) {

            String data = reader.readLine();
            if (data == null) return;

            Packet packet = Packet.deserialize(data);
            Log.info("ROUTER", "Packet received: " + packet.getPacketId());
            PacketFlowStore.add("CLIENT_ROUTER");

            // Simulate packet drop BEFORE enqueuing
            if (random.nextDouble() < simulationConfig.getDropRate()) {
                Log.warn("ROUTER", "Packet dropped (simulated): " + packet.getPacketId());
                metricsStore.packetDropped();
                return;
            }

            // Try to enqueue — reject if queue is full
            if (!packetQueue.enqueue(packet)) {
                Log.warn("ROUTER", "Queue full — packet rejected: " + packet.getPacketId());
                metricsStore.packetDropped();
            } else {
                Log.info("ROUTER", "Packet queued. Queue size: " + packetQueue.size());
                metricsStore.packetQueued();
            }

        } catch (Exception e) {
            Log.error("ROUTER", "Error receiving packet: " + e.getMessage());
        }
    }

    
    private void dispatchLoop() {
        Log.info("ROUTER", "Dispatcher started");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Packet packet = packetQueue.dequeue(500);
                if (packet == null) continue; // timeout, loop again

                metricsStore.packetDequeued();
                forwardWithRetry(packet);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    
    private void forwardWithRetry(Packet packet) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {

            int serverPort = getNextServer();

            if (serverPort == -1) {
                Log.warn("ROUTER", "No servers available. Attempt " + attempt + "/" + MAX_RETRIES);

                if (attempt == MAX_RETRIES) {
                    Log.error("ROUTER", "All retries exhausted — dropping packet: " + packet.getPacketId());
                    metricsStore.packetDropped();
                    return;
                }

                sleep(RETRY_DELAY_MS);
                continue;
            }

            long latency = simulateLatency();
            metricsStore.recordLatency(latency);

            boolean success = forwardToServer(packet, serverPort);

            if (success) {
                if (serverPort == Ports.SERVER1_PORT)
                    PacketFlowStore.add("ROUTER_SERVER1");
                else
                    PacketFlowStore.add("ROUTER_SERVER2");

                metricsStore.packetProcessed();
                Log.info("ROUTER", "Packet delivered on attempt " + attempt + ": " + packet.getPacketId());
                return;
            }

            Log.warn("ROUTER", "Forward failed. Attempt " + attempt + "/" + MAX_RETRIES);
            sleep(RETRY_DELAY_MS);
        }

        Log.error("ROUTER", "All retries exhausted — dropping packet: " + packet.getPacketId());
        metricsStore.packetDropped();
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

        if (routingConfig.getAlgorithm() == RoutingConfig.Algorithm.LEAST_LOAD) {
            return (metricsStore.getServer1Load() <= metricsStore.getServer2Load())
                    ? Ports.SERVER1_PORT : Ports.SERVER2_PORT;
        }

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
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        }
    }

  
    private boolean forwardToServer(Packet packet, int port) {
        try (Socket serverSocket = new Socket("localhost", port);
             PrintWriter writer = new PrintWriter(serverSocket.getOutputStream(), true)) {
            writer.println(packet.serialize());
            return true;
        } catch (Exception e) {
            Log.error("ROUTER", "Failed to forward to port " + port + ": " + e.getMessage());
            return false;
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}