package sim.client;

import sim.config.Ports;
import sim.metrics.MetricsStore;
import sim.model.Packet;
import sim.osi.OsiStack;
import sim.util.Log;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class TrafficSimulator {

    private final MetricsStore metricsStore;
    private final Random       random = new Random();

    public TrafficSimulator(MetricsStore metricsStore) {
        this.metricsStore = metricsStore;
    }

    public void sendPacket() {
    int clientId = random.nextInt(50);

    try {
        OsiStack stack = new OsiStack();

        // Generate varied traffic types for DPI classification
        String message = generateMessage(clientId);
        String encoded = stack.encapsulate(message);

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

private String generateMessage(int clientId) {
    int type = random.nextInt(10);
    switch (type) {
        case 0: return "GET /index.html HTTP/1.1 from client-" + clientId;
        case 1: return "POST /api/data HTTP/1.1 from client-" + clientId;
        case 2: return "DNS QUERY resolve api.example.com client-" + clientId;
        case 3: return "FTP UPLOAD file.txt from client-" + clientId;
        case 4: return "ICMP PING from client-" + clientId;
        case 5: return "ICMP ECHO reply from client-" + clientId;
        case 6: return "GET /dashboard HTML from client-" + clientId;
        case 7: return "DNS RESOLVE hostname client-" + clientId;
        // Occasionally inject suspicious traffic (10% chance)
        case 8: return "ATTACK flood attempt from client-" + clientId;
        default: return "Hello from client-" + clientId + " | " + System.currentTimeMillis();
    }
}

    
}
