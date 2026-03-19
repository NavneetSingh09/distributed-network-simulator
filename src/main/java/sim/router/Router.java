package sim.router;

import sim.model.Packet;
import sim.util.Log;
import sim.config.Ports;
import sim.config.RoutingConfig;
import sim.config.ServerStatusConfig;
import sim.config.SimulationConfig;
import sim.metrics.MetricsStore;
import sim.metrics.PacketFlowStore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Router {

    private int currentServer = 0;
    private Random random = new Random();

    public void start() {

    try (ServerSocket routerSocket = new ServerSocket(Ports.ROUTER_PORT)) {

        Log.info("ROUTER", "Router started on port " + Ports.ROUTER_PORT);

        while (true) {

            Socket clientSocket = routerSocket.accept();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));

            String data = reader.readLine();

            if (data == null) {
                clientSocket.close();
                continue;
            }

            Packet packet = Packet.deserialize(data);

            Log.info("ROUTER", "Packet received: " + packet.getPacketId());

            // record flow: client -> router
            PacketFlowStore.add("CLIENT_ROUTER");

            // 🔴 Simulate packet drop
            if (random.nextDouble() < SimulationConfig.DROP_RATE) {

                Log.warn("ROUTER", "Packet dropped: " + packet.getPacketId());

                MetricsStore.packetDropped();

                clientSocket.close();
                continue;
            }

            int serverPort = getNextServer();

            // ❗ HANDLE SERVER FAILURE (NEW)
            if (serverPort == -1) {

                Log.warn("ROUTER", "Dropping packet - no servers available");

                MetricsStore.packetDropped();

                clientSocket.close();
                continue;
            }

            // 🟡 Simulate latency
            long latency = simulateLatency();

            MetricsStore.recordLatency(latency);

            Log.info("ROUTER", "Forwarding packet to server on port " + serverPort);

            // record router -> server flow
            if (serverPort == Ports.SERVER1_PORT)
                PacketFlowStore.add("ROUTER_SERVER1");
            else
                PacketFlowStore.add("ROUTER_SERVER2");

            forwardToServer(packet, serverPort);

            MetricsStore.packetProcessed();

            clientSocket.close();
        }

    } catch (Exception e) {
        Log.error("ROUTER", "Router failure: " + e.getMessage());
    }
}

    private int getNextServer() {

    boolean s1Up = ServerStatusConfig.SERVER1_UP;
    boolean s2Up = ServerStatusConfig.SERVER2_UP;

    // 🔴 both down
    if (!s1Up && !s2Up) {
        Log.error("ROUTER", "All servers are DOWN!");
        return -1;
    }

    // 🟢 only one available
    if (s1Up && !s2Up) return Ports.SERVER1_PORT;
    if (!s1Up && s2Up) return Ports.SERVER2_PORT;

    // 🔵 both up → apply routing algorithm
    if (RoutingConfig.ALGORITHM.equals("LEAST_LOAD")) {

        int s1 = MetricsStore.getServer1Load();
        int s2 = MetricsStore.getServer2Load();

        return (s1 <= s2) ? Ports.SERVER1_PORT : Ports.SERVER2_PORT;
    }

    // Default: Round Robin
    currentServer = (currentServer + 1) % 2;

    return (currentServer == 0) ? Ports.SERVER1_PORT : Ports.SERVER2_PORT;
}
    private long simulateLatency() {

        try {

            int delay = SimulationConfig.MIN_LATENCY +
            random.nextInt(SimulationConfig.MAX_LATENCY - SimulationConfig.MIN_LATENCY); 

            Log.info("ROUTER", "Simulating network latency: " + delay + "ms");

            Thread.sleep(delay);

            return delay;

        } catch (Exception ignored) {
            return 0;
        }
    }

    private void forwardToServer(Packet packet, int port) {

        try (Socket serverSocket = new Socket("localhost", port);
             PrintWriter writer =
                     new PrintWriter(serverSocket.getOutputStream(), true)) {

            writer.println(packet.serialize());

        } catch (Exception e) {
            Log.error("ROUTER", "Failed to forward packet: " + packet.getPacketId());
        }
    }
}