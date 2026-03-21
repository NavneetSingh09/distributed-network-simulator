package sim.client;

import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

/**
 * Simulates multiple clients sending packets to the router.
 * MetricsStore is injected — no static references.
 */
public class TrafficSimulator {

    private final MetricsStore metricsStore;
    private final Random       random = new Random();

    public TrafficSimulator(MetricsStore metricsStore) {
        this.metricsStore = metricsStore;
    }

    /**
     * Sends one packet to the router (called repeatedly by SimulatorService).
     */
    public void sendPacket() {
        int clientId = random.nextInt(50);

        try {
            OsiStack stack   = new OsiStack();
            String   message = "Hello from client-" + clientId + " | " + System.currentTimeMillis();
            String   encoded = stack.encapsulate(message);

            Packet packet = Packet.newRequest(
                    "10.0.0." + clientId,
                    "10.0.0.100",
                    encoded
            );

            try (Socket socket = new Socket("localhost", Ports.ROUTER_PORT);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println(packet.serialize());
            }

            metricsStore.packetSent();
            Log.info("TRAFFIC", "Packet sent: " + packet.getPacketId());

        } catch (Exception e) {
            Log.error("TRAFFIC", "Failed to send packet: " + e.getMessage());
        }
    }
}
